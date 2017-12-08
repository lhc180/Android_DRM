package cn.com.pyc.drm.model.right;

import android.content.ContentValues;

import java.util.List;
import java.util.Vector;

import cn.com.pyc.drm.model.db.bean.Perconattribute;
import cn.com.pyc.drm.model.db.bean.Perconstraint;
import cn.com.pyc.drm.model.db.bean.Permission;
import cn.com.pyc.drm.model.db.practice.PermissionDAOImpl;
import cn.com.pyc.drm.utils.ConvertToUtils;

class SZPermissionCommon {

    private static final String kConstraintDatetime = "datetime";

    private static final String kConstraintAccumulated = "accumulated";

    private static final String kConstraintIndividual = "individual";

    private static final String kConstraintSystem = "system";

    private static final String kConAttrStart = "start";

    private static final String kConAttrEnd = "end";

    private PermissionDAOImpl permissionDAO = new PermissionDAOImpl();

    // the uid of permission
    String id = null;

    String asset_id = null;

    // the name of permission element
    String element = null;

    // 授权次数
    int odd_count = 0;

    // 授权次数
    int odd_timed_count = 0;

    // 授权时段
    long odd_datetime = 0L; // double
    public long odd_datetime_start = 0L; // double
    public long odd_datetime_end = 0L; // double

    // 授权周期
    double odd_interval = 0.0;

    // 累计使用时间
    double odd_accumulated = 0.0;

    // 绑定的授权用户
    String odd_individual = null;

    // 绑定的授权系统
    String odd_system = null;

    //本地标示: 权限过期
    //String expired;

    @SuppressWarnings("unchecked")
    void initWithPermission(Permission aPermission) {
        this.id = aPermission.getId();

        this.asset_id = aPermission.getAsset_id();

        this.element = aPermission.getElement();

        ContentValues contentValues = new ContentValues();
        aPermission.setPerconstraints((List<Perconstraint>) permissionDAO
                .findByQuery(new String[]{"permission_id"},
                        new String[]{aPermission.getId()},
                        Perconstraint.class));
        for (int i = 0; i < aPermission.getPerconstraints().size(); i++) {
            contentValues.put(aPermission.getPerconstraints().get(i)
                    .getElement(), aPermission.getPerconstraints().get(i).getValue());
        }

        //修改：null时抛异常
        this.odd_accumulated = ConvertToUtils.toDouble(contentValues
                .getAsString(kConstraintAccumulated));

        this.odd_system = contentValues.getAsString(kConstraintSystem);

        this.odd_individual = contentValues.getAsString(kConstraintIndividual);

        if (contentValues.getAsString(kConstraintDatetime) != null) {
            // 如果datetime出现字符串会出错。
            this.odd_datetime = ConvertToUtils.toLong(contentValues.getAsString(kConstraintDatetime));
            for (Perconstraint perconstraint : aPermission.getPerconstraints()) {
                if (perconstraint.getElement().equals(kConstraintDatetime)) {
                    List<Perconattribute> perconattributes = new Vector<Perconattribute>();
                    perconattributes = (List<Perconattribute>) permissionDAO.findByQuery(new
                                    String[]{"perconstraint_id"}, new String[]{perconstraint
                                    .getId()},
                            Perconattribute.class);
                    for (Perconattribute perconattribute : perconattributes) {
                        if (kConAttrStart.equals(perconattribute.getElement()))
                            //this.odd_datetime_start = Long.valueOf(perconattribute.getValue());
                            this.odd_datetime_start = ConvertToUtils.toLong(perconattribute
                                    .getValue());
                        else if (kConAttrEnd.equals(perconattribute.getElement()))
                            //this.odd_datetime_end = Long.valueOf(perconattribute.getValue());
                            this.odd_datetime_end = ConvertToUtils.toLong(perconattribute
                                    .getValue());
                    }
                }
            }
        }
    }

    // void fillWithPermission(Permission aPermission);
    // Boolean isDayPermission();
    // Boolean addTime();

    boolean isAllPermission() {
        return this.odd_datetime == 0L;
    }


    boolean isExpired() {
        return permissionDAO.EXPIRED.equals(permissionDAO.getExpired(this.asset_id));
    }

    int setExpired() {
        return permissionDAO.setExpired(this.asset_id);
    }

}
