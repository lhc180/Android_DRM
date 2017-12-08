package cn.com.pyc.drm.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.client.android.CaptureActivity;
import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import cn.com.meeting.drm.R;
import cn.com.pyc.drm.adapter.JGAdapter;
import cn.com.pyc.drm.adapter.MHAdapter;
import cn.com.pyc.drm.adapter.Rv_BaseAdapter;
import cn.com.pyc.drm.bean.BaseBean;
import cn.com.pyc.drm.bean.MechanismBean;
import cn.com.pyc.drm.bean.MeetingBean;
import cn.com.pyc.drm.bean.ReturnMechanismDataBean;
import cn.com.pyc.drm.bean.ReturnMeetingRecordDataBean;
import cn.com.pyc.drm.common.Constant;
import cn.com.pyc.drm.http.WSHelper;
import cn.com.pyc.drm.utils.CommonUtil;
import cn.com.pyc.drm.utils.DRMUtil;
import cn.com.pyc.drm.utils.DialogUtil;
import cn.com.pyc.drm.utils.OpenPageUtil;
import cn.com.pyc.drm.utils.SPUtils;
import cn.com.pyc.drm.utils.SortUtil;
import cn.com.pyc.drm.utils.StringUtil;
import cn.com.pyc.drm.utils.UIHelper;
import cn.com.pyc.drm.utils.manager.ActicityManager;
import cn.com.pyc.drm.utils.manager.XMLParserVersionManager;
import cn.com.pyc.drm.widget.HighlightImageView;
import cn.com.pyc.drm.widget.IconCenterTextView;
import cn.com.pyc.drm.widget.ToastShow;

import static cn.com.pyc.drm.utils.SPUtils.get;

/**
 * @author 李巷阳
 * @version V1.0
 * @Description: (会议列表界面)
 * @date 2017/2/28 17:04
 */
public class MyMeetingActivityPro extends BaseActivity implements  View.OnClickListener, DialogUtil.OnDialogMechanismListener {

    private String LoginPhone = "";

    private MechanismBean mmechanismbean;

    @ViewInject(R.id.title_tv)
    private TextView mtv_title_tv;

    @ViewInject(R.id.lv_scanhistory)
    private XRecyclerView mlv_scanhistory;

    @ViewInject(R.id.iv_info_dot)
    private View mv_infodot;

    @ViewInject(R.id.rl_sort)
    private RelativeLayout mrl_sort;

    @ViewInject(R.id.rl_mechanism)
    private RelativeLayout mrl_mechanism;

    @ViewInject(R.id.iv_seting)
    private HighlightImageView mhiv_seting;

    @ViewInject(R.id.search_guidetext)
    private IconCenterTextView mSearch_guidetext;

    @ViewInject(R.id.tv_sort)
    private TextView mtv_sort;


    private List<MeetingBean> mDataList;

    private MHAdapter mAdapter;
    private boolean isStop = false;

    private SortUtil.TypeSort typeSort = SortUtil.TypeSort.Reverse;// 默认是倒序


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mymeeting_history_pro);
        init_Value();
        init_View();
        init_Listener();
        load_Data();
        checkVersion();
        registerBroadcast();
    }


    @Override
    protected void init_View() {
        mtv_title_tv.setText(mmechanismbean.getServerName());
        LinearLayoutManager LinearManager = new LinearLayoutManager(this);
        LinearManager.setOrientation(LinearLayoutManager.VERTICAL);
        mlv_scanhistory.setLayoutManager(LinearManager);
        mlv_scanhistory.setRefreshProgressStyle(ProgressStyle.BallSpinFadeLoader);
        mlv_scanhistory.setLaodingMoreProgressStyle(ProgressStyle.BallRotate);
        mlv_scanhistory.setArrowImageView(R.drawable.iconfont_downgrey);
    }

    private void init_Listener() {
        mrl_sort.setOnClickListener(this);
        mrl_mechanism.setOnClickListener(this);
        mhiv_seting.setOnClickListener(this);
        mSearch_guidetext.setOnClickListener(this);
        mlv_scanhistory.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {
                getGetMeetingByOrganization(mmechanismbean.getServerAddress());
            }

            @Override
            public void onLoadMore() {
            }
        });
    }

    @Override
    protected void init_Value() {
        ViewUtils.inject(this);
        ActicityManager.getInstance().add(this);
        UIHelper.showTintStatusBar(this); // 自定义状态栏
        LoginPhone = (String) get(DRMUtil.KEY_PHONE_NUMBER, "");
        mmechanismbean = (MechanismBean) getIntent().getSerializableExtra("MechanismData");
        setMechanismToSP(LoginPhone, mmechanismbean);// 把选中的机构信息存储在SharedPreferences中。
    }

    /**
     * 访问网络
     */
    @Override
    protected void load_Data() {
        // 如果是云平台,就加载账号下面的所有机构,显示，否则,直接加载会议
        if (Constant.BASE_DEFAULT_URL.equals(mmechanismbean.getServerAddress())) {
            // 记录上一次退出时，此帐号登陆的机构。如果mmechanismbean_Str不为""，说明上次已经登陆,记录下来的对象。
            // 如果为""，说明是第一次登陆。
            String mmechanismbean_Str = (String) SPUtils.get(LoginPhone + "loggin_mmechanism", "");
            if (!"".equals(mmechanismbean_Str)) {
                mmechanismbean = StringUtil.getMechanismBeanByStr(mmechanismbean_Str);
                SPUtils.save(LoginPhone, mmechanismbean_Str);
                StringUtil.SaveSP_HostPort(mmechanismbean.getServerAddress());// 解析post和host 存在sp中
                mtv_title_tv.setText(mmechanismbean.getServerName());// 动态改变title栏中机构的名字
                getGetMeetingByOrganization(mmechanismbean.getServerAddress());
            } else {
                StringUtil.SaveSP_HostPort(mmechanismbean.getServerAddress());// 解析post和host 存在sp中
                getWSMechanismAndMetting();
            }
        } else {
            setMechanismToSP(LoginPhone, mmechanismbean);
            StringUtil.SaveSP_HostPort(mmechanismbean.getServerAddress());// 解析post和host 存在sp中
            mtv_title_tv.setText(mmechanismbean.getServerName());// 动态改变title栏中机构的名字
            getGetMeetingByOrganization(mmechanismbean.getServerAddress());
        }
    }

    /**
     * 获取机构接口然后默认选择第一个结构，去访问会议接口。
     */
    private void getWSMechanismAndMetting() {
        WSHelper.newBuilder().setmUrl(Constant.OrganizationNameByPhone_URL).setMethodName(Constant.GetOrganizationNameByPhone_MethodName).putParam("phone", LoginPhone).setCallback(new WSHelper.OnLoadDataListener<ReturnMechanismDataBean>() {
            @Override
            public void onSuccess(ReturnMechanismDataBean bmd) {
                if ("true".equals(bmd.getResult())) {
                    List<MechanismBean> listStr = bmd.getToken();
                    // 如果列表为1个,则是默认显示第一个。
                    // 否则,显示列表,供用户选择
                    if (listStr != null && listStr.size() == 1) {
                        mmechanismbean = listStr.get(0);
                        if (TextUtils.isEmpty(mmechanismbean.getServerAddress()) || TextUtils.isEmpty(mmechanismbean.getServerName()) || TextUtils.isEmpty(mmechanismbean.getSZUserName())) {
                            showToast("机构数据异常");
                            return;
                        }
                        setMechanismToSP(LoginPhone, mmechanismbean);// 把选中的机构信息存储在SharedPreferences中。
                        StringUtil.SaveSP_HostPort(mmechanismbean.getServerAddress());// 解析post和host 存在sp中
                        mtv_title_tv.setText(mmechanismbean.getServerName());// 动态改变title栏中机构的名字
                        getGetMeetingByOrganization(mmechanismbean.getServerAddress());
                    } else {
                        DialogUtil.showDialog_lv(MyMeetingActivityPro.this, setOrganizationAdapter(listStr, LoginPhone));
                    }
                } else {
                    showToast("获取会议列表失败");
                }
            }

            @Override
            public void onError(String error_str, Exception e) {
                showToast("暂未查询出数据。");
                DialogUtil.hideDialog_lv();
                setOrganizationAdapter(null, LoginPhone);
            }
        }).build(mBaseActivity, new TypeToken<ReturnMechanismDataBean>() {}.getType());

    }

    /**
     * 获取所有会议
     */
    private void getGetMeetingByOrganization(String serveraddress) {
        WSHelper.newBuilder().setmUrl(Constant.getMeetingByOrganiztion_URL(serveraddress)).setMethodName(Constant.GetMeetingByOrganization_MethodName).putParam("phone", LoginPhone).putParam("serveraddress", serveraddress).putParam("szusername", mmechanismbean.getSZUserName()).setCallback(new WSHelper.OnLoadDataListener<ReturnMeetingRecordDataBean>() {
            @Override
            public void onSuccess(ReturnMeetingRecordDataBean Rdata) {
                if (Constant.return_true.equals(Rdata.getResult())) {
                    mDataList = Rdata.getData();
                    SortUtil.MeetingSort(mDataList, typeSort);
                    setAdapter(mDataList);
                } else if (Constant.return_false.equals(Rdata.getResult())) {
                    showToast(Rdata.getMsg());
                    setAdapter(null);
                }
                setVisible();
                mlv_scanhistory.refreshComplete();
            }

            @Override
            public void onError(String error_str, Exception e) {
                showToast("会议列表访问失败,请联系管理员");
                setInvisible();
                setAdapter(null);
                mlv_scanhistory.refreshComplete();
            }
        }).build(mBaseActivity, new TypeToken<ReturnMeetingRecordDataBean>() {}.getType());
    }


    /**
     * @author 李巷阳
     * @date 2016-8-16 上午11:20:54
     */
    private void setVisible() {
        mlv_scanhistory.setVisibility(View.VISIBLE);
    }


    /**
     * @author 李巷阳
     * @date 2016-8-16 上午11:19:15
     */
    private void setInvisible() {
        mlv_scanhistory.setVisibility(View.GONE);
    }

    /**
     * 设置我的会议的数据适配器
     *
     * @param meetingRecordData
     */
    private void setAdapter(List<MeetingBean> meetingRecordData) {
        if (meetingRecordData == null) mDataList = null;
        if (mAdapter == null) {
            mAdapter = new MHAdapter(getApplicationContext(), meetingRecordData);
            mlv_scanhistory.setAdapter(mAdapter);
        } else {
            mAdapter.refreshData(meetingRecordData);
        }
        // 我的会议的点击事件
        mAdapter.setOnItemClickListener(new Rv_BaseAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                final MeetingBean mData = mDataList.get(position - 1);
                OpenPageUtil.openDownloadMainByMyMeeting(MyMeetingActivityPro.this, mData, mmechanismbean);
            }
        });
    }



    /**
     * 获取版本号。
     */
    private void checkVersion() {
        if (!CommonUtil.isNetConnect(this)) return;
        if (isStop) return;
        XMLParserVersionManager.getInstance().checkUpdate(this, isStop, new XMLParserVersionManager.OnCheckResultListener() {
            @Override
            public void onResult(boolean hasNewVersion, Object result) {
                if (hasNewVersion) {
                    mv_infodot.setVisibility(View.VISIBLE);
                } else {
                    mv_infodot.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        isStop = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isStop = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(netReceive);
    }

    /**
     * view控件回调
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // 点击排序
            case R.id.rl_sort:
                // 如果倒序,就设置为正序。否则，相反。
                if (typeSort == SortUtil.TypeSort.Reverse) {
                    Drawable nav_up = getResources().getDrawable(R.drawable.sort_reverse_lift);
                    nav_up.setBounds(0, 0, nav_up.getMinimumWidth(), nav_up.getMinimumHeight());
                    mtv_sort.setCompoundDrawables(null, null, nav_up, null);
                    typeSort = SortUtil.TypeSort.Sequence;
                } else {
                    Drawable nav_up = getResources().getDrawable(R.drawable.sort_lift);
                    nav_up.setBounds(0, 0, nav_up.getMinimumWidth(), nav_up.getMinimumHeight());
                    mtv_sort.setCompoundDrawables(null, null, nav_up, null);
                    typeSort = SortUtil.TypeSort.Reverse;
                }
                if (mDataList != null)//不等于null 则排序
                    SortUtil.MeetingSort(mDataList, typeSort);// 排序
                setAdapter(mDataList);
                break;
            // 设置界面
            case R.id.iv_seting:
                openActivity(SettingActivity.class);
                UIHelper.startInAnim(MyMeetingActivityPro.this);
                break;
            // 点击添加机构二维码
            case R.id.rl_mechanism:
                getWSOrganizationName();

               /* DialogUtil.showDialog_lv(MyMeetingActivityPro.this, null);
                getWSOrganizationName2();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1000);
                            getWSOrganizationName2();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();*/

                break;
            // 点击搜索
            case R.id.search_guidetext:
                Bundle bundle = new Bundle();
                bundle.putSerializable("MechanismData", mmechanismbean);
                bundle.putSerializable("MeetingDataList", (Serializable) mDataList);
                openActivity(SearchProductActivity.class, bundle);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                break;
        }

    }


    /**
     * 获取机构接口
     */
    private void getWSOrganizationName() {
        WSHelper.newBuilder().setmUrl(Constant.OrganizationNameByPhone_URL).setMethodName(Constant.GetOrganizationNameByPhone_MethodName).putParam("phone", LoginPhone).setCallback(new WSHelper.OnLoadDataListener<ReturnMechanismDataBean>() {
            @Override
            public void onSuccess(ReturnMechanismDataBean bmd) {
                if ("true".equals(bmd.getResult())) {
                    List<MechanismBean> listStr = bmd.getToken();
                    DialogUtil.showDialog_lv(MyMeetingActivityPro.this, setOrganizationAdapter(listStr, LoginPhone));
                } else {
                    showToast(bmd.getMsg());
                    DialogUtil.hideDialog_lv();
                    setOrganizationAdapter(null, LoginPhone);
                }
            }
            @Override
            public void onError(String error_str, Exception e) {
                showToast("机构列表访问失败,请联系管理员");
                DialogUtil.showDialog_lverrir(MyMeetingActivityPro.this);
                setOrganizationAdapter(null, LoginPhone);
            }
        }).build(mBaseActivity, new TypeToken<ReturnMechanismDataBean>() {}.getType());
    }

   /* *//**
     * 获取机构接口
     *//*
    private void getWSOrganizationName2() {
        WSHelper.newBuilder().setmUrl(Constant.OrganizationNameByPhone_URL).setMethodName(Constant.GetOrganizationNameByPhone_MethodName).putParam("phone", LoginPhone).setCallback(new WSHelper.OnLoadDataListener<ReturnMechanismDataBean>() {
            @Override
            public void onSuccess(ReturnMechanismDataBean bmd) {
                if ("true".equals(bmd.getResult())) {
                    List<MechanismBean> listStr = bmd.getToken();
                    DialogUtil.showDialog_lv2(MyMeetingActivityPro.this, setOrganizationAdapter(listStr, LoginPhone));
                }
            }
            @Override
            public void onError(String error_str, Exception e) {
                showToast("机构列表访问失败,请联系管理员");
                setOrganizationAdapter(null, LoginPhone);
            }
        }).build(mBaseActivity, new TypeToken<ReturnMechanismDataBean>() {}.getType());
    }*/
    /**
     * 设置机构的数据适配器，以及机构的点击事件。
     *
     * @param liststr
     * @param PhoneNumber
     * @return
     */
    private JGAdapter setOrganizationAdapter(final List<MechanismBean> liststr, final String PhoneNumber) {
        JGAdapter mOrganizationAdapter = null;
        if (mOrganizationAdapter == null) {
            mOrganizationAdapter = new JGAdapter(getApplication(), liststr, PhoneNumber);
        } else {
            mOrganizationAdapter.refreshData(liststr);
        }
        // 对话框dialog显示机构列表的 item点击事件回调。
        mOrganizationAdapter.setOnItemClickListener(new Rv_BaseAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                DialogUtil.hideDialog_lv();
                mmechanismbean = liststr.get(position);
                if (TextUtils.isEmpty(mmechanismbean.getServerAddress()) || TextUtils.isEmpty(mmechanismbean.getServerName()) || TextUtils.isEmpty(mmechanismbean.getSZUserName())) {
                    showToast("机构数据异常");
                    return;
                }
                setMechanismToSP(PhoneNumber, mmechanismbean);// 把选中的机构信息存储在SharedPreferences中。
                setMechanismToSPForLoggin(PhoneNumber, mmechanismbean);
                StringUtil.SaveSP_HostPort(mmechanismbean.getServerAddress());// 解析post和host 存在sp中
                mtv_title_tv.setText(mmechanismbean.getServerName());// 动态改变title栏中机构的名字
                getGetMeetingByOrganization(mmechanismbean.getServerAddress());

            }
        });
        return mOrganizationAdapter;
    }


    private void setMechanismToSP(String PhoneNumber, MechanismBean mmechanismbean) {
        String AddressAndNameStr = mmechanismbean.getServerAddress() + "," + mmechanismbean.getServerName() + "," + mmechanismbean.getSZUserName();
        if (!"".equals(PhoneNumber)) {
            SPUtils.save(PhoneNumber, AddressAndNameStr);
        }
    }


    private void setMechanismToSPForLoggin(String PhoneNumber, MechanismBean mmechanismbean) {
        String AddressAndNameStr = mmechanismbean.getServerAddress() + "," + mmechanismbean.getServerName() + "," + mmechanismbean.getSZUserName();
        if (!"".equals(PhoneNumber)) {
            SPUtils.save(PhoneNumber + "loggin_mmechanism", AddressAndNameStr);
        }
    }

    /**
     * @author 李巷阳
     * @date 2016-8-25 下午3:10:12
     */
    private void registerBroadcast() {
        // 就要关闭登陆界面。
        Intent intent = new Intent(DRMUtil.BROADCAST_CLEAR_LOGIN_ACTIVITY);
        this.sendBroadcast(intent);

        // ，关闭会议界面
        IntentFilter netFilter = new IntentFilter();
        netFilter.addAction(DRMUtil.BROADCAST_CLEAR_MYMEETING_ACTIVITY);
        registerReceiver(netReceive, netFilter);
    }

    /**
     * 广播接收者
     */
    private BroadcastReceiver netReceive = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) return;
            String action = intent.getAction();
            if (action == null) return;
            switch (action) {
                // ,关闭此界面。
                case DRMUtil.BROADCAST_CLEAR_MYMEETING_ACTIVITY:
                    MyMeetingActivityPro.this.finish();
                    break;
            }
        }
    };

    /**
     * 机构对话框添加机构回调
     *
     * @param v
     */
    @Override
    public void OnClick_Dialog_Listener(View v) {
        switch (v.getId()) {
            // 添加机构回调
            case R.id.rl_add_mechanism:
                SPUtils.remove(DRMUtil.KEY_LOGIN_TOKEN);
                OpenPageUtil.openZXingCode(MyMeetingActivityPro.this);
                UIHelper.startInAnim(MyMeetingActivityPro.this);
                break;
        }
    }

    /**
     * startActivityForResult回调
     *
     * @author 李巷阳
     * @date 2016-9-9 下午3:13:31
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constant.REQUEST_CODE_SCAN: {
                // 扫码获取机构信息
                if (resultCode == CaptureActivity.RESULT_OK && data != null) {
                    Bundle bundle = data.getExtras();
                    String DataSource = bundle.getString(Constant.DECODED_CONTENT_KEY);
                    saveScanResult(DataSource);
                }
            }
            break;
            // 扫描二维码后关闭此界面
            case Constant.SCANHISTORY_CODE: {
                if (resultCode == Activity.RESULT_OK) {
                    ToastShow.getInstances_().showOk(mBaseActivity, "扫描成功");

                }
            }
            break;
        }
    }

    /**
     * 扫描二维码后触发
     * <p>
     * 1.通过机构码获取机构信息 2.添加机构信息到服务器 3.查询添加机构下面所有的会议
     */
    private void saveScanResult(final String dataSource) {
        if(dataSource==null||"".equals(dataSource)){
            showToast("机构数据异常");
            return;
        }

        if (!(dataSource.contains("ServerName"))) {
            return;
        }
       final MechanismBean mec = JSON.parseObject(dataSource, MechanismBean.class);
        // 判断扫描获取的机构是否正常
        if (TextUtils.isEmpty(mec.getServerAddress()) || TextUtils.isEmpty(mec.getServerName()) || TextUtils.isEmpty(mec.getSZUserName())) {
            showToast("机构数据异常");
            return;
        }
        WSHelper.newBuilder().setmUrl(Constant.onAddOrganization_URL).setMethodName(Constant.onAddOrganization_MethodName)
                .putParam("phone",LoginPhone)
                .putParam("serveraddress",mec.getServerAddress())
                .putParam("servername",mec.getServerName())
                .putParam("szusername",mec.getSZUserName())
                .setCallback(new WSHelper.OnLoadDataListener<BaseBean>() {
                    @Override
                    public void onSuccess(BaseBean bbdata) {
                        if ("true".equals(bbdata.getResult())) {
                            StringUtil.SaveSP_HostPort(mec.getServerAddress());
                            getGetMeetingByOrganization(mec.getServerAddress());
                        }else{
                            mBaseActivity.hideBgLoading();
                            showToast(bbdata.getMsg());
                            DialogUtil.hideDialog_lv();
                        }
                    }
                    @Override
                    public void onError(String error_str, Exception e) {
                        showToast("添加机构失败,请联系管理员");
                        DialogUtil.hideDialog_lv();
                    }
                }).build(mBaseActivity,new TypeToken<BaseBean>() {}.getType());

//      判断是否是机构二维码
////        if (!isMechanismQRcode(DataSource)) return;
//        showBgLoading(getResources().getString(R.string.load_ing));
//        Observable<ReturnMeetingRecordDataBean> addMechanismAndGetMetting = RxAndroid.addMechanismAndGetMetting(mtv_title_tv, LoginPhone, DataSource);
//        Subscriber subscriber = new Subscriber<ReturnMeetingRecordDataBean>() {
//            public void onCompleted() {
//                hideBgLoading();
//            }
//
//            public void onError(Throwable arg0) {
//                showToast(arg0.getMessage());
//                hideBgLoading();
//            }
//
//            @Override
//            public void onNext(ReturnMeetingRecordDataBean Rdata) {
//                if (Constant.return_true.equals(Rdata.getResult())) {
//                    mDataList = Rdata.getData();
//                    SortUtil.MeetingSort(mDataList, typeSort);// 排序
//                    setAdapter(mDataList);
//                    DialogUtil.hideDialog_lv();
//                } else if (Constant.return_false.equals(Rdata.getResult())) {
//                    showToast(Rdata.getMsg());
//                    DialogUtil.hideDialog_lv();
//                }
//            }
//        };
//        addMechanismAndGetMetting.subscribe(subscriber);

    }

    /**
     * 退出
     */
    @Override
    public void onBackPressed() {
        CommonUtil.showExitDialog(this);
    }


    /**
     * @Description: (判断是否是机构二维码)
     * @author 李巷阳
     * @date 2016-9-9 上午11:51:39
     */
    private boolean isMechanismQRcode(final String DataSource) {
        final String GetCompany = StringUtil.getStringByColonHalf(DataSource, "?op", "=");
        if (TextUtils.isEmpty(GetCompany) && !"GetCompany".equals(GetCompany)) {
            showToast(getString(R.string.scan_qr_code_error));
            return false;
        }
        return true;
    }
}
