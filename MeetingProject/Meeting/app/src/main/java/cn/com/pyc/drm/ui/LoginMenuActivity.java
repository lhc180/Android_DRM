package cn.com.pyc.drm.ui;


import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.view.annotation.ViewInject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import cn.com.meeting.drm.R;
import cn.com.pyc.drm.adapter.JGAdapter;
import cn.com.pyc.drm.adapter.Rv_BaseAdapter;
import cn.com.pyc.drm.bean.MechanismBean;
import cn.com.pyc.drm.bean.ReturnDataBean;
import cn.com.pyc.drm.bean.ScanHistory;
import cn.com.pyc.drm.common.Constant;
import cn.com.pyc.drm.http.WSHelper;
import cn.com.pyc.drm.model.MeetingModel;
import cn.com.pyc.drm.model.MeetingNameModel;
import cn.com.pyc.drm.model.ProductListModel;
import cn.com.pyc.drm.utils.CommonUtil;
import cn.com.pyc.drm.utils.DRMLog;
import cn.com.pyc.drm.utils.DRMUtil;
import cn.com.pyc.drm.utils.DeviceUtils;
import cn.com.pyc.drm.utils.DialogUtil;
import cn.com.pyc.drm.utils.OpenPageUtil;
import cn.com.pyc.drm.utils.SPUtils;
import cn.com.pyc.drm.utils.StringUtil;
import cn.com.pyc.drm.utils.UIHelper;
import cn.com.pyc.drm.utils.ViewUtil;
import cn.com.pyc.drm.utils.blurbehind.BlurBehind;
import cn.com.pyc.drm.utils.blurbehind.OnBlurCompleteListener;
import cn.com.pyc.drm.utils.manager.ActicityManager;
import cn.com.pyc.drm.utils.manager.RequestHttpManager;
import cn.com.pyc.drm.utils.manager.ScanHistoryDBManager;
import cn.com.pyc.drm.widget.FlatButton;
import cn.com.pyc.drm.widget.ToastShow;

import static cn.com.pyc.drm.utils.SPUtils.get;
import static cn.com.pyc.drm.utils.StringUtil.SaveSP_Download_HostPort;
import static cn.com.pyc.drm.utils.StringUtil.SaveSP_HostPort;

/**
 * @author 李巷阳
 * @version V1.0
 * @Description: (登陆界面一)
 * @date 2017/3/13 16:15
 */
public class LoginMenuActivity extends BaseActivity implements View.OnClickListener {



    private static final String TAG = "EXAMPLE";
    private static final int SCAN_MEETTING = 101;

    private Boolean isRunning = false;
    private String phoneNumber;
    private String isForActivity;

    @ViewInject(R.id.rootView)
    private View rootView;

    @ViewInject(R.id.bt)
    private Button bt;

    @ViewInject(R.id.rl_top)
    private RelativeLayout rl_top;

    @ViewInject(R.id.rl_bottom)
    private RelativeLayout rl_bottom;

    @ViewInject(R.id.et_username)
    private EditText met_username;

    @ViewInject(R.id.et_password)
    private EditText met_password;

    @ViewInject(R.id.btn_login)
    private FlatButton mbtn_login;

    @ViewInject(R.id.tv_register)
    private TextView mtv_register;

    @ViewInject(R.id.tv_forget_password)
    private TextView mtv_forget_password;

    @ViewInject(R.id.tv_history_record)
    public static TextView mtv_history_record;

    @ViewInject(R.id.bt_Qrcode)
    private Button mbt_Qrcode;
    /**
     * 控制透明度 值越大透明度越小
     */
    private final float thresholdValue = 0.8F;

    /**
     * 位移动画相对于半个屏幕的百分比值 值越大移动的越大
     */
    private final float thresholdTranslationValue = 0.35F;

    /**
     * 动画的执行时间
     */
    private final long durationTime = 500;

    /**
     * 移动距离
     */
    private float translationValue = 0f;

    private long exitTime = 0;

    private Dialog showMecDialog;

    private static final String DB_NAME = "meet_history.db";

    public static MechanismBean localmec;
    public static boolean success;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_pro);
        init_Value();
        init_View();
        auto_Login();
        initListener();
        registerBroadcast();

        /*List<ScanHistory> allScanHistory = ScanHistoryDBManager.Builder(this).findAllScanHistory();
        if (allScanHistory == null || allScanHistory.isEmpty()) {
            mtv_history_record.setVisibility(View.INVISIBLE);
        }else {
            mtv_history_record.setVisibility(View.VISIBLE);
        }*/
    }

    @Override
    protected void onStart() {
        super.onStart();
        List<ScanHistory> allScanHistory = ScanHistoryDBManager.Builder(this).findAllScanHistory();
        if (allScanHistory == null || allScanHistory.isEmpty()) {
            mtv_history_record.setVisibility(View.INVISIBLE);
        }else {
            mtv_history_record.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 初始化value
     */
    @Override
    protected void init_Value() {
        ViewUtils.inject(this);
        ActicityManager.getInstance().add(this);
        phoneNumber = getIntent().getStringExtra(DRMUtil.KEY_PHONE_NUMBER);
        isForActivity = getIntent().getStringExtra(Constant.isForActivity);
        if (!TextUtils.isEmpty(phoneNumber)) {
            met_username.setText(phoneNumber);
            met_password.requestFocus();
        }
    }

    /**
     * 初始化view
     */
    @Override
    protected void init_View() {
        rl_top.setTag(false);
        initTopViewLayoutParams();// 初始化登陆打开动画。
        mtv_history_record.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); // 查看已公开会议添加下划线
    }

    /**
     * 初始化数据
     */
    @Override
    protected void load_Data() {

    }


    /**
     * 初始化事件
     */
    private void initListener() {
        bt.setOnClickListener(this);
        mtv_register.setOnClickListener(this);
        mtv_history_record.setOnClickListener(this);
        mbt_Qrcode.setOnClickListener(this);
    }

    /**
     * 自动登陆
     */
    private void auto_Login() {
        if (Constant.isForWelcomeActivity.equals(isForActivity)) {
            String username = (String) get(DRMUtil.KEY_USERNAME, "");
            String password = (String) get(DRMUtil.KEY_PWD, "");
            String morganization_code = (String) get(DRMUtil.KEY_SELECT_MECHANISM, "");
            // 如果账号为空 ,则不自动登陆
            if (TextUtils.isEmpty(username)) {
                return;
            }
            // 如果账号为手机号,密码为空,则不自动登陆
            if (StringUtil.isMobileNO(username) && "".equals(password)) {
                return;
            }
            // 如果机构码为空,则不自动登陆
            if (TextUtils.isEmpty(morganization_code)) {
                return;
            }
            // 设置当前textview 账号和密码
            setUserName(username);
            setPassWord(password);
            MechanismBean mmechanis_data = StringUtil.getMechanismBeanByStr(morganization_code);
            SaveSP_HostPort(mmechanis_data.getServerAddress());// 保存到sp中的host和port
            // 登陆
            CallLoginWS(username, password,morganization_code);

        }
    }

    /**
     * 登陆
     *
     * @param username
     * @param password
     */
    private void CallLoginWS(final String username, final String password, final String morganization_code) {
        // 登陆  (此框架里面内嵌判断网络是否连接的逻辑。回调)
        WSHelper.newBuilder().setmUrl(Constant.getLogin_URL()).setMethodName(Constant.Login_MethodName).putParam(DRMUtil.KEY_USERNAME, username).putParam(DRMUtil.KEY_PWD, password).setCallback(new WSHelper.OnLoadDataListener<ReturnDataBean>() {
            @Override
            public void onSuccess(ReturnDataBean Rdata) {
                if (Constant.return_true.equals(Rdata.getResult())) {
                    SPUtils.save(DRMUtil.KEY_USERNAME, getUserName());
                    SPUtils.save(DRMUtil.KEY_PWD, getPassWord());
                    SPUtils.save(DRMUtil.KEY_PHONE_NUMBER, Rdata.getToken());
                    SPUtils.save(DRMUtil.KEY_SELECT_MECHANISM, morganization_code);
                    OpenActivity(morganization_code);
                } else if (Constant.return_false.equals(Rdata.getResult())) {
                    UIHelper.showToast(getApplicationContext(), Rdata.getMsg());
                }
            }

            @Override
            public void onError(String error_str, Exception e) {
                showToast(error_str);
                Log.e("error",error_str);
                //默认机构连接不不上，就提示删除改机
                DialogUtil.showDialog_Prompt(mBaseActivity, "删除服务器","关闭", "无法进入该机构,请检查连接。", new DialogUtil.onDialogPromptListener() {
                    @Override
                    public void Onclick_Dialog_Confirm_Listener(View v) {
                        MechanismBean mmechanis_data = StringUtil.getMechanismBeanByStr(morganization_code);
                        ScanHistoryDBManager.Builder(mBaseActivity).deleteMechanismByServerAddress(mmechanis_data.getServerAddress());
                        showToast("删除成功,请重新选择机构。");
//                        getSelectServer(username,password);// 弹出机构的对话框
                    }

                    @Override
                    public void Onclick_Dialog_Cancel_Listener(View v) {

                    }
                });
            }
        }).build(mBaseActivity, new TypeToken<ReturnDataBean>() {}.getType());
    }


    /**
     * 登陆前选择本地机构列表
     */
    private void getSelectServer(final String username, final String password) {
        List<MechanismBean> mListMechanismBean = ScanHistoryDBManager.Builder(mBaseActivity).findAllMechanismScanHistory();
        showMechanismDialog(getMechanismAdapter(mListMechanismBean, username, password), username, password);
    }

    /**
     * 机构列表adapter
     *
     * @param liststr
     * @param username
     * @param password
     * @return
     */
    private JGAdapter getMechanismAdapter(final List<MechanismBean> liststr, final String username, final String password) {
        JGAdapter mMecAdapter = null;
        if (mMecAdapter == null) {
            mMecAdapter = new JGAdapter(mBaseActivity, liststr);
        } else {
            mMecAdapter.refreshData(liststr);
        }
        mMecAdapter.setOnItemClickListener(new Rv_BaseAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                hideDialog_lv();
                MechanismBean mMecbean = liststr.get(position);
                if (TextUtils.isEmpty(mMecbean.getServerAddress()) || TextUtils.isEmpty(mMecbean.getServerName()) || TextUtils.isEmpty(mMecbean.getSZUserName())) {
                    ToastShow.getInstances_().showFail(mBaseActivity, "机构数据异常");
                    return;
                }
                SaveSP_HostPort(mMecbean.getServerAddress());
                // 切换时，更新端口号

                CallLoginWS(username, password,MecToStr(mMecbean));// 登陆
            }
        });
        return mMecAdapter;
    }

    /**
     * 关闭对话框
     **/
    public void hideDialog_lv() {
        if (showMecDialog != null) {
            showMecDialog.dismiss();
        }
    }
    /**
     * 弹出机构对话框
     *
     * @param mechanismAdapter
     */
    private void showMechanismDialog(JGAdapter mechanismAdapter, final String username, final String password) {
        showMecDialog = new Dialog(mBaseActivity, R.style.SZ_LoadBgDialog);
        View view = View.inflate(mBaseActivity, R.layout.dialog_server_lv, null);
        TextView mTv_prompt = (TextView) view.findViewById(R.id.tv_prompt);
        mTv_prompt.setText("请选择机构");
        RecyclerView mLv_control = (RecyclerView) view.findViewById(R.id.lv_control);
        RelativeLayout rl_default_mechanism = (RelativeLayout) view.findViewById(R.id.rl_default_mechanism);
        rl_default_mechanism.setVisibility(View.VISIBLE);
        rl_default_mechanism.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideDialog_lv();
                MechanismBean mMecbean = new MechanismBean("默认服务器",Constant.BASE_URL,"pycmeeting");
                SaveSP_HostPort(mMecbean.getServerAddress());
                CallLoginWS(username, password,MecToStr(mMecbean));// 登陆
            }
        });


        LinearLayoutManager LinearManager = new LinearLayoutManager(mBaseActivity);
        LinearManager.setOrientation(LinearLayoutManager.VERTICAL);
        mLv_control.setLayoutManager(LinearManager);
        mLv_control.setAdapter(mechanismAdapter);

        showMecDialog.setContentView(view);
        showMecDialog.setCancelable(false);
        showMecDialog.setCanceledOnTouchOutside(true);
        showMecDialog.show();

    }
    /**
     * 把对象转化为字符串
     * @param mmb
     * @return
     */
    private String MecToStr(MechanismBean mmb) {return mmb.getServerAddress() + "," + mmb.getServerName() + "," + mmb.getSZUserName();}

    /**
     * 注册广播接收者
     */
    private void registerBroadcast() {
        // 进入下载界面，关闭登陆界面
        IntentFilter netFilter = new IntentFilter();
        netFilter.addAction(DRMUtil.BROADCAST_CLEAR_LOGIN_ACTIVITY);
        registerReceiver(netReceive, netFilter);
    }

    public BroadcastReceiver netReceive = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) return;
            String action = intent.getAction();
            if (action == null) return;
            switch (action) {
                case DRMUtil.BROADCAST_CLEAR_LOGIN_ACTIVITY:
                    LoginMenuActivity.this.finish();
                    break;
            }
        }
    };

    /**
     * 事件处理
     *
     * @param view
     */
    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            // 打开登陆动画
            case R.id.bt:
                toggleAnimation();
                break;

            // 注册
            case R.id.tv_register:
                OpenPageUtil.openActivity(LoginMenuActivity.this, RegisterActivity.class);
                break;

            // 查看历史记录
            case R.id.tv_history_record:
                OpenPageUtil.openActivity(LoginMenuActivity.this, ScanHistoryActivity.class);
                break;
            // 扫描机构码或者会议码
            case R.id.bt_Qrcode:
                SPUtils.remove(DRMUtil.KEY_LOGIN_TOKEN);
                OpenPageUtil.openZXingCode(LoginMenuActivity.this);
                UIHelper.startInAnim(LoginMenuActivity.this);
                break;

            default:
                break;
        }
    }


    /**
     * 获取密码
     * @author 李巷阳
     * @date 2016-10-18 下午6:28:23
     */
    public String getPassWord() {
        return met_password.getText().toString().trim();
    }

    /**
     * 获取账号
     * @author 李巷阳
     * @date 2016-10-18 下午6:01:33
     */
    public String getUserName() {
        return met_username.getText().toString().trim();
    }

    public void setUserName(String userName)
    {
        met_username.setText(userName);
    }
    /**
     * 设置密码
     * @param password
     */
    public void setPassWord(String password){
        met_password.setText(password);
    }


    Handler handler = new Handler();
    /**
     * 二维码扫描会议或者机构的回调
     *
     * @author 李巷阳
     * @date 2016-9-8 上午11:50:45
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constant.REQUEST_CODE_SCAN: {
                // 扫码登录获取产品信息
                if (resultCode == RESULT_OK && data != null) {
                    Bundle bundle = data.getExtras();
                    String DataSource = bundle.getString(Constant.DECODED_CONTENT_KEY);

                    // 判断是否为机构码
                    if (StringUtil.isMechanismQRcode(DataSource)) {
                        saveMecScan(DataSource);// 扫描机构码后添加
                    } else {
                        saveMetScan(DataSource);// 扫描会议码后添加
                    }
                }
            }
            break;
            case Constant.LoginAccount_CODE: {
                handler.postDelayed(new Thread(new Runnable() {
                    @Override
                    public void run() {
                        boolean flag = (boolean) rl_top.getTag();// 如果为true,则设定为false,表示打开。
                        if(flag){
                            rl_top.setTag(true);
                            toggleAnimation();
                        }
                    }
                }),500);

            }
            break;
        }
    }

    /**
     * 扫描机构码后添加
     *
     * @param dataSource
     */
    private void saveMecScan(String dataSource) {
        if (TextUtils.isEmpty(dataSource)) {
            showToast(mBaseActivity.getString(R.string.scaning_empty));
            return;
        }
//        if (!CommonUtil.isNetConnect(mBaseActivity)) {
//            showToast(mBaseActivity.getString(R.string.net_disconnected));
//            return;
//        }
//        // 扫码获取机构添加本地数据库
//        WSHelper.newBuilder().setmUrl(dataSource).setMethodName(dataSource.split("=")[1]).setCallback(new WSHelper.OnLoadDataListener<MechanismBean>() {
//
//            @Override
//            public void onSuccess(MechanismBean mec) {

                if(dataSource==null||"".equals(dataSource)){
                    showToast("机构数据异常");
                    return;
                }
                 final MechanismBean mec =JSON.parseObject(dataSource, MechanismBean.class);
                // 判断扫描获取的机构是否正常
                if (TextUtils.isEmpty(mec.getServerAddress()) || TextUtils.isEmpty(mec.getServerName()) || TextUtils.isEmpty(mec.getSZUserName())) {
                    showToast("机构数据异常");
                    return;
                }
                // 把机构保存在本地数据库
                boolean isFlag = ScanHistoryDBManager.Builder(mBaseActivity).saveScanMechanismHistory(mec);


                success = false;
                if (isFlag) {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            ToastShow.getInstances_().showOk(mBaseActivity, "机构添加成功,请登陆");
                            boolean flag = (boolean) rl_top.getTag();// 如果为true,则设定为false,表示打开。
                            if(!flag){
                                rl_top.setTag(false);
                                toggleAnimation();
                            }
                            //添加成功记录
                            success = true;
                            localmec = mec;
                        }
                    },800);
                } else {
                    showToast("机构数据异常");
                    success = false;
                }
//            }
//
//            @Override
//            public void onError(String error_str, Exception e) {
//                ToastShow.getInstances_().showFail(mBaseActivity, "扫描机构码异常");
//            }
//        }).build(mBaseActivity, new TypeToken<MechanismBean>() {}.getType());
//

    }

    /**
     * 扫描会议码后添加
     *
     * @param dataSource
     */
    private void saveMetScan(final String dataSource) {
        if (TextUtils.isEmpty(dataSource)) {
            showToast(mBaseActivity.getString(R.string.scaning_empty));
            return;
        }
        if (TextUtils.isDigitsOnly(dataSource)) {
            showMsgDialog(dataSource);
            return;
        }
        if (!CommonUtil.isNetConnect(mBaseActivity)) {
            showToast(mBaseActivity.getString(R.string.net_disconnected));
            return;
        }
        final String userName = StringUtil.getStringByResult(dataSource, "username=");
        final String MeetingId = StringUtil.getStringByResult(dataSource, "MeetingID=");//系统会议id
        final String systemType = StringUtil.getStringByResult(dataSource, "SystemType=");
        final String id = StringUtil.getStringByResult(dataSource, "id=");//文件服务会议id

        if (TextUtils.isEmpty(userName) || TextUtils.isEmpty(MeetingId)) {
            showMsgDialog(dataSource);
            return;
        }
        // 保存用户名
        SPUtils.save(DRMUtil.KEY_VISIT_NAME, userName);
        SPUtils.save(DRMUtil.KEY_SUIZHI_CODE, id);
        SPUtils.save(DRMUtil.KEY_MEETINGID, MeetingId);// 存储会议id
        // 每个二维码得IP和port可能是不一样的，需要截取扫描二维码返回的参数的IP和port进行获取会议信息。
        SaveSP_HostPort(dataSource);
        SaveSP_Download_HostPort(dataSource);

        showBgLoading("载入中...");
        // 扫描会议码成功,跳转会议界面。
        Bundle bundle = new Bundle();
        bundle.putString("MeetingID", MeetingId);

        RequestHttpManager.init().getData(DRMUtil.getMeetingNameUrl(), bundle, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> arg0) {
                MeetingModel metNameModel = JSON.parseObject(arg0.result, MeetingModel.class);
                if (metNameModel.getResult()) {
//                            MeetingModel.MeetingName metBean  = metNameModel.getData();
                    MeetingModel.MeetingName metBean = metNameModel.getData();
                    if (metBean == null) {
                        hideBgLoading();
                        showToast(mBaseActivity.getResources().getString(R.string.scan_qr_code_error));
                        return;
                    }
                    // 应对之前版本升级出现的异常。
                    if ("true".equals(metBean.getVerify()) || "false".equals(metBean.getVerify())) {
                        hideBgLoading();
                        showToast(mBaseActivity.getResources().getString(R.string.qrcode_overdue));
                        return;
                    }
                    ScanHistory sh = SaveScanHistory( userName, MeetingId, systemType, dataSource, metBean);
                    // verify判断是否需要登陆验证。不需要就把手机号设置为""
                    if (!"0".equals(metBean.getVerify())) {
                        OpenPageUtil.openScanLoginVerificationActivity(LoginMenuActivity.this, sh);
                    } else {
                        SPUtils.save(DRMUtil.VOTE_URL, metBean.getUrl());
                        SPUtils.save(DRMUtil.KEY_PHONE_NUMBER, "");
                        OpenPageUtil.openDownloadMainByScaning2(LoginMenuActivity.this, sh);
                    }
                    hideBgLoading();
                } else {
                    showToast("服务器异常");
                    hideBgLoading();
                }
            }

            @Override
            public void onFailure(HttpException arg0, String arg1) {
            }
        });
        /*RequestHttpManager.init().postData(DRMUtil.getMeetingNameUrl(), userName, MeetingId, new RequestCallBack<String>() {
            public void onSuccess(ResponseInfo<String> arg0) {
                MeetingNameModel metNameModel = JSON.parseObject(arg0.result, MeetingNameModel.class);
                if (metNameModel.isSuccess()) {
                    MeetingNameModel.MeetingName metBean  = metNameModel.getData();
                    if (metBean == null) {
                        hideBgLoading();
                        showToast(mBaseActivity.getResources().getString(R.string.scan_qr_code_error));
                        return;
                    }
                    // 应对之前版本升级出现的异常。
                    if ("true".equals(metBean.getVerify()) || "false".equals(metBean.getVerify())) {
                        hideBgLoading();
                        showToast(mBaseActivity.getResources().getString(R.string.qrcode_overdue));
                        return;
                    }
                    ScanHistory sh = SaveScanHistory( userName, MeetingId, systemType, dataSource, metBean);
                    // verify判断是否需要登陆验证。不需要就把手机号设置为""
                    if (!"0".equals(metBean.getVerify())) {
                        OpenPageUtil.openScanLoginVerificationActivity(LoginMenuActivity.this, sh);
                    } else {
                        SPUtils.save(DRMUtil.VOTE_URL, metBean.getUrl());
                        SPUtils.save(DRMUtil.KEY_PHONE_NUMBER, "");
                        OpenPageUtil.openDownloadMainByScaning2(LoginMenuActivity.this, sh);
                    }
                    hideBgLoading();
                } else {
                    showToast("服务器异常");
                    hideBgLoading();
                }
            }

            @Override
            public void onFailure(HttpException arg0, String arg1) {
                hideBgLoading();
                showToast("连接服务器失败");
            }
        });*/
    }

    /**
     * @Description: (保存会议信息到历史记录)
     * @author 李巷阳
     * @date 2016-9-8 下午4:36:13
     */
    private ScanHistory SaveScanHistory( final String username, final String MeetingId, final String MeetingType, final String DataSource, MeetingModel.MeetingName o) {
        ScanHistory sh = new ScanHistory();
        sh.setMeetingId(MeetingId);
        sh.setScanDataSource(DataSource);
        sh.setMeetingType(MeetingType);
        sh.setUsername(username);
        sh.setIsverifys(o.getVerify());
        sh.setVerify_url(o.getVerifyurl() + "&DevicesId=" + DeviceUtils.getIMEI(mBaseActivity));
//        sh.setVote_title(o.getTitle());
        sh.setMeetingName(o.getName());
        sh.setVote_url(o.getUrl());
        sh.setClient_url(o.getClient_url());
        sh.setCreateTime(o.getExact_createTime());
        ScanHistoryDBManager.Builder(mBaseActivity).saveScanHistory(sh);
        return sh;
    }

    /**
     * 设置上面View的高度
     */
    private void initTopViewLayoutParams() {
        rootView.post(new Runnable() {
            @Override
            public void run() {
                //设置上下控件的大小
                int rootViewHeight = rootView.getHeight();
                Log.d(TAG, String.valueOf(rootViewHeight));
                int half = rootViewHeight / 2 + 2;
                ViewGroup.LayoutParams rl_topLayoutParams = rl_top.getLayoutParams();
                rl_topLayoutParams.height = half;
                rl_top.setLayoutParams(rl_topLayoutParams);

                ViewGroup.LayoutParams rl_bottomLayoutParams = rl_bottom.getLayoutParams();
                rl_bottomLayoutParams.height = half;
                rl_bottom.setLayoutParams(rl_bottomLayoutParams);

                //计算上下控件移动的距离 当前屏幕一半的百分比
                translationValue = half * thresholdTranslationValue;
            }
        });
    }




    /**
     * 开关动画
     */
    private void toggleAnimation() {
        //正在运行。
        if (isRunning) return;
        //为真打开 为假合并
        boolean flag = (boolean) rl_top.getTag();

        float topStartValue = flag ? -translationValue : 0F;
        float topEndValue = flag ? 0F : -translationValue;

        float bottomStartValue = flag ? translationValue : 0F;
        float bottomEndValue = flag ? 0F : translationValue;

        rl_top.setTag(!flag);

        ObjectAnimator animator = ObjectAnimator.ofFloat(rl_top, "translationY", topStartValue, topEndValue); //动画一
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                //1、属性动画的执行进度 0-1
                float animatedFraction = animation.getAnimatedFraction();

                //2、1百分百不透明 0百分百透明 控制最大透明为百分之五十也就是0.5
                float alphaValue = animatedFraction;
                if ((boolean) rl_top.getTag()) {
                    //从张开到合并 动画应该是从透明到不透明
                    alphaValue = (1F - alphaValue) + thresholdValue;
                } else {
                    //从张开到合并 动画应该是从不透明到透明
                    alphaValue += thresholdValue;
                }
                rl_bottom.setAlpha(alphaValue);
                rl_top.setAlpha(alphaValue);
            }
        });
         // 防止重复打开。
         animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                isRunning = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isRunning = false;
                if ((boolean) rl_top.getTag()) {
                    Log.e("open","动画打开");
                    BlurBehind.getInstance().execute(LoginMenuActivity.this, new OnBlurCompleteListener() {
                        @Override
                        public void onBlurComplete() {
                            Log.e("open","开始跳转");
                            Intent intent = new Intent(LoginMenuActivity.this, LoginAccountActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            if (!TextUtils.isEmpty(phoneNumber)) {
                                intent.putExtra(DRMUtil.KEY_PHONE_NUMBER,phoneNumber);
                            }
                            startActivityForResult(intent, Constant.LoginAccount_CODE);//这里采用startActivityForResult来做跳转，此处的0为一个依据，可以写其他的值，但一定要>=0
                            overridePendingTransition(R.anim.fade_in_anim, R.anim.fade_out_anim);
                        }
                    });


                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                isRunning = false;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setInterpolator(new DecelerateInterpolator());
        animatorSet.play(animator).with(ObjectAnimator.ofFloat(rl_bottom, "translationY", bottomStartValue, bottomEndValue)); //动画二
        animatorSet.setDuration(durationTime);
        animatorSet.start();
    }

    /**
     * @author 李巷阳
     * @date 2016-9-8 下午3:29:05
     */
    private void OpenActivity(String morganization_code) {
        OpenPageUtil.openMyMeetingActivity(LoginMenuActivity.this, morganization_code);

    }

    public void showMsgDialog(final String result) {
        ViewUtil.showCommonDialog(this, getString(R.string.scanning_result), result, getString(R.string.copy), new ViewUtil.DialogCallBack() {
            @Override
            public void onConfirm() {
                CommonUtil.copyText(mBaseActivity, result);
                showToast(getString(R.string.copy_to_clip));
            }
        });
    }
    /**
     * @author 李巷阳
     * @date 2016-8-23 下午8:24:56
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(netReceive);
    }
    /**
     * 点击两次,退出程序。
     *
     * @author 李巷阳
     * @date 2016-9-8 上午11:50:34
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                ActicityManager.getInstance().exit();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
