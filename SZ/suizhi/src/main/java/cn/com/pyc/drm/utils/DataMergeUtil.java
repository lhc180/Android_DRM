package cn.com.pyc.drm.utils;

import android.content.ContentValues;
import android.content.Context;
import android.provider.BaseColumns;
import android.text.TextUtils;

import net.sqlcipher.Cursor;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import cn.com.pyc.drm.common.Constant;
import cn.com.pyc.drm.model.db.bean.Album;
import cn.com.pyc.drm.model.db.bean.AlbumContent;
import cn.com.pyc.drm.model.db.bean.Asset;
import cn.com.pyc.drm.model.db.bean.Bookmark;
import cn.com.pyc.drm.model.db.bean.Perconattribute;
import cn.com.pyc.drm.model.db.bean.Perconstraint;
import cn.com.pyc.drm.model.db.bean.Permission;
import cn.com.pyc.drm.model.db.bean.Right;
import cn.com.pyc.drm.utils.help.DBHelper;
import cn.com.pyc.drm.utils.help.DRMDBHelper;

/**
 * 数据合并处理工具类
 * <p>
 * Created by hudaqiang on 2017/7/5.
 */

public class DataMergeUtil<T> {


    /**
     * 扫描本地sd卡中DRM下含有myproId的文件夹，并获取文件夹名（myProId） <br/>
     * <p>
     * Map: <br/>
     * key = userName, value = List<Ids>
     *
     * @return
     */
    public Map<String, List<String>> getAllBuyMyProductIdMap() {
        Map<String, List<String>> idGroup = new HashMap<>();
        String szPath = PathUtil.getSDCardRootPath() + PathUtil.getDefaultOffset();
        //获取到DRM文件夹下所有文件夹名称
        String[] szNames = new File(szPath).list();
        if (szNames == null) return idGroup;
        for (String szName : szNames) {
            if (TextUtils.isEmpty(szName))
                continue;
            File file = new File(szPath + szName + "/file");
            if (file.isDirectory() && file.exists()) {
                //获取到DRM/Name/file下所有文件夹名称
                String[] idNames = file.list();
                if (idNames == null)
                    continue;
                List<String> ids = new ArrayList<>();
                for (String idName : idNames) {
                    DRMLog.v("SD/DRM/" + szName + ": " + idName);
                    if (idGroup.containsKey(szName)) {
                        idGroup.get(szName).add(idName);
                    } else {
                        ids.add(idName);
                        idGroup.put(szName, ids);
                    }
                }
            }
        }
        return idGroup;
    }

    //获取本地sd卡中DRM下含有myproId的文件夹，并获取文件夹名（myProId）的集合
    public List<String> getAllBuyMyProductIdList() {
        String szPath = PathUtil.getSDCardRootPath() + PathUtil.getDefaultOffset();
        List<String> ids = new ArrayList<>();
        //获取到DRM文件夹下所有文件夹名称
        String[] szNames = new File(szPath).list();
        if (szNames == null) return ids;
        for (String szName : szNames) {
            if (TextUtils.isEmpty(szName))
                continue;
            File file = new File(szPath + szName + "/file");
            if (file.isDirectory() && file.exists()) {
                //获取到DRM/Name/file下所有文件夹名称
                String[] idNames = file.list();
                if (idNames == null)
                    continue;
                for (String idName : idNames) {
                    DRMLog.v("SD/DRM/" + szName + ": " + idName);
                    ids.add(idName);
                }
            }
        }
        return ids;
    }


    /**
     * 将一个数据库中的数据插入到目标数据库
     *
     * @param context
     * @param loginName 用户登录名
     */
    @SuppressWarnings("unchecked")
    public void insertDataFromTargetDB(Context context, String loginName) {
        File dbFile = context.getDatabasePath(loginName + DBHelper.DB_LABEL);
        if (dbFile == null || !dbFile.exists()) {
            DRMLog.e(dbFile != null ? dbFile.getPath() + " 不存在!" : "dbFile == null");
            return;
        }
        DRMLog.i("DB用户：" + dbFile.getPath());
        DRMDBHelper.destoryDBHelper();
        DBHelper helper = DBHelper.getInstance(context, loginName);
        //1.存AlbumContent
        DataMergeUtil<AlbumContent> ac_util = new DataMergeUtil<>();
        List<AlbumContent> albumContentList = ac_util.findAll(AlbumContent.class, helper);
        if (albumContentList == null || albumContentList.isEmpty()) {
            DRMDBHelper.destoryDBHelper();
            return;
        }
        DRMLog.i("DB文件：" + albumContentList.size());
        //2.存Album
        DataMergeUtil<Album> album_util = new DataMergeUtil<>();
        List<Album> albumList = album_util.findAll(Album.class, helper);
        //3.存Perconattribute
        DataMergeUtil<Perconattribute> ptt_util = new DataMergeUtil<>();
        List<Perconattribute> perconattributeList = ptt_util.findAll(Perconattribute.class, helper);
        //4.存Perconstraint
        DataMergeUtil<Perconstraint> pt_util = new DataMergeUtil<>();
        List<Perconstraint> perconstraintList = pt_util.findAll(Perconstraint.class, helper);
        //5.存Permission
        DataMergeUtil<Permission> per_util = new DataMergeUtil<>();
        List<Permission> permissionList = per_util.findAll(Permission.class, helper);
        //6.存Asset
        DataMergeUtil<Asset> asset_util = new DataMergeUtil<>();
        List<Asset> assetList = asset_util.findAll(Asset.class, helper);
        //7.存Right
        DataMergeUtil<Right> right_util = new DataMergeUtil<>();
        List<Right> rightList = right_util.findAll(Right.class, helper);
        //8.存Bookmark
        DataMergeUtil<Bookmark> bm_util = new DataMergeUtil<>();
        List<Bookmark> markList = bm_util.findAll(Bookmark.class, helper);

        DRMDBHelper.destoryDBHelper();
        DBHelper targetDB = DBHelper.getInstance(context, Constant.getAccountId());
        ac_util.saveData(albumContentList, targetDB);
        album_util.saveData(albumList, targetDB);
        ptt_util.saveData(perconattributeList, targetDB);
        pt_util.saveData(perconstraintList, targetDB);
        per_util.saveData(permissionList, targetDB);
        asset_util.saveData(assetList, targetDB);
        right_util.saveData(rightList, targetDB);
        bm_util.saveData(markList, targetDB);
        //TODO:
        context.deleteDatabase(loginName + DBHelper.DB_LABEL);
    }

    /**
     * 将一个文件夹里的文件复制到另一个文件夹
     *
     * @param loginName 用户登录名
     */
    public void copyFileFromTargetFolder(String loginName, FileUtils.OnCopyProgressListener
            listener) {
        String srcPath = new StringBuilder()
                .append(PathUtil.getSDCardRootPath())
                .append(PathUtil.getDefaultOffset())
                .append(loginName).toString();
        DRMLog.d("srcPath: " + srcPath);
        String destPath = new StringBuilder()
                .append(PathUtil.getSDCardRootPath())
                .append(PathUtil.getDefaultOffset())
                .append(Constant.getAccountId()).toString();
        DRMLog.d("destPath: " + destPath);
        File srcFile = new File(srcPath);
        FileUtils.copyFile(srcFile, new File(destPath), listener);
        //TODO:
        //FileUtils.deleteAllFile(srcFile); //删除原文件
    }


    private void saveData(List<T> dataList, DBHelper helper) {
        if (dataList == null || dataList.isEmpty()) return;
        for (T data : dataList) {
            boolean result = save(data, helper);
            DRMLog.e("插入状态：" + result);
        }
    }


    @SuppressWarnings("rawtypes")
    private synchronized boolean save(T entity, DBHelper helper) {
        // ContentValues负责存储一些键值对，键是一个String类型，而值是基本类型
        ContentValues contentValues = new ContentValues();
        Field[] fields = entity.getClass().getDeclaredFields();
        try {
            for (Field field : fields) {
                field.setAccessible(true);
                String fieldName = field.getName();
                Class fieldType = field.getType();
                if (Util_.isValidField(fieldName, fieldType)) {
                    String methodName = "get" + (char) (fieldName.charAt(0) - 32) + fieldName
                            .substring(1);
                    if (methodName.equals("getId")) {
                        String temp = entity.getClass().getMethod(methodName).invoke(entity)
                                .toString();
                        if (!"".equals(temp)) {
                            contentValues.put(BaseColumns._ID, temp);
                        }
                    } else {
                        String temp = entity.getClass().getMethod(methodName).invoke(entity)
                                .toString();
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
        return helper.insert(entity.getClass().getSimpleName(), contentValues) != -1;
    }

    @SuppressWarnings("rawtypes")
    private List<T> findAll(Class<T> clazz, DBHelper helper) {
        List<T> objList = new Vector<>();
        Cursor cursor = null;
        try {
            cursor = helper.query(clazz.getSimpleName(), null, null, null, null, null, null);
            while (cursor.moveToNext()) {
                T obj = clazz.newInstance();
                Field[] fields = obj.getClass().getDeclaredFields();
                for (Field field : fields) {
                    field.setAccessible(true);
                    String fieldName = field.getName();
                    Class fieldType = field.getType();
                    if (Util_.isValidField(fieldName, fieldType)) {
                        String methodName = "set" + (char) (fieldName.charAt(0) - 32) + fieldName
                                .substring(1);
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
        return objList;
    }

}
