package cn.com.pyc.drm.utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import cn.com.pyc.drm.common.Constant;
import cn.com.pyc.drm.model.db.bean.Album;
import cn.com.pyc.drm.model.db.bean.AlbumContent;
import cn.com.pyc.drm.model.db.bean.Asset;
import cn.com.pyc.drm.model.db.bean.Bookmark;
import cn.com.pyc.drm.model.db.bean.Downdata;
import cn.com.pyc.drm.model.db.bean.Perconattribute;
import cn.com.pyc.drm.model.db.bean.Perconstraint;
import cn.com.pyc.drm.model.db.bean.Permission;
import cn.com.pyc.drm.model.db.bean.Right;
import cn.com.pyc.drm.model.db.practice.AlbumContentDAOImpl;
import cn.com.pyc.drm.model.db.practice.AlbumDAOImpl;
import cn.com.pyc.drm.model.db.practice.AssetDAOImpl;
import cn.com.pyc.drm.model.db.practice.BookmarkDAOImpl;
import cn.com.pyc.drm.model.db.practice.DowndataDAOImpl;
import cn.com.pyc.drm.model.db.practice.PerconattributeDAOImpl;
import cn.com.pyc.drm.model.db.practice.PerconstraintDAOImpl;
import cn.com.pyc.drm.model.db.practice.PermissionDAOImpl;
import cn.com.pyc.drm.model.db.practice.RightDAOImpl;
import cn.com.pyc.drm.model.xml.OEX_Agreement.OEX_Asset;
import cn.com.pyc.drm.model.xml.OEX_Agreement.OEX_Permission;
import cn.com.pyc.drm.model.xml.OEX_Rights;
import cn.com.pyc.drm.model.xml.XML2JSON_Album;
import cn.com.pyc.drm.utils.manager.SaveIndexDBManager;
import cn.com.pyc.drm.utils.manager.ScanHistoryDBManager;

public class DRMDBHelper
{

	private String TAG = DRMDBHelper.class.getSimpleName();

	private Context mContext;

	private DBHelper helper;

	/**
	 * 实例化数据DRMDBHelper,Context
	 * 
	 * @param context
	 */
	public DRMDBHelper(Context context)
	{
		mContext = context;
		helper = DBHelper.getInstance(mContext, Constant.getUserName());
	}

	/**
	 * 插入整个DMR文件所对应的内容
	 * 
	 * @param rights
	 *            权限
	 * @param albumInfo
	 * @param myProId
	 * @param author
	 * @param picture_ratio
	 * @param publishDate
	 * @return
	 */
	public boolean insertDRMData(OEX_Rights rights, XML2JSON_Album albumInfo, String myProid, String author, String picture_ratio, String publishDate)
	{
		String Album_Id = AlbumDAOImpl.getInstance().findAlbumId(myProid);// 获取专辑ID
		if (Album_Id == null && DRMUtil.isInsertDRMData)
		{
			try
			{
				DRMUtil.isInsertDRMData = false;
				Right right = new Right();
				long currentTime = System.currentTimeMillis();
				right.setId(String.valueOf(currentTime));
				right.setPro_album_id("0");
				right.setRight_uid(rights.getContextMap().get("uid"));
				right.setRight_version(rights.getContextMap().get("version"));
				SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss",Locale.getDefault());
				String date = sDateFormat.format(new java.util.Date());
				right.setCreate_time(date);
				right.setAccount_id("1");
				right.setUsername(Constant.getUserName());
				// 4、 插入asset内容
				List<OEX_Asset> rightAssets = rights.getAgreement().getAssets();
				LinkedList<Asset> assets = new LinkedList<Asset>();
				for (int i = 0, count = rightAssets.size(); i < count; i++)
				{
					Asset asset = new Asset();
					asset.setId(String.valueOf(currentTime + i));
					asset.setAsset_uid(rightAssets.get(i).getOdd_uid());
					asset.setRight_id(right.getId());
					asset.setCek_cipher_value(rightAssets.get(i).getCipheralue());
					asset.setCek_encrypt_method(rightAssets.get(i).getEnc_algorithm());
					asset.setCek_retrieval_key(rightAssets.get(i).getRetrieval_url());
					asset.setDigest_method(rightAssets.get(i).getDigest_algorithm_key());
					asset.setDigest_value(rightAssets.get(i).getDigest_algorithm_value());
					asset.setCreate_time(date);
					asset.setRight_version(right.getRight_version());
					asset.setUsername(right.getUsername());
					assets.add(asset);
				}
				int n = 0;
				// 3、 插入权限表及约束内容
				List<OEX_Permission> rightPermissions = rights.getAgreement().getPermission();
				for (int i = 0, count = rightPermissions.size(); i < count; i++)
				{
					Permission permission = new Permission();
					permission.setId(String.valueOf(currentTime + i));
					int assetId = Integer.parseInt(rightPermissions.get(i).getAssent_id().substring(5)) - 1;
					String assentId = assets.get(assetId).getId();
					permission.setAsset_id(assentId);
					permission.setCreate_time(date);
					permission.setElement(String.valueOf(rightPermissions.get(i).getType()));
					List<Map<String, String>> attributes = rightPermissions.get(i).getAttributes();
					for (Map<String, String> map : attributes)
					{
						for (Map.Entry<String, String> entry : map.entrySet())
						{
							n++;
							Perconstraint perconstraint = new Perconstraint();
							perconstraint.setId(String.valueOf(currentTime + n));
							perconstraint.setElement(entry.getKey());
							if (entry.getKey().equals("datetime"))
							{
								Perconattribute startPerconattribute = new Perconattribute();
								Perconattribute endPerconattribute = new Perconattribute();
								String start = rightPermissions.get(i).getStartTime();
								String end = rightPermissions.get(i).getEndTime();
								startPerconattribute.setElement("start");
								startPerconattribute.setValue(start);
								startPerconattribute.setCreate_time(date);
								startPerconattribute.setPerconstraint_id(perconstraint.getId());
								endPerconattribute.setElement("end");
								endPerconattribute.setValue(end);
								endPerconattribute.setCreate_time(date);
								endPerconattribute.setPerconstraint_id(perconstraint.getId());
								perconstraint.addPerconattributes(startPerconattribute);
								perconstraint.addPerconattributes(endPerconattribute);
							}
							perconstraint.setPermission_id(permission.getId());
							perconstraint.setValue(entry.getValue());
							perconstraint.setCreate_time(date);
							permission.addPerconstraint(perconstraint);
						}
					}
					assets.get(assetId).addPermission(permission);
				}
				right.setAssets(assets);
				new RightDAOImpl().cascadedSave(right);

				// jsonObj变成局部变量
				JSONObject albObject = albumInfo.getInfoObj();
				Album album = new Album();
				album.setId(String.valueOf(currentTime));
				album.setName(albObject.getString("albumName"));
				album.setRight_id(albObject.getString("rid"));
				album.setProduct_id(albObject.getString("albumId"));
				album.setModify_time(date);
				album.setCategory(albObject.getString("albumCategory"));
				album.setItem_number(String.valueOf(albumInfo.getContentList().size() / 2));
				album.setUsername(Constant.getUserName());
				album.setPicture(albObject.getString("picture"));
				album.setMyproduct_id(myProid);
				album.setAuthor(author == null ? "" : author);
				album.setPicture_ratio(picture_ratio == null ? "1" : picture_ratio);
				album.setPublishDate(publishDate == null ? "" : publishDate);
				List<String> contents = albumInfo.getContentList();
				for (int i = 0, count = contents.size(); i < count; i += 2)
				{
					AlbumContent albumContent = new AlbumContent();
					for (int j = 0; j < assets.size(); j++)
					{
						String Content_id = contents.get(i).replace("\"", "");
						String odduid = assets.get(j).getAsset_uid();
						if (Content_id.equals(odduid))
						{
							albumContent.setAlbum_id(album.getId());
							albumContent.setModify_time(date);
							albumContent.setName(contents.get(i + 1));
							albumContent.setAsset_id(assets.get(j).getId());
							albumContent.setContent_id(contents.get(i));
							album.addAlbumContent(albumContent);
						}
					}
				}
				DRMLog.e("insert-album", "album: " + album.toString());
				new AlbumDAOImpl().cascadedSave(album);
				DRMUtil.isInsertDRMData = true;
			} catch (Exception e)
			{
				// DRMUtil.isInsertDRMData = true;
				e.printStackTrace();
			}
		}
		return true;
	}

	/**
	 * 判断权限类型,并插入到权限详情表中
	 * 
	 * 用于表示用户是否具有该权限。1表示有，0表示没有
	 */
	private String checkPermission()
	{

		return "1";
	}

	/**
	 * 方法：检查某表列是否存在
	 * 
	 * @param tableName
	 *            表名
	 * @param columnName
	 *            列名
	 * @return
	 */
	public boolean checkColumnExist(String tableName, String columnName)
	{
		boolean result = false;
		Cursor cursor = null;
		try
		{
			// 查询一行
			SQLiteDatabase db = helper.getDB();
			cursor = db.rawQuery("SELECT * FROM " + tableName + " LIMIT 0", null);
			result = (cursor != null && cursor.getColumnIndex(columnName) != -1);
		} catch (Exception e)
		{
			e.printStackTrace();
		} finally
		{
			if (null != cursor && !cursor.isClosed())
			{
				cursor.close();
			}
		}

		return result;
	}

	/**
	 * 创建并且检查Album表,看新增加的字段是否存在，不存在就增加
	 */
	public void checkAlbumTable()
	{
		AlbumDAOImpl.getInstance().create(Album.class);
		// 检查表字段
		boolean authorExist = checkColumnExist(Album.class.getSimpleName(), "author");
		boolean ratioExist = checkColumnExist(Album.class.getSimpleName(), "picture_ratio");
		boolean dateExist = checkColumnExist(Album.class.getSimpleName(), "publishDate");
		DRMLog.e(TAG, "authorExist = " + authorExist);
		DRMLog.e(TAG, "ratioExist = " + ratioExist);
		DRMLog.e(TAG, "publishDateExist = " + dateExist);
		try
		{
			if (!authorExist)
			{
				// 增加author字段
				helper.ExecSQL("ALTER TABLE " + Album.class.getSimpleName() + " ADD COLUMN author");
			}
			if (!ratioExist)
			{
				// 增加picture_ratio字段
				helper.ExecSQL("ALTER TABLE " + Album.class.getSimpleName() + " ADD COLUMN picture_ratio");
			}
			if (!dateExist)
			{
				// 增加publishDate字段
				helper.ExecSQL("ALTER TABLE " + Album.class.getSimpleName() + " ADD COLUMN publishDate");
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 检查下载进度记录表(创建并且检查Downdata表,看新增加的字段是否存在，不存在就增加)
	 */
	public void checkDowndataTable()
	{
		DowndataDAOImpl.getInstance().create(Downdata.class);
		boolean result = checkColumnExist(Downdata.class.getSimpleName(), "completeSize");
		DRMLog.e(TAG, "completeSizeExist = " + result);
		if (!result)
		{
			// 删除，重新建表
			FileUtils.delAllFile(DRMUtil.DEFAULT_SAVE_FILE_DOWNLOAD_PATH + File.separator);
			DowndataDAOImpl.getInstance().DeleteTableData(Downdata.class.getSimpleName());
			DowndataDAOImpl.getInstance().create(Downdata.class);
		}
	}

	/**
	 * 创建数据表
	 */
	public boolean createDBTable()
	{
		checkAlbumTable();

		checkDowndataTable();

		RightDAOImpl rightDAOImpl = new RightDAOImpl();
		rightDAOImpl.create(Right.class);

		AssetDAOImpl assetDAOImpl = new AssetDAOImpl();
		assetDAOImpl.create(Asset.class);

		PermissionDAOImpl permissionDAOImpl = new PermissionDAOImpl();
		permissionDAOImpl.create(Permission.class);

		PerconstraintDAOImpl perconstraintDAOImpl = new PerconstraintDAOImpl();
		perconstraintDAOImpl.create(Perconstraint.class);

		PerconattributeDAOImpl perconattributeDAOImpl = new PerconattributeDAOImpl();
		perconattributeDAOImpl.create(Perconattribute.class);

		AlbumContentDAOImpl.getInstance().create(AlbumContent.class);
		
		BookmarkDAOImpl.getInstance().create(Bookmark.class);
		
		AlbumDAOImpl.getInstance().create(Album.class);

		
		return true;
	}

	// ////////////////////////////////////////////
	// ////////////////////////////////////////////
	// ///////////以下是static静态方法////////////////////
	// ////////////////////////////////////////////
	// ////////////////////////////////////////////
	/**
	 * 销毁数据库实例,db和helper
	 */
	public static void destoryDBHelper()
	{
		DBHelper.setMdbHelperNULL();
	}

	/**
	 * 清除数据表
	 */
	public static void deleteTableData()
	{
		DowndataDAOImpl.getInstance().DeleteTableData(Downdata.class.getSimpleName());
		AlbumDAOImpl.getInstance().DeleteTableData(Perconattribute.class.getSimpleName());
		AlbumDAOImpl.getInstance().DeleteTableData(Perconstraint.class.getSimpleName());
		AlbumDAOImpl.getInstance().DeleteTableData(Permission.class.getSimpleName());
		AlbumDAOImpl.getInstance().DeleteTableData(Asset.class.getSimpleName());
		AlbumDAOImpl.getInstance().DeleteTableData(AlbumContent.class.getSimpleName());
		AlbumDAOImpl.getInstance().DeleteTableData(Album.class.getSimpleName());
		AlbumDAOImpl.getInstance().DeleteTableData(Right.class.getSimpleName());
		AlbumDAOImpl.getInstance().DeleteTableData("sqlite_sequence");
		BookmarkDAOImpl.getInstance().DeleteTableData(Bookmark.class.getSimpleName());
		// AlbumDAOImpl.getInstance().DeleteTableData("account");
		// AlbumDAOImpl.getInstance().DeleteTableData("albuminfo");
		// AlbumDAOImpl.getInstance().DeleteTableData("content");
	}

	/**
	 * 删除专辑的相关内容（文件数据，权限，书签，当前阅读索引） <br/>
	 * 更新专辑时操作
	 * 
	 * @param mContext
	 * @param myProId
	 */
	public static void deleteAlbumAttachInfos(Context mContext, String myProId)
	{
		// 当前阅读索引重新计数
		MediaUtils.MPDF_CURRENTPOS = -1;
		SaveIndexDBManager.Builder(mContext).deleteByMyProId(myProId);
		// 1.删除专辑album
		AlbumDAOImpl.getInstance().deleteAlbumByMyProId(myProId);

		// 获取专辑ID
		String album_Id = AlbumDAOImpl.getInstance().findAlbumId(myProId);
		if (album_Id == null)
			return;

		String albumContentId = AlbumDAOImpl.getInstance().findAlbumContentId(album_Id);
		if (albumContentId != null)
		{
			// 2.删除AlbumContent
			AlbumDAOImpl.getInstance().DeleteAlbumContent(album_Id);
		}

		String RightContentId = AlbumDAOImpl.getInstance().findRightId(album_Id);
		if (RightContentId != null)
		{
			// 3.删除Right
			AlbumDAOImpl.getInstance().DeleteRight(album_Id);
		}
		List<String> assetIdList = AlbumDAOImpl.getInstance().findAssetId(album_Id);
		if (assetIdList != null && !assetIdList.isEmpty())
		{
			// 4.删除Asset
			AlbumDAOImpl.getInstance().DeleteAsset(album_Id);

			for (int i = 0; i < assetIdList.size(); i++)
			{
				String assetId = assetIdList.get(i);
				// 5.删除书签
				BookmarkDAOImpl.getInstance().DeleteBookMark(assetId);
				String PermissionId = AlbumDAOImpl.getInstance().findPermissionId(assetId);
				if (PermissionId != null)
				{
					// 6.删除Permission
					AlbumDAOImpl.getInstance().DeletePermission(assetId);
					ArrayList<String> perconstraintIdList = AlbumDAOImpl.getInstance().findPerconstraintId(PermissionId);
					if (perconstraintIdList != null)
					{
						// 7.删除Perconstraint
						AlbumDAOImpl.getInstance().DeletePerconstraint(PermissionId);
					}
				}
			}
		}
	}
	
	
	/**
	* @Description: (创建数据库) 
	* @author 李巷阳
	* @date 2016-9-8 上午11:03:01
	 */
	public static void setCreateDatabase(Context cxt) {
		DRMDBHelper helper = new DRMDBHelper(cxt);
		helper.checkAlbumTable();
		helper.checkDowndataTable();
		ScanHistoryDBManager ScanHistoryhelper = ScanHistoryDBManager.Builder(cxt);
		ScanHistoryhelper.checkScanHistoryTable();
	}

}
