package cn.com.pyc.drm.ui;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.com.meeting.drm.R;
import cn.com.pyc.drm.bean.ReturnDataBean;
import cn.com.pyc.drm.utils.CommonUtil;
import cn.com.pyc.drm.utils.OpenPageUtil;
import cn.com.pyc.drm.utils.StringUtil;
import cn.com.pyc.drm.utils.UIHelper;
import cn.com.pyc.drm.utils.manager.ActicityManager;
import cn.com.pyc.drm.utils.manager.RequestHttpManager;
import cn.com.pyc.drm.widget.ClearEditText;
import cn.com.pyc.drm.widget.FlatButton;
import cn.com.pyc.drm.widget.HighlightImageView;

import com.alibaba.fastjson.JSON;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

/**
 * @Description: (注册)
 * @author 李巷阳
 * @date 2016-8-15 下午4:23:01
 * @version V1.0
 */
public class RegisterActivity extends BaseActivity implements OnClickListener {

	@ViewInject(R.id.title_tv)
	private TextView mtv_title_tv;

	@ViewInject(R.id.et_phone_number)
	private ClearEditText met_phone_number;

	@ViewInject(R.id.et_verification_code)
	private ClearEditText met_verification_code;

	@ViewInject(R.id.btn_verification_code)
	private FlatButton mbtn_verification_code;

	@ViewInject(R.id.btn_real_name)
	private ClearEditText mbtn_real_name;

	@ViewInject(R.id.btn_set_password)
	private ClearEditText mbtn_set_password;

	@ViewInject(R.id.btn_regist)
	private FlatButton mbtn_regist;

	@ViewInject(R.id.back_img)
	private HighlightImageView mback_img;

	@ViewInject(R.id.al_tv_notice)
	private TextView mal_tv_notice;

	private String phoneNumber;
	private MyCount mc;

	/**
	 * @Description: (用一句话描述该文件做什么)
	 * @author 李巷阳
	 * @date 2016-8-15 下午4:24:10
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		init_Value();
		init_View();
		initListener();
		load_Data();
	}

	/**
	 * @Description: (用一句话描述该文件做什么)
	 * @author 李巷阳
	 * @date 2016-8-15 下午4:39:44
	 */
	private void initListener() {
		// TODO Auto-generated method stub
		mbtn_verification_code.setOnClickListener(this);
		mbtn_regist.setOnClickListener(this);
		mback_img.setOnClickListener(this);
	}

	/**
	 * @Description: (用一句话描述该文件做什么)
	 * @author 李巷阳
	 * @date 2016-8-15 下午4:24:02
	 */
	@Override
	protected void init_View() {
		// TODO Auto-generated method stub
		mtv_title_tv.setText("注册");
		if (!TextUtils.isEmpty(phoneNumber)) {
			met_phone_number.setText(phoneNumber);
			met_verification_code.requestFocus();
		}
	}

	/**
	 * @Description: (用一句话描述该文件做什么)
	 * @author 李巷阳
	 * @date 2016-8-15 下午4:24:02
	 */
	@Override
	protected void load_Data() {
		// TODO Auto-generated method stub

	}

	private void showNotice(String text) {
		if (mal_tv_notice.getVisibility() == View.INVISIBLE)
			mal_tv_notice.setVisibility(View.VISIBLE);
		mal_tv_notice.setText(text);
	}

	private void hideNotice() {
		if (mal_tv_notice.getVisibility() == View.VISIBLE)
			mal_tv_notice.setVisibility(View.INVISIBLE);
		mal_tv_notice.setText(null);
	}

	/**
	 * @Description: (用一句话描述该文件做什么)
	 * @author 李巷阳
	 * @date 2016-8-15 下午4:24:02
	 */
	@Override
	protected void init_Value() {
		ViewUtils.inject(this);
		ActicityManager.getInstance().add(this);
		// 自定义状态栏
		UIHelper.showTintStatusBar(this);
		phoneNumber = getIntent().getStringExtra("phone_number");
	}

	/**
	 * @Description: (用一句话描述该文件做什么)
	 * @author 李巷阳
	 * @date 2016-8-16 下午3:56:34
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		/**
		 * 获取验证码
		 */
		case R.id.btn_verification_code:
			// 获取手机验证码
			getVerfyCode();
			break;
		case R.id.btn_regist:
			// 注册
			toRegister();
			break;
		case R.id.back_img:
			finish();
			break;
		default:
			break;
		}
	}

	private void toRegister() {
		hideNotice();
		if (!CommonUtil.isNetConnect(RegisterActivity.this)) {
			UIHelper.showToast(getApplicationContext(), "网络不给力！");
			return;
		}
		// 手机号
		final String phonenumber = getPhoneNo();
		// 手机验证码
		String secutrity = met_verification_code.getText().toString().trim();
		// 设置真实姓名
		String real_name = mbtn_real_name.getText().toString().trim();
		// 确认密码
		String psd_set = mbtn_set_password.getText().toString().trim();

		if (!StringUtil.isMobileNO(phonenumber)) {
			UIHelper.showToast(getApplicationContext(), "请您输入正确的手机号");
			mal_tv_notice.setVisibility(View.VISIBLE);
			mal_tv_notice.setText("请您输入正确的手机号");
			return;
		}
		if (TextUtils.isEmpty(secutrity)) {
			UIHelper.showToast(getApplicationContext(), "手机验证码不能为空");
			mal_tv_notice.setVisibility(View.VISIBLE);
			mal_tv_notice.setText("手机验证码不能为空");
			return;
		}
		if (TextUtils.isEmpty(real_name)) {
			UIHelper.showToast(getApplicationContext(), "真实姓名不能为空");
			mal_tv_notice.setVisibility(View.VISIBLE);
			mal_tv_notice.setText("真实姓名不能为空");

			return;
		}
		if (TextUtils.isEmpty(psd_set)) {
			UIHelper.showToast(getApplicationContext(), "密码不能为空");
			mal_tv_notice.setVisibility(View.VISIBLE);
			mal_tv_notice.setText("密码不能为空");
			return;
		}
		if (psd_set.length() < 6) {
			UIHelper.showToast(getApplicationContext(), "\"设置密码\"须6位或以上任意字符。");
			mal_tv_notice.setVisibility(View.VISIBLE);
			mal_tv_notice.setText("\"设置密码\"须6位或以上任意字符。");
			return;
		}

		showBgLoading("正在注册");
		new AsyncTask<String, String, String>() {
			protected String doInBackground(String... params) {
				String return_str = RequestHttpManager.getRegisterChecking(params[0], params[1], params[2], params[3]);
				return return_str;
			}

			@Override
			protected void onPostExecute(String result) {
				if (result != null) {
					ReturnDataBean bm = JSON.parseObject(result, ReturnDataBean.class);
					setRegisterReturnCode(bm);
				} else {
					UIHelper.showToast(getApplicationContext(), "注册失败");
				}
				hideBgLoading();
			}
		}.execute(phonenumber, real_name, psd_set, secutrity);

	}

	/**
	 * @Description: (用一句话描述该文件做什么)
	 * @author 李巷阳
	 * @date 2016-8-16 下午5:56:34
	 */
	protected void setRegisterReturnCode(ReturnDataBean bm) {

		if (bm == null)
			return;
		if ("true".equals(bm.getResult())) {
			
			AlertDialog.Builder builder = new Builder(this);
			View view = View.inflate(getApplicationContext(), R.layout.dialog_user, null);
			
			TextView tv_info=(TextView) view.findViewById(R.id.Cancellation);//提示信息
			LinearLayout confirm = (LinearLayout) view.findViewById(R.id.confirm);// 确定
			LinearLayout cancel = (LinearLayout) view.findViewById(R.id.cancel);// 取消
			TextView tv_btn0 = (TextView) view.findViewById(R.id.exitBtn0);
			tv_btn0.setText("去登录");
			tv_info.setText("注册成功,点击进入登陆界面?");
			builder.setView(view);
			final AlertDialog dialog = builder.create();
			dialog.show();
			confirm.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Bundle bundle=new Bundle();
					bundle.putString("phone_number", getPhoneNo());
					openActivity(LoginActivity.class,bundle);
					finish();
					if (dialog != null) {
						dialog.dismiss();
					}
				}
			});
			cancel.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (dialog != null)
						dialog.dismiss();
				}
			});
			
		} else{
			mal_tv_notice.setText(bm.getMsg());
			mal_tv_notice.setVisibility(View.VISIBLE);
		}
		UIHelper.showToast(getApplicationContext(),bm.getMsg());
		hideBgLoading();
	}

	public String getPhoneNo() {
		return met_phone_number.getText().toString().trim();
	}

	/**
	 * @Description: (用一句话描述该文件做什么)
	 * @author 李巷阳
	 * @date 2016-8-16 下午4:00:57
	 */
	private void getVerfyCode() {
		hideNotice();
		String phonenumber = getPhoneNo();
		if (!CommonUtil.isNetConnect(RegisterActivity.this)) {
			UIHelper.showToast(getApplicationContext(), "网络不给力！");
			return;
		}
		if (!StringUtil.isMobileNO(phonenumber)) {
			UIHelper.showToast(getApplicationContext(), "请输入正确的手机号");
			return;
		}
		// mbtn_verification_code
		// .setBackgroundDrawable(getResources().getDrawable(R.drawable.imb_white1));
		mbtn_verification_code.setEnabled(false);
		mbtn_verification_code.setTextColor(getResources().getColor(R.color.black));
		mc = new MyCount(60000, 1000);
		mc.start();
		// 获取手机验证码的时候，设置手机号不可编辑
		met_phone_number.setEnabled(false);
		met_phone_number.setTextColor(getResources().getColor(R.color.gray_stroke));

		new AsyncTask<String, String, String>() {
			protected String doInBackground(String... params) {
				String return_str = RequestHttpManager.getGetPhoneCode(params[0]);
				return return_str;
			}

			@Override
			protected void onPostExecute(String result) {
				if (result != null) {
					ReturnDataBean vcb = JSON.parseObject(result, ReturnDataBean.class);
					if ("true".equals(vcb.getResult())) {
						UIHelper.showToast(getApplicationContext(), vcb.getMsg());
						return ;
					} else{
						UIHelper.showToast(getApplicationContext(), vcb.getMsg());
					}
				} else {
					UIHelper.showToast(getApplicationContext(), "获取短信验证码失败");
				}
				setMyCountTimerCancel();
			}
		}.execute(phonenumber);

	}

	/* 定义一个倒计时的内部类 */
	class MyCount extends CountDownTimer {
		public MyCount(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}

		@Override
		public void onFinish() {
			setMyCountTimerCancel();
		}

		@Override
		public void onTick(long millisUntilFinished) {
			mbtn_verification_code.setText("重新获取 (" + millisUntilFinished / 1000 + ")");
		}
	}

	private void setMyCountTimerCancel() {
		mbtn_verification_code.setEnabled(true);
		// 设置手机号可以编辑setFocusable
		met_phone_number.setEnabled(true);
		met_phone_number.setTextColor(getResources().getColor(R.color.black));

		mbtn_verification_code.setTextColor(getResources().getColor(R.color.white));
		mbtn_verification_code.setText("获取验证码");
		if (mc != null) {
			mc.cancel();
			mc = null;
		}
	}

}
