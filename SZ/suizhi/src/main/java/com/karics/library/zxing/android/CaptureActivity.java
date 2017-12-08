package com.karics.library.zxing.android;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import cn.com.pyc.drm.R;
import cn.com.pyc.drm.widget.HighlightImageView;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;
import com.karics.library.zxing.camera.CameraManager;
import com.karics.library.zxing.view.ViewfinderView;

/**
 * 这个activity打开相机，在后台线程做常规的扫描；它绘制了一个结果view来帮助正确地显示条形码，在扫描的时候显示反馈信息，
 * 然后在扫描成功的时候覆盖扫描结果
 * 
 */
public final class CaptureActivity extends Activity implements SurfaceHolder.Callback
{

	private static final String TAG = CaptureActivity.class.getSimpleName();
	// 扫一扫
	public static final int REQUEST_CODE_SCAN = 0x0000;

	public static final String DECODED_CONTENT_KEY = "codedContent";
	private static final String DECODED_BITMAP_KEY = "codedBitmap";

	// 相机控制
	private CameraManager cameraManager;
	private CaptureActivityHandler handler;
	private ViewfinderView viewfinderView;
	private boolean hasSurface;
	private IntentSource source;
	private Collection<BarcodeFormat> decodeFormats;
	private Map<DecodeHintType, ?> decodeHints;
	private String characterSet;
	// 电量控制
	private InactivityTimer inactivityTimer;
	// 声音、震动控制
	private BeepManager beepManager;

	private HighlightImageView imageButton_back;
	private ImageView mFlashImg;
	int sign = 0;

	public ViewfinderView getViewfinderView()
	{
		return viewfinderView;
	}

	public Handler getHandler()
	{
		return handler;
	}

	public CameraManager getCameraManager()
	{
		return cameraManager;
	}

	public void drawViewfinder()
	{
		viewfinderView.drawViewfinder();
	}

	/**
	 * OnCreate中初始化一些辅助类，如InactivityTimer（休眠）、Beep（声音）以及AmbientLight（闪光灯）
	 */
	@Override
	public void onCreate(Bundle icicle)
	{
		super.onCreate(icicle);
		// 保持Activity处于唤醒状态
		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_bar_code_capture);

		hasSurface = false;

		inactivityTimer = new InactivityTimer(this);
		beepManager = new BeepManager(this);

		imageButton_back = (HighlightImageView) findViewById(R.id.im_scanner_back);
		imageButton_back.setOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				finish();
			}
		});

		mFlashImg = (ImageView) findViewById(R.id.im_scanner_flash);
		mFlashImg.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				if (sign % 2 == 0)
				{
					mFlashImg.setSelected(true);
					if (cameraManager != null)
						cameraManager.openFlashLight();
				} else
				{
					mFlashImg.setSelected(false);
					if (cameraManager != null)
						cameraManager.closeFlashLight();
				}
				sign++;
			}
		});
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		// CameraManager必须在这里初始化，而不是在onCreate()中。
		// 这是必须的，因为当我们第一次进入时需要显示帮助页，我们并不想打开Camera,测量屏幕大小
		// 当扫描框的尺寸不正确时会出现bug
		cameraManager = new CameraManager(getApplication());

		viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
		viewfinderView.setCameraManager(cameraManager);

		handler = null;

		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		if (hasSurface)
		{
			// activity在paused时但不会stopped,因此surface仍旧存在；
			// surfaceCreated()不会调用，因此在这里初始化camera
			initCamera(surfaceHolder);
		} else
		{
			// 重置callback，等待surfaceCreated()来初始化camera
			surfaceHolder.addCallback(this);
		}

		beepManager.updatePrefs();
		inactivityTimer.onResume();

		source = IntentSource.NONE;
		decodeFormats = null;
		characterSet = null;
	}

	@Override
	protected void onPause()
	{
		if (handler != null)
		{
			handler.quitSynchronously();
			handler = null;
		}
		inactivityTimer.onPause();
		beepManager.close();
		cameraManager.closeDriver();
		if (!hasSurface)
		{
			SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
			SurfaceHolder surfaceHolder = surfaceView.getHolder();
			surfaceHolder.removeCallback(this);
		}
		super.onPause();
	}

	@Override
	protected void onDestroy()
	{
		inactivityTimer.shutdown();
		viewfinderView.recyle();
		super.onDestroy();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder)
	{
		if (!hasSurface)
		{
			hasSurface = true;
			initCamera(holder);
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder)
	{
		hasSurface = false;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
	{

	}

	/**
	 * 扫描成功，处理反馈信息
	 * 
	 * @param rawResult
	 * @param barcode
	 * @param scaleFactor
	 */
	public void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor)
	{
		inactivityTimer.onActivity();

		boolean fromLiveScan = barcode != null;
		// 这里处理解码完成后的结果，此处将参数回传到Activity处理
		if (rawResult != null)
		{
			beepManager.playBeepSoundAndVibrate();
			// Toast.makeText(this, "扫描成功", Toast.LENGTH_SHORT).show();
			Intent intent = getIntent();
			intent.putExtra(DECODED_CONTENT_KEY, rawResult.getText());
			// intent.putExtra(DECODED_BITMAP_KEY, barcode);
			setResult(RESULT_OK, intent);
			finish();
		}

	}

	/**
	 * 初始化Camera
	 * 
	 * @param surfaceHolder
	 */
	private void initCamera(SurfaceHolder surfaceHolder)
	{
		if (surfaceHolder == null)
		{
			throw new IllegalStateException("No SurfaceHolder provided");
		}
		if (cameraManager.isOpen())
		{
			return;
		}
		try
		{
			// 打开Camera硬件设备
			cameraManager.openDriver(surfaceHolder);
			// 创建一个handler来打开预览，并抛出一个运行时异常
			if (handler == null)
			{
				handler = new CaptureActivityHandler(this, decodeFormats, decodeHints, characterSet, cameraManager);
			}
		} catch (IOException ioe)
		{
			Log.w(TAG, ioe);
			displayFrameworkBugMessageAndExit();
		} catch (RuntimeException e)
		{
			Log.w(TAG, "Unexpected error initializing camera", e);
			displayFrameworkBugMessageAndExit();
		}
	}

	/**
	 * 显示底层错误信息并退出应用
	 */
	@SuppressLint("NewApi")
	private void displayFrameworkBugMessageAndExit()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_Holo_Dialog);
		builder.setTitle(getString(R.string.app_name));
		builder.setMessage(getString(R.string.msg_camera_framework_bug));
		builder.setPositiveButton(R.string.button_ok, new FinishListener(this));
		builder.setOnCancelListener(new FinishListener(this));
		builder.show();
	}

	@Override
	public void onBackPressed()
	{
		this.finish();
	}

}
