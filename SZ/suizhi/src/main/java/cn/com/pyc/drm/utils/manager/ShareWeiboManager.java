package cn.com.pyc.drm.utils.manager;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.widget.Toast;

import com.sina.weibo.sdk.WbSdk;
import com.sina.weibo.sdk.api.ImageObject;
import com.sina.weibo.sdk.api.TextObject;
import com.sina.weibo.sdk.api.WebpageObject;
import com.sina.weibo.sdk.api.WeiboMultiMessage;
import com.sina.weibo.sdk.auth.AccessTokenKeeper;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WbConnectErrorMessage;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.share.WbShareCallback;
import com.sina.weibo.sdk.share.WbShareHandler;
import com.sina.weibo.sdk.utils.Utility;

import org.xutils.common.Callback;

import cn.com.pyc.drm.R;
import cn.com.pyc.drm.common.SZConfig;
import cn.com.pyc.drm.ui.BaseActivity;
import cn.com.pyc.drm.utils.ImageUtils;
import cn.com.pyc.drm.utils.help.ImageLoadHelp;

/**
 * sina 微博分享管理类
 * <p>
 * Created by hudaqiang on 2017/11/6.
 */
public class ShareWeiboManager {

    private BaseActivity activity;
    private SsoHandler ssoHandler;
    private WbShareHandler shareHandler;

    private ShareManager.ShareData shareData;
    private TYPE shareType = TYPE.WEBPAGE; //默认网页分享

    enum TYPE {
        TEXT, WEBPAGE
    }

    /*设置分享数据*/
    void setShareData(ShareManager.ShareData shareData) {
        this.shareData = shareData;
    }

    //设置分享类别，默认分享一个网页分享
    void setShareType(TYPE shareType) {
        this.shareType = shareType;
    }

    ShareWeiboManager(BaseActivity acty) {
        activity = acty;
        Context context = activity.getApplicationContext();
        WbSdk.install(context, new AuthInfo(context, SZConfig.WEIBO_APPID, SZConfig
                .WEIBO_REDIRECT_URL, SZConfig.WEIBO_SCOPE));
        ssoHandler = new SsoHandler(activity);
        shareHandler = new WbShareHandler(activity);
    }

    /**
     * SSO 授权回调
     * <p>
     * 重要：发起SSO登陆的Activity的onActivityResult()调用此onActivityResult方法
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (ssoHandler != null) {
            ssoHandler.authorizeCallBack(requestCode, resultCode, data);
        }
    }

    /**
     * 处理分享的回调，在发起分享的Activity的onNewIntent()调用此onNewIntent方法
     */
    public void onNewIntent(Intent intent) {
        if (shareHandler != null) {
            shareHandler.doResultIntent(intent, wbShareCallback);
        }
    }

    /**
     * 分享到微博
     */
    void shareWeibo() {
        if (hasAuth()) {
            share();
        } else {
            auth();
        }
    }


    private void share() {
        if (shareHandler == null) return;

        if (TextUtils.isEmpty(shareData.getSummary())) {
            Toast.makeText(activity, "分享的文本内容为空！", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!shareHandler.registerApp()) {
            Toast.makeText(activity, "应用注册到微博失败", Toast.LENGTH_SHORT).show();
            return;
        }
//        if (!shareHandler.supportMulti()) {
//            Toast.makeText(activity, "不支持分享到微博", Toast.LENGTH_SHORT).show();
//            return;
//        }
        activity.showBgLoading(activity.getString(R.string.please_waiting));
        ImageLoadHelp.loadDrawable(shareData.getImageUrl(), new Callback
                .CommonCallback<Drawable>() {

            @Override
            public void onSuccess(Drawable result) {
                Bitmap bitmap = ImageUtils.drawableToBitmap(result);
                if (shareType == TYPE.TEXT) {
                    shareText(bitmap);
                } else if (shareType == TYPE.WEBPAGE) {
                    shareWebPage(bitmap);
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                Bitmap defaultBitmap = BitmapFactory.decodeResource(activity.getResources(), R
                        .drawable.ic_launchers);
                if (shareType == TYPE.TEXT) {
                    shareText(defaultBitmap);
                } else if (shareType == TYPE.WEBPAGE) {
                    shareWebPage(defaultBitmap);
                }
            }

            @Override
            public void onCancelled(CancelledException cex) {
            }

            @Override
            public void onFinished() {
                activity.hideBgLoading();
            }
        });
    }

    private void shareText(Bitmap bitmap) {
        WeiboMultiMessage weiboMessage = new WeiboMultiMessage();

        TextObject textObject = new TextObject();
        textObject.text = shareData.getSummary()
                + "#" + shareData.getTitle() + "#" + shareData.getLink();
        textObject.title = shareData.getTitle();
        textObject.actionUrl = shareData.getLink();

        ImageObject imageObject = new ImageObject();
        imageObject.setImageObject(bitmap);

        weiboMessage.textObject = textObject;
        weiboMessage.imageObject = imageObject;
        shareHandler.shareMessage(weiboMessage, false);
    }

    private void shareWebPage(Bitmap bitmap) {
        final WeiboMultiMessage weiboMessage = new WeiboMultiMessage();

        WebpageObject mediaObject = new WebpageObject();
        mediaObject.identify = Utility.generateGUID();
        mediaObject.title = shareData.getTitle();
        mediaObject.description = shareData.getSummary();
        //设置缩略图。 注意：最终压缩过的缩略图大小不得超过 32kb。
        mediaObject.setThumbImage(bitmap);
        mediaObject.actionUrl = shareData.getLink();
        mediaObject.defaultText = shareData.getSummary();

        weiboMessage.mediaObject = mediaObject;
        shareHandler.shareMessage(weiboMessage, false);
    }

    /**
     * 授权
     */
    private void auth() {
        if (ssoHandler == null) return;
        ssoHandler.authorize(new SelfWbAuthListener());
    }


    private boolean hasAuth() {
        Oauth2AccessToken token = AccessTokenKeeper.readAccessToken(activity);
        return token != null && token.isSessionValid();
    }


    private class SelfWbAuthListener implements com.sina.weibo.sdk.auth.WbAuthListener {
        @Override
        public void onSuccess(Oauth2AccessToken oauth2AccessToken) {
            if (oauth2AccessToken != null && oauth2AccessToken.isSessionValid()) {
                AccessTokenKeeper.writeAccessToken(activity, oauth2AccessToken);
                //授权成功，开始分享
                share();
            }
        }

        @Override
        public void cancel() {

        }

        @Override
        public void onFailure(WbConnectErrorMessage errorMessage) {
            Toast.makeText(activity, "授权失败(" + errorMessage.getErrorMessage() + ")", Toast
                    .LENGTH_SHORT).show();
        }
    }

    private WbShareCallback wbShareCallback = new WbShareCallback() {
        @Override
        public void onWbShareSuccess() {
        }

        @Override
        public void onWbShareCancel() {
            //Toast.makeText(activity, "分享已取消！", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onWbShareFail() {
            Toast.makeText(activity, "分享失败！", Toast.LENGTH_SHORT).show();
        }
    };
}