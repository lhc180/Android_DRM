package cn.com.pyc.drm.ui.presenter;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;

import java.util.List;

import cn.com.meeting.drm.R;
import cn.com.pyc.drm.adapter.JGAdapter;
import cn.com.pyc.drm.adapter.Rv_BaseAdapter;
import cn.com.pyc.drm.bean.MechanismBean;
import cn.com.pyc.drm.bean.ReturnDataBean;
import cn.com.pyc.drm.bean.ScanHistory;
import cn.com.pyc.drm.common.Constant;
import cn.com.pyc.drm.model.MeetingNameModel;
import cn.com.pyc.drm.model.MeetingNameModel.MeetingName;
import cn.com.pyc.drm.ui.view.ILoginView;
import cn.com.pyc.drm.utils.CommonUtil;
import cn.com.pyc.drm.utils.DRMUtil;
import cn.com.pyc.drm.utils.DeviceUtils;
import cn.com.pyc.drm.utils.SPUtils;
import cn.com.pyc.drm.utils.StringUtil;
import cn.com.pyc.drm.utils.UIHelper;
import cn.com.pyc.drm.utils.manager.RequestHttpManager;
import cn.com.pyc.drm.utils.manager.RxAndroid;
import cn.com.pyc.drm.utils.manager.ScanHistoryDBManager;
import cn.com.pyc.drm.widget.ToastShow;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;


/**
 * @author 李巷阳
 * @version V1.0
 * @Description: (用一句话描述该文件做什么)
 * @date 2016-10-18 下午6:02:49
 */
public class LoginPresenter {

    private ILoginView mILoginView;
    private AsyncTask<String, String, String> mLoginTask;
    private Dialog showMechanismDialog;


    /**
     * @param mILoginView
     */
    public LoginPresenter(ILoginView mILoginView) {
        super();
        this.mILoginView = mILoginView;
    }






    /**
     * 登陆
     *
     * @author 李巷阳
     * @date 2016-8-17 上午10:53:09
     */
    public void loging(final Context ctx, final String uname_set, final String psd_set) {


        if (!CommonUtil.isNetConnect(ctx)) {
            UIHelper.showToast(ctx, ctx.getResources().getString(R.string.net_disconnected));
            return;
        }

        if (TextUtils.isEmpty(uname_set)) {
            UIHelper.showToast(ctx, ctx.getResources().getString(R.string.login_account_not_empty));
            return;
        }

        mILoginView.showBgLoading(ctx.getResources().getString(R.string.login_ing));

        Observable<ReturnDataBean> observableservice = RxAndroid.getLoginChecking(uname_set, psd_set);
        Subscriber subscriber = new Subscriber<ReturnDataBean>() {
            public void onError(Throwable arg0) {
                mILoginView.showError(ctx.getResources().getString(R.string.login_account_error));
            }

            @Override
            public void onNext(ReturnDataBean Rdata) {
                if (Rdata != null) {
                    if (Constant.return_true.equals(Rdata.getResult())) {
                        SPUtils.save("username", uname_set);
                        SPUtils.save("password", psd_set);
                        SPUtils.save(DRMUtil.KEY_PHONE_NUMBER, Rdata.getToken());
                        String morganization_code = (String) SPUtils.get(Rdata.getToken(), "");//获取机构号码(如果为Null或为空,则进入机构揭界面。)
                        mILoginView.showSuccess(morganization_code);
                    } else if (Constant.return_false.equals(Rdata.getResult())) {
                        mILoginView.showError(Rdata.getMsg());
                    }
                } else {
                    mILoginView.showError(ctx.getResources().getString(R.string.login_account_error));
                }
            }

            @Override
            public void onCompleted() {
                mILoginView.hideBgLoading();
            }
        };
        observableservice.subscribe(subscriber);

    }

    private void showLoginDialog_lv(Context mContext, JGAdapter baseAdapter) {
        showMechanismDialog = new Dialog(mContext, R.style.SZ_LoadBgDialog);
        View view = View.inflate(mContext, R.layout.dialog_server_lv, null);
        TextView mTv_prompt = (TextView) view.findViewById(R.id.tv_prompt);
        mTv_prompt.setText("请选择机构");
        RecyclerView mLv_control = (RecyclerView) view.findViewById(R.id.lv_control);
        LinearLayoutManager LinearManager = new LinearLayoutManager(mContext);
        LinearManager.setOrientation(LinearLayoutManager.VERTICAL);
        mLv_control.setLayoutManager(LinearManager);
        mLv_control.setAdapter(baseAdapter);

        showMechanismDialog.setContentView(view);
        showMechanismDialog.setCancelable(false);
        showMechanismDialog.setCanceledOnTouchOutside(true);
        showMechanismDialog.show();

    }

    /**
     * 设置机构的数据适配器，以及机构的点击事件。
     *
     * @param liststr
     * @return
     */
    private JGAdapter setOrganizationAdapter(final Context mContext, final List<MechanismBean> liststr,final String username,final String password) {
        JGAdapter mOrganizationAdapter = null;
        if (mOrganizationAdapter == null) {
            mOrganizationAdapter = new JGAdapter(mContext, liststr);
        } else {
            mOrganizationAdapter.refreshData(liststr);
        }
        mOrganizationAdapter.setOnItemClickListener(new Rv_BaseAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                hideDialog_lv();
                MechanismBean mMechanismbean = liststr.get(position);
                if (TextUtils.isEmpty(mMechanismbean.getServerAddress()) || TextUtils.isEmpty(mMechanismbean.getServerName()) || TextUtils.isEmpty(mMechanismbean.getSZUserName())) {
                    //                    showToast("机构数据异常");
                    ToastShow.getInstances_().showFail(mContext, "机构数据异常");
                    return;
                }
                // 切换时，更新端口号
                String[] hostAndPortString = StringUtil.getHostAndPortByResult(mMechanismbean.getServerAddress());
                if (hostAndPortString != null) {
                    // 保存主机名。eg： video.suizhi.net
                    SPUtils.save(DRMUtil.SCAN_FOR_HOST, hostAndPortString[0]);
                    // 保存端口号。eg：8657
                    SPUtils.save(DRMUtil.SCAN_FOR_PORT, hostAndPortString[1]);
                }
                mILoginView.ResultMechanismValidate(username,password);
            }
        });
        return mOrganizationAdapter;
    }
    /** 关闭对话框 **/
    public void hideDialog_lv(){
        if (showMechanismDialog != null) {
            showMechanismDialog.dismiss();
        }
    }

    // 添加机构信息保存在SharedPreferences里面
    private void setMechanismToSP(String PhoneNumber, MechanismBean mmb) {
        String AddressAndNameStr = mmb.getServerAddress() + "," + mmb.getServerName() + "," + mmb.getSZUserName();
        if (!"".equals(PhoneNumber)) {
            SPUtils.save(PhoneNumber, AddressAndNameStr);
        }
    }

    /**
     * 自动登陆判断
     *
     * @author 李巷阳
     * @date 2016-10-19 上午11:57:44
     */
    public void auto_str_isEmpty(Context ctx) {
        String username = (String) SPUtils.get("username", "");
        String password = (String) SPUtils.get("password", "");
        // 如果账号为空 ,则不自动登陆
        if (TextUtils.isEmpty(username)) {
            return;
        }
        // 如果账号为手机号,密码为空,则不自动登陆
        if (StringUtil.isMobileNO(username) && "".equals(password)) {
            return;
        }
        loging(ctx, username, password);
    }

    public void loging_validate(Context ctx, String username, String password) {
//        if (mMechanismbean != null) {
//            // 切换时，更新端口号
//            String[] hostAndPortString = StringUtil.getHostAndPortByResult(mMechanismbean.getServerAddress());
//            if (hostAndPortString != null) {
//                // 保存主机名。eg： video.suizhi.net
//                SPUtils.save(DRMUtil.SCAN_FOR_HOST, hostAndPortString[0]);
//                // 保存端口号。eg：8657
//                SPUtils.save(DRMUtil.SCAN_FOR_PORT, hostAndPortString[1]);
//            }
//            mILoginView.ResultMechanismValidate(username,password);
//        } else {
            List<MechanismBean> mListMechanismBean = ScanHistoryDBManager.Builder(ctx).findAllMechanismScanHistory();
            showLoginDialog_lv(ctx, setOrganizationAdapter(ctx, mListMechanismBean,username,password));
//        }
    }

    /**
     * 保存扫描到的用户名称
     */
    public void saveMeetingScanResult(final Context cxt, final String DataSource) {
        if (TextUtils.isEmpty(DataSource)) {
            mILoginView.showToast(cxt.getString(R.string.scaning_empty));
            return;
        }
        if (TextUtils.isDigitsOnly(DataSource)) {
            mILoginView.showMsgDialog(DataSource);
            return;
        }
        if (!CommonUtil.isNetConnect(cxt)) {
            mILoginView.showToast(cxt.getString(R.string.net_disconnected));
            return;
        }
        final String userName = StringUtil.getStringByResult(DataSource, "username=");
        final String id = StringUtil.getStringByResult(DataSource, "id=");
        final String systemType = StringUtil.getStringByResult(DataSource, "SystemType=");

        if (TextUtils.isEmpty(userName) || TextUtils.isEmpty(id)) {
            mILoginView.showMsgDialog(DataSource);
            return;
        }
        cancelTask();
        mLoginTask = new AsyncTask<String, String, String>() {
            protected void onPreExecute() {
                mILoginView.showBgLoading("载入中...");
            }

            @Override
            protected String doInBackground(String... params) {
                if (isCancelled()) return null;
                // 保存用户名
                SPUtils.save(DRMUtil.KEY_VISIT_NAME, userName);
                SPUtils.save(DRMUtil.KEY_MEETINGID, id);// 存储会议id
                // 根据返回的url获取主机和端口保存
                String[] hostAndPortString = StringUtil.getHostAndPortByResult(DataSource);
                if (hostAndPortString != null) {
                    // 保存主机名。eg： video.suizhi.net
                    SPUtils.save(DRMUtil.SCAN_FOR_HOST, hostAndPortString[0]);
                    // 保存端口号。eg：8657
                    SPUtils.save(DRMUtil.SCAN_FOR_PORT, hostAndPortString[1]);
                }
                return null;
            }

            protected void onPostExecute(String result) {
                getQRcodeCheckUser(cxt, userName, id, systemType, DataSource);
            }

        };
        mLoginTask.execute();
    }

    public void saveMechanismScanResult(final Context cxt, final String DataSource) {
        if (TextUtils.isEmpty(DataSource)) {
            mILoginView.showToast(cxt.getString(R.string.scaning_empty));
            return;
        }
        if (!CommonUtil.isNetConnect(cxt)) {
            mILoginView.showToast(cxt.getString(R.string.net_disconnected));
            return;
        }
        RxAndroid.getMechanismData(DataSource).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<MechanismBean>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable throwable) {
                mILoginView.showToast(cxt.getString(R.string.scan_qr_code_error));
            }

            @Override
            public void onNext(MechanismBean mb) {
                // 判断扫描获取的机构是否正常
                if (TextUtils.isEmpty(mb.getServerAddress()) || TextUtils.isEmpty(mb.getServerName()) || TextUtils.isEmpty(mb.getSZUserName())) {
                    mILoginView.showToast("机构数据异常");
                    return;
                }
                // 把机构保存在本地数据库
                boolean isFlag = ScanHistoryDBManager.Builder(cxt).saveScanMechanismHistory(mb);
                if (isFlag) {
                    mILoginView.addMechanismCallback(mb);
                } else {
                    mILoginView.showToast(cxt.getString(R.string.save_mechanism_err));
                }

            }
        });

    }


    /**
     * @Description: (二维码登陆验证)
     * @author 李巷阳
     * @date 2016-9-8 下午4:20:21
     */
    private void getQRcodeCheckUser(final Context cxt, final String username, final String MeetingId, final String MeetingType, final String DataSource) {

        RequestHttpManager.init().postData(DRMUtil.getMeetingNameUrl(), username, MeetingId, new RequestCallBack<String>() {
            public void onSuccess(ResponseInfo<String> arg0) {
                MeetingNameModel model = JSON.parseObject(arg0.result, MeetingNameModel.class);
                if (model.isSuccess()) {
                    MeetingName o = model.getData();
                    if (o == null) {
                        mILoginView.hideBgLoading();
                        mILoginView.showToast(cxt.getResources().getString(R.string.scan_qr_code_error));
                        return;
                    }
                    if ("true".equals(o.getVerify()) || "false".equals(o.getVerify())) {
                        mILoginView.hideBgLoading();
                        mILoginView.showToast(cxt.getResources().getString(R.string.qrcode_overdue));
                        return;
                    }
                    ScanHistory sh = SaveScanHistory(cxt, username, MeetingId, MeetingType, DataSource, o);
                    // verify判断是否需要登陆验证。不需要就把手机号设置为""
                    if (!"0".equals(o.getVerify())) {
                        mILoginView.openScanLoginVerificationActivity(sh);
                    } else {
                        SPUtils.save(DRMUtil.VOTE_URL, o.getUrl());
                        SPUtils.save(DRMUtil.KEY_PHONE_NUMBER, "");
                        mILoginView.openDownloadMainByScaning2(sh);
                    }
                    mILoginView.hideBgLoading();
                } else {
                    mILoginView.showToast("服务器异常");
                    mILoginView.hideBgLoading();
                }
                cancelTask();
            }

            @Override
            public void onFailure(HttpException arg0, String arg1) {
                mILoginView.hideBgLoading();
                mILoginView.showToast("连接服务器失败");
            }
        });
    }

    /**
     * @Description: (保存会议信息到历史记录)
     * @author 李巷阳
     * @date 2016-9-8 下午4:36:13
     */
    private ScanHistory SaveScanHistory(final Context cxt, final String username, final String MeetingId, final String MeetingType, final String DataSource, MeetingName o) {
        ScanHistory sh = new ScanHistory();
        sh.setMeetingId(MeetingId);
        sh.setScanDataSource(DataSource);
        sh.setMeetingType(MeetingType);
        sh.setUsername(username);
        sh.setIsverifys(o.getVerify());
        sh.setVerify_url(o.getVerifyurl() + "&DevicesId=" + DeviceUtils.getIMEI(cxt));
        sh.setVote_title(o.getTitle());
        sh.setMeetingName(o.getName());
        sh.setVote_url(o.getUrl());
        sh.setClient_url(o.getClient_url());
        sh.setCreateTime(o.getCreateTime());
        ScanHistoryDBManager.Builder(cxt).saveScanHistory(sh);
        return sh;
    }

    // 取消任务
    private void cancelTask() {
        if (mLoginTask != null && (!mLoginTask.isCancelled() || mLoginTask.getStatus() == AsyncTask.Status.RUNNING)) {
            mLoginTask.cancel(true);
            mLoginTask = null;
        }
    }

}
