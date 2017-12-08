package cn.com.pyc.drm.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.sax.StartElementListener;
import cn.com.meeting.drm.R;
import cn.com.pyc.drm.bean.MechanismBean;
import cn.com.pyc.drm.bean.ReturnMeetingRecordDataBean.databean;
import cn.com.pyc.drm.bean.ScanHistory;
import cn.com.pyc.drm.common.Constant;
import cn.com.pyc.drm.ui.BaseActivity;
import cn.com.pyc.drm.ui.DownloadMainActivity;
import cn.com.pyc.drm.ui.MechanismActivity;
import cn.com.pyc.drm.ui.MuPDFFindAllNotes;
import cn.com.pyc.drm.ui.MyMeetingActivity;
import cn.com.pyc.drm.ui.ScanHistoryActivity;
import cn.com.pyc.drm.ui.ScanLoginVerificationActivity;

import com.google.zxing.client.android.CaptureActivity;

/**
 * 管理打开其他de页面的工具类
 * 
 * @author hudq
 * 
 */
public class OpenPageUtil {

	/**
	 * 开启页面
	 * 
	 * @param ctx
	 * @param cls
	 */
	public static void openActivity(Context ctx, Class<?> cls) {
		Intent intent = new Intent(ctx, cls);
		ctx.startActivity(intent);
	}

	/** 系统分享 */
	public static void sharedMore(Context ctx, String text) {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_TEXT, text);
		ctx.startActivity(Intent.createChooser(intent, "选择分享"));
	}

	/** 打开系统短信界面 */
	public static void sharedSendMsg(Context ctx, String text) {
		Intent sendIntent = new Intent(Intent.ACTION_VIEW);
		// sendIntent.setData(Uri.parse("smsto:"));
		sendIntent.setType("vnd.android-dir/mms-sms");
		sendIntent.putExtra("sms_body", text);
		ctx.startActivity(Intent.createChooser(sendIntent, null));
	}

	/**
	 * 扫一扫
	 * 
	 * @param ctx
	 */
	public static void openZXingCode(Activity activity) {
		Intent scanerIntent = new Intent(activity, CaptureActivity.class);
		activity.startActivityForResult(scanerIntent, Constant.REQUEST_CODE_SCAN);
	}

	/**
	 * 通过扫一扫，进入到主界面
	 * 
	 * @param activity
	 * @param result
	 *            扫描到的信息
	 * @param id
	 *            扫描的id
	 * 
	 * @param systemType
	 *            类型
	 * 
	 */
	public static void openDownloadMainByScaning(Activity activity, String result, String id, String systemType) {
		Intent intent = new Intent(activity, DownloadMainActivity.class);
		intent.putExtra("result_url", result);
		intent.putExtra("result_id", id);
		intent.putExtra("system_type", systemType);
		activity.startActivity(intent);
	}

	/**
	 * 通过扫一扫，进入到主界面
	 * 
	 * @param activity
	 * @param result
	 *            扫描到的信息
	 * @param id
	 *            扫描的id
	 * 
	 * @param systemType
	 *            类型
	 * 
	 */
	public static void openDownloadMainByScaning2(Activity activity, ScanHistory sh) {
		Intent intent = new Intent(activity, DownloadMainActivity.class);
		Bundle bundle = new Bundle();
		bundle.putSerializable("MeetingDetails", sh);
		intent.putExtras(bundle);
		activity.startActivity(intent);
		UIHelper.startInAnim(activity);

	}

	public static void openDownloadMainByMyMeeting(final Activity activity, final databean dbbean, final MechanismBean mmechanismbean) {
		AsyncTask<String, String, ScanHistory> mLoginTask = new AsyncTask<String, String, ScanHistory>() {
			protected void onPreExecute() {
				((BaseActivity) activity).showBgLoading(activity.getResources().getString(R.string.load_ing));
			};

			@Override
			protected ScanHistory doInBackground(String... params) {
				SPUtils.save(DRMUtil.KEY_VISIT_NAME, mmechanismbean.getSZUserName());
				SPUtils.save(DRMUtil.KEY_MEETINGID, dbbean.getSuiZhiCode());// 存储会议id
				SPUtils.save(DRMUtil.VOTE_URL, dbbean.getUrl());
				// 根据返回的url获取主机和端口保存
				String[] hostAndPortString = StringUtil.getHostAndPortByResult(dbbean.getQRCode() + "/");
				if (hostAndPortString != null) {
					// 保存主机名。eg： video.suizhi.net
					SPUtils.save(DRMUtil.SCAN_FOR_HOST, hostAndPortString[0]);
					// 保存端口号。eg：8657
					SPUtils.save(DRMUtil.SCAN_FOR_PORT, hostAndPortString[1]);
				}
				ScanHistory sh = new ScanHistory();
				sh.setMeetingId(dbbean.getSuiZhiCode());
				sh.setScanDataSource(dbbean.getQRCode());
				sh.setMeetingType(dbbean.getMeetingType());
				sh.setUsername(mmechanismbean.getSZUserName());
				sh.setIsverifys(dbbean.getVerify());
				sh.setMeetingName(dbbean.getMeetingName());
				sh.setClient_url(dbbean.getClient_url());
				sh.setVerify_url(dbbean.getVerifyUrl());
				TimeUtil.sleep_time(1000);
				return sh;
			}


			protected void onPostExecute(ScanHistory sh) {
				if(Constant.sign_verify.equals(sh.getIsverifys())){
					openScanVerificationActivity(activity, sh);
				}else{
					openDownloadMainByScaning2(activity, sh);
				}
				((BaseActivity) activity).hideBgLoading();
			}


		}.execute();
	}
	private static void openScanVerificationActivity(final Activity activity, ScanHistory sh) {
		Intent intent = new Intent(activity, ScanLoginVerificationActivity.class);
		Bundle bundle = new Bundle();
		bundle.putSerializable("MeetingDetails", sh);
		intent.putExtras(bundle);
		activity.startActivity(intent);
	};
	/**
	 * @Description: (打开备注列表)
	 * @author 李巷阳
	 * @date 2016-6-16 下午5:08:23
	 */
	public static void openMuPDFFindAllNotes(Activity activity, String asset_id) {
		Intent intent = new Intent();
		intent.setClass(activity, MuPDFFindAllNotes.class);
		Bundle bundle = new Bundle();
		bundle.putString("asset_id", asset_id);
		intent.putExtras(bundle);
		activity.startActivityForResult(intent, 0);
		UIHelper.startInAnim(activity);

	}

	/**
	 * @Description: (打开验证界面)
	 * @author 李巷阳
	 * @date 2016-6-16 下午5:08:23
	 */
	public static void openScanLoginVerificationActivity(Activity activity, ScanHistory sh) {
		Intent intent = new Intent(activity, ScanLoginVerificationActivity.class);
		Bundle bundle = new Bundle();
		bundle.putSerializable("MeetingDetails", sh);
		intent.putExtras(bundle);
		activity.startActivity(intent);

	}

	/**
	 * @Description: (登陆跳转机构界面)
	 * @author 李巷阳
	 * @date 2016-9-8 下午5:36:35
	 */
	public static void openMechanismActivity(Activity activity, String isforloginactivity) {
		Intent intent = new Intent(activity, MechanismActivity.class);
		Bundle bundle = new Bundle();
		bundle.putString("isForActivity", isforloginactivity);
		intent.putExtras(bundle);
		activity.startActivity(intent);
		UIHelper.startInAnim(activity);
	}

	/**
	 * @Description: (登陆跳转会议界面)
	 * @author 李巷阳
	 * @date 2016-9-8 下午5:39:26
	 */
	public static void openMyMeetingActivity(Activity activity, String morganization_code) {
		Intent intent = new Intent(activity, MyMeetingActivity.class);
		MechanismBean mmechanis_data = StringUtil.getMechanismBeanByStr(morganization_code);
		Bundle bundle = new Bundle();
		bundle.putSerializable("MechanismData", mmechanis_data);
		intent.putExtras(bundle);
		activity.startActivity(intent);
		UIHelper.startInAnim(activity);
	}

	
	public static void openMyMeetingbyMechanism(Activity activity){
		Intent intent = new Intent(activity, MechanismActivity.class);
		Bundle bundle = new Bundle();
		bundle.putString("isForActivity", "ForScanHistoryActivity");
		intent.putExtras(bundle);
		activity.startActivityForResult(intent, DRMUtil.RefreshMyMeetingActivity);// 这里采用startActivityForResult来做跳转，此处的0为一个依据，可以写其他的值，但一定要>=0
		UIHelper.startInAnim(activity);
	}
	
	
	
}
