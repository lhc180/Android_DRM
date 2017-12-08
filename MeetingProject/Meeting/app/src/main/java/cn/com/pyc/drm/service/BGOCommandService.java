package cn.com.pyc.drm.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import cn.com.pyc.drm.bean.event.DBMakerEvent;
import cn.com.pyc.drm.common.ConstantValue;
import cn.com.pyc.drm.utils.DRMDBHelper;
import cn.com.pyc.drm.utils.DRMLog;
import cn.com.pyc.drm.utils.ParserDRMFileUtil;
import de.greenrobot.event.EventBus;

/**
 * 服务，执行一些的耗时操作
 * 
 * @author hudq
 * 
 */
public class BGOCommandService extends IntentService
{

	private static final String TAG = BGOCommandService.class.getSimpleName();

	/**
	 * 开启
	 * 
	 * @param context
	 * @param option
	 */
	public static void startBGOService(Context context, int option)
	{
		DRMDBHelper.destoryDBHelper();

		Intent i = new Intent(context, BGOCommandService.class);
		i.putExtra("option", option);
		context.startService(i);
	}

	public BGOCommandService()
	{
		super(TAG);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		DRMLog.d(TAG + "  onStartCommand");
		return super.onStartCommand(intent, flags, startId);
	}

	// 处理完onStartCommand()后执行，注意onHandlerIntent是在后台线程中运行,异步操作。
	@Override
	protected void onHandleIntent(Intent intent)
	{
		if (intent == null)
			return;
		// 不同方式处理字段
		int opt = intent.getIntExtra("option", 0);
		DRMLog.d(TAG, "option = " + opt);
		switch (opt)
		{
		case ConstantValue.CREATE_DB:
		{
			DRMDBHelper drmDB = new DRMDBHelper(this.getApplicationContext());
			drmDB.createDBTable();
			// 创建成功，发送通知信息，通知登录页面，开始登录
			EventBus.getDefault().post(new DBMakerEvent(true));
		}
			break;

		case ConstantValue.RESOLVE_DOWNLOAD:
		{
			// 解析文件
			String myProId = intent.getStringExtra("myProId");
			int position = intent.getIntExtra("position", 0);
			String author = intent.getStringExtra("author");
			String picture_ratio = intent.getStringExtra("picture_ratio");
			String publishDate = intent.getStringExtra("publish_date");
			ParserDRMFileUtil.getInstance().parserDRMFiles(this, myProId, position, author, picture_ratio, publishDate);
		}
			break;
		}
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		DRMLog.d(TAG + "  onDestroy");
	}

}
