package cn.com.pyc.drm.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

import cn.com.meeting.drm.R;
import cn.com.pyc.drm.dialog.NormalDialog;
import cn.com.pyc.drm.dialog.NormalDialog.NormalDialogCallBack;
import cn.com.pyc.drm.utils.UIHelper;
import cn.com.pyc.drm.utils.manager.ActicityManager;
import cn.com.pyc.drm.widget.HighlightImageView;

@SuppressLint("SetJavaScriptEnabled")
public class VoteActivity extends BaseActivity implements OnClickListener {

	@ViewInject(R.id.back_img)
	private HighlightImageView back;

	@ViewInject(R.id.title_tv)
	private TextView title_tv;

	@ViewInject(R.id.Ll_root_layout)
	private LinearLayout Ll_root_layout;

	@ViewInject(R.id.wv)
	private WebView myWebView;

	private String url;

	private String titlename;

	private AlertDialog clearDlg;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.retailers_activity);
		init_Value();
		init();
		startWebView();
	}

	private void startWebView() {
			myWebView.loadUrl(url);

		// 设置WebChromeClient
		myWebView.setWebChromeClient(new WebChromeClient() {

			@Override
			public void onReceivedTitle(WebView view, String title) {
				title_tv.setText(title);
				super.onReceivedTitle(view, title);
			}

			@Override
			public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
				// 构架一个builder来显示网页中的对话框
				Builder builder = new Builder(view.getContext());
				builder.setTitle("提示");
				builder.setMessage(message);
				builder.setPositiveButton("确定", new AlertDialog.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						result.confirm();
					}
				});
				builder.setCancelable(false);
				builder.create();
				builder.show();
				return true;
			}

			@Override
			public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
				// 构架一个builder来显示网页中的对话框
				Builder builder = new Builder(view.getContext());
				builder.setTitle("提示");
				builder.setMessage(message);
				builder.setNegativeButton("确定", new AlertDialog.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						result.confirm();
					}
				});
				builder.setPositiveButton("取消", new AlertDialog.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						result.cancel();
					}
				});
				builder.setCancelable(false);
				builder.create();
				builder.show();
				return true;
			}

		});
		myWebView.setWebViewClient(new WebViewClient() {
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				myWebView.setVisibility(View.VISIBLE);
				hideBgLoading();
				super.onPageFinished(view, url);
			}
		});

		WebSettings webSettings = myWebView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webSettings.setBuiltInZoomControls(false);
		webSettings.setSupportZoom(true);
		webSettings.setUseWideViewPort(true);
		webSettings.setLoadWithOverviewMode(true);

	};

	@Override
	protected void init_Value() {
		// TODO Auto-generated method stub

		UIHelper.showTintStatusBar(this);
		ActicityManager.getInstance().add(this);
		showBgLoading("正在加载.");
		url = getIntent().getStringExtra("webview_url");
		titlename = getIntent().getStringExtra("vote_title");
		ViewUtils.inject(this);
	}

	private void init() {
		back.setVisibility(View.VISIBLE);
		title_tv.setVisibility(View.VISIBLE);

		back.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back_img:
			clearDlg = new NormalDialog().dialog(VoteActivity.this, "返回可能导致未保存的数据丢失。", "确定返回吗？", new NormalDialogCallBack() {
				public void getConfirm(View v) {
					UIHelper.finishActivity(VoteActivity.this);
				}

				public void getCancel(View v) {

				}
			});

			break;

		default:
			break;
		}
	}

	private void clearDlag() {
		if (clearDlg != null)
			clearDlg.dismiss();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK) && myWebView.canGoBack()) {
			myWebView.goBack();
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		clearDlag();
		Ll_root_layout.removeView(myWebView);
		myWebView.removeAllViews();
		myWebView.destroy();
	}

	@Override
	protected void init_View() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void load_Data() {
		// TODO Auto-generated method stub

	}

}
