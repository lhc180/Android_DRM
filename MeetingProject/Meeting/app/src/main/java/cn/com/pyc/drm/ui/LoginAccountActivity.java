package cn.com.pyc.drm.ui;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

import java.util.List;

import cn.com.meeting.drm.R;
import cn.com.pyc.drm.adapter.JGAdapter;
import cn.com.pyc.drm.adapter.Rv_BaseAdapter;
import cn.com.pyc.drm.bean.MechanismBean;
import cn.com.pyc.drm.bean.ReturnDataBean;
import cn.com.pyc.drm.bean.ScanMechanismHistory;
import cn.com.pyc.drm.common.Constant;
import cn.com.pyc.drm.http.WSHelper;
import cn.com.pyc.drm.utils.DRMUtil;
import cn.com.pyc.drm.utils.DialogUtil;
import cn.com.pyc.drm.utils.OpenPageUtil;
import cn.com.pyc.drm.utils.SPUtils;
import cn.com.pyc.drm.utils.StringUtil;
import cn.com.pyc.drm.utils.UIHelper;
import cn.com.pyc.drm.utils.blurbehind.BlurBehind;
import cn.com.pyc.drm.utils.manager.ActicityManager;
import cn.com.pyc.drm.utils.manager.ScanHistoryDBManager;
import cn.com.pyc.drm.widget.FlatButton;
import cn.com.pyc.drm.widget.ToastShow;

import static cn.com.pyc.drm.utils.StringUtil.SaveSP_HostPort;

/**
 * @author 李巷阳
 * @version V1.0
 * @Description: (登陆界面二)
 * @date 2017/3/13 16:22
 */
public class LoginAccountActivity extends BaseActivity implements View.OnClickListener {

    @ViewInject(R.id.rootView)
    private View rootView;

    @ViewInject(R.id.ll_login)
    private LinearLayout mLl_login;

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

    private Dialog showMecDialog;
    private String phoneNumber;


    /**
     * 移动距离
     */
    private float translationValue = 0f;

    /**
     * 位移动画相对于半个屏幕的百分比值 值越大移动的越大
     */
    private final float thresholdTranslationValue = 0.35F;
    public static JGAdapter mechanismAdapter;
    private MechanismBean localmec;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_account);
        init_BlurBackground();
        init_View();
        init_Value();
        initTopViewLayoutParams();
        init_listener();

    }

    private void init_listener() {
        rootView.setOnClickListener(this);
        mbtn_login.setOnClickListener(this);
        mtv_forget_password.setOnClickListener(this);
    }

    /**
     * 设置模糊背景
     */
    private void init_BlurBackground() {
        BlurBehind.getInstance()
                .withAlpha(150)
                .withFilterColor(Color.parseColor("#e0000000"))
                .setBackground(this);
    }

    @Override
    protected void init_View() {
        ViewUtils.inject(this);
        ActicityManager.getInstance().add(this);

    }

    @Override
    protected void load_Data() {

    }

    @Override
    protected void init_Value() {
        phoneNumber = getIntent().getStringExtra(DRMUtil.KEY_PHONE_NUMBER);
        if (!TextUtils.isEmpty(phoneNumber)) {
            met_username.setText(phoneNumber);
            met_password.requestFocus();
        }
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
                int half = rootViewHeight  + 2;
                //计算上下控件移动的距离 当前屏幕一半的百分比
                translationValue = half * thresholdTranslationValue;
                ViewGroup.LayoutParams ll_LayoutParams = mLl_login.getLayoutParams();
                ll_LayoutParams.height = (int) translationValue;
                mLl_login.setLayoutParams(ll_LayoutParams);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            // FrameLayout 点击背景。关闭界面。
            case R.id.rootView:
                InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                im.hideSoftInputFromWindow(getCurrentFocus().getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                LoginAccountActivity.this.finish();
                overridePendingTransition(R.anim.fade_in_anim, R.anim.fade_out_anim);
                break;
            // 登陆
            case R.id.btn_login:
                final String username = getUserName();
                final String password = getPassWord();
                if (TextUtils.isEmpty(username)) {
                    showToast(mBaseActivity.getResources().getString(R.string.login_account_not_empty));
                    return;
                }

                localmec = LoginMenuActivity.localmec;
                if (localmec!=null){
                    CallLoginWS2(username, password,localmec);// 登陆
                    localmec = null;
                }else {
                    getSelectServer(username, password);
                }
                break;
            // 找回密码
            case R.id.tv_forget_password:
                String met_username = getUserName();
                Bundle bundle = new Bundle();
                if (StringUtil.isMobileNO(met_username)) {
                    bundle.putString("phone_number", met_username);
                }
                openActivity(RetrievepasswordActivity.class, bundle);
                finish();
                break;
            // 为了防止点击账号密码周边,引起的关闭当前activity的bug.
            case R.id.ll_login:
                break;
        }
    }

    /**
     * 登陆前选择本地机构列表
     */
    private void getSelectServer(final String username, final String password) {
        List<MechanismBean> mListMechanismBean = ScanHistoryDBManager.Builder(mBaseActivity).findAllMechanismScanHistory();
        // 如果本地无多个服务器地址,且未指定服务器,应自动登陆服务器
        if(mListMechanismBean!=null && mListMechanismBean.size()>0){
            if (mListMechanismBean.contains(localmec)){
                CallLoginWS(username, password,localmec);// 登陆
            }else {
                mechanismAdapter = getMechanismAdapter(mListMechanismBean, username, password);
                showMechanismDialog(mechanismAdapter,username,password);
            }
        }else{
            MechanismBean mMecbean = new MechanismBean("默认服务器", Constant.BASE_DEFAULT_URL,"pycmeeting");
            SaveSP_HostPort(mMecbean.getServerAddress());
            CallLoginWS(username, password,mMecbean);// 登陆
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
                MechanismBean mMecbean = new MechanismBean("默认服务器", Constant.BASE_DEFAULT_URL,"pycmeeting");
                SaveSP_HostPort(mMecbean.getServerAddress());
                CallLoginWS(username, password,mMecbean);// 登陆
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
     * 关闭对话框
     **/
    public void hideDialog_lv() {
        if (showMecDialog != null) {
            showMecDialog.dismiss();
        }
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

                CallLoginWS(username, password,mMecbean);// 登陆
            }
        });
        return mMecAdapter;
    }
    /**
     * 登陆
     * @param username
     * @param password
     */
    private void CallLoginWS(final String username, final String password, final MechanismBean mMecbean) {
        final String morganization_code=MecToStr(mMecbean);
        // 登陆  (此框架里面内嵌判断网络是否连接的逻辑。回调)
        String str = Constant.getLogin_URL();
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
                DialogUtil.showDialog_Prompt(mBaseActivity, "删除服务器","关闭", "无法进入该机构,请检查连接。", new DialogUtil.onDialogPromptListener() {
                    @Override
                    public void Onclick_Dialog_Confirm_Listener(View v) {
                        ScanHistoryDBManager.Builder(mBaseActivity).deleteMechanismByServerAddress(mMecbean.getServerAddress());
                        showToast("删除成功,请重新选择机构。");
                        getSelectServer(username,password);// 弹出机构的对话框
                    }

                    @Override
                    public void Onclick_Dialog_Cancel_Listener(View v) {

                    }
                });
            }
        }).build(mBaseActivity, new TypeToken<ReturnDataBean>() {}.getType());
    }

    /**
     * 登陆
     * @param username
     * @param password
     */
    private void CallLoginWS2(final String username, final String password, final MechanismBean mMecbean) {
        final String morganization_code=MecToStr(mMecbean);
        // 登陆  (此框架里面内嵌判断网络是否连接的逻辑。回调)
        String str = mMecbean.getServerAddress();
        String url = str.substring(0,str.indexOf("?"))+"/WebService/WSUserLogin.asmx?op=LoginChecking";
        WSHelper.newBuilder().setmUrl(url).setMethodName(Constant.Login_MethodName).putParam(DRMUtil.KEY_USERNAME, username).putParam(DRMUtil.KEY_PWD, password).setCallback(new WSHelper.OnLoadDataListener<ReturnDataBean>() {
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
                DialogUtil.showDialog_Prompt(mBaseActivity, "删除服务器","关闭", "无法进入该机构,请检查连接。", new DialogUtil.onDialogPromptListener() {
                    @Override
                    public void Onclick_Dialog_Confirm_Listener(View v) {
                        ScanHistoryDBManager.Builder(mBaseActivity).deleteMechanismByServerAddress(mMecbean.getServerAddress());
                        showToast("删除成功,请重新选择机构。");
                        getSelectServer(username,password);// 弹出机构的对话框
                    }

                    @Override
                    public void Onclick_Dialog_Cancel_Listener(View v) {

                    }
                });
            }
        }).build(mBaseActivity, new TypeToken<ReturnDataBean>() {}.getType());

    }
    /**
     * @author 李巷阳
     * @date 2016-9-8 下午3:29:05
     */
    private void OpenActivity(String morganization_code) {
        OpenPageUtil.openMyMeetingActivity(LoginAccountActivity.this, morganization_code);
        LoginAccountActivity.this.finish();
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
    /**
     * 把对象转化为字符串
     * @param mmb
     * @return
     */
    private String MecToStr(MechanismBean mmb) {return mmb.getServerAddress() + "," + mmb.getServerName() + "," + mmb.getSZUserName();}

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.setResult(0, null);
    }
}
