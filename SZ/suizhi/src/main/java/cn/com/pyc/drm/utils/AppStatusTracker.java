package cn.com.pyc.drm.utils;

import android.app.Activity;
import android.app.Application;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;

import cn.com.pyc.drm.utils.help.MusicHelp;

public class AppStatusTracker {
    private static final String TAG = "AppTracker";
    public int activityCount = 0;
    private long backgroundStamp = 0L;
    private boolean isForeground = false;

    private static class AppStatusTrackerInner {
        private static AppStatusTracker instance = new AppStatusTracker();
    }

    private AppStatusTracker() {
    }

    public static AppStatusTracker getInstance() {
        return AppStatusTrackerInner.instance;
    }

    /**
     * 监听APP内Activity生命周期
     *
     * @param app
     */
    public void registerActivityLifecycle(Application app) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH)
            return;

        Application.ActivityLifecycleCallbacks callbacks = new Application
                .ActivityLifecycleCallbacks() {

            @Override
            public void onActivityStopped(Activity activity) {
                if (activityCount > 0) activityCount--;

                if (activityCount == 0) {
                    isForeground = false;
                    backgroundStamp = System.currentTimeMillis();
                }
                DRMLog.d(TAG, activity.toString()
                        + ":onActivityStopped, foreground activity: "
                        + activityCount);
            }

            @Override
            public void onActivityStarted(Activity activity) {
                activityCount++;
                DRMLog.d(TAG, activity.toString() + ":onActivityStarted: "
                        + activityCount);
                if (activity.getComponentName() != null) {
                    //音乐界面不显示悬浮窗
                    if (TextUtils.equals(activity.getComponentName().getClassName(),
                            "cn.com.pyc.drm.ui.MusicPlayActivity")) {
                        MusicHelp.removeMusicView(activity);
                    }
                }
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity,
                                                    Bundle outState) {
            }

            @Override
            public void onActivityResumed(Activity activity) {
                DRMLog.d(TAG, activity.toString() + ":onActivityResumed");
                isForeground = true;
                backgroundStamp = 0L;
            }

            @Override
            public void onActivityPaused(Activity activity) {
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                DRMLog.d(TAG, activity.toString() + ":onActivityDestroyed");
            }

            @Override
            public void onActivityCreated(Activity activity,
                                          Bundle savedInstanceState) {
            }
        };
        app.unregisterActivityLifecycleCallbacks(callbacks);
        app.registerActivityLifecycleCallbacks(callbacks);
    }
}
