package cn.com.pyc.drm.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;

import org.json.JSONObject;
import org.xutils.common.Callback;

import cn.com.pyc.drm.R;
import cn.com.pyc.drm.common.Code;
import cn.com.pyc.drm.common.Constant;
import cn.com.pyc.drm.common.DrmPat;
import cn.com.pyc.drm.model.CodeModel;
import cn.com.pyc.drm.model.ProductInfo;
import cn.com.pyc.drm.model.VerifyItemModel;
import cn.com.pyc.drm.model.db.bean.AlbumContent;
import cn.com.pyc.drm.model.db.practice.AlbumContentDAOImpl;
import cn.com.pyc.drm.utils.APIUtil;
import cn.com.pyc.drm.utils.ClipboardUtil;
import cn.com.pyc.drm.utils.CommonUtil;
import cn.com.pyc.drm.utils.DRMLog;
import cn.com.pyc.drm.utils.OpenPageUtil;
import cn.com.pyc.drm.utils.ViewUtil;
import cn.com.pyc.drm.utils.help.MusicHelp;
import cn.com.pyc.drm.utils.manager.HttpEngine;
import cn.com.pyc.drm.utils.manager.PromptMsgManager;
import cn.com.pyc.drm.utils.manager.ShareMomentEngine;
import cn.jpush.android.api.JPushInterface;

/**
 * Activity基类，包含检测剪贴板分享
 *
 * @author hudq
 */
public abstract class BaseActivity extends Activity {

    private Dialog loadingBgDlg;
    private Dialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkDevice();
    }

    private void checkDevice() {
        if (!CommonUtil.isSdCardCanUsed()) {
            showToastLong(getString(R.string.disable_sd_card_not_download));
        }
    }

    /**
     * 初始化控件
     */
    protected abstract void initView();

    /**
     * 加载数据
     */
    protected abstract void loadData();

    /**
     * 获取传递参数
     */
    protected abstract void getValue();

    public void showToast(int resId) {
        showToast(getString(resId));
    }

    public void showToast(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    public void showToastLong(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }

    /**
     * 通过类名启动Activity
     *
     * @param clz
     */
    public void openActivity(Class<?> clz) {
        openActivity(clz, null);
    }

    /**
     * 通过类名启动Activity，并且含有Bundle数据
     *
     * @param clz
     * @param bundle 传递的bundle数据
     */
    public void openActivity(Class<?> clz, Bundle bundle) {
        Intent intent = new Intent(this, clz);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        startActivity(intent);
    }

    /**
     * 显示加载loading框,带背景
     */
    public void showBgLoading(String msg) {
        if (loadingBgDlg == null) {
            loadingBgDlg = new LoadingBgDialog(this, msg);
            loadingBgDlg.setCancelable(true); // true设置返回取消
            loadingBgDlg.setCanceledOnTouchOutside(false);
        }
        if (!loadingBgDlg.isShowing()) {
            loadingBgDlg.show();
        }
    }

    /**
     * 隐藏加载loading，带背景
     */
    public void hideBgLoading() {
        if (loadingBgDlg != null) {
            loadingBgDlg.dismiss();
            loadingBgDlg = null;
        }
    }

    /**
     * loading对话框，带背景
     *
     * @author qd
     */
    class LoadingBgDialog extends Dialog {

        private String content;

        private LoadingBgDialog(Context context, String content) {
            super(context, R.style.LoadBgDialog);
            this.content = content;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            this.setContentView(R.layout.dialog_bgloading);
            TextView tvContent = (TextView) this.findViewById(R.id.tv_dialog);
            if (!TextUtils.isEmpty(content)) {
                tvContent.setText(content);
            } else {
                tvContent.setText(getString(R.string.load_ing));
            }
        }
    }

    /**
     * 显示加载进度框，不带背景
     */
    public void showLoading() {
        if (loadingDialog == null) {
            loadingDialog = new LoadingDialog(this);
            loadingDialog.setCancelable(true); // true设置返回取消
            loadingDialog.setCanceledOnTouchOutside(false);
        }
        if (!loadingDialog.isShowing()) {
            loadingDialog.show();
        }
    }

    /**
     * 隐藏加载进度框，不带背景
     */
    public void hideLoading() {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
            loadingDialog = null;
        }
    }

    /**
     * loading 加载框，不带背景的
     *
     * @author hudq
     */
    class LoadingDialog extends Dialog {

        private LoadingDialog(Context context) {
            super(context, R.style.LoadDialog);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            this.setContentView(R.layout.dialog_loading);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        JPushInterface.onPause(this);
        HttpEngine.cancelHttp(longCodeCancelable);
        HttpEngine.cancelHttp(validCancelable);
        hasEyeClipboard = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        JPushInterface.onResume(this);
        MusicHelp.showFloatView(this);
        checkShare();
    }


    /**
     * 监控剪贴板内容，检测分享
     */
    private boolean hasEyeClipboard = false;
    private Callback.Cancelable longCodeCancelable;
    private Callback.Cancelable validCancelable;

    private void checkShare() {
        if (!CommonUtil.isNetConnect(this))
            return;
        String clipContent = ClipboardUtil.eyesClipboard(this);
        if (TextUtils.isEmpty(clipContent))   //剪贴板空
            return;
        if (clipContent.length() <= ShareMomentEngine.SHARE_PREFIX.length())
            return;
        if (!clipContent.contains(ShareMomentEngine.SHARE_PREFIX))
            return;
        //我分享了一个时刻，请已购买者复制本消息后，打开绥知APP浏览***SZywRQ3j***
        int start = clipContent.indexOf(ShareMomentEngine.SHARE_PREFIX);
        int end = clipContent.length();
        if (clipContent.endsWith("***")) {
            end = clipContent.length() - 3;
        }
        int start_ = start + ShareMomentEngine.SHARE_PREFIX.length();
        if (start == -1 || start_ > end)
            return;
        String shortCode = clipContent.substring(start_, end);
        if (hasEyeClipboard)
            return;
        hasEyeClipboard = true;
        getLongCode(shortCode);
    }

    /**
     * 获取长码
     *
     * @param shortCode
     */
    private void getLongCode(String shortCode) {
        Bundle bundle = new Bundle();
        bundle.putString("username", Constant.getName());
        bundle.putString("token", Constant.getToken());
        bundle.putString("shortCode", shortCode);
        longCodeCancelable = HttpEngine.post(APIUtil.getLongParameterUrl(), bundle, new Callback
                .CommonCallback<String>() {

            @Override
            public void onSuccess(String s) {
                DRMLog.i("LongString: " + s);
                CodeModel cm = JSON.parseObject(s, CodeModel.class);
                if (cm != null && cm.isYes(cm.getCode())) {
                    try {
                        org.json.JSONObject json = new JSONObject(cm.getData());
                        String proId = json.optString("proId");
                        String itemId = json.optString("itemId");
                        String category = json.optString("category");
                        String sharePosition = json.optString("sharePosition");
                        ShareMomentEngine.setSharePosition(category, sharePosition);
                        verifyItem(proId, itemId, category);
                    } catch (org.json.JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onError(Throwable throwable, boolean b) {
                DRMLog.e("" + throwable.getMessage());
            }

            @Override
            public void onCancelled(CancelledException e) {
            }

            @Override
            public void onFinished() {
            }
        });
    }

    /**
     * 验证分享的item权限
     *
     * @param proId
     * @param itemId
     * @param category
     */
    private void verifyItem(final String proId, final String itemId, final String category) {
        Bundle bundle = new Bundle();
        bundle.putString("username", Constant.getName());
        bundle.putString("token", Constant.getToken());
        bundle.putString("productId", proId);
        bundle.putString("itemId", itemId);
        bundle.putString("category", category);
        bundle.putString("application_name", DrmPat.APP_FULLNAME);
        validCancelable = HttpEngine.post(APIUtil.getItemValidUrl(), bundle, new Callback
                .CommonCallback<String>() {
            @Override
            public void onSuccess(String s) {
                DRMLog.i("verifyItem: " + s);
                VerifyItemModel vm = JSON.parseObject(s, VerifyItemModel.class);
                verifyResult(vm, proId, itemId, category);
            }

            @Override
            public void onError(Throwable throwable, boolean b) {
                DRMLog.e("" + throwable.getMessage());
            }

            @Override
            public void onCancelled(CancelledException e) {
            }

            @Override
            public void onFinished() {
            }
        });
    }

    /**
     * 验证结果：<br/>
     * 1.未购买：打开网页详情 <br/>
     * 2.已购买： <br/>
     * </t>    a.未下载：去下载          <br/>
     * </t>    b.已下载：打开播放器       <br/>
     */
    private void verifyResult(VerifyItemModel vm,
                              String proId,
                              String itemId,
                              String category) {
        if (vm.getCode() == null) return;
        if (Code.SUCCESS.equals(vm.getCode())) {
            //已购买
            buy(itemId, category, vm.getData());
        } else if (Code._16003.equals(vm.getCode())) {
            //未购买
            noBuy(proId, category);
        } else {
            ShareMomentEngine.getSharePosition(category); //只是为了删除此值
            ClipboardUtil.clearClipboard(this); //清空剪贴板
            PromptMsgManager.showToast(this, vm.getCode());
        }
    }

    private void noBuy(final String proId, String category) {
        ViewUtil.DialogCallBackPat callBack = new ViewUtil.DialogCallBackPat() {
            @Override
            public void onCancel() {
                ClipboardUtil.clearClipboard(BaseActivity.this); //清空剪贴板
            }

            @Override
            public void onConfirm() {
                OpenPageUtil.openBrowserOfSystem(BaseActivity.this, APIUtil.getProductDetailUrl
                        (proId, null));
            }
        };
        PromptMsgManager proMsg = new PromptMsgManager.Builder()
                .setPositiveText(getString(R.string.to_look))
                .setNegativeText(getString(R.string.cancel))
                .setDialogCallback(callBack)
                .create();
        proMsg.show(this, Code._16003);
        ShareMomentEngine.getSharePosition(category); //只是为了删除此值
    }

    private void buy(final String itemId, final String category, final ProductInfo o) {
        final AlbumContent ac = AlbumContentDAOImpl.getInstance().findAlbumContentByContentId
                (itemId);
        //final ProductInfo o = findDataByProId(proId);
        if (o == null) {
            PromptMsgManager.showToast(this, Code._19001);
            return;
        }
        o.setCategory(category);
        ClipboardUtil.clearClipboard(this); //清空剪贴板
        if (ac == null) {
            //未下载, 提示：您尚未下载该内容，是否立即开始下载？　配【立即下载】【取消】
            ViewUtil.showCommonDialog(this, "",
                    getString(R.string.share_moment_download_ask),
                    getString(R.string.download_now), "",
                    new ViewUtil.DialogCallBackPat() {
                        @Override
                        public void onConfirm() {
                            OpenPageUtil.openFileListPageToDownload(BaseActivity.this, o, itemId);
                        }

                        @Override
                        public void onCancel() {
                        }
                    });
        } else {
            //已下载, 提示：检测到新分享，是否立即打开？ 配【打开】【取消】
            ViewUtil.showCommonDialog(this, "",
                    getString(R.string.share_moment_new_open_ask),
                    getString(R.string.open), "",
                    new ViewUtil.DialogCallBackPat() {
                        @Override
                        public void onConfirm() {
                            ShareMomentEngine.setSelectProId(o.getProId());
                            OpenPageUtil.openPageFromCheck(BaseActivity.this, category,
                                    o.getMyProId(), o.getProductName(),
                                    o.getPicture_url(), ac.getContent_id(),
                                    ac.getMusicLrcId());
                        }

                        @Override
                        public void onCancel() {
                        }
                    });
        }
    }
    //    private ProductInfo findDataByProId(String proId) {
//        if (cacheList == null) return null;
//        ProductInfo o = null;
//        for (ProductInfo productInfo : cacheList) {
//            if (proId.equals(productInfo.getProId())) {
//                o = productInfo;
//                break;
//            }
//        }
//        return o;
//    }

}
