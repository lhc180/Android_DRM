package cn.com.pyc.szpbb.sdk.database.dbBase;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.provider.BaseColumns;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import cn.com.pyc.szpbb.sdk.database.BaseDbManager;
import cn.com.pyc.szpbb.util.GenericsUtil;
import cn.com.pyc.szpbb.util.SZLog;

public class BaseDAO<T> {
    // 获取实体对象类型
    private Class<T> entityClass;
    protected SQLiteDatabase db;

    @SuppressWarnings("unchecked")
    public BaseDAO() {
        //eg: BaseDAOPracticeImpl<SZAlbum>---->SZAlbum.class;
        entityClass = GenericsUtil.getSuperClassGenricType(getClass());
        SZLog.i("entityClass: " + entityClass.getSimpleName());
        db = BaseDbManager.Builder().getSQLiteDatabase();
    }

    /**
     * 校验字段是否合法
     *
     * @param fieldName 字段名
     * @param fieldType 字段类型
     * @return Boolean
     */
    private boolean validField(String fieldName, Class fieldType) {
        return !fieldName.equalsIgnoreCase("serialVersionUID")
                && !fieldName.equals("CREATOR")
                && !fieldType.equals(Collections.class)
                && !fieldType.equals(Map.class)
                && !fieldType.equals(List.class)
                && !fieldType.equals(Set.class);
    }

    /**
     * 数据表创建方法
     *
     * @param entityClass entity Class
     */
    public void create(Class<T> entityClass) {
        String tableName = entityClass.getSimpleName();
        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                BaseColumns._ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT, ");
        Field[] fields = entityClass.getDeclaredFields(); // 得到当前类entityClass所有的成员变量
        for (Field field : fields) {
            String fieldName = field.getName(); // 变量名
            Class fieldType = field.getType(); // 变量类型
            if (validField(fieldName, fieldType) && !fieldName.equalsIgnoreCase("id")) {
                if (fieldType.equals(int.class)) {
                    sql.append(fieldName + " INTEGER, ");
                } else if (fieldType.equals(String.class)) {
                    sql.append(fieldName + " varchar, ");
                } else if (fieldType.equals(float.class) || fieldType.equals(double.class)) {
                    sql.append(fieldName + " varchar, ");
                } else if (fieldType.equals(long.class)) {
                    sql.append(fieldName + " varchar, ");
                }
            }
        }
        sql.replace(sql.lastIndexOf(","), sql.length(), ")");
        SZLog.d("create SQL: " + sql.toString());
        db.execSQL(sql.toString());
    }

    /*
     * 数据保存方法，包括： 单数据实体保存方法——save(T entity)； <br/>
     * 多数据实体保存方法——batchSave(Collection<T> entityCollection)；<br/>
     * 单数据实体级联保存方法——cascadedSave(T entity)；
     * 多数据实体级联保存方法——cascadedBatchSave(Collection<T> entityCollection)。
     */

    /*
     * 单数据实体保存方法
     */
    @SuppressWarnings("rawtypes")
    public synchronized boolean save(T entity) {
        // ContentValues负责存储一些键值对，键是一个String类型，而值是基本类型
        ContentValues contentValues = new ContentValues();
        //Field[] fields = entityClass.getDeclaredFields();
        Field[] fields = entity.getClass().getDeclaredFields();
        try {
            for (Field field : fields) {
                String fieldName = field.getName();
                Class fieldType = field.getType();
                if (validField(fieldName, fieldType)) {
                    //eg: fieldName = name;-----> getName
                    String methodName = "get" + ((char) (fieldName.charAt(0) - 32)) + fieldName
                            .substring(1);
                    //取得name的值
                    //String temp = entity.getClass().getMethod(methodName).invoke(entity)
                    // .toString();
                    //SZLog.i("fieldValue1: " + methodName + "=" + temp);
                    field.setAccessible(true);
                    Object obj = field.get(entity);
                    String temp = obj != null ? obj.toString() : "";
                    //SZLog.i(fieldName + ": " + temp);
                    if (methodName.equals("getId")) {
                        if (!"".equals(temp))
                            contentValues.put(BaseColumns._ID, temp);
                    } else {
                        contentValues.put(fieldName, temp);
                    }
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
//        } catch (InvocationTargetException e) {
//            e.printStackTrace();
//        } catch (NoSuchMethodException e) {
//            e.printStackTrace();
        }
        return db.insert(entityClass.getSimpleName(), null, contentValues) != -1;
    }

    /*
     * 多数据实体保存方法
     */
    @SuppressWarnings("rawtypes")
    public long[] batchSave(Collection<T> entityCollection) {
        Iterator it = entityCollection.iterator();
        long[] result = new long[entityCollection.size()];
        int count = 0;
        try {
            while (it.hasNext()) {
                Object singleEntity = it.next();
                ContentValues contentValues = new ContentValues();
                Field[] fields = singleEntity.getClass().getDeclaredFields();
                for (Field field : fields) {
                    String fieldName = field.getName();
                    Class fieldType = field.getType();
                    if (validField(fieldName, fieldType)) {
                        //TODO:
                        String methodName = "get" + (char) (fieldName.charAt(0) - 32) + fieldName
                                .substring(1);
                        String temp = singleEntity.getClass().getMethod(methodName).invoke
                                (singleEntity).toString();
                        if (methodName.equals("getId")) {
                            if (!"".equals(temp))
                                contentValues.put(BaseColumns._ID, temp);
                        } else {
                            contentValues.put(fieldName, temp);
                        }
                    }
                }
                result[count] = db.insert(singleEntity.getClass().getSimpleName(), null,
                        contentValues);
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

    /*
     * 级联保存
     */
    @SuppressWarnings("rawtypes")
    private long[] batchSaveEntity(List<Object> entityList) {
        final int size = entityList.size();
        long[] result = new long[size];
        try {
            for (int count = 0; count < size; count++) {
                ContentValues contentValues = new ContentValues();
                Object obj = entityList.get(count);
                Field[] fields = obj.getClass().getDeclaredFields();
                for (Field field : fields) {
                    String fieldName = field.getName();
                    Class fieldType = field.getType();
                    if (validField(fieldName, fieldType)) {
                        String methodName = "get" + (char) (fieldName.charAt(0) - 32) + fieldName
                                .substring(1);
                        String temp = obj.getClass().getMethod(methodName).invoke(obj).toString();
                        //SZLog.i(fieldName + " = " + temp);
                        if (methodName.equals("getId")) {
                            if (!"".equals(temp))
                                contentValues.put(BaseColumns._ID, temp);
                        } else
                            contentValues.put(fieldName, temp);
                    }
                }
                String tableName = obj.getClass().getSimpleName();
                SZLog.d("tableName: " + tableName);
                result[count] = db.insert(tableName, null, contentValues);
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

    @SuppressWarnings({"unchecked", "rawtypes"})
    private List<Object> saveIterator(List<Object> objList) {
        List<Object> tempList = new Vector<Object>();
        try {
            for (Object obj : objList) {
                Field[] fields = obj.getClass().getDeclaredFields();
                for (Field field : fields) {
                    if (field.getType().equals(List.class)) {
                        String methodName = "get" + (char) (field.getName().charAt(0) - 32) +
                                field.getName().substring(1);// 装配方法名称
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
        }
        return tempList;
    }

    /**
     * 单数据实体级联保存方法
     */
    public boolean cascadedSave(T entity) {
        save(entity);
        List<Object> objList = new Vector<Object>();
        objList.add(entity);
        boolean flag = true;
        while (flag) {
            objList = saveIterator(objList);
            if (!objList.isEmpty()) {
                flag = true;
            } else {
                flag = false;
            }
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
        while (flag) {
            objList = saveIterator(objList);
            if (!objList.isEmpty()) {
                flag = true;
            } else {
                flag = false;
            }
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
            id = entity.getClass().getMethod("getId").invoke(entity).toString();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return db.delete(entityClass.getSimpleName(), BaseColumns._ID + "=?", new String[]{id});
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

                String id = "";
                id = singleEntity.getClass().getMethod("getId")
                        .invoke(singleEntity).toString();
                result[count] = db.delete(singleEntity.getClass().getSimpleName(), BaseColumns
                        ._ID + "=?", new String[]{id});
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
        return db.delete(tableName, BaseColumns._ID + "=?", new String[]{id});
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
                        String childTableName = GenericsUtil.getFieldGenericType(
                                field).getSimpleName();// 装配子表名称
                        Cursor cursor = db.query(childTableName, null, obj.getClass()
                                .getSimpleName().charAt(0)
                                + obj.getClass().getSimpleName().substring(1)
                                + "_id =?", new String[]{foreignId}, null, null, null);
                        while (cursor.moveToNext()) {
                            Object singleObj = GenericsUtil.getFieldGenericType(field)
                                    .newInstance();
                            Field[] singleFields = singleObj.getClass()
                                    .getDeclaredFields();
                            for (Field fied : singleFields) {
                                String fiedName = fied.getName();
                                Class fiedType = fied.getType();
                                if (validField(fiedName, fiedType)) {
                                    String methodName = "set"
                                            + (char) (fiedName.charAt(0) - 32)
                                            + fiedName.substring(1);
                                    int column = 0;
                                    String value = "";
                                    if (methodName.equals("setId")) {
                                        column = cursor.getColumnIndex(BaseColumns._ID);
                                        value = cursor.getString(column);
                                    } else {
                                        column = cursor.getColumnIndex(fiedName);
                                        value = cursor.getString(column);
                                    }
                                    singleObj.getClass().getMethod(methodName, new Class[]{String
                                            .class}).invoke(singleObj, new Object[]{value});
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
        Cursor cursor = db.query(entityClass.getSimpleName(), null,
                BaseColumns._ID + "=?", new String[]{id}, null, null, null);
        try {
            while (cursor.moveToNext()) {
                T obj = entityClass.newInstance();
                Field[] fields = obj.getClass().getDeclaredFields();
                for (Field fied : fields) {
                    String fiedName = fied.getName();
                    Class fiedType = fied.getType();
                    if (validField(fiedName, fiedType)) {
                        String methodName = "set" + (char) (fiedName.charAt(0) - 32) + fiedName
                                .substring(1);
                        int column = 0;
                        String value = "";
                        if (methodName.equals("setId")) {
                            column = cursor.getColumnIndex(BaseColumns._ID);
                            value = cursor.getString(column);
                        } else {
                            column = cursor.getColumnIndex(fiedName);
                            value = cursor.getString(column);
                        }
                        obj.getClass().getMethod(methodName, new Class[]{String.class}).invoke
                                (obj, new Object[]{value});
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
        } catch (InstantiationException e1) {
            e1.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        Stack<StackItem> stack = new Stack<StackItem>();
        boolean flag = true;

        while (flag) {
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
     * 多数据实体级联删除方法
     */
    public boolean cascadedBatchDelete(Collection<T> entityCollection) {
        List<Object> objList = new Vector<Object>();
        objList.addAll(entityCollection);
        Stack<StackItem> stack = new Stack<StackItem>();

        boolean flag = true;
        while (flag) {
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

    /**
     * 数据更新方法，包括： 单数据实体更新方法——update(T entity)；<br/>
     * 多数据实体更新方法——batchUpdate(Collection<T> entityCollection)；<br/>
     * 单数据实体级联更新方法——cascadedUpdate(T entity)，<br/>
     * cascadedSetActiveById(Serializable id, boolean active)；<br/>
     * 多数据实体级联更新方法——cascadedBatchUpdate(Collection<T> entityCollection)。
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
            for (Field fied : fields) {
                String fiedName = fied.getName();
                Class fiedType = fied.getType();
                if (validField(fiedName, fiedType)) {

                    String methodName = "get"
                            + (char) (fiedName.charAt(0) - 32)
                            + fiedName.substring(1);
                    String temp = entity.getClass().getMethod(methodName)
                            .invoke(entity).toString();
                    contentValues.put(fiedName, temp);
                }
            }
            result = db.update(entityClass.getSimpleName(),
                    contentValues, "_id=?", new String[]{entity.getClass()
                            .getMethod("getId").invoke(entity).toString()});
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

    /*
     * 多数据实体更新方法
     */
    @SuppressWarnings("rawtypes")
    public int[] batchUpdate(Collection<T> entityCollection) {
        Iterator it = entityCollection.iterator();
        int[] result = new int[entityCollection.size()];
        int count = 0;
        try {
            while (it.hasNext()) {
                Object singleEntity = it.next();
                ContentValues contentValues = new ContentValues();
                Field[] fields = singleEntity.getClass().getDeclaredFields();
                for (Field fied : fields) {
                    String fiedName = fied.getName();
                    Class fiedType = fied.getType();
                    if (validField(fiedName, fiedType)) {
                        String methodName = "get"
                                + (char) (fiedName.charAt(0) - 32)
                                + fiedName.substring(1);
                        String temp = singleEntity.getClass().getMethod(methodName)
                                .invoke(singleEntity).toString();
                        contentValues.put(fiedName, temp);
                    }
                }
                result[count] = db.update(entityClass.getSimpleName(), contentValues, "_id=?",
                        new String[]{singleEntity.getClass()
                                .getMethod("getId").invoke(singleEntity).toString()});
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

    /*
     * 级联更新实体
     */
    @SuppressWarnings("rawtypes")
    private int[] batchUpdateEntity(List<Object> entityList) {
        int[] result = new int[entityList.size()];
        int count = 0;
        try {
            for (Object obj : entityList) {
                ContentValues contentValues = new ContentValues();
                Field[] fields = obj.getClass().getDeclaredFields();
                for (Field fied : fields) {
                    String fiedName = fied.getName();
                    Class fiedType = fied.getType();
                    if (!fiedName.equalsIgnoreCase("id")
                            && validField(fiedName, fiedType)) {
                        String methodName = "get"
                                + (char) (fiedName.charAt(0) - 32)
                                + fiedName.substring(1);
                        String temp = obj.getClass().getMethod(methodName)
                                .invoke(obj).toString();
                        contentValues.put(fiedName, temp);
                    }
                }
                result[count] = db.update(obj.getClass().getSimpleName(),
                        contentValues, "_id=?", new String[]{obj.getClass()
                                .getMethod("getId").invoke(obj).toString()});
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

    @SuppressWarnings({"rawtypes", "unchecked"})
    private List<Object> updateIterator(List<Object> objList) {
        List<Object> tempList = new Vector<Object>();
        try {
            for (Object obj : objList) {
                Field[] fields = obj.getClass().getDeclaredFields();
                for (Field field : fields) {
                    if (field.getType().equals(List.class)) {
                        String methodName = "get"
                                + (char) (field.getName().charAt(0) - 32)
                                + field.getName().substring(1);// 装配方法名称
                        List temp = (List) obj.getClass().getMethod(methodName)
                                .invoke(obj);
                        if (!temp.isEmpty()) {
                            batchUpdateEntity(temp);
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
        while (flag) {
            objList = updateIterator(objList);
            if (!objList.isEmpty()) {
                flag = true;
            } else {
                flag = false;
            }
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
        while (flag) {
            objList = updateIterator(objList);
            if (!objList.isEmpty()) {
                flag = true;
            } else {
                flag = false;
            }
        }
        return true;
    }

    @SuppressWarnings("rawtypes")
    public T findById(String id) {
        T obj = null;
        Cursor cursor = db.query(entityClass.getSimpleName(), null,
                BaseColumns._ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
        try {
            obj = entityClass.newInstance();
            Field[] fields = obj.getClass().getDeclaredFields();
            for (Field fied : fields) {
                String fiedName = fied.getName();
                Class fiedType = fied.getType();
                if (validField(fiedName, fiedType)) {
                    String methodName = "set"
                            + (char) (fiedName.charAt(0) - 32)
                            + fiedName.substring(1);
                    int column = 0;
                    String value = "";
                    if (methodName.equals("setId")) {
                        column = cursor
                                .getColumnIndex(BaseColumns._ID);
                        value = cursor.getString(column);
                    } else {
                        column = cursor.getColumnIndex(fiedName);
                        value = cursor.getString(column);
                    }
                    obj.getClass().getMethod(methodName, new Class[]{String.class}).invoke(obj,
                            new Object[]{value});
                }
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
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
    public List<T> findAll(Class<T> entityClass, String orderBy) {
        List<T> objList = new Vector<T>();
        Cursor cursor = db.query(entityClass.getSimpleName(), null, null,
                null, null, null, BaseColumns._ID + " " + orderBy);
        try {
            while (cursor.moveToNext()) {
                T obj = entityClass.newInstance();
                Field[] fields = obj.getClass().getDeclaredFields();
                for (Field field : fields) {
                    String fieldName = field.getName();
                    Class fieldType = field.getType();
                    if (validField(fieldName, fieldType)) {
                        String methodName = "set"
                                + (char) (fieldName.charAt(0) - 32)
                                + fieldName.substring(1);
                        int column = 0;
                        String value = "";
                        if (methodName.equals("setId")) {
                            column = cursor.getColumnIndex(BaseColumns._ID);
                            value = cursor.getString(column);
                        } else {
                            column = cursor.getColumnIndex(fieldName);
                            value = cursor.getString(column);
                        }
                        obj.getClass().getMethod(methodName, new Class[]{String.class})
                                .invoke(obj, new Object[]{value});
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
        Cursor cursor = db.rawQuery(sql, null);
        try {
            while (cursor.moveToNext()) {
                Object obj = entityClass.newInstance();
                Field[] fields = obj.getClass().getDeclaredFields();
                for (Field field : fields) {
                    String fieldName = field.getName();
                    Class fieldType = field.getType();
                    if (validField(fieldName, fieldType)) {
                        String methodName = "set"
                                + (char) (fieldName.charAt(0) - 32)
                                + fieldName.substring(1);
                        int column = 0;
                        String value = "";
                        if (methodName.equals("setId")) {
                            column = cursor.getColumnIndex(BaseColumns._ID);
                            value = cursor.getString(column);
                        } else {
                            column = cursor.getColumnIndex(fieldName);
                            value = cursor.getString(column);
                        }
                        obj.getClass().getMethod(methodName, new Class[]{String.class})
                                .invoke(obj, new Object[]{value});
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
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return objList;
    }

    public List<?> findByQuery(String[] elements, String[] args, Class<?> entityClass) {
        if (elements.length != args.length) return null;

        String tabelName = entityClass.getSimpleName();
        StringBuilder sql = new StringBuilder("SELECT * FROM " + tabelName + " WHERE ");
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

    public T findObjectByQuery(String[] elements, String[] args, Class<?> entityClass) {
        if (elements.length != args.length) return null;

        String tabelName = entityClass.getSimpleName();
        StringBuilder sql = new StringBuilder("SELECT * FROM " + tabelName + " WHERE ");
        for (int i = 0; i < elements.length; i++) {
            sql.append(elements[i] + " = '" + args[i] + "' ");
        }
        @SuppressWarnings("unchecked")
        T result = (T) findByQuery(sql.toString(), entityClass).get(0);
        return result;
    }
}
