package cn.com.pyc.drm.utils.manager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.tencent.connect.share.QQShare;
import com.tencent.connect.share.QzoneShare;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXTextObject;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import org.xutils.common.Callback;

import java.util.ArrayList;
import java.util.regex.PatternSyntaxException;

import cn.com.pyc.drm.R;
import cn.com.pyc.drm.common.SZConfig;
import cn.com.pyc.drm.pay.weixin.Util;
import cn.com.pyc.drm.ui.BaseActivity;
import cn.com.pyc.drm.utils.ClipboardUtil;
import cn.com.pyc.drm.utils.CommonUtil;
import cn.com.pyc.drm.utils.DRMLog;
import cn.com.pyc.drm.utils.ImageUtils;
import cn.com.pyc.drm.utils.StringUtil;
import cn.com.pyc.drm.utils.help.ImageLoadHelp;
import cn.com.pyc.drm.utils.help.UIHelper;

import static com.alipay.android.phone.mrpc.core.HttpManager.TAG;

/**
 * 第三方分享功能
 * Created by hudaqiang on 2017/10/26.
 */

public class ShareManager {

    private BaseActivity activity;
    private ShareData shareData;

    private IWXAPI wxAPI;
    private Tencent tencent;
    private ShareWeiboManager weiboManager; //新浪微博分享管理

    public ShareManager(BaseActivity acty) {
        activity = acty;
        Context context = activity.getApplicationContext();

        wxAPI = WXAPIFactory.createWXAPI(context, SZConfig.WEIXIN_APPID, true);
        tencent = Tencent.createInstance(SZConfig.QQ_APPID, context);
        weiboManager = new ShareWeiboManager(activity);
        weiboManager.setShareType(ShareWeiboManager.TYPE.TEXT);
    }

    /*设置分享数据*/
    public void setShareData(ShareData data) {
        shareData = data;
        weiboManager.setShareData(shareData);
    }

    // 通过返回的信息String，获取分享的数据信息：
    // Array:["链接的URL", "标题", "简介", "LOGO图片URL"],结果如下格式：
    public ShareData getShareData(String result) {
        if (result == null || result.length() < 2) return null;
        if (result.contains("[") && result.contains("]")) {
            result = result.substring(1, result.length() - 1);
        }
        try {
            if (result.contains("\"")) {
                result = result.replaceAll("\"", "");
            }
            String[] values = result.split(",");
            if (values.length != 4) return null;
            ShareData data = new ShareData();
            data.setLink(values[0]);
            data.setTitle(StringUtil.unicodeToChinese(values[1]));
            data.setSummary(StringUtil.unicodeToChinese(values[2]));
            data.setImageUrl(values[3]);
            DRMLog.d(TAG, "share data：" + data.toString());
            return data;
        } catch (PatternSyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    //获取微博分享的管理对象
    public ShareWeiboManager getWeiboManager() {
        return weiboManager;
    }

    /**
     * 显示分享对话框
     */
    public void showShareDialog() {
        View contentView = View.inflate(activity, R.layout.popup_share_layout, null);
        final PopupWindow popupWindow = new PopupWindow(contentView);
        popupWindow.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
        popupWindow.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        popupWindow.setFocusable(true);
        //为PopupWindow设置透明背景.
        popupWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
        popupWindow.setOutsideTouchable(false);
        //设置PopupWindow进入和退出动画
        popupWindow.setAnimationStyle(R.style.anim_popup_bottombar);
        UIHelper.setScreenAlpha(activity, 0.5f);
        //设置PopupWindow显示的位置
        popupWindow.showAtLocation(activity.getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                UIHelper.setScreenAlpha(activity, 1f);
            }
        });
        contentView.findViewById(R.id.share_qq_layout).setOnClickListener(new View
                .OnClickListener() {
            @Override
            public void onClick(View v) {
                shareQQ();
                popupWindow.dismiss();
            }
        });
        contentView.findViewById(R.id.share_wechat_layout).setOnClickListener(new View
                .OnClickListener() {
            @Override
            public void onClick(View v) {
                shareWeChat(true, false);
                popupWindow.dismiss();
            }
        });
        contentView.findViewById(R.id.share_link_layout).setOnClickListener(new View
                .OnClickListener() {
            @Override
            public void onClick(View v) {
                shareCopyLink();
                popupWindow.dismiss();
            }
        });
        contentView.findViewById(R.id.share_qzone_layout).setOnClickListener(new View
                .OnClickListener() {
            @Override
            public void onClick(View v) {
                shareQZone();
                popupWindow.dismiss();
            }
        });
        contentView.findViewById(R.id.share_wechat_friends_layout).setOnClickListener(new View
                .OnClickListener() {
            @Override
            public void onClick(View v) {
                shareWeChat(true, true);
                popupWindow.dismiss();
            }
        });
        contentView.findViewById(R.id.share_weibo_layout).setOnClickListener(new View
                .OnClickListener() {
            @Override
            public void onClick(View v) {
                weiboManager.shareWeibo();
                popupWindow.dismiss();
            }
        });
        contentView.findViewById(R.id.share_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
    }

    /**
     * 分享到qq
     */
    private void shareQQ() {
        if (tencent == null) return;

        if (!CommonUtil.isQQClientAvailable(activity)) {
            Toast.makeText(activity, "没有安装QQ客户端", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(shareData.getSummary())) {
            Toast.makeText(activity, "分享的文本内容为空！", Toast.LENGTH_SHORT).show();
            return;
        }

        Bundle params = new Bundle();
        params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);
        params.putString(QQShare.SHARE_TO_QQ_TITLE, shareData.getTitle());// 标题,最长30字符
        params.putString(QQShare.SHARE_TO_QQ_SUMMARY, shareData.getSummary());// 摘要，最长40个字符。
        params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, shareData.getLink());// 内容地址
        params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, shareData.getImageUrl());// 网络图片地址
        params.putString(QQShare.SHARE_TO_QQ_APP_NAME, activity.getResources().getString(R.string
                .app_name));// 应用名称
        //params.putString(QQShare.SHARE_TO_QQ_EXT_INT, "0");//其他附加功能
        // 分享操作要在主线程中完成
        tencent.shareToQQ(activity, params, mIUiListener);
    }

    /**
     * 分享到qzone
     */
    private void shareQZone() {
        if (tencent == null) return;

        if (TextUtils.isEmpty(shareData.getSummary())) {
            Toast.makeText(activity, "分享的文本内容为空！", Toast.LENGTH_SHORT).show();
            return;
        }

        Bundle params = new Bundle();
        params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, QzoneShare
                .SHARE_TO_QZONE_TYPE_IMAGE_TEXT);
        params.putString(QzoneShare.SHARE_TO_QQ_TITLE, shareData.getTitle());// 标题,最多200个字符。
        params.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, shareData.getSummary());// 摘要，最多600字符。
        params.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, shareData.getLink());// 内容地址
        ArrayList<String> imageUrls = new ArrayList<>();
        imageUrls.add(shareData.getImageUrl());
        params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, imageUrls);
        params.putString(QzoneShare.SHARE_TO_QQ_APP_NAME, activity.getResources().getString(R.string
                .app_name));// 应用名称
        // 分享操作要在主线程中完成
        tencent.shareToQzone(activity, params, mIUiListener);
    }


    /**
     * 分享到微信 <br/>
     * <p>
     * 发送到聊天界面——WXSceneSession
     * 发送到朋友圈——WXSceneTimeline
     * 添加到微信收藏——WXSceneFavorite
     *
     * @param link    是否分享的是链接
     * @param friends 是否分享到朋友圈
     */
    private boolean shareWeChat(boolean link, final boolean friends) {
        if (wxAPI == null) return false;

        if (!wxAPI.isWXAppInstalled()) {
            Toast.makeText(activity, "没有安装微信客户端", Toast.LENGTH_SHORT).show();
            //String url = "http://android.myapp.com/myapp/detail.htm?apkName=com.tencent.mm";
            //OpenPageUtil.openBrowserOfSystem(activity, url);
            return false;
        }
        if (!wxAPI.registerApp(SZConfig.WEIXIN_APPID)) {
            Toast.makeText(activity, "应用注册到微信失败", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (friends && wxAPI.getWXAppSupportAPI() < 0x21020001) {
            Toast.makeText(activity, "您的微信版本过低，不支持分享到朋友圈", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!link) {
            return shareTextMsg(shareData.getSummary(), friends);
        }
        //链接分享
        activity.showBgLoading(activity.getString(R.string.please_waiting));
        ImageLoadHelp.loadDrawable(shareData.getImageUrl(), new Callback.CommonCallback<Drawable>
                () {

            @Override
            public void onSuccess(Drawable result) {
                shareLinkMsg(shareData.getTitle(), shareData.getSummary(), shareData
                        .getLink(), ImageUtils.drawableToBitmap(result), friends);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                shareLinkMsg(shareData.getTitle(), shareData.getSummary(), shareData.getLink(),
                        null, friends);
            }

            @Override
            public void onCancelled(CancelledException cex) {
            }

            @Override
            public void onFinished() {
                activity.hideBgLoading();
            }
        });
        return true;
    }


    //分享文本信息
    private boolean shareTextMsg(String content, boolean friends) {
        DRMLog.d(TAG, "share text: " + content);
        if (TextUtils.isEmpty(content)) {
            Toast.makeText(activity, "分享的文本内容为空！", Toast.LENGTH_SHORT).show();
            return false;
        }

        // 初始化一个WXTextObject对象
        WXTextObject textObj = new WXTextObject();
        textObj.text = content;

        // 用WXTextObject对象初始化一个WXMediaMessage对象
        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = textObj;
        // 发送文本类型的消息时，title字段不起作用
        // msg.title = "Will be ignored";
        msg.description = content;

        // 构造一个Req
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("text"); // transaction字段用于唯一标识一个请求
        req.message = msg;
        req.scene = friends ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req
                .WXSceneSession;

        // 调用api接口发送数据到微信
        return wxAPI.sendReq(req);
    }

    //分享一个网页url
    private boolean shareLinkMsg(String title, String content, String url, Bitmap icon,
                                 boolean friends) {
        DRMLog.d(TAG, "share link: " + url);
        if (TextUtils.isEmpty(url)) {
            Toast.makeText(activity, "分享的网页链接为空！", Toast.LENGTH_SHORT).show();
            return false;
        }

        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = url;

        WXMediaMessage msg = new WXMediaMessage(webpage);
        //网页标题
        msg.title = (title == null) ? activity.getResources().getString(R.string.app_name) : title;
        //网页描述
        msg.description = content == null ? "" : content;
        // 处理图标
        if (icon == null || icon.isRecycled()) {
            icon = BitmapFactory.decodeResource(activity.getResources(), R.drawable.ic_launchers);
        }

        int width = icon.getWidth();
        int height = icon.getHeight();
        int max = Math.max(width, height);
        final int maxSize = CommonUtil.dip2px(activity, 52f);
        float scale = 1;
        if (max > maxSize) {
            scale = maxSize * 1f / width;
        }
        icon = Bitmap.createScaledBitmap(icon, (int) (width * scale), (int) (height * scale), true);
        msg.thumbData = Util.bmpToByteArray(icon, true); //分享图片大小不能大于32kb
        int imageSize = msg.thumbData.length / 1024;
        DRMLog.d("share image size: " + imageSize);
        if (imageSize > 32) {
            //Toast.makeText(activity, "您分享的图片过大", Toast.LENGTH_SHORT).show();
            icon = Bitmap.createScaledBitmap(icon, (int) (width * scale * 0.8),
                    (int) (height * scale * 0.8), true);
            msg.thumbData = Util.bmpToByteArray(icon, true);
        }
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("webpage");
        req.message = msg;
        req.scene = friends ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req
                .WXSceneSession;

        // 调用api接口发送数据到微信
        return wxAPI.sendReq(req);
    }

    private String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis())
                : type + System.currentTimeMillis();
    }

    /**
     * 复制链接
     */
    private void shareCopyLink() {
        if (TextUtils.isEmpty(shareData.getLink())) {
            Toast.makeText(activity, "复制的url链接为空！", Toast.LENGTH_SHORT).show();
            return;
        }
        ClipboardUtil.copyText(activity, shareData.getLink());
        Toast.makeText(activity, "链接已复制到剪贴板", Toast.LENGTH_SHORT).show();
    }


    /**
     * 分享的数据模型
     */
    public static class ShareData {
        private String title;       //标题
        private String summary;     //内容描述
        private String link;        //链接
        private String imageUrl;    //网络图片地址

        String getTitle() {
            return title;
        }

        void setTitle(String title) {
            this.title = title;
        }

        String getSummary() {
            return summary;
        }

        void setSummary(String summary) {
            this.summary = summary;
        }

        String getLink() {
            return link;
        }

        public void setLink(String link) {
            this.link = link;
        }

        String getImageUrl() {
            return imageUrl;
        }

        void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        @Override
        public String toString() {
            return "\ntitle='" + title + '\'' +
                    "\nsummary='" + summary + '\'' +
                    "\nlink='" + link + '\'' +
                    "\nimageUrl='" + imageUrl + '\'';
        }
    }


    public IUiListener mIUiListener = new IUiListener() {
        @Override
        public void onComplete(Object o) {
        }

        @Override
        public void onError(UiError uiError) {
            Toast.makeText(activity, "分享失败！", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCancel() {
            //Toast.makeText(activity, "分享已取消！", Toast.LENGTH_SHORT).show();
        }
    };

}
