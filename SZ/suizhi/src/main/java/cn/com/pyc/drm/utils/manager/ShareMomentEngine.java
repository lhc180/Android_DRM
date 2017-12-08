package cn.com.pyc.drm.utils.manager;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.alibaba.fastjson.JSON;

import org.xutils.common.Callback;

import cn.com.pyc.drm.R;
import cn.com.pyc.drm.common.Constant;
import cn.com.pyc.drm.common.DrmPat;
import cn.com.pyc.drm.model.CodeModel;
import cn.com.pyc.drm.ui.BaseActivity;
import cn.com.pyc.drm.ui.BaseFragmentActivity;
import cn.com.pyc.drm.utils.APIUtil;
import cn.com.pyc.drm.utils.ClipboardUtil;
import cn.com.pyc.drm.utils.DRMLog;
import cn.com.pyc.drm.utils.DRMUtil;
import cn.com.pyc.drm.utils.SPUtils;
import cn.com.pyc.drm.utils.ViewUtil;

/**
 * 分享此刻管理控制类
 * <p>
 * Created by hudq on 2017/4/7.
 */

public class ShareMomentEngine {

    public static final String SHARE_PREFIX = "SZS";
    /* 以下字段不可混淆**/
    private String proId;                   //产品id
    private String myProId;                 //购买产品id
    private String itemId;                  //当前文件id
    private String sharePosition;           //分享此刻位置
    private String shareUser;               //分享人
    private String category;                //专辑类别（VIDEO，BOOK，MUSIC）
    /* 以上字段不可混淆**/

    //当前选择点击进入的专辑的proId
    public static boolean setSelectProId(String proId) {
        return SPUtils.save(DRMUtil.KEY_SELECT_PRO_ID, proId);
    }

    //获取选择点击进入的专辑proId
    public static String getSelectProId() {
        return (String) SPUtils.get(DRMUtil.KEY_SELECT_PRO_ID, "");
    }

    // 分享此刻的位置
    public static boolean setSharePosition(String key, String value) {
        return SPUtils.save("share_" + key, value);
    }

    //获取分享此刻的位置，并清除
    public static String getSharePosition(String key) {
        String sharePosition = (String) SPUtils.get("share_" + key, "");
        Log.w("drm", "sharePosition[" + key + "]: " + sharePosition);
        SPUtils.remove("share_" + key);
        return sharePosition;
    }

    //清除所有记录的分享此刻的位置
    public static void clearSharePosition() {
        SPUtils.remove("share_" + DrmPat.VIDEO);
        SPUtils.remove("share_" + DrmPat.MUSIC);
        SPUtils.remove("share_" + DrmPat.BOOK);
        Log.w("drm", "remove share pos");
    }

    /**
     * 生成分享码
     *
     * @param activity
     */
    public void work(final Activity activity) {
        showLoading(activity);
        Bundle bundle = new Bundle();
        bundle.putString("username", Constant.getName());
        bundle.putString("token", Constant.getToken());
        bundle.putString("longParameter", JSON.toJSONString(this));
        HttpEngine.post(APIUtil.getShortCodeUrl(), bundle, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String s) {
                CodeModel cm = JSON.parseObject(s, CodeModel.class);
                if (cm != null && cm.isYes(cm.getCode())) {
                    String shortCode = cm.getData();
                    ClipboardUtil.copyText(activity, activity.getString(R.string
                            .share_moment_content, SHARE_PREFIX + shortCode));
                    ViewUtil.showSingleCommonDialog(activity, "", activity.getString(R.string
                            .share_moment_code_copy), activity.getString(R.string.i_known), null);
                }
            }

            @Override
            public void onError(Throwable throwable, boolean b) {
                DRMLog.d(throwable.getMessage());
                showFail(activity);
            }

            @Override
            public void onCancelled(CancelledException e) {

            }

            @Override
            public void onFinished() {
                hideLoading(activity);
            }
        });
    }

    private ShareMomentEngine(Intern intern) {
        this.proId = intern.proId;
        this.myProId = intern.myProId;
        this.itemId = intern.itemId;
        this.shareUser = Constant.getName();
        this.sharePosition = intern.sharePosition;
        this.category = intern.category;
    }


    private void showLoading(Activity activity) {
        if (activity instanceof BaseActivity) {
            ((BaseActivity) activity).showBgLoading("正在生成分享码");
        } else if (activity instanceof BaseFragmentActivity) {
            ((BaseFragmentActivity) activity).showBgLoading("正在生成分享码");
        }
    }

    private void hideLoading(Activity activity) {
        if (activity instanceof BaseActivity) {
            ((BaseActivity) activity).hideBgLoading();
        } else if (activity instanceof BaseFragmentActivity) {
            ((BaseFragmentActivity) activity).hideBgLoading();
        }
    }

    private void showFail(Activity activity) {
        if (activity instanceof BaseActivity) {
            ((BaseActivity) activity).showToast("生成分享码失败，请稍后再试");
        } else if (activity instanceof BaseFragmentActivity) {
            ((BaseFragmentActivity) activity).showToast("生成分享码失败，请稍后再试");
        }
    }


    public static class Intern {
        private String proId;
        private String myProId;
        private String itemId;
        private String sharePosition;
        private String category;

        public Intern setProId(String proId) {
            this.proId = proId;
            return this;
        }

        public Intern setMyProId(String myProId) {
            this.myProId = myProId;
            return this;
        }

        public Intern setItemId(String itemId) {
            this.itemId = itemId;
            return this;
        }

        public Intern setSharePosition(String sharePosition) {
            this.sharePosition = sharePosition;
            return this;
        }

        public Intern setCategory(String category) {
            this.category = category;
            return this;
        }

        public ShareMomentEngine launch() {
            return new ShareMomentEngine(this);
        }
    }

    public String getProId() {
        return proId;
    }

    public void setProId(String proId) {
        this.proId = proId;
    }

    public String getMyProId() {
        return myProId;
    }

    public void setMyProId(String myProId) {
        this.myProId = myProId;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getSharePosition() {
        return sharePosition;
    }

    public void setSharePosition(String sharePosition) {
        this.sharePosition = sharePosition;
    }

    public String getShareUser() {
        return shareUser;
    }

    public void setShareUser(String shareUser) {
        this.shareUser = shareUser;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCategory() {
        return category;
    }
}
