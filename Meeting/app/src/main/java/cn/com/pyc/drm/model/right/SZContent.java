package cn.com.pyc.drm.model.right;

import java.math.BigDecimal;
import java.util.List;

import cn.com.pyc.drm.model.db.bean.Asset;
import cn.com.pyc.drm.model.db.bean.Permission;
import cn.com.pyc.drm.model.db.practice.AssetDAOImpl;
import cn.com.pyc.drm.utils.DRMLog;
import cn.com.pyc.drm.utils.TimeUtil;

public class SZContent
{
	static String kConstraintDatetime = "datetime";

	static String kConstraintAccumulated = "accumulated";

	static String kConstraintIndividual = "individual";

	static String kConstraintSystem = "system";

	static String kPermissionPlay = "PLAY";

	static String kPermissionDisplay = "DISPLAY";

	private AssetDAOImpl assetDAO = new AssetDAOImpl();

	private SZPermissionDisplay permissionDisplay = null;
	public SZPermissionPlay permissionPlay = null;

	@SuppressWarnings("unchecked")
	public SZContent(String assetID)
	{
		Asset asset = assetDAO.findObjectByQuery(new String[] { "_id" }, new String[] { assetID }, Asset.class);
		asset.setPermissions((List<Permission>) assetDAO.findByQuery(new String[] { "asset_id" }, new String[] { assetID }, Permission.class));
		for (int i = 0; i < asset.getPermissions().size(); i++)
		{
			Permission permission = asset.getPermissions().get(i);
			DRMLog.d("SZContent:" + permission.getElement());
			if (kPermissionPlay.equals(permission.getElement()))
			{
				permissionPlay = new SZPermissionPlay();
				permissionPlay.initWithPermission(permission);
			} else if (kPermissionDisplay.equals(permission.getElement()))
			{
				permissionDisplay = new SZPermissionDisplay();
				permissionDisplay.initWithPermission(permission);
			}
		}
	}

	/**
	 * 验权
	 * 
	 * @return
	 */
	public Boolean checkOpen()
	{
		SZCheckRight rightCheck = new SZCheckRight();
		return rightCheck.checkWithPermission(permissionPlay) || rightCheck.checkWithPermission(permissionDisplay);
	}

	public double getOdd_datetime_end()
	{
		if (permissionPlay != null)
		{
			return this.permissionPlay.odd_datetime_end;
		} else if (permissionDisplay != null)
		{
			return this.permissionDisplay.odd_datetime_end;
		}
		return 0.0;
	}
	
	// 获取是否到生效时间
	public boolean getIsEffectivetime(){
		long currentTime = System.currentTimeMillis();
		if (permissionPlay != null)
		{
			long availableTime = (long) (currentTime - this.permissionPlay.odd_datetime_start);
			return availableTime > 0 ? true : false;

		} else if (permissionDisplay != null)
		{
			long availableTime = (long) (currentTime - this.permissionDisplay.odd_datetime_start);

			return availableTime > 0 ? true : false;
		}
		return true;
		
	}
	
	
	
	// 获取生效时间
	public double getOdd_datetime_start()
	{
		if (permissionPlay != null)
		{
			return this.permissionPlay.odd_datetime_start;
		} else if (permissionDisplay != null)
		{
			return this.permissionDisplay.odd_datetime_start;
		}
		return 0.0;
	}

	// 获取剩余时间
	public long getAvailbaleTime()
	{
		long currentTime = System.currentTimeMillis();
		if (this.permissionDisplay != null)
		{
			if (this.permissionDisplay.isAllPermission() )
				return -1;
			else
			{
				long availableTime = (long) (this.permissionDisplay.odd_datetime_end - currentTime);
				availableTime = availableTime > 0 ? availableTime : 0;
				return availableTime;
			}
		} else if (this.permissionPlay != null)
		{
			if (this.permissionPlay.isAllPermission())
				return -1;
			else
			{
				long availableTime = (long) (this.permissionPlay.odd_datetime_end - currentTime);
				availableTime = availableTime > 0 ? availableTime : 0;
				return availableTime;
			}
		}
		return 0;
	}

}
