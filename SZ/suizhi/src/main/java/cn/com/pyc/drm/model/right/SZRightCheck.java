package cn.com.pyc.drm.model.right;

import cn.com.pyc.drm.utils.DRMLog;

class SZRightCheck {
//    // 用户账户权限
//    private boolean individualPermission;
//    // 设备权限
//    private boolean systemPermission;
//    // 时间权限
//    private boolean datePermission;
//    // 累计时间权限
//    private boolean accumulatedPermission;

    //当前时间
    //private long currentTime = System.currentTimeMillis();

    /**
     * 检查权限 <br/>
     * <p>
     * 1.检查用户权限 <br/>
     * 2.检查设备权限 <br/>
     * 3.检查时间权限 <br/>
     *
     * @param aPermission SZPermissionCommon
     * @return Boolean
     */
    boolean checkWithPermission(SZPermissionCommon aPermission) {
        if (aPermission == null) {
            return false;
        }

        // 用户账户权限,设备权限,时间权限,累计时间权限
        boolean individualPermission, systemPermission, datePermission, accumulatedPermission;
        DRMLog.d("individual:" + aPermission.odd_individual);
        // 验证用户账户权限
        individualPermission = true; //去掉，update20170705
//        if (Constant.getName().equals(aPermission.odd_individual)) {
//            individualPermission = true;
//        } else {
//            individualPermission = false;
//            DRMLog.e("individual is not userName");
//        }
        // 验证设备权限
        systemPermission = true;

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

    /**
     * 检查权限是否生效
     * <p>
     * 主要针对权限时间还未到的情况
     *
     * @param aPermission SZPermissionCommon
     * @return Boolean
     */
    boolean checkFileIneffective(SZPermissionCommon aPermission) {
        if (aPermission == null) {
            return false;
        }
        long currentTime = System.currentTimeMillis();
        return aPermission.odd_datetime_start > currentTime;
    }

    /**
     * 检验本地存储标记的权限标志
     *
     * @param aPermission SZPermissionCommon
     * @return Boolean
     */
    boolean isExpired(SZPermissionCommon aPermission) {
        return aPermission != null && aPermission.isExpired();
    }
}
