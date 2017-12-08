package cn.com.pyc.drm.model.right;

import cn.com.pyc.drm.common.Constant;
import cn.com.pyc.drm.common.DrmPat;
import cn.com.pyc.drm.utils.DRMLog;

public class SZCheckRight
{

	// 用户账户权限
	private Boolean individualPermission;
	// 设备权限
	private Boolean systemPermission;
	// 时间权限
	private Boolean datePermission;
	// 累计时间权限
	private Boolean accumulatedPermission;

	public Boolean checkWithPermission(SZPermissionCommon aPermission)
	{
		if (aPermission == null)
		{
			DRMLog.e("", "permission reference is null");
			return false;
		}

		// 验证用户账户权限
		DRMLog.d("individual:" + aPermission.odd_individual);
		/**** 10-31 update. ****/
//		if (Constant.LoginConfig.type == DrmPat.LOGIN_SCANING)
//		{
			// 会议扫码，直接放行，不判断用户名
			DRMLog.i("扫码登录");
			individualPermission = true;
//		} 
//		else
//		{
//			if (Constant.getUserName().equals(aPermission.odd_individual))
//			{
//				individualPermission = true;
//			} else
//			{
//				individualPermission = false;
//				DRMLog.e("", "individual is not name");
//			}
//		}
		// if (Constant.getUserName().equals(aPermission.odd_individual))
		// {
		// individualPermission = true;
		// } else
		// {
		// individualPermission = false;
		// DRMLog.e("", "individual is not name");
		// }
		/**** 10-31 update. ****/

		// 验证设备权限
		systemPermission = true;
		// if(aPermission.odd_system.contains(AppContext.IMEI))
		// systemPermission = true;
		// else
		// systemPermission = false;

		// 验证时间权限
		if (aPermission.isAllPermission())
		{
			datePermission = true;
			accumulatedPermission = true;
		} else
		{
			double currentTime = System.currentTimeMillis();
			if (aPermission.odd_datetime_end > aPermission.odd_datetime_start && aPermission.odd_datetime_start < currentTime
					&& aPermission.odd_datetime_end > currentTime)
			{
				datePermission = true;
			} else
			{
				datePermission = false;
			}
			// 验证累加时间权限
			if (aPermission.odd_accumulated <= aPermission.odd_datetime)
			{
				accumulatedPermission = true;
			} else
			{
				accumulatedPermission = false;
			}
		}
		return individualPermission && systemPermission && datePermission && accumulatedPermission;
	}
}
