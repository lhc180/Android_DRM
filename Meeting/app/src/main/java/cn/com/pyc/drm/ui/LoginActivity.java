package cn.com.pyc.drm.ui;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

import cn.com.meeting.drm.R;
import cn.com.pyc.drm.bean.ScanHistory;
import cn.com.pyc.drm.common.Constant;
import cn.com.pyc.drm.ui.presenter.LoginPresenter;
import cn.com.pyc.drm.ui.view.ILoginView;
import cn.com.pyc.drm.utils.CommonUtil;
import cn.com.pyc.drm.utils.DRMUtil;
import cn.com.pyc.drm.utils.OpenPageUtil;
import cn.com.pyc.drm.utils.SPUtils;
import cn.com.pyc.drm.utils.StringUtil;
import cn.com.pyc.drm.utils.UIHelper;
import cn.com.pyc.drm.utils.ViewUtil;
import cn.com.pyc.drm.utils.ViewUtil.DialogCallBack;
import cn.com.pyc.drm.utils.manager.ActicityManager;
import cn.com.pyc.drm.widget.ClearEditText;
import cn.com.pyc.drm.widget.FlatButton;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @Description: (登陆界面(采用mvp模式))
 * @author 李巷阳
 * @date 2016-10-18 下午5:49:20
 * @version V1.0
 */
public class LoginActivity extends BaseActivity implements OnClickListener, ILoginView {

	@ViewInject(R.id.tv_register)
	private TextView mtv_register;

	@ViewInject(R.id.tv_forget_password)
	private TextView mtv_forget_password;

	@ViewInject(R.id.tv_scan_code)
	private TextView mtv_scan_code;

	@ViewInject(R.id.tv_history_record)
	private TextView mtv_history_record;

	@ViewInject(R.id.et_username)
	private ClearEditText met_username;

	@ViewInject(R.id.et_password)
	private ClearEditText met_password;

	@ViewInject(R.id.btn_login)
	private FlatButton mbtn_login;

	private String phoneNumber;

	private String isForActivity;

	private LoginPresenter login_presenter;

	private long exitTime = 0;
	/**
	 * @author 李巷阳
	 * @date 2016-10-18 下午5:50:28
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		login_presenter = new LoginPresenter(this);
		setContentView(R.layout.activity_login2);
		init_Value();
		auto_Login();
		initListener();
		registerBroadcast();

	}

	/**
	 * @author 李巷阳
	 * @date 2016-10-18 下午5:50:09
	 */
	@Override
	protected void init_Value() {
		ViewUtils.inject(this);
		phoneNumber = getIntent().getStringExtra(DRMUtil.KEY_PHONE_NUMBER);
		isForActivity = getIntent().getStringExtra(Constant.isForActivity);
		if (!TextUtils.isEmpty(phoneNumber)) {
			met_username.setText(phoneNumber);
			met_password.requestFocus();
		}
	}

	/**
	 * @author 李巷阳
	 * @date 2016-10-18 下午5:50:09
	 */
	@Override
	protected void init_View() {
	}

	/**
	 * @Description: (如果来自欢迎界面跳转，就获取账号密码就去登陆。)
	 * @author 李巷阳
	 * @date 2016-10-19 上午10:56:34
	 */
	private void auto_Login() {
		if (Constant.isForWelcomeActivity.equals(isForActivity)) {
			login_presenter.auto_str_isEmpty(getApplicationContext());
		}
	}

	/**
	 * @author 李巷阳
	 * @date 2016-10-18 下午5:50:09
	 */
	@Override
	protected void load_Data() {
	}

	/**
	 * @author 李巷阳
	 * @date 2016-8-22 下午4:22:29
	 */
	private void registerBroadcast() {
		// 进入下载界面，关闭登陆界面
		IntentFilter netFilter = new IntentFilter();
		netFilter.addAction(DRMUtil.BROADCAST_CLEAR_LOGIN_ACTIVITY);
		registerReceiver(netReceive, netFilter);
	}

	private BroadcastReceiver netReceive = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent == null)
				return;
			String action = intent.getAction();
			if (action == null)
				return;
			switch (action) {
			case DRMUtil.BROADCAST_CLEAR_LOGIN_ACTIVITY:
				LoginActivity.this.finish();
				break;
			}
		}
	};

	/**
	 * @author 李巷阳
	 * @date 2016-8-15 下午4:31:26
	 */
	private void initListener() {
		mtv_register.setOnClickListener(this);
		mtv_forget_password.setOnClickListener(this);
		mtv_scan_code.setOnClickListener(this);
		mtv_history_record.setOnClickListener(this);
		mbtn_login.setOnClickListener(this);
	}

	/**
	 * @author 李巷阳
	 * @date 2016-10-18 下午6:01:33
	 */
	@Override
	public void showError(String string) {
		UIHelper.showToast(getApplicationContext(), string);
	}

	/**
	 * @author 李巷阳
	 * @date 2016-10-18 下午6:01:33
	 */
	@Override
	public void showSuccess(String morganization_code) {
		OpenActivity(morganization_code);
	}

	/**
	 * 非自定义登录的url，显示结果对话框
	 * 
	 * @param result
	 */
	public void showMsgDialog(final String result) {
		ViewUtil.showCommonDialog(this, getString(R.string.scanning_result), result, getString(R.string.copy), new DialogCallBack() {
			@Override
			public void onConfirm() {
				CommonUtil.copyText(LoginActivity.this, result);
				showToast(getString(R.string.copy_to_clip));
			}
		});
	}

	/**
	 * @author 李巷阳
	 * @date 2016-9-8 下午3:29:05
	 */
	private void OpenActivity(String morganization_code) {
		// 如果为null 或者为 ""时候 进入机构界面
		if (!StringUtil.StrisEmpty(morganization_code)) {
			OpenPageUtil.openMechanismActivity(LoginActivity.this, Constant.isForLoginActivity);
		} else {
			OpenPageUtil.openMyMeetingActivity(LoginActivity.this, morganization_code);
		}
	}

	/**
	 * @author 李巷阳
	 * @date 2016-10-18 下午6:28:23
	 */
	@Override
	public String getPassWord() {
		return met_password.getText().toString().trim();
	}

	/**
	 * @author 李巷阳
	 * @date 2016-10-18 下午6:01:33
	 */
	@Override
	public String getUserName() {
		return met_username.getText().toString().trim();
	}

	/**
	 * @author 李巷阳
	 * @date 2016-10-18 下午6:01:33
	 */
	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		// 注册
		case R.id.tv_register:
			OpenPageUtil.openActivity(LoginActivity.this, RegisterActivity.class);
			break;
		// 找回密码
		case R.id.tv_forget_password:
			String met_username = getUserName();
			Bundle bundle = new Bundle();
			if (StringUtil.isMobileNO(met_username)) {
				bundle.putString("phone_number", met_username);
			}
			openActivity(RetrievepasswordActivity.class, bundle);
			break;
		// 扫码登录
		case R.id.tv_scan_code:
			SPUtils.remove(DRMUtil.KEY_LOGIN_TOKEN);
			OpenPageUtil.openZXingCode(LoginActivity.this);
			UIHelper.startInAnim(LoginActivity.this);
			break;
		// 历史记录
		case R.id.tv_history_record:
			OpenPageUtil.openActivity(LoginActivity.this, ScanHistoryActivity.class);
			break;
		// 登陆
		case R.id.btn_login:
			login_presenter.loging(getApplicationContext(), getUserName(), getPassWord());
			break;
		default:
			break;
		}

	}
	/**
	 * 二维码扫描回调
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
			if (resultCode == LoginActivity.RESULT_OK && data != null) {
				Bundle bundle = data.getExtras();
				String DataSource = bundle.getString(Constant.DECODED_CONTENT_KEY);
				login_presenter.saveScanResult(getApplication(),DataSource);
			}
		}
			break;
		}
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
	/**
	 * 跳转第三方登陆activity
	 * 
	 * @author 李巷阳
	 * @date 2016-10-19 下午2:43:58
	 */
	@Override
	public void openScanLoginVerificationActivity(ScanHistory sh) {
		OpenPageUtil.openScanLoginVerificationActivity(this, sh);
	}

	/**
	 * 跳转主界面
	 * @author 李巷阳
	 * @date 2016-10-19 下午2:46:28
	 */
	@Override
	public void openDownloadMainByScaning2(ScanHistory sh) {
		OpenPageUtil.openDownloadMainByScaning2(this, sh);
	}
}
