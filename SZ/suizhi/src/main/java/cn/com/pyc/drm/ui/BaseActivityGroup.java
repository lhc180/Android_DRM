package cn.com.pyc.drm.ui;

import android.app.ActivityGroup;
import android.os.Bundle;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;

import org.xutils.common.Callback;

import cn.com.pyc.drm.R;
import cn.com.pyc.drm.common.Code;
import cn.com.pyc.drm.common.SZConfig;
import cn.com.pyc.drm.model.BaseModel;
import cn.com.pyc.drm.service.AppUpdateService;
import cn.com.pyc.drm.utils.APIUtil;
import cn.com.pyc.drm.utils.DRMLog;
import cn.com.pyc.drm.utils.ViewUtil;
import cn.com.pyc.drm.utils.manager.ActicityManager;
import cn.com.pyc.drm.utils.manager.HttpEngine;
import cn.com.pyc.drm.utils.manager.PromptMsgManager;

/**
 * 之前使用Activity,此处使用ActivityGroup作为容器
 * <p>
 * Created by hudaqiang on 2017/6/29.
 */
public class BaseActivityGroup extends ActivityGroup {

    protected static final int TAB_1 = 0;
    protected static final int TAB_2 = 1;
    protected static final int TAB_3 = 2;

    private static final String TAG = "baseAG";
    //private Callback.Cancelable mWxCancelable;
    private Callback.Cancelable mForceCancelable;
    private boolean isForceUpdate = false;

    protected void showToast(String text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }


    /**
     * 强制更新检测，验证App版本号
     */
    protected void forceUpdate() {
        if (isForceUpdate)
            return;
        isForceUpdate = true;
        Bundle params = new Bundle();
        //params.putString("application_name", DrmPat.APP_FULLNAME); //基础参数已添加
        //params.putString("app_version", CommonUtil.getAppVersionName(this));
        mForceCancelable = HttpEngine.post(APIUtil.getForceUpdateUrl(), params, new Callback
                .CommonCallback<String>() {
            @Override
            public void onSuccess(String s) {
                DRMLog.d("forceUpdate: " + s);
                //if (isStop) return;
                BaseModel model = JSON.parseObject(s, BaseModel.class);
                if (model != null && Code._16002.equals(model.getCode())) {
                    ViewUtil.DialogCallBackPat callBack = new ViewUtil.DialogCallBackPat() {
                        @Override
                        public void onCancel() {
                            android.os.Process.killProcess(android.os.Process.myPid());
                            System.exit(1);
                        }

                        @Override
                        public void onConfirm() {
                            ActicityManager.getInstance().remove(BaseActivityGroup.this);
                            BaseActivityGroup.this.finish();
                            AppUpdateService.openAppUpdateService(BaseActivityGroup.this, SZConfig
                                    .APK_NEW_URL, "-new");
                        }
                    };
                    PromptMsgManager proMsg = new PromptMsgManager.Builder()
                            .setPositiveText(getString(R.string.update_app_now))
                            .setNegativeText(getString(R.string.menu_exit))
                            .setDialogCallback(callBack)
                            .create();
                    proMsg.show(BaseActivityGroup.this, Code._16002);
                }
            }

            @Override
            public void onError(Throwable throwable, boolean b) {
                DRMLog.e("onError: " + throwable.getMessage());
            }

            @Override
            public void onCancelled(CancelledException e) {
            }

            @Override
            public void onFinished() {
                isForceUpdate = false;
            }
        });
    }

    /*
     * 检查qq登录access_token是否有效
     */
//    protected void checkQQAccess_token() {
//        long expires_in = (Long) SPUtils.get(DRMUtil.KEY_QQ_EXPIRES_IN, 0L);
//        long QQ_expires_in = (expires_in - System.currentTimeMillis()) / 1000;
//        DRMLog.d(TAG, "QQ_expires_in = " + QQ_expires_in);
//        if (QQ_expires_in < 0 || QQ_expires_in == 0) {
//            // 过期了(有效期3个月)，打开登录页
//            openLoginGuide();
//        } else {
//            SZConfig.LoginConfig.type = DrmPat.LOGIN_QQ;
//            PathUtil.createFilePath();
//            OpenPageUtil.openActivity(this, HomeActivity.class);
//            finish();
//        }
//    }

//    private void openLoginGuide() {
//        showToast(getString(R.string.login_lose_efficacy));
//        ClearKeyUtil.removeKey();
//        OpenPageUtil.openActivity(this, LoginActivity.class);
//        finish();
//    }
//
//    protected String getWeChatToken() {
//        return (String) SPUtils.get(DRMUtil.KEY_WECHAT_ACCESS_TOKEN, "");
//    }
//
//    protected String getQQToken() {
//        return (String) SPUtils.get(DRMUtil.KEY_WECHAT_ACCESS_TOKEN, "");
//    }

    /*
     * 检验授权微信凭证（access_token）是否有效
     */
//    protected void checkWeChatAccess_token() {
//        String openid = (String) SPUtils.get(DRMUtil.KEY_WECHAT_OPENID, "");
//        String path = "https://api.weixin.qq.com/sns/auth?access_token=" + getWeChatToken() +
//                "&openid=" + openid;
//        mWxCancelable = HttpEngine.get(path, new Callback.CommonCallback<String>() {
//
//            @Override
//            public void onCancelled(CancelledException arg0) {
//            }
//
//            @Override
//            public void onError(Throwable arg0, boolean arg1) {
//                showToast(getString(R.string.network_is_not_available));
//                openLoginGuide();
//            }
//
//            @Override
//            public void onFinished() {
//            }
//
//            @Override
//            public void onSuccess(String arg0) {
//                try {
//                    JSONObject json = new JSONObject(arg0);
//                    if (json.getInt("errcode") != 0) { // 登录失效，打开登录页
//                        openLoginGuide();
//                    } else {
//                        SZConfig.LoginConfig.type = DrmPat.LOGIN_WECHAT;
//                        PathUtil.createFilePath();
//                    }
//                } catch (Exception e) {
//                    openLoginGuide();
//                }
//            }
//        });
//    }

    /*
     * 普通登录校验
     */
//    protected void checkGeneralLogin() {
//        if (Util_.checkLogin()) {
//            SZConfig.LoginConfig.type = DrmPat.LOGIN_GENERAL;
//            PathUtil.createFilePath();
//        } else {
//            ClearKeyUtil.removeKey();
//        }
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //HttpEngine.cancelHttp(mWxCancelable);
        HttpEngine.cancelHttp(mForceCancelable);
    }
}
