package cn.com.pyc.drm.utils.manager;

import android.app.Activity;

import java.util.Stack;

public class ActicityManager {

    private static ActicityManager instance;
    private static Stack<Activity> activityStack;

    private ActicityManager() {
    }

    public static ActicityManager getInstance() {
        if (null == instance) {
            instance = new ActicityManager();
        }

        if (activityStack == null) {
            activityStack = new Stack<Activity>();
        }
        return instance;
    }

    public void add(Activity activity) {
        activityStack.add(activity);
    }

    public void remove(Activity activity) {
        activityStack.remove(activity);
    }

    /**
     * 获取当前Activity（堆栈中最后一个压入的）
     */
    public Activity currentActivity() {
        if (!activityStack.isEmpty())
            return activityStack.lastElement();
        return null;
    }

    /**
     * 退出
     */
    public void exit() {
        if (!activityStack.isEmpty()) {
            for (Activity a : activityStack) {
                if (a != null && !a.isFinishing())
                    a.finish();
            }
            activityStack.clear();
        }
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }
}
