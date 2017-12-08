package cn.com.pyc.szpbb.sdk.authentication;

import cn.com.pyc.szpbb.common.Fields;
import cn.com.pyc.szpbb.util.SZLog;

class SZCheckRight {
    // 用户账户权限
    private boolean individualPermission;
    // 设备权限
    private boolean systemPermission;
    // 时间权限
    private boolean datePermission;
    // 累计时间权限
    private boolean accumulatedPermission;

    /**
     * 检查权限 <br/>
     * <p>
     * 1.检查用户权限 <br/>
     * 2.检查设备权限 <br/>
     * 3.检查时间权限 <br/>
     *
     * @param aPermission
     * @return
     */
    boolean checkWithPermission(SZPermissionCommon aPermission) {
        if (aPermission == null) {
            return false;
        }

        /**** 11-25 update. ****/
        // if (Constant.LoginConfig.type == DrmPat.LOGIN_SCANING)
        // {
        // // 会议扫码，直接放行，不判断用户名
        // DRMLog.i("扫码登录");
        // individualPermission = true;
        // } else
        // {
        // if (Constant.getUserName().equals(aPermission.odd_individual))
        // {
        // individualPermission = true;
        // } else
        // {
        // individualPermission = false;
        // DRMLog.e("", "individual is not login name");
        // }
        // }
        // 验证用户账户权限，默认guestpbb
        SZLog.v("individual-name：" + aPermission.odd_individual);
        if (Fields.INDIVIDUAL.equals(aPermission.odd_individual)) {
            individualPermission = true;
        } else {
            individualPermission = false;
        }
        /**** 11-25 update. ****/

        // 验证设备权限
        systemPermission = true;
        // if(aPermission.odd_system.contains(AppContext.IMEI))
        // systemPermission = true;
        // else
        // systemPermission = false;

        // 验证时间权限
        if (aPermission.isAllPermission()) {
            datePermission = true;
            accumulatedPermission = true;
        } else {
            long currentTime = System.currentTimeMillis();
            if (aPermission.odd_datetime_end > aPermission.odd_datetime_start
                    && aPermission.odd_datetime_start < currentTime
                    && aPermission.odd_datetime_end > currentTime) {
                datePermission = true;
            } else {
                datePermission = false;
            }
            // 验证累加时间权限
            if (aPermission.odd_accumulated <= aPermission.odd_datetime) {
                accumulatedPermission = true;
            } else {
                accumulatedPermission = false;
            }
        }
        return individualPermission && systemPermission && datePermission && accumulatedPermission;
    }
}
