package cn.com.pyc.drm.service;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import cn.com.meeting.drm.R;
import cn.com.pyc.drm.ui.MusicHomeActivity;
import cn.com.pyc.drm.utils.CommonUtil;
import cn.com.pyc.drm.utils.DRMLog;
import cn.com.pyc.drm.utils.DRMUtil;
import cn.com.pyc.drm.utils.DeviceUtils;

/**
 * 悬浮图标服务,主要应用在音乐播放时候
 * 
 * @author hudq
 * 
 */
public class FloatViewService extends Service
{

	private static final String TAG = "FloatViewService";
	private Context mContext;
	private WindowManager mWindowManager = null;
	private WindowManager.LayoutParams wmParams = null;
	private FrameLayout mView;
	private int mTouchStartX;
	private int mTouchStartY;
	private int mInScreenX;
	private int mInScreenY;
	private ImageView img;

	private long endMillis;
	private long startMillis;

	/**
	 * 开启悬浮图标
	 * 
	 * @param context
	 * @param hasAnimation
	 *            是否有动画,true有动画，false没有动画;默认没有动画
	 */
	public static final void openFloatView(Context context, boolean hasAnimation)
	{
		if (CommonUtil.isTopActivy(context, MusicHomeActivity.class.getName()))
		{
			// 在音乐主界面，不开启悬浮窗
			return;
		}
		Intent service = new Intent(context, FloatViewService.class);
		service.putExtra("hasAnimation", hasAnimation);
		context.startService(service);
	}

	/**
	 * 关闭悬浮图标
	 * 
	 * @param context
	 */
	public static final void closeFloatView(Context context)
	{
		Intent service = new Intent(context, FloatViewService.class);
		context.stopService(service);
	}

	// ///////////////////////////////////
	// ///////////////////////////////////
	// ///////////////////////////////////

	@Override
	public void onCreate()
	{
		super.onCreate();
		DRMLog.e(TAG, "onCreate()");
		mContext = this;
		createView();
		// 注册监听HOME键的广播
		registerReceiver(mHomeKeyEventReceiver, new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
	}

	private BroadcastReceiver mHomeKeyEventReceiver = new BroadcastReceiver()
	{
		String SYSTEM_REASON = "reason";
		String SYSTEM_HOME_KEY = "homekey";
		String SYSTEM_HOME_KEY_LONG = "recentapps";
		@Override
		public void onReceive(Context context, Intent intent)
		{
			String action = intent.getAction();
			if (action != null && action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))
			{
				String reason = intent.getStringExtra(SYSTEM_REASON);
				if (TextUtils.equals(reason, SYSTEM_HOME_KEY))
				{
					// 表示按了home键,程序到了后台
					closeFloatView(context);
				} else if (TextUtils.equals(reason, SYSTEM_HOME_KEY_LONG))
				{
					// 表示长按home键,显示最近使用的程序列表
				}
			}
		}
	};

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		if (intent == null)
			return super.onStartCommand(intent, flags, startId);
		DRMLog.e(TAG, "onStartCommand()");
		// 默认没有旋转动画
		boolean hasAnim = intent.getBooleanExtra("hasAnimation", false);
		if (hasAnim)
		{
			if (img.getAnimation() == null)
				s_play(img, true);
		} else
		{
			s_play(img, false);
		}
		return super.onStartCommand(intent, flags, startId);
	}

	private void createView()
	{
		mView = new FrameLayout(getApplicationContext());
		img = new ImageView(getApplicationContext());
		img.setScaleType(ScaleType.CENTER_CROP);
		img.setImageResource(R.drawable.ic_music_image);
		mView.addView(img);

		// 获取WindowManager
		mWindowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);

		final int mStatusHeight = CommonUtil.getStatusHeight(this);
		Point outSize = new Point();
		mWindowManager.getDefaultDisplay().getSize(outSize);
		int screenHeight = outSize.y;

		// 设置LayoutParams(全局变量）相关参数
		wmParams = new WindowManager.LayoutParams();

		if (DeviceUtils.hasKitkat())
		{
			// 使用TYPE_TOAST这个不需要权限的type时, 悬浮窗正常显示, 但低版本不能接受触摸事件.
			wmParams.type = WindowManager.LayoutParams.TYPE_TOAST;
		} else
		{
			// 那么优先级比TYPE_SYSTEM_ALERT会降低一些, 即拉下通知栏不可见
			wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;
		}

		// 设置图片格式，效果为背景透明
		wmParams.format = PixelFormat.RGBA_8888;

		wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

		// 以屏幕左上角为原点，设置x、y初始值
		wmParams.gravity = Gravity.LEFT | Gravity.TOP; // 调整悬浮窗口至左上角
		wmParams.x = 0;
		wmParams.y = screenHeight / 2;
		// 设置悬浮窗口长宽数据
		wmParams.width = CommonUtil.dip2px(this, 46);
		wmParams.height = CommonUtil.dip2px(this, 46);

		mWindowManager.addView(mView, wmParams);

		mView.setOnTouchListener(new OnTouchListener()
		{

			 @SuppressLint("ClickableViewAccessibility") @Override
			public boolean onTouch(View v, MotionEvent event)
			{

				// 获取相对屏幕的坐标，即以屏幕左上角为原点
				mInScreenX = (int) event.getRawX();
				mInScreenY = (int) event.getRawY() - mStatusHeight;
				DRMLog.i("mInScreenX= " + mInScreenX + "，yInScreen= " + mInScreenY);

				switch (event.getAction())
				{
				case MotionEvent.ACTION_DOWN:
				{
					startMillis = System.currentTimeMillis();
					// 获取相对View的坐标，即以此View左上角为原点
					mTouchStartX = (int) event.getX();
					mTouchStartY = (int) event.getY();
					DRMLog.d("mTouchStartX= " + mTouchStartX + "，mTouchStartY= " + mTouchStartY);
				}
					break;
				case MotionEvent.ACTION_MOVE:
				{
					// 更新浮动窗口位置参数
					wmParams.x = mInScreenX - mTouchStartX;
					wmParams.y = mInScreenY - mTouchStartY;
					mWindowManager.updateViewLayout(mView, wmParams);
				}
					break;
				case MotionEvent.ACTION_UP:
				{
					mTouchStartX = 0;
					mTouchStartY = 0;
					endMillis = System.currentTimeMillis();
					if ((endMillis - startMillis) < 200)
					{
						if (DRMUtil.Music_Album_Playing != null)
						{
							Bundle bundle = new Bundle();
							bundle.putSerializable("Album", DRMUtil.Music_Album_Playing);
							Intent intent = new Intent(mContext, MusicHomeActivity.class);
							intent.putExtras(bundle);
							if (MusicHomeActivity.sMusicActivity != null)
							{
								mContext = MusicHomeActivity.sMusicActivity;
								mContext.startActivity(intent);
								((Activity) mContext).overridePendingTransition(R.anim.activity_music_open, R.anim.fade_out_scale);
								return true;
							}
							mContext.startActivity(intent);
						}
					}
				}
					break;
				}
				return true;
			}
		});
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		DRMLog.d(TAG + " onDestroy()");
		s_play(img, false);
		mWindowManager.removeView(mView);
		unregisterReceiver(mHomeKeyEventReceiver);
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	/**
	 * 设置转动图标
	 * 
	 * @param mImg
	 * @param isAnimation
	 *            是否动画
	 */
	private final void s_play(final View mImg, boolean isAnimation)
	{
		if (mImg == null)
			return;
		if (isAnimation)
		{
			Animation an = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
			an.setInterpolator(new LinearInterpolator());// 不停顿
			an.setRepeatCount(Animation.INFINITE);// 重复次数
			an.setFillEnabled(true);
			an.setFillBefore(false);
			an.setFillAfter(true);// 停在最后
			an.setDuration(2500);
			mImg.startAnimation(an);
		} else
		{
			mImg.clearAnimation();
		}
	}

}
