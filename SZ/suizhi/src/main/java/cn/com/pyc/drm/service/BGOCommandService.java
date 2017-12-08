package cn.com.pyc.drm.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import java.io.File;

import cn.com.pyc.drm.bean.event.DBMakerEvent;
import cn.com.pyc.drm.common.Constant;
import cn.com.pyc.drm.utils.DRMLog;
import cn.com.pyc.drm.utils.PathUtil;
import cn.com.pyc.drm.utils.help.DBHelper;
import cn.com.pyc.drm.utils.help.DRMDBHelper;
import de.greenrobot.event.EventBus;

/**
 * 服务，执行一些的耗时操作
 *
 * @author hudq
 */
public class BGOCommandService extends IntentService {

    //public static final int RESOLVE_DOWNLOAD = 0xa2;        //解析
    public static final int CREATE_DB = 0xa3;               //创建DB
    public static final int RENAME_DB = 0xa4;               //重名民文件夹路径和数据库

    private static final String TAG = BGOCommandService.class.getSimpleName();

    /**
     * 开启
     *
     * @param context
     * @param option
     */
    public static void startBGOService(Context context, int option) {
        DRMDBHelper.destoryDBHelper();
        Intent i = new Intent(context, BGOCommandService.class);
        i.putExtra("option", option);
        context.startService(i);
    }

    public BGOCommandService() {
        super(TAG);
        setIntentRedelivery(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        DRMLog.d(TAG + "  onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    // 处理完onStartCommand()后执行，注意onHandlerIntent是在后台线程中运行,异步操作。
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) return;
        if (!intent.hasExtra("option"))
            throw new IllegalArgumentException("hasExtra,'option' must be required.");
        int opt = intent.getIntExtra("option", 0);
        switch (opt) {
            case CREATE_DB: // 创建db
            {
                DRMDBHelper drmDB = new DRMDBHelper(this.getApplicationContext());
                drmDB.createDBTable();
                // 创建成功，发送通知信息，通知登录页面开始登录
                EventBus.getDefault().post(new DBMakerEvent(true));
            }
            break;
            //case RESOLVE_DOWNLOAD: // 解析专辑
            //{
            //String myProId = intent.getStringExtra("myProId");
            //int position = intent.getIntExtra("position", 0);
            //ProductInfo o = intent.getParcelableExtra("DownloadInfo");
            //ParserDRMUtil.getInstance().parserDRM(this, myProId, position, o);
            //}
            //break;
            // case IConstant.RESOLVE_DOWNLOAD_2: //解析下载的文件
            // {
            // FileData o = intent.getParcelableExtra("FileData");
            // ParserDRMUtil.getInstance().parserDRMFile(this, o);
            // }
            // break;

            case RENAME_DB:
                File dbFile = getDatabasePath(Constant.getLoginName() + DBHelper.DB_LABEL);
                if (dbFile.exists()) {
                    File toDbFile = getDatabasePath(Constant.getAccountId());
                    DRMLog.e("数据库路径1：" + dbFile.getPath());
                    DRMLog.e("数据库路径2：" + toDbFile.getPath());
                    dbFile.renameTo(toDbFile);
                    PathUtil.createFilePath();
                }
                break;
            default:
                break;
        }
    }

}
