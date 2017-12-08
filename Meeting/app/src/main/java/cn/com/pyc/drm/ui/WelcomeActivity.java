package cn.com.pyc.drm.ui;

import android.graphics.PixelFormat;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.TextView;
import cn.com.meeting.drm.R;
import cn.com.pyc.drm.common.App;
import cn.com.pyc.drm.common.Constant;
import cn.com.pyc.drm.utils.CommonUtil;
import cn.com.pyc.drm.utils.DRMDBHelper;
import cn.com.pyc.drm.utils.DRMUtil;
import cn.com.pyc.drm.utils.FileUtils;
import cn.com.pyc.drm.utils.SPUtils;

public class WelcomeActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);
		if (getWindow() != null) {
			getWindow().setFormat(PixelFormat.RGBA_8888);
		}
		init_Value();
		init_View();
		load_Data();
	}

	@Override
	protected void init_Value() {
		FileUtils.checkSDCardCrashLog();//检查缓存文件
	}

	@Override
	protected void init_View() {
		View view = findViewById(R.id.welcome_layout);
		TextView tvVersion = (TextView) findViewById(R.id.text_version);
		tvVersion.setText(Constant.version+CommonUtil.getAppVersionName(this));
		startAnimation(view);//开启动画
	}

	private void startAnimation(View view) {
		AlphaAnimation animation = new AlphaAnimation(0.5f, 1.0f);
		animation.setDuration(2600);
		animation.setFillEnabled(true);
		animation.setFillAfter(true);
		view.startAnimation(animation);
		animation.setAnimationListener(new AnimationListener() {
			public void onAnimationEnd(Animation arg0) {
				String LoginPhone = (String) SPUtils.get(DRMUtil.KEY_PHONE_NUMBER, "");
				Bundle bundle = new Bundle();
				bundle.putString(Constant.isForActivity, Constant.isForWelcomeActivity);
				bundle.putString(DRMUtil.KEY_PHONE_NUMBER, LoginPhone);
				openActivity(LoginActivity.class, bundle);
				finish();
			}
			@Override
			public void onAnimationStart(Animation arg0) {
			}

			@Override
			public void onAnimationRepeat(Animation arg0) {
			}
		});
	}

	@Override
	protected void load_Data() {
		// /// 检查登录，如果升级版本，则检查Album表中的字段
		// 是否登录了
		if (TextUtils.isEmpty(Constant.getUserName()))
			return;
		// 获取保存的版本号
		String mSaveVersion = (String) SPUtils.get(DRMUtil.KEY_APP_VERSION, "");
		// 当前app版本号
		String mCurVersion = CommonUtil.getAppVersionName(App.getInstance());
		// 是否升级版本了
		if (TextUtils.equals(mSaveVersion, mCurVersion))
			return;
		// 创建数据库
		DRMDBHelper.setCreateDatabase(getApplicationContext());
	}
}
