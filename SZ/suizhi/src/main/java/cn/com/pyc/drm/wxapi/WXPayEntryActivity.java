package cn.com.pyc.drm.wxapi;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.tencent.mm.opensdk.utils.Log;

import java.util.LinkedHashMap;

import cn.com.pyc.drm.R;
import cn.com.pyc.drm.common.SZConfig;
import cn.com.pyc.drm.pay.PayUtil;
import cn.com.pyc.drm.ui.BrowserActivity;
import cn.com.pyc.drm.utils.SPUtils;
import cn.com.pyc.drm.widget.ProgressWebView;


public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler {
    private IWXAPI api;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    	api = WXAPIFactory.createWXAPI(this, SZConfig.WEIXIN_APPID);
        api.handleIntent(getIntent(), this);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
        api.handleIntent(intent, this);
	}

	@Override
	public void onReq(BaseReq req) {
	}

	@Override
	public void onResp(BaseResp resp) {
		//支付结果的回调,在这儿需要再次访问服务器统一支付结果
		if (resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
			LinkedHashMap<String, String> wxParam = PayUtil.getWXParam(resp.errCode+"");
			String s = JSON.toJSONString(wxParam);
			BrowserActivity.mWebView.loadUrl("javascript:payFinish_Android("+s+")");
			PayUtil.payCallBack(wxParam);
			finish();
		}
	}
}