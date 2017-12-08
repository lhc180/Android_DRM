package cn.com.pyc.szpbb.sdk.authentication;

import android.content.ContentValues;

import java.util.List;
import java.util.Vector;

import cn.com.pyc.szpbb.sdk.database.bean.Perconattribute;
import cn.com.pyc.szpbb.sdk.database.bean.Perconstraint;
import cn.com.pyc.szpbb.sdk.database.bean.Permission;
import cn.com.pyc.szpbb.sdk.database.practice.PermissionDAOImpl;

class SZPermissionCommon {

    private static final String kConstraintDatetime = "datetime";

    private static final String kConstraintAccumulated = "accumulated";

    private static final String kConstraintIndividual = "individual";

    private static final String kConstraintSystem = "system";

    private static final String kConAttrStart = "start";

    private static final String kConAttrEnd = "end";

    private PermissionDAOImpl permissionDAO = PermissionDAOImpl.getInstance();

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
    long odd_datetime = 0L;
    long odd_datetime_start = 0L;
    long odd_datetime_end = 0L;

    // 累计使用时间
    long odd_accumulated = 0L;

    // 绑定的授权用户
    String odd_individual = null;

    // 授权周期
    double odd_interval = 0.0;

    // 绑定的授权系统
    String odd_system = null;

    @SuppressWarnings("unchecked")
    void initWithPermission(Permission aPermission) {
        this.id = aPermission.getId();

        this.asset_id = aPermission.getAsset_id();

        this.element = aPermission.getElement();

        ContentValues contentValues = new ContentValues();
        aPermission.setPerconstraints((List<Perconstraint>) permissionDAO
                .findByQuery(new String[]{"permission_id"}, new String[]{aPermission.getId()},
                        Perconstraint.class));
        for (int i = 0; i < aPermission.getPerconstraints().size(); i++) {
            contentValues.put(aPermission.getPerconstraints().get(i).getElement(), aPermission
                    .getPerconstraints().get(i).getValue());
        }

        this.odd_accumulated = Long.parseLong(contentValues.getAsString(kConstraintAccumulated));

        this.odd_system = contentValues.getAsString(kConstraintSystem);

        this.odd_individual = contentValues.getAsString(kConstraintIndividual);

        if (contentValues.getAsString(kConstraintDatetime) != null) {
            // 如果datetime出现字符串会出错。
            this.odd_datetime = Long.parseLong(contentValues.getAsString(kConstraintDatetime));
            for (Perconstraint perconstraint : aPermission.getPerconstraints()) {
                if (perconstraint.getElement().equals(kConstraintDatetime)) {
                    List<Perconattribute> perconattributes = new Vector<Perconattribute>();
                    perconattributes = (List<Perconattribute>) permissionDAO
                            .findByQuery(new String[]{"perconstraint_id"}, new
                                            String[]{perconstraint.getId()},
                                    Perconattribute.class);
                    for (Perconattribute perconattribute : perconattributes) {
                        if (kConAttrStart.equals(perconattribute.getElement()))
                            this.odd_datetime_start = Long.valueOf(perconattribute.getValue());
                        else if (kConAttrEnd.equals(perconattribute.getElement()))
                            this.odd_datetime_end = Long.valueOf(perconattribute.getValue());
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

}
