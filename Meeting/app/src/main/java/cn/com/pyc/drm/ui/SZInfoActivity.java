package cn.com.pyc.drm.ui;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.com.meeting.drm.R;
import cn.com.pyc.drm.utils.UIHelper;
import cn.com.pyc.drm.utils.manager.ActicityManager;
import cn.com.pyc.drm.widget.HighlightImageView;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

public class SZInfoActivity extends BaseActivity implements OnClickListener
{

	@ViewInject(R.id.back_img)
	private HighlightImageView back;

	@ViewInject(R.id.title_tv)
	private TextView title_tv;

	@ViewInject(R.id.wv)
	private WebView myWebView;
	
	@ViewInject(R.id.Ll_root_layout)
	private LinearLayout Ll_root_layout;

	private final String DEFAULT_URL = "http://kaihui.suizhi.com/appabout.html";

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sz_about2);
		UIHelper.showTintStatusBar(this);
		ActicityManager.getInstance().add(this);
		ViewUtils.inject(this);
		init_View();
		startWebView();
	}

	private void startWebView()
	{
		myWebView.loadUrl(DEFAULT_URL);
		setwebchromeclients();

		myWebView.setWebViewClient(new WebViewClient()
		{
			public boolean shouldOverrideUrlLoading(WebView view, String url)
			{
				 //view.loadUrl(url);
			     // Otherwise allow the OS to handle things like tel, mailto, etc.  
	            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));  
	            startActivity( intent ); 
				return true;
			}
		});
		WebSettings webSettings = myWebView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webSettings.setSupportZoom(true);
		webSettings.setUseWideViewPort(true);
		webSettings.setLoadWithOverviewMode(true);
		myWebView.setSaveEnabled(false);
		webSettings.setBuiltInZoomControls(false);
		webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);

	};

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
				// title_tv.setText(title);
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
			public boolean onJsConfirm(WebView view, String url, String message,
					final JsResult result)
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

		title_tv.setText("关于我们");
	}

	@Override
	protected void load_Data()
	{
	}

	@Override
	protected void init_Value()
	{
		showBgLoading("正在加载.");
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
		case R.id.back_img:
			UIHelper.finishActivity(SZInfoActivity.this);
			// finish();
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

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		Ll_root_layout.removeView(myWebView);
		myWebView.removeAllViews();
		myWebView.destroy();
	}

}
