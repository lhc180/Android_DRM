package cn.com.pyc.drm.pay;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.sdk.app.PayTask;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.xutils.common.Callback;

import java.util.LinkedHashMap;
import java.util.Map;

import cn.com.pyc.drm.common.Constant;
import cn.com.pyc.drm.common.DrmPat;
import cn.com.pyc.drm.common.SZConfig;
import cn.com.pyc.drm.model.AliPayModel;
import cn.com.pyc.drm.model.BaseModel;
import cn.com.pyc.drm.model.PayModel;
import cn.com.pyc.drm.pay.ali.PayResult;
import cn.com.pyc.drm.ui.BrowserActivity;
import cn.com.pyc.drm.utils.APIUtil;
import cn.com.pyc.drm.utils.CommonUtil;
import cn.com.pyc.drm.utils.DRMUtil;
import cn.com.pyc.drm.utils.SPUtils;
import cn.com.pyc.drm.utils.SecurityUtil;
import cn.com.pyc.drm.utils.manager.HttpEngine;
import cn.com.pyc.drm.widget.ProgressWebView;
import cn.com.pyc.loger.LogerEngine;

import static cn.com.pyc.drm.utils.help.DownloadHelp.getMapParamsString;


/**
 * Created by songyumei on 2017/10/30.
 */

public class PayUtil {
    public static final int SDK_PAY_FLAG = 1;
    private  ProgressWebView mWebView;
    private static Context mContext;
    private  IWXAPI api;

    private AliPayModel aliPayModel;
    private static PayModel payModel;
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @SuppressWarnings("unused")
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SDK_PAY_FLAG: {
                    PayResult payResult = new PayResult((Map<String, String>) msg.obj);
                    /**
                     对于支付结果，请商户依赖服务端的异步通知结果。同步通知结果，仅作为支付结束的通知。
                     */
                    String resultStatus = payResult.getResultStatus();
                    LinkedHashMap<String, String> aliParam = getAliParam(resultStatus);
                    String s = JSON.toJSONString(aliParam);
                    mWebView.loadUrl("javascript:payFinish_Android("+s+")");
                    payCallBack(aliParam);
                    break;
                }
                default:
                    break;
            }
        }
    };

    public PayUtil(Context context, ProgressWebView mWebView) {
        this.mWebView = mWebView;
        this.mContext = context;
        api = WXAPIFactory.createWXAPI(context, SZConfig.WEIXIN_APPID,false);
        api.registerApp(SZConfig.WEIXIN_APPID);
    }

    public void pay(String[] array) {
        String substring = array[1].substring(1, array[1].lastIndexOf("'"));
        if (TextUtils.equals(array[0],"AliPay")) {
            aliPayModel = JSON.parseObject(substring, AliPayModel.class);
            payV2(aliPayModel);//支付宝支付
        }else if (TextUtils.equals(array[0],"WXPay")){
            payWeixin(substring);//微信支付
        }
    }
    /**
     * 支付宝支付业务
     */
    private void payV2(final AliPayModel aliPayModel) {
        if (!checkAliPayInstalled()) {
            LinkedHashMap<String, String> aliParam = getAliParam("-4");
            String s = JSON.toJSONString(aliParam);
            mWebView.loadUrl("javascript:payFinish_Android("+s+")");
            payCallBack(aliParam);
        }
        Runnable payRunnable = new Runnable() {

            @Override
            public void run() {//调用支付宝的SDK的支付功能
                PayTask alipay = new PayTask((Activity) mContext);
                Map<String, String> result = alipay.payV2(aliPayModel.getOrderString(), true);
                Message msg = new Message();
                msg.what = SDK_PAY_FLAG;
                msg.obj = result;
                mHandler.sendMessage(msg);//支付宝返回支付的结果通过handler同步到主线程
            }
        };

        Thread payThread = new Thread(payRunnable);
        payThread.start();
    }

    /*支付宝接口回调参数*/
    private LinkedHashMap<String, String> getAliParam(String resultStatus) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put("application_name", DrmPat.APP_FULLNAME);
        map.put("app_version", CommonUtil.getAppVersionName(mContext));
        map.put("IMEI", Constant.IMEI);
        map.put("username", Constant.getName());
        map.put("token", Constant.getToken());
        map.put("payType", "AliPay");

        String errMsg = "";
        // 判断resultStatus 为9000则代表支付成功
        if (TextUtils.equals(resultStatus,"9000")) {
            resultStatus = "0";
            errMsg = "支付成功";
        }else if (TextUtils.equals(resultStatus, "8000")){
            errMsg = "正在处理中，支付结果未知（有可能已经支付成功），请查询商户订单列表中订单的支付状态";
        }else if (TextUtils.equals(resultStatus, "4000")){
            errMsg = "订单支付失败";
        }else if (TextUtils.equals(resultStatus, "5000")){
            errMsg = "重复请求";
        }else if (TextUtils.equals(resultStatus, "6001")){
            errMsg = "用户中途取消";
        }else if (TextUtils.equals(resultStatus, "6002")){
            errMsg = "网络连接出错";
        }else if (TextUtils.equals(resultStatus, "6004")){
            errMsg = "支付结果未知（有可能已经支付成功），请查询商户订单列表中订单的支付状态";
        }else {
            errMsg = "其它支付错误";
        }
        map.put("code", resultStatus);
        map.put("msg", errMsg);
        map.put("mybill_id", aliPayModel.getMybill_id());
        map.put("accountId", (String) SPUtils.get(DRMUtil.KEY_VISIT_ACCOUNTID,""));
        map.put("bill_num", aliPayModel.getBill_num());
        map.put("pay_money", aliPayModel.getPay_money());
        return map;
    }

    /*支付宝是否安装*/
    private  boolean checkAliPayInstalled() {
        Uri uri = Uri.parse("alipays://platformapi/startApp");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        ComponentName componentName = intent.resolveActivity(mContext.getPackageManager());
        return componentName != null;
    }
    private void payWeixin(String substring) {
        payModel = JSON.parseObject(substring, PayModel.class);
        if (api == null) return;
        if (!api.isWXAppInstalled()) {
            Toast.makeText(mContext, "没有安装微信客户端", Toast.LENGTH_SHORT).show();
            LinkedHashMap<String, String> wxParam = getWXParam("-4");
            String s = JSON.toJSONString(wxParam);
            mWebView.loadUrl("javascript:payFinish_Android("+s+")");
            payCallBack(wxParam);
            return;
        }
        if (!api.registerApp(SZConfig.WEIXIN_APPID)) {
            Toast.makeText(mContext, "应用注册到微信失败", Toast.LENGTH_SHORT).show();
            return;
        }
        
        payWX(payModel);
    }

    public static LinkedHashMap<String, String> getWXParam(String code) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put("application_name", DrmPat.APP_FULLNAME);
        map.put("app_version", CommonUtil.getAppVersionName(mContext));
        map.put("IMEI", Constant.IMEI);
        map.put("username", Constant.getName());
        map.put("token", Constant.getToken());
        map.put("payType", "WXPay");

        String errMsg = "";
        // 判断resultStatus 为9000则代表支付成功
        if (TextUtils.equals(code,"-4")) {
            errMsg = "未安装微信";
        }else if (TextUtils.equals(code, "0")) {
            errMsg = "支付成功";
        }else if (TextUtils.equals(code, "-1")){
            errMsg = "失败";
        }else if (TextUtils.equals(code, "-2")){
            errMsg = "用户退出支付";
        }else{
            code = "-3";
            errMsg = "未知错误";
        }
        map.put("code", code);
        map.put("msg", errMsg);
        map.put("mybill_id", payModel.getMybill_id());
        map.put("accountId", (String) SPUtils.get(DRMUtil.KEY_VISIT_ACCOUNTID,""));
        map.put("bill_num", payModel.getBill_num());
        map.put("pay_money", payModel.getPay_money());
        return map;
    }

    /**
     * 微信支付
     * @param payInfo
     */
    private  void payWX(PayModel payInfo) {
        PayReq req = new PayReq();
        req.appId = SZConfig.WEIXIN_APPID;  // 测试用appId
        req.partnerId = "1336886201";//商户id
        req.prepayId= payInfo.getPrepayid();//订单号
        req.packageValue = "Sign=WXPay";
        req.nonceStr= payInfo.getNoncestr();
        req.timeStamp= payInfo.getTimestamp();
        req.sign= payInfo.getSign();
        // 在支付之前，如果应用没有注册到微信，应该先调用IWXMsg.registerApp将应用注册到微信
        api.sendReq(req);
    }

    public static void payCallBack(LinkedHashMap<String, String> map) {
        HttpEngine.post(APIUtil.getPayCallBackUrl(), map, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                BaseModel o = JSON.parseObject(result, BaseModel.class);
                String res = o.getMsg();
                if (TextUtils.isEmpty(o.getMsg())) {
                    res = getErrorMsg(o.getCode());
                }
                LogerEngine.error(mContext,res,null);
            }
            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
            }
            @Override
            public void onCancelled(CancelledException cex) {
            }
            @Override
            public void onFinished() {
            }
        });
    }

    private static String getErrorMsg(String code) {
        String res = "";
        switch (code) {
            case "1000":
                res = "true,表示正常";
                break;
            case "13001":
                res = "参数传递错误";
                break;
            case "13002":
                res = "充值单号传递错误";
                break;
            case "13003":
                res = "输入用户名不是手机号";
                break;
            case "14001":
                res = "token验证失败";
                break;
            case "14002":
                res = "authKey验证错误";
                break;
            case "15001":
                res = "用户名不存在";
                break;
            case "15002":
                res = "密码错误";
                break;
            case "15003":
                res = "级联获取myProdcut错误";
                break;
            case "15004":
                res = "证书数据不存在";
                break;
            case "15005":
                res = "店铺不存在";
                break;
            case "15006":
                res = "获取短码失败";
                break;
            case "15007":
                res = "获取长码失败（长码不存在）";
                break;
            case "15008":
                res = "获取文件列表失败";
                break;
            case "15009":
                res = "验证码错误";
                break;
            case "16001":
                res = "注册设备数超出";
                break;
            case "16002":
                res = "APP版本号太低";
                break;
            case "16003":
                res = "没有权限打开文件";
                break;
            case "16004":
                res = "已购买，但没到开始使用时间";
                break;
            case "16005":
                res = "文件已过期";
                break;
            case "16006":
                res = "设备未注册";
                break;
            case "16007":
                res = "文件格式不支持";
                break;
            case "16008":
                res = "产品更新失败";
                break;
            case "16009":
                res = "文件更新失败";
                break;
            case "16010":
                res = "文件更新失败";
                break;
            case "16011":
                res = "请求文件服务器发生错误";
                break;
            case "16012":
                res = "文件已被更新，请重新下载";
                break;
            case "16013":
                res = "没有权限观看文件（插件在线观看）";
                break;
            case "16014":
                res = "卖家禁止此文件在此端使用";
                break;
            case "16015":
                res = "注册失败";
                break;
            case "16016":
                res = "找回密码失败";
                break;
            case "16017":
                res = "用户不存在";
                break;
            case "16018":
                res = "用户不存在";
                break;
            case "16019":
                res = "验证码发送频繁，请稍后重试";
                break;
            case "16020":
                res = "支付失败";
                break;
            case "16021":
                res = "记录阅读日志失败";
                break;
            case "16022":
                res = "更新阅读日志失败";
                break;
            case "19001":
                res = "系统未知异常";
                break;
            case "19002":
                res = "IO异常";
                break;
            case "19003":
                res = "数据库异常";
                break;
        }
        return res;
    }
}
