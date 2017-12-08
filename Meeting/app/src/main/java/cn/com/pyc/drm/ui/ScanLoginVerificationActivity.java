package cn.com.pyc.drm.ui;

import cn.com.meeting.drm.R;
import cn.com.pyc.drm.bean.ScanHistory;
import cn.com.pyc.drm.bean.event.SettingActivityCloseEvent;
import cn.com.pyc.drm.utils.DRMUtil;
import cn.com.pyc.drm.utils.OpenPageUtil;
import cn.com.pyc.drm.utils.SPUtils;
import cn.com.pyc.drm.utils.UIHelper;
import cn.com.pyc.drm.utils.manager.ActicityManager;
import cn.com.pyc.drm.widget.HighlightImageView;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

import de.greenrobot.event.EventBus;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 验证身份
 * 
 */
public class ScanLoginVerificationActivity extends BaseActivity implements OnClickListener
{

	@ViewInject(R.id.back_img)
	private HighlightImageView back;

	@ViewInject(R.id.title_tv)
	private TextView title_tv;

	@ViewInject(R.id.wv)
	private WebView myWebView;

	@ViewInject(R.id.Ll_root_layout)
	private LinearLayout Ll_root_layout;
 
	
	private ScanHistory sh;


	
	final Handler myHandler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.retailers_activity);
		init_Value();
		init_View();
		startWebView();
	}

	@SuppressLint("SetJavaScriptEnabled")
	private void startWebView()
	{
		WebSettings webSettings = myWebView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webSettings.setSupportZoom(true);
		webSettings.setUseWideViewPort(true);
		webSettings.setLoadWithOverviewMode(true);
		myWebView.setSaveEnabled(false);
		webSettings.setBuiltInZoomControls(false);

		myWebView.loadUrl(sh.getVerify_url());
		setwebchromeclients();

		myWebView.addJavascriptInterface(new JavaScriptStartDownloadActivity(this),
				"StartDownloadActivity");

		myWebView.setWebViewClient(new WebViewClient()
		{
			public boolean shouldOverrideUrlLoading(WebView view, String url)
			{
				view.loadUrl(url);
				return true;
			}
		});
	};

	public class JavaScriptStartDownloadActivity
	{
		Context mContext;

		JavaScriptStartDownloadActivity(Context c)
		{
			mContext = c;
		}

		@JavascriptInterface
		public void LandingSuccess(final String mobile,final String url)
		{
			myHandler.post(new Runnable()
			{
				public void run()
				{
					SPUtils.save(DRMUtil.KEY_PHONE_NUMBER,mobile);
					SPUtils.save(DRMUtil.VOTE_URL, url);
					Log.e("url_url", url);
//					Toast.makeText(getApplicationContext(), "登录成功！", Toast.LENGTH_SHORT).show();
//					ScanLoginVerificationActivity.this.setResult(Activity.RESULT_OK, null);
					OpenPageUtil.openDownloadMainByScaning2(ScanLoginVerificationActivity.this, sh);
					EventBus.getDefault().post(new SettingActivityCloseEvent());
					finish();
				}
			});
		}
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
		case R.id.back_img:
			UIHelper.finishActivity(ScanLoginVerificationActivity.this);
			break;

		default:
			break;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if ((keyCode == KeyEvent.KEYCODE_BACK) && myWebView.canGoBack())
		{
			myWebView.goBack();
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	private void setwebchromeclients()
	{
		myWebView.setWebChromeClient(new WebChromeClient()
		{

			public void onProgressChanged(WebView view, int newProgress)
			{
				if (newProgress == 100)
				{
					myWebView.setVisibility(View.VISIBLE);
					hideBgLoading();
				}
				super.onProgressChanged(view, newProgress);
			}

			@Override
			public void onReceivedTitle(WebView view, String title)
			{
				title_tv.setText(title);
				super.onReceivedTitle(view, title);
			}

			@Override
			public boolean onJsAlert(WebView view, String url, String message, final JsResult result)
			{
				// 构架一个builder来显示网页中的对话框
				Builder builder = new Builder(view.getContext());
				builder.setTitle("提示");
				builder.setMessage(message);
				builder.setPositiveButton("确定", new AlertDialog.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface arg0, int arg1)
					{
						result.confirm();
					}
				});
				builder.setCancelable(false);
				builder.create();
				builder.show();
				return true;
			}

			@Override
			public boolean onJsConfirm(WebView view, String url, String message,final JsResult result)
			{
				// 构架一个builder来显示网页中的对话框
				Builder builder = new Builder(view.getContext());
				builder.setTitle("提示");
				builder.setMessage(message);
				builder.setNegativeButton("确定", new AlertDialog.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface arg0, int arg1)
					{
						result.confirm();
					}
				});
				builder.setPositiveButton("取消", new AlertDialog.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface arg0, int arg1)
					{
						result.cancel();
					}
				});
				builder.setCancelable(false);
				builder.create();
				builder.show();
				return true;
			}
		});
	}

	@Override
	protected void init_View()
	{
		back.setVisibility(View.VISIBLE);
		title_tv.setVisibility(View.VISIBLE);
		back.setOnClickListener(this);
	}

	@Override
	protected void load_Data()
	{
	}

	protected void init_Value()
	{
		sh = (ScanHistory) getIntent().getSerializableExtra("MeetingDetails");
		showBgLoading("正在加载.");
		UIHelper.showTintStatusBar(this);
		ActicityManager.getInstance().add(this);
		ViewUtils.inject(this);
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		myHandler.removeCallbacksAndMessages(null);
		Ll_root_layout.removeView(myWebView);
		myWebView.removeAllViews();
		myWebView.destroy();

	}



}
