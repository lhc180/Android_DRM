package cn.com.pyc.drm.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import cn.com.meeting.drm.R;
import cn.com.pyc.drm.bean.event.AlbumUpdateEvent;
import cn.com.pyc.drm.common.Constant;
import cn.com.pyc.drm.model.DownloadInfo;
import cn.com.pyc.drm.model.db.bean.Album;
import cn.com.pyc.drm.model.db.bean.Downdata;
import cn.com.pyc.drm.model.db.practice.AlbumDAOImpl;
import cn.com.pyc.drm.model.db.practice.DowndataDAOImpl;
import cn.com.pyc.drm.ui.BaseActivity;
import cn.com.pyc.drm.ui.DownloadMainActivity;
import cn.com.pyc.drm.utils.CommonUtil;
import cn.com.pyc.drm.utils.ConvertToUtils;
import cn.com.pyc.drm.utils.DRMLog;
import cn.com.pyc.drm.utils.FileUtils;
import cn.com.pyc.drm.utils.StringUtil;
import cn.com.pyc.drm.utils.manager.DownloadTaskManager;
import cn.com.pyc.drm.widget.RoundProgressBar;

import com.android.maxwin.view.ScaleImageView;
import com.android.maxwin.view.XListView;
import com.nostra13.universalimageloader.core.ImageLoader;

import de.greenrobot.event.EventBus;

/**
 * 网络数据适配器
 * 
 * @author qd
 * 
 */
public class DownloadMainAdapter extends BaseAdapter
{

	private BaseActivity context;
	private List<DownloadInfo> infos = new ArrayList<>();

	public DownloadMainAdapter(BaseActivity context, List<DownloadInfo> infos)
	{
		super();
		this.infos = infos;
		this.context = context;
	}

	public List<DownloadInfo> getInfos()
	{
		return infos;
	}

	// public void setData(List<DownloadInfo> infos) {
	// this.infos = infos;
	// notifyDataSetChanged();
	// }

	/**
	 * 更多数据
	 * 
	 * @param infos
	 */
	public void addLastInfos(List<DownloadInfo> infos)
	{
		// TODO:
		// if (!this.infos.containsAll(infos)) {
		// this.infos.addAll(infos);
		// }
		// 插到当前数据的末尾
		this.infos.addAll(this.infos.size(), infos);
	}

	@Override
	public int getCount()
	{
		return infos.size();
	}

	@Override
	public Object getItem(int position)
	{
		return infos.get(position);
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		if (convertView == null)
		{
			convertView = View.inflate(context, R.layout.item_download_main,
					null);
		}
		ScaleImageView image = (ScaleImageView) convertView
				.findViewById(R.id.image);
		TextView name = (TextView) convertView.findViewById(R.id.name);
		TextView tips = (TextView) convertView.findViewById(R.id.tips);
		TextView types = (TextView) convertView.findViewById(R.id.types);
		TextView desc = (TextView) convertView.findViewById(R.id.desc);
		ImageView arrow = (ImageView) convertView
				.findViewById(R.id.image_arrow_right);
		Button imgUpdate = (Button) convertView
				.findViewById(R.id.button_update);
		RoundProgressBar mRoundProgressBar = (RoundProgressBar) convertView
				.findViewById(R.id.RoundProgressBar);
		TextView tvProgress = (TextView) convertView
				.findViewById(R.id.progress);
		ImageView download_title = (ImageView) convertView
				.findViewById(R.id.download_title);
		// -------
		convertView.setTag(position);
		// 数据
		DownloadInfo info = infos.get(position);
		// 将position赋值给downloadinfo的position；
		info.setPosition(position);
		// 设置图片
		double ratio = ConvertToUtils.toDouble(info.getPicture_ratio(), 1.0);
		int imgWidth = (Constant.screenWidth - CommonUtil.dip2px(context, 16)) / 2;
		image.setImageWidth(imgWidth);
		// 系数 picture_ratio = width/height
		image.setImageHeight((int) (imgWidth / ratio));
		// 初始化数据
		showNetData(context, image, name, tips, types, desc, arrow,
				mRoundProgressBar, tvProgress, download_title, imgUpdate, info);
		return convertView;
	}

	/**
	 * 显示数据
	 * 
	 * @param context
	 *            BaseActivity
	 * @param image
	 * @param name
	 * @param tips
	 * @param types
	 * @param desc
	 * @param arrow
	 * @param mRoundProgressBar
	 * @param tvProgress
	 * @param imgUpdate
	 * @param info
	 *            eg:DownloadInfo
	 */
	private void showNetData(final BaseActivity context, ScaleImageView image,
			TextView name, TextView tips, TextView types, TextView desc,
			ImageView arrow, RoundProgressBar mRoundProgressBar,
			TextView tvProgress, ImageView download_title, Button imgUpdate,
			DownloadInfo info)
	{
		String myProId = info.getMyProId();
		// 查询本地数据库
		Album album = AlbumDAOImpl.getInstance().findAlbumByMyproId(myProId);
		if (album != null)
		{
			DRMLog.d("本地：" + info.getProductName());
			// 本地
			// arrow.setVisibility(View.VISIBLE);
			mRoundProgressBar.setVisibility(View.INVISIBLE);
			tvProgress.setVisibility(View.INVISIBLE);
			download_title.setVisibility(View.VISIBLE);
			checkMeetAlbumHasFresh(album, info, imgUpdate, arrow,
					download_title);

			// 解析文件后
			analysisAlbum(context, types, album);
			// tips
			if ("".equals(StringUtil.formatAuthors(album.getAuthor())))
			{
				tips.setVisibility(View.GONE);
			} else
			{
				tips.setText(StringUtil.formatAuthors(album.getAuthor()));
				tips.setVisibility(View.VISIBLE);
			}
			// desc.setText("");
			name.setText(album.getName());
			// DRMLog.e("album-url: ", album.getPicture());
			if (TextUtils.isEmpty(album.getPicture())
					|| "null".equalsIgnoreCase(album.getPicture()))
			{
				image.setBackground(context.getResources().getDrawable(
						R.drawable.ic_cover_page));
			} else
			{
				ImageLoader.getInstance().displayImage(album.getPicture(),
						image, Constant.options, null);
			}
		} else
		{
			// 网络数据
			if (imgUpdate.getVisibility() == View.VISIBLE)
				imgUpdate.setVisibility(View.GONE);
			arrow.setVisibility(View.GONE);
			mRoundProgressBar.setVisibility(View.VISIBLE);
			tvProgress.setVisibility(View.VISIBLE);
			download_title.setVisibility(View.GONE);
			Downdata downData = DowndataDAOImpl.getInstance().findDowndataById(
					myProId);
			int taskState = info.getTaskState();
			// 下载进度数据不为空
			if (downData != null)
			{
				changeView(context, mRoundProgressBar, tvProgress, taskState);
				// 如果进度保存数据不为空，初始化进入，显示暂停按钮
				if (taskState == DownloadTaskManager.INIT)
					pause(context, mRoundProgressBar, tvProgress);
				
				StringBuffer sb = new StringBuffer();
				tvProgress.setText(TextUtils.equals(downData.getIsDownload(),
						"0") ? sb.append(downData.getFileOffsetstr())
						.append("/").append(downData.getTotalSize()).toString()
						: context.getString(R.string.fail));
				if (taskState == DownloadTaskManager.COMPLETE_PARSER)
					tvProgress.setText(context.getString(R.string.Parsering));
				if (taskState == DownloadTaskManager.CONNECTING)
				{
					if (!TextUtils.isEmpty(downData.getProgress()))
					{
						int progress = Integer.valueOf(downData.getProgress());
						tvProgress.setText(context
								.getString(R.string.Connecting));
						mRoundProgressBar.setProgress(progress);
					}
				}
			} else
			{
				// 下载进度数据==空
				changeView(context, mRoundProgressBar, tvProgress, taskState);
			}
			// 正在下载
			downloading(context, mRoundProgressBar, tvProgress, info);

			name.setText(info.getProductName());
			// tips
			if ("".equals(StringUtil.formatAuthors(info.getAuthors())))
			{
				tips.setVisibility(View.GONE);
			} else
			{
				tips.setText(StringUtil.formatAuthors(info.getAuthors()));
				tips.setVisibility(View.VISIBLE);
			}
			types.setText(info.getCategory());
			// desc.setText("");
			if (TextUtils.isEmpty(info.getPicture_url()))
			{
				image.setBackground(context.getResources().getDrawable(
						R.drawable.ic_cover_page));
			} else
			{
				ImageLoader.getInstance().displayImage(info.getPicture_url(),
						image, Constant.options, null);
			}
		}
	}

	@Deprecated
	private static class ViewNetHolder
	{
		ScaleImageView image;
		TextView name;
		TextView tips;
		TextView types;
		TextView desc;
		ImageView arrow;
		TextView tvProgress;
		RoundProgressBar mRoundProgressBar;
	}

	/**
	 * 下载任务开始，单个item状态变化更新，减少整个ui的刷新
	 * 
	 * @param context
	 * @param position
	 * @param info
	 * @param mNetListView
	 */
	public void updateItemViewWhenDownload(Context context, int position,
			DownloadInfo info, XListView mNetListView)
	{
		int visiblePosition = mNetListView.getFirstVisiblePosition();
		int offset = position - visiblePosition;
		// 只有在可见区域才更新
		if (offset < 0 && offset > 10) { return; }
		// View itemView = mNetListView.getChildAt(offset);
		DRMLog.i("更新状态，position = " + position);
		View itemView = mNetListView.findViewWithTag(position);
		if (itemView == null) { return; }
		ImageView arrow = (ImageView) itemView
				.findViewById(R.id.image_arrow_right);
		RoundProgressBar mRoundProgressBar = (RoundProgressBar) itemView
				.findViewById(R.id.RoundProgressBar);
		TextView tvProgress = (TextView) itemView.findViewById(R.id.progress);
		if (arrow.getVisibility() == View.VISIBLE)
			arrow.setVisibility(View.INVISIBLE);
		if (mRoundProgressBar.getVisibility() == View.INVISIBLE)
			mRoundProgressBar.setVisibility(View.VISIBLE);
		if (tvProgress.getVisibility() == View.INVISIBLE)
			tvProgress.setVisibility(View.VISIBLE);
		// 查看数据库是否有下载进度记录，如果没有，则添加。有则修改。
		Downdata downData = DowndataDAOImpl.getInstance().findDowndataById(
				info.getMyProId());
		// 下载状态
		int taskState = info.getTaskState();
		if (downData != null)
		{
			// 保存进度数据不为空
			StringBuffer sb = new StringBuffer();
			tvProgress
					.setText(TextUtils.equals(downData.getIsDownload(), "0") ? sb
							.append(downData.getFileOffsetstr()).append("/")
							.append(downData.getTotalSize()).toString()
							: context.getString(R.string.fail));
			// 如果是正在连接，并且下载进度不为空，则设置下载进度。
			if (taskState == DownloadTaskManager.CONNECTING)
			{
				if (!TextUtils.isEmpty(downData.getProgress()))
				{
					int progress = Integer.valueOf(downData.getProgress());
					mRoundProgressBar.setProgress(progress);
				}
			}
		}
		changeView(context, mRoundProgressBar, tvProgress, taskState);
	}

	/**
	 * 修改view状态
	 * 
	 * @param context
	 * @param netHolder
	 * @param taskState
	 */
	private void changeView(Context context,
			RoundProgressBar mRoundProgressBar, TextView tvProgress,
			int taskState)
	{
		switch (taskState) {
		case DownloadTaskManager.INIT:
		{
			// DRMLog.i("开始");
			init(context, mRoundProgressBar, tvProgress);
		}
			break;
		case DownloadTaskManager.WAITING:
		{
			// DRMLog.i("等待中");
			waiting(context, mRoundProgressBar, tvProgress);
		}
			break;
		case DownloadTaskManager.CONNECTING:
		{
			// DRMLog.i("正在连接");
			connecting(context, mRoundProgressBar, tvProgress);
		}
			break;
		case DownloadTaskManager.PAUSE:
		{
			// DRMLog.i("暂停中");
			pause(context, mRoundProgressBar, tvProgress);
		}
			break;
		case DownloadTaskManager.COMPLETE_PARSER:
		{
			// DRMLog.i("解析文件中");
			completeStartParser(context, mRoundProgressBar, tvProgress);
		}
			break;

		case DownloadTaskManager.DOWNLOAD_ERROR:
		{
			// DRMLog.i("下载异常，ftpPath shutdown");
			downloadError(context, mRoundProgressBar, tvProgress);
		}
			break;

		case DownloadTaskManager.CONNECT_ERROR:
		{
			// DRMLog.i("连接异常");
			connectError(context, mRoundProgressBar, tvProgress);
		}
			break;
		}
	}

	/**
	 * 更新进度
	 * 
	 * @param context
	 * @param position
	 * @param info
	 * @param mNetListView
	 */
	public void updateProgress(Context context, int position, DownloadInfo o,
			XListView mNetListView)
	{
		int visiblePosition = mNetListView.getFirstVisiblePosition();
		int offset = position - visiblePosition;
		// 只有在可见区域才更新
		if (offset < 0 && offset > 10) { return; }
		View itemView = mNetListView.findViewWithTag(position);
		if (itemView == null) { return; }

		ImageView arrow = (ImageView) itemView
				.findViewById(R.id.image_arrow_right);
		RoundProgressBar mRoundProgressBar = (RoundProgressBar) itemView
				.findViewById(R.id.RoundProgressBar);
		TextView tvProgress = (TextView) itemView.findViewById(R.id.progress);

		if (arrow.getVisibility() == View.VISIBLE)
			arrow.setVisibility(View.GONE);
		if (mRoundProgressBar.getVisibility() == View.GONE)
			mRoundProgressBar.setVisibility(View.VISIBLE);
		if (tvProgress.getVisibility() == View.GONE)
			tvProgress.setVisibility(View.VISIBLE);

		mRoundProgressBar.setBackground(null);
		mRoundProgressBar.setCricleColor(context.getResources().getColor(
				R.color.round_color));
		mRoundProgressBar.setTextColor(context.getResources().getColor(
				R.color.dialog_blue));
		// 更新数据的部分
		mRoundProgressBar.setProgress(o.getProgress());
		StringBuffer sb = new StringBuffer();
		String mUpdateProgress = sb
				.append(FileUtils.convertStorage(o.getCurrentSize()))
				.append("/").append(FileUtils.convertStorage(o.getTotalSize()))
				.toString();
		tvProgress.setText(mUpdateProgress);
	}

	/**
	 * 解析文件完成，更新单个item，减少整个ui的刷新
	 * 
	 * @param context
	 * 
	 * @param position
	 *            位置
	 * @param infos
	 *            数据源
	 * @param mNetListView
	 */
	public void updateItemViewAfterAnalysisFile(Context context, int position,
			DownloadInfo info, XListView mNetListView)
	{
		int visiblePosition = mNetListView.getFirstVisiblePosition();
		int offset = position - visiblePosition;
		// 只有在可见区域才更新
		if (offset < 0 && offset > 10) { return; }
		View itemView = mNetListView.findViewWithTag(position);
		if (itemView == null) { return; }
		TextView types = (TextView) itemView.findViewById(R.id.types);
		ImageView arrow = (ImageView) itemView
				.findViewById(R.id.image_arrow_right);
		ImageView download_title = (ImageView) itemView
				.findViewById(R.id.download_title);
		RoundProgressBar mRoundProgressBar = (RoundProgressBar) itemView
				.findViewById(R.id.RoundProgressBar);
		TextView tvProgress = (TextView) itemView.findViewById(R.id.progress);
		// DownloadInfo info = infos.get(position);
		Album album = AlbumDAOImpl.getInstance().findAlbumByMyproId(
				info.getMyProId());
		if (album != null)
		{
			// 本地
			// arrow.setVisibility(View.VISIBLE);
			mRoundProgressBar.setVisibility(View.INVISIBLE);
			tvProgress.setVisibility(View.INVISIBLE);
			download_title.setVisibility(View.VISIBLE);
			analysisAlbum(context, types, album);
		}
	}

	/**
	 * 开始状态
	 * 
	 * @param context
	 * @param holder
	 */
	private void init(Context context, RoundProgressBar mRoundProgressBar,
			TextView tvProgress)
	{
		tvProgress.setText(null);
		mRoundProgressBar.setProgress(0);
		mRoundProgressBar.setBackground(context.getResources().getDrawable(
				R.drawable.download_button));
		mRoundProgressBar.setTextColor(context.getResources().getColor(
				R.color.touming));
		mRoundProgressBar.setCricleColor(context.getResources().getColor(
				R.color.touming));

	}

	/**
	 * 等待中
	 * 
	 * @param context
	 * @param holder
	 */
	private void waiting(Context context, RoundProgressBar mRoundProgressBar,
			TextView tvProgress)
	{
		tvProgress.setText(context.getResources().getString(R.string.Waiting));
		mRoundProgressBar.setProgress(0);
		// holder.mRoundProgressBar.setBackground(context.getResources()
		// .getDrawable(R.drawable.download_not));
		mRoundProgressBar.setBackground(null);
		mRoundProgressBar.setCricleColor(context.getResources().getColor(
				R.color.round_color));
		mRoundProgressBar.setTextColor(context.getResources().getColor(
				R.color.dialog_blue));
	}

	/**
	 * 连接中
	 * 
	 * @param context
	 * @param holder
	 */
	private void connecting(Context context,
			RoundProgressBar mRoundProgressBar, TextView tvProgress)
	{
		tvProgress.setText(context.getResources()
				.getString(R.string.Connecting));
		// holder.mRoundProgressBar.setBackground(context.getResources()
		// .getDrawable(R.drawable.download_not));
		mRoundProgressBar.setBackground(null);
		mRoundProgressBar.setCricleColor(context.getResources().getColor(
				R.color.round_color));
		mRoundProgressBar.setTextColor(context.getResources().getColor(
				R.color.dialog_blue));
	}

	/**
	 * 暂停中
	 * 
	 * @param context
	 * @param holder
	 */
	private void pause(Context context, RoundProgressBar mRoundProgressBar,
			TextView tvProgress)
	{
		// 下载进度置为0。
		mRoundProgressBar.setProgress(0);
		// 更换下载图片为暂停图片。
		mRoundProgressBar.setBackground(context.getResources().getDrawable(
				R.drawable.download_pause));
		// 下载进度的百分比，字数，设置为透明
		mRoundProgressBar.setTextColor(context.getResources().getColor(
				R.color.touming));
		mRoundProgressBar.setCricleColor(context.getResources().getColor(
				R.color.touming));
		String mProgressText = tvProgress.getText().toString().trim();
		// 判断如果下载进度为正在连接，则显示为暂停状态。
		if (TextUtils.equals(mProgressText,
				context.getResources().getString(R.string.Connecting))
				|| TextUtils.equals(mProgressText, context.getResources()
						.getString(R.string.Waiting))
				|| TextUtils.equals(mProgressText, context.getResources()
						.getString(R.string.fail)))
		{
			// 等待，正在连接-->暂停
			tvProgress.setText(context.getResources().getString(
					R.string.Pauseing));
		}
	}

	/**
	 * 下载中
	 * 
	 * @param context
	 * @param holder
	 * @param o
	 */
	private void downloading(Context context,
			RoundProgressBar mRoundProgressBar, TextView tvProgress,
			DownloadInfo o)
	{
		if (o.getTaskState() == DownloadTaskManager.DOWNLOADING)
		{
			// DRMLog.i("下载中");
			// holder.mRoundProgressBar.setBackground(context.getResources()
			// .getDrawable(R.drawable.download_not));
			mRoundProgressBar.setBackground(null);
			mRoundProgressBar.setCricleColor(context.getResources().getColor(
					R.color.round_color));
			mRoundProgressBar.setTextColor(context.getResources().getColor(
					R.color.dialog_blue));
			mRoundProgressBar.setProgress(o.getProgress());
			StringBuffer sb = new StringBuffer();
			String mUpdateProgress = sb
					.append(FileUtils.convertStorage(o.getCurrentSize()))
					.append("/")
					.append(FileUtils.convertStorage(o.getTotalSize()))
					.toString();
			tvProgress.setText(mUpdateProgress);
		}
	}

	/**
	 * 下载完成，开始准备解析文件
	 * 
	 * @param context
	 * @param holder
	 */
	private void completeStartParser(Context context,
			RoundProgressBar mRoundProgressBar, TextView tvProgress)
	{
		tvProgress.setText(context.getString(R.string.Parsering));
		mRoundProgressBar.setProgress(100);
		mRoundProgressBar.setTextColor(context.getResources().getColor(
				R.color.dialog_blue));
		// holder.mRoundProgressBar.setBackground(context.getResources()
		// .getDrawable(R.drawable.download_not));
		mRoundProgressBar.setBackground(null);
		mRoundProgressBar.setCricleColor(context.getResources().getColor(
				R.color.round_color));
	}

	/**
	 * 下载异常，ftpPath shutdown
	 * 
	 * @param context
	 * @param holder
	 */
	private void downloadError(Context context,
			RoundProgressBar mRoundProgressBar, TextView tvProgress)
	{
		mRoundProgressBar.setProgress(0);
		mRoundProgressBar.setBackground(context.getResources().getDrawable(
				R.drawable.download_pause));
		mRoundProgressBar.setCricleColor(context.getResources().getColor(
				R.color.touming));
		tvProgress.setText(context.getResources().getString(R.string.fail));
		if (mRoundProgressBar.getVisibility() == View.GONE)
			mRoundProgressBar.setVisibility(View.VISIBLE);
		if (tvProgress.getVisibility() == View.GONE)
			tvProgress.setVisibility(View.VISIBLE);
	}

	/**
	 * 连接异常，一般服务端文件错误
	 * 
	 * @param context
	 * @param holder
	 */
	private void connectError(Context context,
			RoundProgressBar mRoundProgressBar, TextView tvProgress)
	{
		mRoundProgressBar.setProgress(0);
		mRoundProgressBar.setBackground(context.getResources().getDrawable(
				R.drawable.download_button));
		mRoundProgressBar.setCricleColor(context.getResources().getColor(
				R.color.touming));
		tvProgress.setText(context.getResources().getString(
				R.string.Connect_fail));
		if (mRoundProgressBar.getVisibility() == View.GONE)
			mRoundProgressBar.setVisibility(View.VISIBLE);
		if (tvProgress.getVisibility() == View.GONE)
			tvProgress.setVisibility(View.VISIBLE);
	}

	/**
	 * 解析专辑列表
	 * 
	 * @param context
	 * @param holder
	 * @param album
	 */
	private void analysisAlbum(Context context, TextView types, Album album)
	{
		String type = album.getCategory();
		String num = album.getItem_number();
		if (type != null)
		{
			switch (type) {
			case "MUSIC":
				types.setText(context.getString(R.string.music_count, num));
				break;
			case "BOOK":
				types.setText(context.getString(R.string.book_count, num));
				break;
			case "VIDEO":
				types.setText(context.getString(R.string.video_count, num));
				break;
			}
		}
	}

	/**
	 * （会议系统使用）根据publishDate发布时间，判断此专辑是否有更新
	 * 
	 * @param album
	 * @param DownloadInfo
	 * @param imgUpdate
	 * @param arrow
	 */
	private void checkMeetAlbumHasFresh(final Album album,
			final DownloadInfo o, final Button imgUpdate, ImageView arrow,
			final ImageView download_title)
	{
		// boolean isScaning = (Constant.LoginConfig.type ==
		// DrmPat.LOGIN_SCANING);
		// if (!isScaning)
		// return;

		// 产品发布的时间
		String publishDate = o.getPublishDate();
		// 解析后保存的发布时间
		String savePublishDate = album.getPublishDate();
		DRMLog.d("publishDate = " + publishDate);
		DRMLog.d("savePublishDate = " + savePublishDate);
		if (TextUtils.isEmpty(publishDate)) return;
		/*
		 * if (TextUtils.isEmpty(savePublishDate)) return;
		 */
		if (TextUtils.equals(publishDate, savePublishDate)) return;
		// 需要更新
		imgUpdate.setVisibility(View.VISIBLE);
		arrow.setVisibility(View.GONE);
		download_title.setVisibility(View.GONE);
		final String myProId = album.getMyproduct_id();
		imgUpdate.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				// 发送通知：1.删除此前下载的专辑，2.删除权限，3.重新下载
				int taskSize = DownloadMainActivity.sTaskIdSet.size();
				if (taskSize >= Constant.sTaskCount)
				{
					context.showToast(context
							.getString(
									R.string.please_waiting_n_update_download,
									taskSize));
					return;
				}
				EventBus.getDefault().post(new AlbumUpdateEvent(o, myProId));
				imgUpdate.setVisibility(View.GONE);
			}
		});
	}

}
