package cn.com.pyc.drm.common;

import android.app.Application;
import android.content.ComponentCallbacks2;
import android.util.Log;

import org.xutils.x;

import cn.com.pyc.drm.BuildConfig;
import cn.com.pyc.drm.utils.AppStatusTracker;
import cn.com.pyc.drm.utils.PathUtil;
import cn.com.pyc.drm.utils.help.ImageLoadHelp;
import cn.com.pyc.drm.utils.help.MusicHelp;
import cn.com.pyc.drm.utils.manager.ActicityManager;
import cn.com.pyc.drm.utils.manager.ExecutorManager;
import cn.jpush.android.api.JPushInterface;

public class App extends Application {

    private static final String D_URL = "http://192.168.85.5:82/HostMonitor/client/log/addLog";
    private static final String R_URL = "http://114.112.104.138:6001/HostMonitor/client/log/addLog";
    private static App sContext;

    public static App getInstance() {
        return sContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = this;
        x.Ext.init(this);
        x.Ext.setDebug(false);
        LogConfig.setLogHttpUrl(BuildConfig.DEBUG ? D_URL : R_URL);

        PathUtil.renameSaveFileDir(this);
        Constant.init(getApplicationContext());
        try {
            JPushInterface.setDebugMode(SZConfig.DEVELOPER_MODE);
            JPushInterface.init(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        initDebugMode();

        AppStatusTracker.getInstance().registerActivityLifecycle(this);
    }

    private void initDebugMode() {
        // 检查应用可能存在的警告或问题，供开发者发现问题隐患
//        if (SZConfig.DEVELOPER_MODE
//                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
//            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
//                    .detectDiskReads().detectDiskWrites().penaltyLog()
//                    .detectNetwork().build());
//            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
//                    .penaltyDeath().penaltyLog().detectLeakedSqlLiteObjects()
//                    .detectLeakedClosableObjects().build());
//        }
        // 记录崩溃日志
        if (!SZConfig.DEVELOPER_MODE) {
            CrashHandler crashHandler = CrashHandler.getInstance();
            crashHandler.init(getApplicationContext());
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.v("App", "onTerminate");
        ActicityManager.getInstance().exit();
        destroyResource();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        System.err.print("onLowMemory");
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        Log.v("App", "onTrimMemory = " + level);
        if (level > ComponentCallbacks2.TRIM_MEMORY_BACKGROUND) {
            destroyResource();
        }
    }

    private void destroyResource() {
        ImageLoadHelp.clearCache();
        ExecutorManager.getInstance().shutdownNow();
        MusicHelp.release(this);
        // TODO:
    }

}
