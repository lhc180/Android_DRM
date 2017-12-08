package cn.com.pyc.szpbb.sdk;

import java.util.List;

import cn.com.pyc.szpbb.sdk.authentication.SZContentPermission;
import cn.com.pyc.szpbb.sdk.database.BaseDbManager;
import cn.com.pyc.szpbb.sdk.database.bean.SZAlbum;
import cn.com.pyc.szpbb.sdk.database.bean.SZAlbumContent;
import cn.com.pyc.szpbb.sdk.database.practice.AlbumContentDAOImpl;

/**
 * 文件操作管理类
 * <p>
 * Created by hudq on 2016/12/8.
 */
public class SZFileInterface {

    /**
     * 根据文件fileId查询所对应的文件
     *
     * @param fileId 文件id,{@link SZAlbumContent}中contentId
     * @return {@link SZAlbumContent}
     */
    public static SZAlbumContent findAlbumContent(String fileId) {
        return AlbumContentDAOImpl.getInstance().findAlbumContentByContentId(fileId);
    }

    /**
     * 根据文件夹id查询所有的文件
     *
     * @param folderId 文件夹id,{@link SZAlbum}中的myproduct_id
     * @return List
     */
    public static List<SZAlbumContent> findAllAlbumContent(String folderId) {
        return AlbumContentDAOImpl.getInstance().findAlbumContentByMyProId(folderId);
    }

    /**
     * 根据文件fileId删除对应的文件
     *
     * @param fileId 文件id,{@link SZAlbumContent}中contentId
     * @return int, 影响的行数
     */
    public static int deleteAlbumContentByFileId(String fileId) {
        return AlbumContentDAOImpl.getInstance().deleteAlbumContentByContenId(fileId);
    }

    /**
     * 更新文件内容(更新)
     *
     * @param ac {@link SZAlbumContent}
     * @return int, 影响的行数
     */
    public static int updateAlbumContent(SZAlbumContent ac) {
        return AlbumContentDAOImpl.getInstance().updateAlbumContent(ac);
    }

    /**
     * 删除文件夹下所有的文件
     *
     * @param folderId 文件夹id,{@link SZAlbum}
     * @return int, 影响的行数
     */
    public static int deleteAlbumContentByFolderId(String folderId) {
        return AlbumContentDAOImpl.getInstance().deleteAlbumContentByMyProId(folderId);
    }

    /**
     * 是否存在此文件记录
     *
     * @param fileId 文件id，即{@link SZAlbumContent}中的contentId
     * @return boolean
     */
    public static boolean existAlbumContent(String fileId) {
        return AlbumContentDAOImpl.getInstance().existAlbumContent(fileId);
    }


    /**
     * 根据文件夹id,删除文件夹相关表中的数据
     *
     * @param folderId 文件夹folderId，即{@link SZAlbum}中的myProId
     */
    @Deprecated
    public static void deleteFolderInfos(String folderId) {
        BaseDbManager.Builder().deleteAlbumAttachInfos(folderId);
    }

    /**
     * 根据文件id，删除文件相关联的表的数据
     *
     * @param fileId 文件fileId，即{@link SZAlbumContent}中的contentId
     */
    public static void deleteFileInfos(String fileId) {
        BaseDbManager.Builder().deleteAlbumContentAttachInfos(fileId);
    }

    /**
     * 获取文件的权限信息
     *
     * @param albumContent {@link SZAlbumContent}
     * @return {@link SZContentPermission}
     */
    public static SZContentPermission getPermission(SZAlbumContent albumContent) {
        return new SZContentPermission(albumContent);
    }


}
