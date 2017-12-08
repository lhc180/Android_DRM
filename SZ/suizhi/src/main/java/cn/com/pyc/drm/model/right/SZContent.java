package cn.com.pyc.drm.model.right;

import java.util.List;

import cn.com.pyc.drm.common.SZConfig;
import cn.com.pyc.drm.model.db.bean.Asset;
import cn.com.pyc.drm.model.db.bean.Permission;
import cn.com.pyc.drm.model.db.practice.AssetDAOImpl;
import cn.com.pyc.drm.model.db.practice.PermissionDAOImpl;
import cn.com.pyc.drm.utils.AESUtil;
import cn.com.pyc.drm.utils.DRMLog;

/**
 * 权限获取和显示
 */
public class SZContent {
    //static final String kConstraintDatetime = "datetime";
    //static final String kConstraintAccumulated = "accumulated";
    //static final String kConstraintIndividual = "individual";
    //static final String kConstraintSystem = "system";

    //OEX_Agreement: DISPLAY, PLAY, PRINT, EXECUTE, EXPORT
    private static final String kPermissionPlay = "PLAY";
    private static final String kPermissionDisplay = "DISPLAY";
    private static final String kPermissionPrint = "PRINT";

    private String cek_Cipher_Value;

    private SZPermissionDisplay permissionDisplay = null;
    private SZPermissionPlay permissionPlay = null;
    private SZPermissionPrint permissionPrint = null; //移动端没有打印权限，应该忽略
    private SZRightCheck rightCheck;

    @SuppressWarnings("unchecked")
    public SZContent(String assetId) {
        Asset asset = AssetDAOImpl.getInstance().findObjectByQuery(new String[]{"_id"},
                new String[]{assetId}, Asset.class);
        if (asset != null) {
            List<Permission> permissions = (List<Permission>) PermissionDAOImpl.getInstance()
                    .findByQuery(new String[]{"asset_id"}, new String[]{assetId}, Permission.class);
            asset.setPermissions(permissions);

            String aesKey = asset.getCek_cipher_value();
            DRMLog.e("a: " + aesKey);
            cek_Cipher_Value = (aesKey.length() == 32) ? aesKey : AESUtil.decrypt(aesKey,
                    SZConfig.FILE_KEY_SECRET);
            DRMLog.e("d: " + cek_Cipher_Value);

            int count = permissions.size();
            for (int i = 0; i < count; i++) {
                Permission permission = permissions.get(i);
                if (kPermissionPlay.equals(permission.getElement())) {
                    permissionPlay = new SZPermissionPlay();
                    permissionPlay.initWithPermission(permission);
                } else if (kPermissionDisplay.equals(permission.getElement())) {
                    permissionDisplay = new SZPermissionDisplay();
                    permissionDisplay.initWithPermission(permission);
                } else if (kPermissionPrint.equals(permission.getElement())) {
                    //print权限
                    permissionPrint = new SZPermissionPrint();
                    permissionPrint.initWithPermission(permission);
                }
            }
        }
        rightCheck = new SZRightCheck();
    }

    /**
     * 返回秘钥
     *
     * @return
     */
    public String getCek_Cipher_Value() {
        return cek_Cipher_Value;
    }

    /**
     * 检查权限是否到开始生效的时间
     * <p>
     * 是：未生效 ； 否：已生效
     *
     * @return true:未生效  ；  否：已经生效
     */
    public boolean isInEffective() {
        return rightCheck.checkFileIneffective(permissionPlay)
                || rightCheck.checkFileIneffective(permissionDisplay);
    }


    /**
     * 验权
     *
     * @return
     */
    public boolean checkOpen() {
        if (rightCheck.isExpired(permissionDisplay)
                || rightCheck.isExpired(permissionPlay)
                || rightCheck.isExpired(permissionPrint)) {
            return false;
        }
        return rightCheck.checkWithPermission(permissionPlay)
                || rightCheck.checkWithPermission(permissionDisplay)
                || rightCheck.checkWithPermission(permissionPrint);
    }

    public boolean checkPrint() {
        return permissionPrint != null;
    }

    /**
     * 结束时间
     */
    public long getOdd_datetime_end() {
        if (permissionPlay != null) {
            return this.permissionPlay.odd_datetime_end;
        } else if (permissionDisplay != null) {
            return this.permissionDisplay.odd_datetime_end;
        } else if (permissionPrint != null) {
            return this.permissionPrint.odd_datetime_end;
        }
        return 0L;
    }

    public long getOdd_datetime_start() {
        return permissionPlay.odd_datetime_start;
    }

    /**
     * 获取剩余时间, -1永久，0过期
     */
    public long getAvailbaleTime() {
        long currentTime = System.currentTimeMillis();
        if (this.permissionDisplay != null) {
            if (this.permissionDisplay.isAllPermission()) //永久
                return -1;
            else {
                long availableTime = (this.permissionDisplay.odd_datetime_end - currentTime);
                if (availableTime > 0) {
                    return availableTime;
                } else {
                    //更新expired
                    this.permissionDisplay.setExpired();
                    return 0;
                }
                //return availableTime > 0 ? availableTime : 0;
            }
        } else if (this.permissionPlay != null) {
            if (this.permissionPlay.isAllPermission()) //永久
                return -1;
            else {
                if (this.permissionPlay.odd_datetime_end <= currentTime) {
                    //更新expired
                    this.permissionPlay.setExpired();
                    return 0;
                } else {
                    //if (this.permissionPlay.odd_datetime_start > currentTime) { //文件未生效
                    //    //更新expired
                    //    this.permissionPlay.setExpired();
                    //    return 0;
                    //}
                    return this.permissionPlay.odd_datetime_end - currentTime;
                }
            }
        } else if (this.permissionPrint != null) {  //add code
            if (this.permissionPrint.isAllPermission()) //永久
                return -1;
            else {
                if (this.permissionPlay.odd_datetime_end <= currentTime) {
                    //更新expired
                    this.permissionPlay.setExpired();
                    return 0;
                } else {
                    return this.permissionPlay.odd_datetime_end - currentTime;
                }
            }
        }
        return 0;
    }

}
