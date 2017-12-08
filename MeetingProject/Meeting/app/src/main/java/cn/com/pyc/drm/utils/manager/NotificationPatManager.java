package cn.com.pyc.drm.utils.manager;

import java.io.File;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;
import cn.com.meeting.drm.R;
import cn.com.pyc.drm.common.App;
import cn.com.pyc.drm.model.db.bean.Album;
import cn.com.pyc.drm.ui.MusicHomeActivity;
import cn.com.pyc.drm.utils.CommonUtil;
import cn.com.pyc.drm.utils.DRMLog;
import cn.com.pyc.drm.utils.DRMUtil;
import cn.com.pyc.drm.utils.DeviceUtils;
import cn.com.pyc.drm.utils.FileUtils;
import cn.com.pyc.drm.utils.ImageUtils;
import cn.com.pyc.drm.utils.MediaUtils;
import cn.com.pyc.drm.utils.ObjectCacheUtil;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

/**
 * 音乐状态栏
 * 
 * @author hudq
 * 
 */
public class NotificationPatManager
{

	private static final int notificationId = 0x01a1;

	private Context mContext;

	private NotificationManager notificationManager;
	private Notification notification;

	private Album album;

	// 设置当前专辑
	public void setAlbum(Album album)
	{
		this.album = album;
	}

	// /////////////////////////////
	// /////////////////////////////
	public NotificationPatManager(Context context)
	{
		this.mContext = context;
		notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	}

	/**
	 * 更新状态栏
	 * 
	 * @param musicName
	 * @param imgUrl
	 * @param hasPermitted
	 */
	public void updateNotification(String musicName, String imgUrl, boolean hasPermitted)
	{
		if (notificationManager == null)
			return;
		if (notification == null)
		{
			showMusicNotification(musicName, imgUrl, hasPermitted);
		} else
		{
			DRMLog.d("updateNotification");
			notification.contentView.setTextViewText(R.id.statusbar_track_name, musicName.replaceAll("\"", ""));
			notification.contentView.setTextViewText(R.id.statusbar_track_status,
					hasPermitted ? mContext.getString(R.string.play_working) : mContext.getString(R.string.play_miss_authority));
			notificationManager.notify(notificationId, notification);
		}
	}

	/**
	 * 取消状态栏
	 */
	public void cancelNotification()
	{
		if (notificationManager != null)
			notificationManager.cancel(notificationId);
	}

	private void showMusicNotification(String musicName, String imgUrl, boolean hasPermitted)
	{
		DRMLog.d("showMusicNotification");
		final RemoteViews mRemoteView = new RemoteViews(mContext.getPackageName(), R.layout.notification_music_playbar);
		int imgSize = CommonUtil.dip2px(mContext, 66);

//		if (DRMUtil.Music_Album_Playing != null)
//		{
//			imgUrl = DRMUtil.Music_Album_Playing.getPicture();
//			String filePath = DRMUtil.DEFAULT_HIGH_SPEED_FUZZY_PATH + File.separator + FileUtils.getNameFromFilePath(imgUrl);
//			Bitmap bitmap = ImageUtils.zoomBitmap(ImageUtils.getBitmapByPath(filePath), imgSize, imgSize);
//			mRemoteView.setImageViewBitmap(R.id.statusbar_artist_image, bitmap);
//		} else
//		{
//			ImageLoader.getInstance().loadImage(imgUrl, new SimpleImageLoadingListener()
//			{
//				@Override
//				public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage)
//				{
//					super.onLoadingComplete(imageUri, view, loadedImage);
//					mRemoteView.setImageViewBitmap(R.id.statusbar_artist_image, loadedImage);
//				}
//			});
//		}
		mRemoteView.setTextViewText(R.id.statusbar_track_name, musicName.replaceAll("\"", ""));
		mRemoteView.setTextViewText(R.id.statusbar_track_status,
				hasPermitted ? mContext.getString(R.string.play_working) : mContext.getString(R.string.play_miss_authority));
		// API3.0 以上的时候显示按钮，否则消失
		if (DeviceUtils.hasHoneycomb())
		{
			mRemoteView.setViewVisibility(R.id.statusbar_content_close_image, View.VISIBLE);
			// 处理点击事件
			Intent intent = new Intent(DRMUtil.BROADCAST_MUSIC_STATUSBAR);
			intent.putExtra(MediaUtils.NOTIFY_BUTTONID_TAG, MediaUtils.BUTTON_CLOSE_ID);
			// 得到要执行的PendingIntent，发送关闭的广播
			PendingIntent mPendingIntent = PendingIntent.getBroadcast(mContext, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			// 点击控件的监听
			mRemoteView.setOnClickPendingIntent(R.id.statusbar_content_close_image, mPendingIntent);
		} else
		{
			mRemoteView.setViewVisibility(R.id.statusbar_content_close_image, View.GONE);
		}
		Notification.Builder builder = new Notification.Builder(mContext);
		// builder.setContent(mRemoteView);
		builder.setAutoCancel(true);
		builder.setContentIntent(getDefalutPending(mContext));
		builder.setWhen(System.currentTimeMillis());
		builder.setTicker(mContext.getString(R.string.play_working));
		builder.setPriority(Notification.PRIORITY_DEFAULT);
		builder.setOngoing(true);
		builder.setSmallIcon(R.drawable.ic_menu_notification);
		notification = builder.build();
		notification.contentView = mRemoteView;
		notification.flags = Notification.FLAG_ONGOING_EVENT | Notification.FLAG_AUTO_CANCEL;
		notificationManager.notify(notificationId, notification);
	}

	/**
	 * @获取默认的pendingIntent,为了防止2.3及以下版本报错
	 * @flags属性: 在顶部常驻:Notification.FLAG_ONGOING_EVENT <br/>
	 *           点击去除：Notification.FLAG_AUTO_CANCEL <br/>
	 */
	private PendingIntent getDefalutPending(Context context)
	{
		if (null == album)
		{
			album = DRMUtil.Music_Album_Playing;

			if (null == album)
			{
				album = (Album) ObjectCacheUtil.init(Album.class).readObjFromCache(mContext);
			}
		}

		Intent intent = new Intent(context, MusicHomeActivity.class);
		Bundle extra = new Bundle();
		extra.putSerializable("Album", album);
		intent.putExtras(extra);

		PendingIntent pendingIntent = PendingIntent.getActivity(App.getInstance(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		return pendingIntent;
	}
}
