package cn.com.pyc.drm.utils;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import cn.com.pyc.drm.common.ConstantValue;
import cn.com.pyc.drm.model.db.bean.AlbumContent;
import cn.com.pyc.drm.model.db.bean.Asset;
import cn.com.pyc.drm.model.db.practice.AlbumContentDAOImpl;
import cn.com.pyc.drm.model.db.practice.AssetDAOImpl;
import cn.com.pyc.drm.model.right.ContentRight;
import cn.com.pyc.drm.model.right.SZContent;

public class MediaUtils
{

	private String TAG = "MediaUtils";

	// music,video
	private List<ContentRight> mediaRight = new ArrayList<ContentRight>();
	private List<AlbumContent> mediaList = new ArrayList<AlbumContent>();
	private List<Asset> assetList = new ArrayList<Asset>();

	// pdf
	private List<ContentRight> pdfmediaRight = new ArrayList<ContentRight>();
	private List<AlbumContent> pdfmediaList = new ArrayList<AlbumContent>();
	private List<Asset> pdfassetList = new ArrayList<Asset>();

	// 默认状态
	public static int playState = ConstantValue.OPTION_STOP;
	// 播放模式，默认列表循环
	public static int playMode = ConstantValue.CIRCLE;

	public static String current_MusicAlbum_Id = "";

	public final static String NOTIFY_BUTTONID_TAG = "ButtonId";
	/** 上一首 按钮点击 ID */
	public final static int BUTTON_PREV_ID = 1;
	/** 播放/暂停 按钮点击 ID */
	public final static int BUTTON_PALY_ID = 2;
	/** 下一首 按钮点击 ID */
	public final static int BUTTON_NEXT_ID = 3;
	/** 关闭按钮 */
	public final static int BUTTON_CLOSE_ID = 4;

	// 0, 记录当前播放的位置
	public static int MUSIC_CURRENTPOS = -1;
	// pdf当前播放的位置
	public static int MPDF_CURRENTPOS = -1;

	// public static boolean b = true;

	private static MediaUtils instance = new MediaUtils();

	public static MediaUtils getInstance()
	{
		return instance;
	}

	/**
	 * 音乐、视频
	 * 
	 * @param context
	 * @param albumID
	 */
	@SuppressWarnings("unchecked")
	public void initMedia(Context context, String albumID)
	{
		mediaList.clear();// 子专辑的集合。
		mediaRight.clear();// 子专辑权限集合。
		assetList.clear();// 子专辑解密集合。
		// AlbumContentDAOImpl contentDAOImpl = new AlbumContentDAOImpl();
		String[] pid = { "album_id" };
		String[] pidvalue = { albumID };
		mediaList = (List<AlbumContent>) AlbumContentDAOImpl.getInstance().findByQuery(pid, pidvalue, AlbumContent.class);
		// mediaList = (List<AlbumContent>) contentDAOImpl.findByQuery(new
		// String[]{"album_id"}, new String[]{albumID}, AlbumContent.class);
		for (int i = 0; i < mediaList.size(); i++)
		{
			String[] pid1 = { "_id" };
			String[] pidvalue1 = { mediaList.get(i).getAsset_id() };
			Asset tmp = (Asset) AssetDAOImpl.getInstance().findByQuery(pid1, pidvalue1, Asset.class).get(0);
			// Asset tmp = ((List<Asset>)contentDAOImpl.findByQuery(new
			// String[]{"_id"}, new String[]{mediaList.get(i).getAsset_id()},
			// Asset.class)).get(0);
			assetList.add(tmp);
			SZContent szcont = new SZContent(mediaList.get(i).getAsset_id());
			ContentRight right = new ContentRight();
			right.order = i;// 子专辑的标识
			right.permitted = szcont.checkOpen();// 是否鉴权
			right.availableTime = szcont.getAvailbaleTime();// 获取剩余时间
			right.odd_datetime_end = szcont.getOdd_datetime_end();// 获取结束时间
			right.odd_datetime_start = szcont.getOdd_datetime_start();// 获取开始时间
			right.IsEffectivetime = szcont.getIsEffectivetime();// 获取是否需要到生效时间
			mediaRight.add(right);

			DRMLog.d(TAG, "availableTime: " + right.availableTime);
			DRMLog.d(TAG, "permitted: " + right.permitted);
		}
	}

	/**
	 * pdf
	 * 
	 * @param context
	 * @param albumID
	 */
	@SuppressWarnings("unchecked")
	public void initPdf(Context context, String albumID)
	{
		pdfmediaList.clear();// 子专辑的集合。
		pdfmediaRight.clear();// 子专辑权限集合。
		pdfassetList.clear();// 子专辑解密集合。
		String[] pid = { "album_id" };
		String[] pidvalue = { albumID };
		pdfmediaList = (List<AlbumContent>) AlbumContentDAOImpl.getInstance().findByQuery(pid, pidvalue, AlbumContent.class);
		// AlbumContentDAOImpl contentDAOImpl = new AlbumContentDAOImpl();
		// pdfmediaList = (List<AlbumContent>) contentDAOImpl.findByQuery(new
		// String[]{"album_id"}, new String[]{albumID}, AlbumContent.class);
		for (int i = 0; i < pdfmediaList.size(); i++)
		{
			String[] pid1 = { "_id" };
			String[] pidvalue1 = { pdfmediaList.get(i).getAsset_id() };
			Asset tmp = (Asset) AssetDAOImpl.getInstance().findByQuery(pid1, pidvalue1, Asset.class).get(0);
			// Asset tmp = ((List<Asset>)contentDAOImpl.findByQuery(new
			// String[]{"_id"}, new String[]{pdfmediaList.get(i).getAsset_id()},
			// Asset.class)).get(0);
			pdfassetList.add(tmp);
			SZContent szcont = new SZContent(pdfmediaList.get(i).getAsset_id());
			ContentRight right = new ContentRight();
			right.order = i;// 子专辑的标识
			right.permitted = szcont.checkOpen();// 是否鉴权
			right.availableTime = szcont.getAvailbaleTime();// 获取剩余时间
			right.odd_datetime_end = szcont.getOdd_datetime_end();// 获取结束时间
			right.odd_datetime_start = szcont.getOdd_datetime_start();// 获取开始时间
			right.IsEffectivetime = szcont.getIsEffectivetime();// 获取是否需要到生效时间
			
			pdfmediaRight.add(right);

			DRMLog.d(TAG, "availableTime: " + right.availableTime);
			DRMLog.d(TAG, "permitted: " + right.permitted);
		}
	}

	public List<AlbumContent> getMediaList()
	{
		return mediaList;
	}

	public List<ContentRight> getMediaRight()
	{
		return mediaRight;
	}

	public List<Asset> getAssetList()
	{
		return assetList;
	}

	public void setAssetList(List<Asset> assetList)
	{
		this.assetList = assetList;
	}

	public void addMusicRight(ContentRight right)
	{
		this.mediaRight.add(right);
	}

	// ////////////////////////////////////////////

	public List<ContentRight> getPdfmediaRight()
	{
		return pdfmediaRight;
	}

	public void setPdfmediaRight(List<ContentRight> pdfmediaRight)
	{
		this.pdfmediaRight = pdfmediaRight;
	}

	public List<AlbumContent> getPdfmediaList()
	{
		return pdfmediaList;
	}

	public void setPdfmediaList(List<AlbumContent> pdfmediaList)
	{
		this.pdfmediaList = pdfmediaList;
	}

	public List<Asset> getPdfassetList()
	{
		return pdfassetList;
	}

	public void setPdfassetList(List<Asset> pdfassetList)
	{
		this.pdfassetList = pdfassetList;
	}

	public String durationToString(String duration)
	{

		String reVal = "";
		int j = Integer.valueOf(duration);
		int i = j / 1000;
		int min = (int) i / 60;
		int sec = i % 60;
		if (min > 9)
		{
			if (sec > 9)
			{
				reVal = min + ":" + sec;
			}
			if (sec <= 9)
			{
				reVal = min + ":0" + sec;
			}
		} else
		{
			if (sec > 9)
			{
				reVal = "0" + min + ":" + sec;
			}
			if (sec <= 9)
			{
				reVal = "0" + min + ":0" + sec;
			}
		}
		return reVal;
	}

	public String getAvailbaleTimeToString(long seconds)
	{
		int temp = 0;
		StringBuffer sb = new StringBuffer("有效时间:");
		if (seconds == -1)
		{
			sb.append("永久权限");
			return sb.toString();
		}
		temp = (int) (seconds / (60 * 60 * 24 * 1000));
		sb.append((temp < 10) ? "0" + temp + "天" : "" + temp + "天");
		temp = (int) (seconds % (60 * 60 * 24 * 1000) / (60 * 60 * 1000));
		sb.append((temp < 10) ? "0" + temp + "小时" : "" + temp + "小时");
		return sb.toString();

	}
}