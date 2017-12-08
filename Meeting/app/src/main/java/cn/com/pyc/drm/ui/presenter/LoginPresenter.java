package cn.com.pyc.drm.ui.presenter;

import com.alibaba.fastjson.JSON;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;

import rx.Observable;
import rx.Subscriber;
import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import cn.com.meeting.drm.R;
import cn.com.pyc.drm.bean.ReturnDataBean;
import cn.com.pyc.drm.bean.ScanHistory;
import cn.com.pyc.drm.common.Constant;
import cn.com.pyc.drm.model.MeetingNameModel;
import cn.com.pyc.drm.model.MeetingNameModel.MeetingName;
import cn.com.pyc.drm.ui.LoginActivity;
import cn.com.pyc.drm.ui.view.ILoginView;
import cn.com.pyc.drm.utils.CommonUtil;
import cn.com.pyc.drm.utils.DRMLog;
import cn.com.pyc.drm.utils.DRMUtil;
import cn.com.pyc.drm.utils.DeviceUtils;
import cn.com.pyc.drm.utils.OpenPageUtil;
import cn.com.pyc.drm.utils.SPUtils;
import cn.com.pyc.drm.utils.StringUtil;
import cn.com.pyc.drm.utils.UIHelper;
import cn.com.pyc.drm.utils.manager.RequestHttpManager;
import cn.com.pyc.drm.utils.manager.RxAndroid;
import cn.com.pyc.drm.utils.manager.ScanHistoryDBManager;

/**
 * @Description: (用一句话描述该文件做什么)
 * @author 李巷阳
 * @date 2016-10-18 下午6:02:49
 * @version V1.0
 */
public class LoginPresenter {

	private ILoginView mILoginView;
	private AsyncTask<String, String, String> mLoginTask;

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

	/**
	 * 保存扫描到的用户名称
	 * 
	 * @param result
	 */
	public void saveScanResult(final Context cxt, final String DataSource) {
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
			};

			@Override
			protected String doInBackground(String... params) {
				if (isCancelled())
					return null;
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
			};
		};
		mLoginTask.execute();
	}

	/**
	 * 
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
	 * 
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
