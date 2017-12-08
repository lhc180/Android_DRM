package cn.com.pyc.drm.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class CustomWebView extends WebView
{

	public CustomWebView(Context context)
	{
		super(context);
		init();
	}
	public CustomWebView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init();
	}
	public CustomWebView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		init();
	}
	public CustomWebView(Context context, AttributeSet attrs, int defStyle, boolean privateBrowsing)
	{
		super(context, attrs, defStyle, privateBrowsing);
		init();
	}
	
	@SuppressLint("SetJavaScriptEnabled")
	private void init()
	{
		WebSettings webSettings = getSettings();
		webSettings.setJavaScriptEnabled(true);
		webSettings.setSupportZoom(true);
		webSettings.setUseWideViewPort(true);
		webSettings.setLoadWithOverviewMode(true);
		webSettings.setBuiltInZoomControls(false);
		webSettings.setDomStorageEnabled(true); 
		setSaveEnabled(false);
	}

}
