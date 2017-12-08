package cn.com.pyc.drm.model.dbBase;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.provider.BaseColumns;

import net.sqlcipher.Cursor;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import cn.com.pyc.drm.common.App;
import cn.com.pyc.drm.common.Constant;
import cn.com.pyc.drm.utils.DRMLog;
import cn.com.pyc.drm.utils.GenericsUtils;
import cn.com.pyc.drm.utils.help.DBHelper;

public class SZBaseDAO<T> {
    /*
     * 获取实体对象类型
     */
    protected Class<T> entityClass;
    protected DBHelper dbHelper;// = DBHelper.getInstance(App.getInstance(),
    // Constant.getUserName());

    @SuppressWarnings("unchecked")
    protected SZBaseDAO() {
        if (dbHelper == null) {
            //dbHelper = DBHelper.getInstance(App.getInstance(), Constant.getName());
            dbHelper = DBHelper.getInstance(App.getInstance(), Constant.getAccountId());
        }
        entityClass = cn.com.pyc.drm.utils.GenericsUtils.getSuperClassGenricType(this.getClass());
        //// SQLiteDatabase.loadLibs(App.getInstance());
    }

    // /////////////////////////////////////////////////////
    // ///////////////////custom opration//////////////////
    // /////////////////////////////////////////////////////

    /**
     * 删除表数据
     */
    public void DeleteTableData(String tableName) {
        DRMLog.i(tableName);
        dbHelper.DeleteTableData(tableName);
    }

    // 查询Right的_id
    public String findRightId(String _id) {
        String result = null;
        Cursor cursor = null;
        try {
            cursor = dbHelper.rawQuery("SELECT * FROM Right WHERE _id=?", new String[]{_id});
            if (cursor.moveToNext()) {
                result = cursor.getString(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return result;
    }

    /**
     * 删除right
     */
    public void DeleteRight(String _id) {
        dbHelper.DeleteRight(_id);
    }

    /**
     * 根据right_id查询Asset
     *
     * @param right_id
     * @return
     */
    public ArrayList<String> findAssetId(String right_id) {
        Cursor cursor = null;
        ArrayList<String> al = new ArrayList<String>();
        try {
            cursor = dbHelper.rawQuery("SELECT * FROM Asset WHERE right_id=?",
                    new String[]{right_id});
            while (cursor.moveToNext()) {
                al.add(cursor.getString(0));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return al;
    }

    /**
     * 删除Asset
     *
     * @param right_id
     */
    public void DeleteAsset(String right_id) {
        dbHelper.DeleteAsset(right_id);
    }

    /**
     * 查询单个文件权限表
     */
    public String findPermissionId(String _id) {
        String result = null;
        Cursor cursor = null;
        try {
            cursor = dbHelper.rawQuery("SELECT * FROM Permission WHERE _id=?", new String[]{_id});
            if (cursor.moveToNext()) {
                result = cursor.getString(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return result;
    }

    /**
     * 删除Permission约束
     *
     * @param _id
     */
    public void DeletePermission(String _id) {
        dbHelper.DeletePermission(_id);
    }

    /**
     * 根据permission_id查询文件权限信息约束表
     *
     * @param permission_id
     * @return
     */
    public ArrayList<String> findPerconstraintId(String permission_id) {
        Cursor cursor = null;
        ArrayList<String> al = new ArrayList<String>();
        try {
            cursor = dbHelper.rawQuery("SELECT * FROM Perconstraint WHERE Permission_id=?",
                    new String[]{permission_id});
            while (cursor.moveToNext()) {
                al.add(cursor.getString(0));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return al;
    }

    /**
     * 根据permission_id删除Perconstraint
     *
     * @param permission_id
     */
    public void DeletePerconstraint(String permission_id) {
        dbHelper.DeletePerconstraint(permission_id);
    }

    // //////////////////////////////////////////////////////////////////////////
    // //////////////////////////////////////////////////////////////////////////
    // //////////////////////////////////////////////////////////////////////////
    // /////////////////////////数据表方法/////////////////////////////////
    // //////////////////////////////////////////////////////////////////////////
    // //////////////////////////////////////////////////////////////////////////
    // //////////////////////////////////////////////////////////////////////////

    private boolean isValid(String fieldName, Class fieldType) {
        return !fieldName.equalsIgnoreCase("serialVersionUID")
                && !fieldName.equalsIgnoreCase("$change") //studo instant run 运行字段的问题
                && !fieldType.equals(Collections.class)
                && !fieldType.equals(Map.class)
                && !fieldType.equals(List.class)
                && !fieldType.equals(Set.class);
    }

    /**
     * 数据表创建方法
     */
    @SuppressWarnings("rawtypes")
    public void create(Class<T> entityClass) {
        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS "
                + entityClass.getSimpleName() + " (" + BaseColumns._ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT, ");
        Field[] fields = entityClass.getDeclaredFields(); // 得到当前类entityClass所有的成员变量
        for (Field field : fields) {
            field.setAccessible(true);
            String fieldName = field.getName(); // 变量名
            Class fieldType = field.getType(); // 变量类型
            if (isValid(fieldName, fieldType) && !fieldName.equalsIgnoreCase("id")) {
                if (fieldType.equals(int.class)) { // !fiedName.equalsIgnoreCase("id")原因？？？
                    sql.append(fieldName + " INTEGER, ");
                } else if (fieldType.equals(String.class)) {
                    sql.append(fieldName + " varchar, ");
                } else if (fieldType.equals(float.class) | fieldType.equals(double.class)) {
                    sql.append(fieldName + " double, ");
                } else if (fieldType.equals(long.class)) {
                    sql.append(fieldName + " double, ");
                }
            }
        }
        sql.replace(sql.lastIndexOf(","), sql.length(), ")");
        DRMLog.d("SQL: " + sql.toString());
        dbHelper.ExecSQL(sql.toString());
    }

	/*
     * 数据保存方法，包括： 单数据实体保存方法——save(T entity)； 多数据实体保存方法——batchSave(Collection<T>
	 * entityCollection)； 单数据实体级联保存方法——cascadedSave(T entity)；
	 * 多数据实体级联保存方法——cascadedBatchSave(Collection<T> entityCollection)。
	 */

    /**
     * 单数据实体保存方法
     * <p>
     * 请使用对应的实现类，eg：AlbumDAOImpl，RightDAOImpl...
     */
    @SuppressWarnings("rawtypes")
    public synchronized boolean save(T entity) {
        DRMLog.e("save TableName: " + entityClass.getSimpleName());
        // ContentValues负责存储一些键值对，键是一个String类型，而值是基本类型
        ContentValues contentValues = new ContentValues();
        Field[] fields = entityClass.getDeclaredFields();
        try {
            for (Field field : fields) {
                field.setAccessible(true);
                String fieldName = field.getName();
                Class fieldType = field.getType();
                if (isValid(fieldName, fieldType)) {
                    String methodName = "get" + (char) (fieldName.charAt(0) - 32)
                            + fieldName.substring(1);

                    if (methodName.equals("getId")) {
                        String temp = entity.getClass().getMethod(methodName)
                                .invoke(entity).toString();
                        if (!"".equals(temp))
                            contentValues.put(BaseColumns._ID, temp);
                    } else {
                        String temp = entity.getClass().getMethod(methodName)
                                .invoke(entity).toString();
                        contentValues.put(fieldName, temp);
                    }
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return dbHelper.insert(entityClass.getSimpleName(), contentValues) != -1;
    }

    /*
     * 多数据实体保存方法
     */
    @SuppressWarnings("rawtypes")
    public int[] batchSave(Collection<T> entityCollection) {
        Iterator it = entityCollection.iterator();
        int[] result = new int[entityCollection.size()];
        int count = 0;
        try {
            while (it.hasNext()) {
                Object singleEntity = it.next();
                ContentValues contentValues = new ContentValues();
                Field[] fields = singleEntity.getClass().getDeclaredFields();
                for (Field field : fields) {
                    field.setAccessible(true);
                    String fieldName = field.getName();
                    Class fieldType = field.getType();
                    if (isValid(fieldName, fieldType)) {
                        String methodName = "get" + (char) (fieldName.charAt(0) - 32)
                                + fieldName.substring(1);
                        if (methodName.equals("getId")) {
                            String temp = singleEntity.getClass()
                                    .getMethod(methodName).invoke(singleEntity)
                                    .toString();
                            if (!"".equals(temp))
                                contentValues.put(BaseColumns._ID, temp);
                        } else {
                            String temp = singleEntity.getClass().getMethod(methodName).invoke
                                    (singleEntity)
                                    .toString();
                            contentValues.put(fieldName, temp);
                        }
                    }
                }
                result[count] = (int) dbHelper.insert(singleEntity.getClass()
                        .getSimpleName(), contentValues);
                count++;
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return result;
    }

    /*
     * 级联保存
     */
    @SuppressWarnings("rawtypes")
    private int[] batchSaveEntity(List<Object> entityList) {
        int[] result = new int[entityList.size()];
        try {
            for (int count = 0; count < entityList.size(); count++) {
                ContentValues contentValues = new ContentValues();
                Field[] fields = entityList.get(count).getClass().getDeclaredFields();
                for (Field field : fields) {
                    field.setAccessible(true);
                    String fieldName = field.getName();
                    Class fieldType = field.getType();
                    if (isValid(fieldName, fieldType)) {
                        String methodName = "get" + (char) (fieldName.charAt(0) - 32)
                                + fieldName.substring(1);
                        if (methodName.equals("getId")) {
                            String temp = entityList.get(count).getClass().getMethod(methodName)
                                    .invoke(entityList.get(count)).toString();
                            if (!"".equals(temp))
                                contentValues.put(BaseColumns._ID, temp);
                        } else {
                            String temp = entityList.get(count).getClass()
                                    .getMethod(methodName)
                                    .invoke(entityList.get(count)).toString();
                            contentValues.put(fieldName, temp);
                        }
                    }
                }
                result[count] = (int) dbHelper.insert(entityList.get(count)
                        .getClass().getSimpleName(), contentValues);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return result;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private List<Object> saveIterator(List<Object> objList) {
        List<Object> tempList = new Vector<Object>();
        try {
            for (Object obj : objList) {
                Field[] fields = obj.getClass().getDeclaredFields();
                for (Field field : fields) {
                    field.setAccessible(true);
                    if (field.getType().equals(List.class)) {
                        String methodName = "get"
                                + (char) (field.getName().charAt(0) - 32)
                                + field.getName().substring(1);// 装配方法名称

                        List temp = (List) obj.getClass().getMethod(methodName).invoke(obj);
                        if (!temp.isEmpty()) {
                            batchSaveEntity(temp);
                            tempList.addAll(temp);
                        }
                    }
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return tempList;
    }

    /*
     * 单数据实体级联保存方法
     */
    public boolean cascadedSave(T entity) {
        save(entity);
        List<Object> objList = new Vector<Object>();
        objList.add(entity);
        boolean flag = true;
        while (flag == true) {
            objList = saveIterator(objList);
            if (!objList.isEmpty()) {
                flag = true;
            } else
                flag = false;
        }
        return true;
    }

    /*
     * 多数据实体级联保存方法
     */
    public boolean cascadedBatchSave(Collection<T> entityCollection) {
        batchSave(entityCollection);
        List<Object> objList = new Vector<Object>();
        objList.addAll(entityCollection);
        boolean flag = true;
        while (flag == true) {
            objList = saveIterator(objList);
            if (!objList.isEmpty()) {
                flag = true;
            } else
                flag = false;
        }
        return true;
    }

	/*
     * 数据删除方法，包括： 单数据实体删除方法——delete(T entity)，deleteById(Serializable id)；
	 * 多数据实体删除方法——batchDelete(Collection<T> entityCollection)；
	 * 单数据实体级联删除方法——cascadedDeleteById(Serializable id)；
	 * 多数据实体级联删除方法——cascadedBatchDelete(Collection<T> entityCollection)。
	 */

    /*
     * 单数据实体删除方法
     */
    public int delete(T entity) {
        String id = "";
        try {
            if (entity != null) {
                Method method = entity.getClass().getMethod("getId");
                if (method != null){
                    id = method.invoke(entity).toString();
                }

            }
//            id = entity.getClass().getMethod("getId").invoke(entity).toString();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return dbHelper.delete(entityClass.getSimpleName(), id);
    }

    /*
     * 多数据实体删除方法
     */
    @SuppressWarnings("rawtypes")
    public int[] batchDelete(Collection<T> entityCollection) {
        Iterator it = entityCollection.iterator();
        int[] result = new int[entityCollection.size()];
        int count = 0;
        try {
            while (it.hasNext()) {
                Object singleEntity = it.next();
                String id = singleEntity.getClass().getMethod("getId")
                        .invoke(singleEntity).toString();
                result[count] = dbHelper.delete(singleEntity.getClass()
                        .getSimpleName(), id);
                count++;
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return result;
    }

    // 根据id删除table中数据
    public int deleteEntityByIdAndName(String id, String tableName) {
        return dbHelper.delete(tableName, id);
    }

    /**
     * 级联删除
     */
    @SuppressWarnings("unused")
    private class StackItem {
        private String id;
        private String entityName;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getEntityName() {
            return entityName;
        }

        public void setEntityName(String entityName) {
            this.entityName = entityName;
        }
    }

    @SuppressLint("NewApi")
    @SuppressWarnings({"rawtypes", "unchecked"})
    private List<Object> iterator(List<Object> objList, Stack stack) {
        List<Object> tempList = new Vector<Object>();
        try {
            for (Object obj : objList) {
                StackItem stackItem = new StackItem();
                /* 主表id即为子表外键，先通过对象得到对象类名，再通过类名查找类中定义的方法，得到对象中存储的id */
                String foreignId = obj.getClass().getMethod("getId").invoke(obj).toString();
                stackItem.id = foreignId;
                stackItem.entityName = obj.getClass().getSimpleName();

                // /*通过主表的领域值查找子表名，并查询子表内容*/
                Field[] fields = obj.getClass().getDeclaredFields();
                for (Field field : fields) {
                    if (field.getType().equals(List.class)) {
                        String childTableName = GenericsUtils.getFieldGenericType(
                                field).getSimpleName();// 装配子表名称
                        Cursor cursor = dbHelper.query(childTableName, null, obj
                                .getClass().getSimpleName().charAt(0)
                                + obj.getClass().getSimpleName().substring(1)
                                + "_id =?", new String[]{foreignId});
                        while (cursor.moveToNext()) {
                            Object singleObj = GenericsUtils.getFieldGenericType(field)
                                    .newInstance();
                            Field[] singleFields = singleObj.getClass().getDeclaredFields();
                            for (Field sField : singleFields) {
                                sField.setAccessible(true);
                                String fieldName = sField.getName();
                                Class fieldType = sField.getType();
                                if (isValid(fieldName, fieldType)) {
                                    String methodName = "set"
                                            + (char) (fieldName.charAt(0) - 32)
                                            + fieldName.substring(1);
                                    if (methodName.equals("setId")) {
                                        int idColumn = cursor.getColumnIndex(BaseColumns._ID);
                                        String idValue = cursor.getString(idColumn);
                                        singleObj.getClass().getMethod(methodName,
                                                new Class[]{String.class}).invoke(singleObj,
                                                new Object[]{idValue});
                                    } else {
                                        int idColumn = cursor.getColumnIndex(fieldName);
                                        String Value = cursor.getString(idColumn);
                                        singleObj.getClass().getMethod(methodName,
                                                new Class[]{String.class}).invoke(singleObj,
                                                new Object[]{Value});
                                    }
                                }
                            }
                            tempList.add(singleObj);
                        }
                    }
                }
                stack.push(stackItem);
            }
        } catch (IllegalAccessException e1) {
            e1.printStackTrace();
        } catch (IllegalArgumentException e1) {
            e1.printStackTrace();
        } catch (InvocationTargetException e1) {
            e1.printStackTrace();
        } catch (NoSuchMethodException e1) {
            e1.printStackTrace();
        } catch (SecurityException e1) {
            e1.printStackTrace();
        } catch (InstantiationException e1) {
            e1.printStackTrace();
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return tempList;
    }

    /*
     * 单数据实体级联删除方法
     */
    @SuppressLint({"UseValueOf", "NewApi"})
    @SuppressWarnings("rawtypes")
    public boolean cascadedDeleteById(String id) {
        List<Object> objList = new Vector<Object>();
        Cursor cursor = null;
        try {
            cursor = dbHelper.query(entityClass.getSimpleName(), null, BaseColumns._ID +
                    "=?", new String[]{id});
            while (cursor.moveToNext()) {
                T obj = entityClass.newInstance();
                Field[] fields = obj.getClass().getDeclaredFields();
                for (Field field : fields) {
                    field.setAccessible(true);
                    String fieldName = field.getName();
                    Class fieldType = field.getType();
                    if (isValid(fieldName, fieldType)) {
                        String methodName = "set" + (char) (fieldName.charAt(0) - 32)
                                + fieldName.substring(1);
                        if (methodName.equals("setId")) {
                            int idColumn = cursor.getColumnIndex(BaseColumns._ID);
                            String idValue = cursor.getString(idColumn);
                            obj.getClass().getMethod(methodName, new Class[]{String.class})
                                    .invoke(obj, new Object[]{idValue});
                        } else {
                            int idColumn = cursor.getColumnIndex(fieldName);
                            String Value = cursor.getString(idColumn);
                            obj.getClass().getMethod(methodName, new Class[]{String.class})
                                    .invoke(obj, new Object[]{Value});
                        }
                    }
                }
                objList.add(obj);
            }
            Stack<StackItem> stack = new Stack<StackItem>();
            boolean flag = true;

            while (flag == true) {
                objList = iterator(objList, stack);
                if (!objList.isEmpty()) {
                    flag = true;
                } else
                    flag = false;
            }
            while (!stack.isEmpty()) {
                StackItem stackItem = stack.pop();
                deleteEntityByIdAndName(stackItem.getId(), stackItem.getEntityName());
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InstantiationException e1) {
            e1.printStackTrace();
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return true;
    }

    /*
     * 多数据实体级联删除方法
     */
    public boolean cascadedBatchDelete(Collection<T> entityCollection) {
        List<Object> objList = new Vector<Object>();
        objList.addAll(entityCollection);
        Stack<StackItem> stack = new Stack<StackItem>();

        boolean flag = true;
        while (flag == true) {
            objList = iterator(objList, stack);
            if (!objList.isEmpty()) {
                flag = true;
            } else
                flag = false;
        }
        while (!stack.isEmpty()) {
            StackItem stackItem = stack.pop();
            deleteEntityByIdAndName(stackItem.getId(),
                    stackItem.getEntityName());
        }
        return true;
    }

	/*
     * 数据更新方法，包括： 单数据实体更新方法——update(T entity)；
	 * 多数据实体更新方法——batchUpdate(Collection<T> entityCollection)；
	 * 单数据实体级联更新方法——cascadedUpdate(T entity)，cascadedSetActiveById(Serializable
	 * id, boolean active)； 多数据实体级联更新方法——cascadedBatchUpdate(Collection<T>
	 * entityCollection)。
	 */

    /*
     * 单数据实体更新方法
     */
    @SuppressWarnings("rawtypes")
    public synchronized int update(T entity) {
        int result = 0;
        ContentValues contentValues = new ContentValues();
        Field[] fields = entityClass.getDeclaredFields();
        try {
            for (Field field : fields) {
                field.setAccessible(true);
                String fieldName = field.getName();
                Class fieldType = field.getType();
                if (!fieldName.equalsIgnoreCase("id")
                        && isValid(fieldName, fieldType)) {
                    String methodName = "get" + (char) (fieldName.charAt(0) - 32)
                            + fieldName.substring(1);
                    String temp = entity.getClass().getMethod(methodName).invoke(entity).toString();
                    contentValues.put(fieldName, temp);
                }
            }
            result = dbHelper.update(entityClass.getSimpleName(),
                    contentValues, "_id=?", new String[]{entity.getClass().getMethod("getId")
                            .invoke(entity).toString()});
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return result;
    }

    /*
     * 多数据实体更新方法
     */
    @SuppressWarnings("rawtypes")
    public int[] batchUpdate(Collection<T> entityCollection) {
        Iterator it = entityCollection.iterator();
        int[] result = new int[entityCollection.size()];
        int count = 0;
        while (it.hasNext()) {
            Object singleEntity = it.next();
            ContentValues contentValues = new ContentValues();
            Field[] fields = singleEntity.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                String fieldName = field.getName();
                Class fieldType = field.getType();
                if (!fieldName.equalsIgnoreCase("id") && isValid(fieldName, fieldType)) {
                    String temp;
                    try {
                        String methodName = "get"
                                + (char) (fieldName.charAt(0) - 32)
                                + fieldName.substring(1);
                        temp = singleEntity.getClass().getMethod(methodName)
                                .invoke(singleEntity).toString();
                        contentValues.put(fieldName, temp);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    } catch (IndexOutOfBoundsException e) {
                        e.printStackTrace();
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }
            }
            try {
                result[count] = dbHelper.update(
                        entityClass.getSimpleName(),
                        contentValues,
                        "_id=?",
                        new String[]{singleEntity.getClass()
                                .getMethod("getId").invoke(singleEntity)
                                .toString()});
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            count++;
        }
        return result;
    }

    /*
     * 级联更新实体
     */
    @SuppressWarnings("rawtypes")
    private int[] batchUpdateEntity(List<Object> entityList) {
        int[] result = new int[entityList.size()];
        int count = 0;
        for (Object obj : entityList) {
            ContentValues contentValues = new ContentValues();
            Field[] fields = obj.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                String fieldName = field.getName();
                Class fieldType = field.getType();
                if (!fieldName.equalsIgnoreCase("id")
                        && isValid(fieldName, fieldType)) {
                    try {
                        String methodName = "get"
                                + (char) (fieldName.charAt(0) - 32)
                                + fieldName.substring(1);
                        String temp = obj.getClass().getMethod(methodName)
                                .invoke(obj).toString();
                        contentValues.put(fieldName, temp);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    } catch (IndexOutOfBoundsException e) {
                        e.printStackTrace();
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }
            }
            try {
                result[count] = dbHelper.update(obj.getClass().getSimpleName(),
                        contentValues, "_id=?", new String[]{obj.getClass()
                                .getMethod("getId").invoke(obj).toString()});
                count++;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private List<Object> updateIterator(List<Object> objList) {
        List<Object> tempList = new Vector<Object>();
        for (Object obj : objList) {
            Field[] fields = obj.getClass().getDeclaredFields();
            for (Field field : fields) {
                if (field.getType().equals(List.class)) {
                    String methodName = "get"
                            + (char) (field.getName().charAt(0) - 32)
                            + field.getName().substring(1);// 装配方法名称
                    try {
                        List temp = (List) obj.getClass().getMethod(methodName)
                                .invoke(obj);
                        if (!temp.isEmpty()) {
                            batchUpdateEntity(temp);
                            tempList.addAll(temp);
                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    } catch (IndexOutOfBoundsException e) {
                        e.printStackTrace();
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return tempList;
    }

    /*
     * 单数据实体级联更新方法
     */
    public boolean cascadedUpdate(T entity) {
        update(entity);
        List<Object> objList = new Vector<Object>();
        objList.add(entity);
        boolean flag = true;
        while (flag == true) {
            objList = updateIterator(objList);
            if (!objList.isEmpty()) {
                flag = true;
            } else
                flag = false;
        }
        return true;
    }

    /*
     * 多数据实体级联更新方法
     */
    public boolean cascadedBatchUpdate(Collection<T> entityCollection) {
        batchUpdate(entityCollection);
        List<Object> objList = new Vector<Object>();
        objList.addAll(entityCollection);
        boolean flag = true;
        while (flag == true) {
            objList = updateIterator(objList);
            if (!objList.isEmpty()) {
                flag = true;
            } else
                flag = false;
        }
        return true;
    }

    @SuppressWarnings("rawtypes")
    public T findById(String id) {
        T obj = null;
        Cursor cursor = null;
        try {
            cursor = dbHelper.query(entityClass.getSimpleName(), null,
                    BaseColumns._ID + "=?", new String[]{String.valueOf(id)});
            if (cursor.moveToNext()) {
                obj = entityClass.newInstance();
                Field[] fields = obj.getClass().getDeclaredFields();
                for (Field field : fields) {
                    field.setAccessible(true);
                    String fieldName = field.getName();
                    Class fieldType = field.getType();
                    if (isValid(fieldName, fieldType)) {
                        String methodName = "set"
                                + (char) (fieldName.charAt(0) - 32)
                                + fieldName.substring(1);
                        if (methodName.equals("setId")) {
                            int idColumn = cursor
                                    .getColumnIndex(BaseColumns._ID);
                            String idValue = cursor.getString(idColumn);
                            obj.getClass()
                                    .getMethod(methodName,
                                            new Class[]{String.class})
                                    .invoke(obj, new Object[]{idValue});
                        } else {
                            int idColumn = cursor.getColumnIndex(fieldName);
                            String Value = cursor.getString(idColumn);
                            obj.getClass().getMethod(methodName, new Class[]{String.class})
                                    .invoke(obj, new Object[]{Value});
                        }
                    }
                }
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return obj;
    }

    @SuppressLint("NewApi")
    @SuppressWarnings("rawtypes")
    public List<T> findAll(Class<T> entityClass, String order) {
        List<T> objList = new Vector<T>();
        Cursor cursor = null;
        try {
            cursor = dbHelper.query(entityClass.getSimpleName(), null, null,
                    null, null, null, BaseColumns._ID + " " + order);
            while (cursor.moveToNext()) {
                T obj = entityClass.newInstance();
                Field[] fields = obj.getClass().getDeclaredFields();
                for (Field field : fields) {
                    field.setAccessible(true);
                    String fieldName = field.getName();
                    Class fieldType = field.getType();
                    if (isValid(fieldName, fieldType)) {
                        String methodName = "set"
                                + (char) (fieldName.charAt(0) - 32)
                                + fieldName.substring(1);
                        if (methodName.equals("setId")) {
                            int idColumn = cursor
                                    .getColumnIndex(BaseColumns._ID);
                            String idValue = cursor.getString(idColumn);
                            obj.getClass()
                                    .getMethod(methodName,
                                            new Class[]{String.class})
                                    .invoke(obj, new Object[]{idValue});
                        } else {
                            int idColumn = cursor.getColumnIndex(fieldName);
                            String Value = cursor.getString(idColumn);
                            obj.getClass().getMethod(methodName,
                                    new Class[]{String.class}).invoke(obj, new Object[]{Value});
                        }
                    }
                }
                objList.add(obj);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return objList;
    }

    @SuppressLint("NewApi")
    @SuppressWarnings("rawtypes")
    public List<?> findByQuery(String sql, Class<?> entityClass) {
        List<Object> objList = new Vector<Object>();
        Cursor cursor = null;
        try {
            cursor = dbHelper.rawQuery(sql, null);
            while (cursor.moveToNext()) {
                Object obj = entityClass.newInstance();
                Field[] fields = obj.getClass().getDeclaredFields();
                for (Field field : fields) {
                    field.setAccessible(true);
                    String fieldName = field.getName();
                    Class fieldType = field.getType();
                    if (isValid(fieldName, fieldType)) {
                        String methodName = "set" + (char) (fieldName.charAt(0) - 32)
                                + fieldName.substring(1);
                        if (methodName.equals("setId")) {
                            int idColumn = cursor.getColumnIndex(BaseColumns._ID);
                            String idValue = cursor.getString(idColumn);
                            obj.getClass().getMethod(methodName, new Class[]{String.class})
                                    .invoke(obj, new Object[]{idValue});
                        } else {
                            int idColumn = cursor.getColumnIndex(fieldName);
                            String Value = cursor.getString(idColumn);
                            obj.getClass().getMethod(methodName,
                                    new Class[]{String.class}).invoke(obj, new Object[]{Value});
                        }
                    }
                }
                objList.add(obj);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        // dbHelper.close();
        return objList;
    }

    public List<?> findByQuery(String[] elements, String[] args, Class<?> entityClass) {
        if (elements.length != args.length) return null;
        StringBuilder sql = new StringBuilder("SELECT * FROM "
                + entityClass.getSimpleName() + " WHERE ");
        for (int i = 0; i < elements.length; i++) {
            if (i < elements.length - 1) {
                sql.append(elements[i] + " = '" + args[i] + "' " + "AND ");
            } else {
                sql.append(elements[i] + " = '" + args[i] + "' ");
            }
        }
        // sql.append("order by _id desc");
        return findByQuery(sql.toString(), entityClass);
    }

    @SuppressWarnings("unchecked")
    public T findObjectByQuery(String[] elements, String[] args, Class<?> entityClass) {
        if (elements.length != args.length) return null;
        StringBuilder sql = new StringBuilder("SELECT * FROM "
                + entityClass.getSimpleName() + " WHERE ");
        for (int i = 0; i < elements.length; i++) {
            sql.append(elements[i] + " = '" + args[i] + "' ");
        }
        List<?> list = findByQuery(sql.toString(), entityClass);
        if (list != null && !list.isEmpty()) {
            return (T) list.get(0);
        }
        return null;
    }

    /* 自定义SQL操作 */
    public void executeBySql(String sql) {
        dbHelper.ExecSQL(sql);
        dbHelper.closeDb();
    }

}
