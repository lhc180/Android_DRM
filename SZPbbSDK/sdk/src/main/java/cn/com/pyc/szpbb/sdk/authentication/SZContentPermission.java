package cn.com.pyc.szpbb.sdk.authentication;

import java.util.List;

import cn.com.pyc.szpbb.sdk.database.bean.SZAlbumContent;
import cn.com.pyc.szpbb.sdk.database.bean.Asset;
import cn.com.pyc.szpbb.sdk.database.bean.Permission;
import cn.com.pyc.szpbb.sdk.database.practice.AssetDAOImpl;
import cn.com.pyc.szpbb.util.FormatUtil;

public class SZContentPermission {
    private static final String kConstraintDatetime = "datetime";

    private static final String kConstraintAccumulated = "accumulated";

    private static final String kConstraintIndividual = "individual";

    private static final String kConstraintSystem = "system";

    private static final String kPermissionPlay = "PLAY";

    private static final String kPermissionDisplay = "DISPLAY";

    private AssetDAOImpl assetDAO = AssetDAOImpl.getInstance();

    private SZPermissionDisplay permissionDisplay = null;
    private SZPermissionPlay permissionPlay = null;
    private String cek_cipher_value = "";

    public SZContentPermission(SZAlbumContent albumContent) {
        init(albumContent.getAsset_id());
    }


    private SZContentPermission(String assetId) {
        init(assetId);
    }

    @SuppressWarnings("unchecked")
    private void init(String assetId) {
        Asset asset = assetDAO.findObjectByQuery(new String[]{"_id"}, new String[]{assetId},
                Asset.class);
        asset.setPermissions((List<Permission>) assetDAO.findByQuery(
                new String[]{"asset_id"}, new String[]{assetId}, Permission.class));
        cek_cipher_value = asset.getCek_cipher_value();

        List<Permission> permissions = asset.getPermissions();
        for (Permission permission : permissions) {
            if (kPermissionPlay.equals(permission.getElement())) {
                permissionPlay = new SZPermissionPlay();
                permissionPlay.initWithPermission(permission);
            } else if (kPermissionDisplay.equals(permission.getElement())) {
                permissionDisplay = new SZPermissionDisplay();
                permissionDisplay.initWithPermission(permission);
            }
        }
    }

    /**
     * 获取秘钥，初始化SZContent之后赋值
     *
     * @return String
     */
    public String getCipherValue() {
        return cek_cipher_value;
    }

    /**
     * 验证有效性
     *
     * @return boolean
     */
    public boolean checkOpen() {
        SZCheckRight rightCheck = new SZCheckRight();
        return rightCheck.checkWithPermission(permissionPlay)
                || rightCheck.checkWithPermission(permissionDisplay);
    }

    /**
     * 获取文件的过期时间，返回格式（yyyy-MM-dd HH:mm:ss） <br/>
     * 永久有效可能返回“-1”
     *
     * @return String
     */
    public String getExpireTime() {
        return FormatUtil.getToOddEndTime(getOdd_datetime_end());
    }

    /**
     * 获取文件的过期时间
     *
     * @return long
     */
    private long getOdd_datetime_end() {
        if (permissionPlay != null) {
            return this.permissionPlay.odd_datetime_end;
        } else if (permissionDisplay != null) {
            return this.permissionDisplay.odd_datetime_end;
        }
        return 0L;
    }

    /**
     * 获取文件剩余有效时间 <br/>
     *
     * @return 固定格式： <br/>
     * //     * (1)  -1: 永久有效 <br/>
     * //     * (2)   0: 已经过期 <br/>
     * //     * (3)   xx天xx小时: 剩余有效期天数，eg:12天02小时 <br/>
     * //
     */
    public String getSurplusTime() {
        return FormatUtil.getLeftAvailableTime(getAvailbaleTime());
    }

    /**
     * 获取文件剩余有效时间
     *
     * @return long
     */
    private long getAvailbaleTime() {
        long currentTime = System.currentTimeMillis();
        if (this.permissionDisplay != null) {
            if (this.permissionDisplay.isAllPermission()) {
                return -1;
            } else {
                long availableTime = this.permissionDisplay.odd_datetime_end - currentTime;
                // availableTime = availableTime > 0 ? availableTime : 0;
                return availableTime > 0 ? availableTime : 0;
            }
        } else if (this.permissionPlay != null) {
            if (this.permissionPlay.isAllPermission()) {
                return -1;
            } else {
                if (this.permissionPlay.odd_datetime_end < currentTime) {
                    return 0;
                } else {
                    if (this.permissionPlay.odd_datetime_start > currentTime) {
                        return 0;
                    }
                    return this.permissionPlay.odd_datetime_end - currentTime;
                }
            }
        }
        return 0;
    }

    protected long getOdd_datetime_start() {
        if (permissionPlay != null) {
            return this.permissionPlay.odd_datetime_start;
        } else if (permissionDisplay != null) {
            return this.permissionDisplay.odd_datetime_start;
        }
        return 0L;
    }

}
