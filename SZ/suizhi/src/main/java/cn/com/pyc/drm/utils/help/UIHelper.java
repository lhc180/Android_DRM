package cn.com.pyc.drm.utils.help;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import cn.com.pyc.drm.R;
import cn.com.pyc.drm.utils.manager.SystemBarTintManager;

/**
 * UI操作工具包：<br/>
 * 封装程序UI相关的一些操作
 *
 * @author hudq
 */
public class UIHelper {

    /**
     * 设置屏幕透明度
     */
    public static void setScreenAlpha(Activity activity, float bgAlpha) {
        if (activity.getWindow() != null) {
            WindowManager.LayoutParams params = activity.getWindow().getAttributes();
            params.alpha = bgAlpha;
            activity.getWindow().setAttributes(params);
        }
    }

    /**
     * 显示自定义颜色状态栏 ,必须在setContentView之后调用<br\>
     * <p>
     * android 4.4之后生效
     *
     * @param activity
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void showTintStatusBar(Activity activity) {
        // 顶部标题栏颜色
        showTintStatusBar(activity,
                activity.getResources().getColor(R.color.title_bg_color));
    }

    /**
     * 显示自定义颜色状态栏 ,必须在setContentView之后调用
     *
     * @param activity
     * @param colorId  颜色资源id
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void showTintStatusBar(Activity activity, int colorId) {
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) return;
            if (activity.getWindow() == null) return;
            // 透明状态栏
            activity.getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            // 透明导航栏
            activity.getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            ViewGroup mRootView = (ViewGroup) activity
                    .findViewById(android.R.id.content);
            mRootView = (ViewGroup) mRootView.getChildAt(0);
            mRootView.setFitsSystemWindows(true);
            mRootView.setClipToPadding(true);

            SystemBarTintManager tintManager = new SystemBarTintManager(
                    activity);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setNavigationBarTintEnabled(false);
            tintManager.setStatusBarTintColor(colorId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // public static void setImmersionType(Activity activity){
    // /**
    // * 沉浸式状态栏
    // */
    // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
    // // 状态栏透明 需要在创建SystemBarTintManager 之前调用。
    // setTranslucentStatus(true,activity);
    // }
    //
    // SystemBarTintManagerImmersion tintManager = new
    // SystemBarTintManagerImmersion(activity);
    // tintManager.setStatusBarTintEnabled(true);
    // // 使StatusBarTintView 和 actionbar的颜色保持一致，风格统一。
    // tintManager.setStatusBarTintResource(0x000000000);
    // // 设置状态栏的文字颜色
    // tintManager.setStatusBarDarkMode(false, activity);
    // }
    //
    //
    // @TargetApi(19)
    // private static void setTranslucentStatus(boolean on,Activity activity) {
    // Window win = activity.getWindow();
    // WindowManager.LayoutParams winParams = win.getAttributes();
    // final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
    // if (on) {
    // winParams.flags |= bits;
    // } else {
    // winParams.flags &= ~bits;
    // }
    // win.setAttributes(winParams);
    // }

    /**
     * 显示自定义颜色状态栏 ,必须在setContentView之后调用
     *
     * @param activity
     * @param drawable drawable资源
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void showTintStatusBar(Activity activity, Drawable drawable) {
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) return;
            if (activity.getWindow() == null) return;
            // 透明状态栏
            activity.getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            // 透明导航栏
            activity.getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            ViewGroup mRootView = (ViewGroup) activity
                    .findViewById(android.R.id.content);
            mRootView = (ViewGroup) mRootView.getChildAt(0);
            mRootView.setFitsSystemWindows(true);
            mRootView.setClipToPadding(true);

            SystemBarTintManager tintManager = new SystemBarTintManager(
                    activity);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setNavigationBarTintEnabled(false);
            tintManager.setStatusBarTintDrawable(drawable);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * activity 进入开启的动画
     *
     * @param activity
     */
    public static void startInAnim(Activity activity) {
        // 进入动画
        activity.overridePendingTransition(R.anim.trans_x_in,
                R.anim.fade_out_scale);
    }

    /**
     * activity退出返回的动画
     *
     * @param activity
     */
    public static void finishOutAnim(Activity activity) {
        // 退出动画
        activity.overridePendingTransition(R.anim.fade_in_scale,
                R.anim.trans_x_out);
    }

    /**
     * 调用finish()销毁，带动画
     *
     * @param activity
     */
    public static void finishActivity(Activity activity) {
        activity.finish();
        finishOutAnim(activity);
    }

    /**
     * 弹出Toast消息
     *
     * @param msg
     */
    public static void showToast(Context ctx, String msg) {
        Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
    }


    private static long mLastClickBackTime = 0;

    /**
     * 显示退出提示
     */
    public static void showExitTips(Activity ctx) {
        long curTime = System.currentTimeMillis();
        if (curTime - mLastClickBackTime < 2000) {
            ctx.finish();
        } else {
            mLastClickBackTime = curTime;
            Toast.makeText(ctx.getApplicationContext(), "再按一次退出应用！",
                    Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * 设置UI是否可用
     *
     * @param aty
     * @param enable
     */
    public static void setEnableUI(Activity aty, boolean enable) {
        if (aty.getWindow() != null) {
            aty.getWindow().getDecorView().setEnabled(enable);
        }
    }

    /**
     * 设置隐藏标题栏
     *
     * @param activity
     */
    public static void setNoTitleBar(Activity activity) {
        activity.requestWindowFeature(Window.FEATURE_NO_TITLE);
    }

    /**
     * 设置全屏,在 setContentView() 之前调用.
     *
     * @param activity
     */
    public static void setFullScreen(Activity activity) {
        if (activity.getWindow() == null) return;
        activity.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    /**
     * 取消全屏
     *
     * @param activity
     */
    public static void cancelFullScreen(Activity activity) {
        if (activity.getWindow() == null) return;
        activity.getWindow().clearFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

}
