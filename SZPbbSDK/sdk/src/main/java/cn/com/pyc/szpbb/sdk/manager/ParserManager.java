package cn.com.pyc.szpbb.sdk.manager;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.json.JSONObject;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import cn.com.pyc.szpbb.common.Actions;
import cn.com.pyc.szpbb.common.Fields;
import cn.com.pyc.szpbb.common.K;
import cn.com.pyc.szpbb.sdk.SZInitInterface;
import cn.com.pyc.szpbb.sdk.bean.FILEFormat;
import cn.com.pyc.szpbb.sdk.database.bean.SZAlbum;
import cn.com.pyc.szpbb.sdk.database.bean.SZAlbumContent;
import cn.com.pyc.szpbb.sdk.database.bean.Asset;
import cn.com.pyc.szpbb.sdk.database.bean.Perconattribute;
import cn.com.pyc.szpbb.sdk.database.bean.Perconstraint;
import cn.com.pyc.szpbb.sdk.database.bean.Permission;
import cn.com.pyc.szpbb.sdk.database.bean.Right;
import cn.com.pyc.szpbb.sdk.database.practice.AlbumContentDAOImpl;
import cn.com.pyc.szpbb.sdk.database.practice.AlbumDAOImpl;
import cn.com.pyc.szpbb.sdk.database.practice.RightDAOImpl;
import cn.com.pyc.szpbb.sdk.db.DownData2DBManager;
import cn.com.pyc.szpbb.sdk.db.DownDataDBManager;
import cn.com.pyc.szpbb.sdk.models.SZFileData;
import cn.com.pyc.szpbb.sdk.models.SZFolderInfo;
import cn.com.pyc.szpbb.sdk.models.xml.OEX_Agreement.OEX_Asset;
import cn.com.pyc.szpbb.sdk.models.xml.OEX_Agreement.OEX_Permission;
import cn.com.pyc.szpbb.sdk.models.xml.OEX_Rights;
import cn.com.pyc.szpbb.sdk.models.xml.XML2JSON_Album;
import cn.com.pyc.szpbb.util.FileUtil;
import cn.com.pyc.szpbb.util.PathUtil;
import cn.com.pyc.szpbb.util.SZLog;

/**
 * 解析文件
 */
public class ParserManager {

    private static final String TAG = "ParserFile";

    private static ReentrantLock lock;

    private ParserManager() {
    }

    private static class ParserManagerInner {
        private static final ParserManager INSTANCE = new ParserManager();
    }

    public static ParserManager getInstance() {
        if (lock == null) {
            lock = new ReentrantLock(true);
        }
        return ParserManagerInner.INSTANCE;
    }

    private void unlock() {
        if (lock != null) {
            lock.unlock();
            lock = null;
        }
    }

    private void lock() {
        if (lock != null) {
            lock.lock();
        }
    }

    /**
     * 解析下载的文件夹
     *
     * @param context
     * @param o       {@link SZFolderInfo}
     */
    public void parserFolder(Context context, SZFolderInfo o) {
        SZLog.w("start parser file: " + o.getProductName());
        //context.sendBroadcast(new Intent(Actions.ACTION_PARSERING).putExtra(
        //        K.TAG_FOLDERINFO, o));
        String myProId = o.getMyProId();
        DownDataDBManager.Builder().deleteByFolderId(o.getMyProId()); //删除可能存在的记录
        // 下载文件路径
        final String drmPath = new StringBuffer()
                .append(PathUtil.DEF_DOWNLOAD_PATH)
                .append("/")
                .append(myProId)
                .append(Fields._DRM).toString();
        // 解析后文件保存路径
        final String decodePath = new StringBuffer()
                .append(PathUtil.DEF_FILE_PATH)
                .append("/")
                .append(myProId)
                .toString();
        try {
            // 创建解压目录
            FileUtil.createDirectory(drmPath);
            FileUtil.createDirectory(decodePath);

            lock();
            List<ParserEngine.CommonFile> list = ParserEngine.parserDRMFile(drmPath,
                    decodePath);
            if (list == null) {
                context.sendBroadcast(new Intent(Actions.ACTION_DOWNLOAD_ERROR)
                        .putExtra(K.TAG_FOLDERINFO, o));
                return;
            }

            XML2JSON_Album albumInfo = null;
            OEX_Rights rights = null;
            for (ParserEngine.CommonFile c : list) {
                if (c.filetype == FILEFormat.ALBUMINFO) {
                    albumInfo = ParserEngine.parserJSON(new File(c.filepath), list);
                } else if (c.filetype == FILEFormat.RIGHT) {
                    rights = ParserEngine.parserRight(new File(c.filepath));
                }
            }
            // 将专辑信息和权限插入表中
            if (albumInfo != null && rights != null) {
                if (insertFolderData(rights, albumInfo, list, o)) {
                    SZLog.d(TAG, "insert success");
                    for (ParserEngine.CommonFile c : list) {
                        FILEFormat filetype = c.filetype;
                        if (filetype == FILEFormat.ALBUMINFO
                                || filetype == FILEFormat.RIGHT) {
                            FileUtil.deleteFileWithPath(c.filepath);
                        }
                    }
                    FileUtil.deleteFileWithPath(drmPath);
                } else {
                    SZLog.d(TAG, "insert failed");
                }
            } else {
                Log.e(TAG, "文件有误，解析文件异常");
            }
            // 解析完成！
            Intent intent = new Intent(Actions.ACTION_FINISHED);
            intent.putExtra(K.TAG_FOLDERINFO, o);
            context.sendBroadcast(intent);
            SZLog.v(TAG, "end parser file: " + myProId);
        } catch (Exception e) {
            e.printStackTrace();
            context.sendBroadcast(new Intent(Actions.ACTION_DOWNLOAD_ERROR)
                    .putExtra(K.TAG_FOLDERINFO, o));
        } finally {
            unlock();
        }
    }

    /**
     * @param rights
     * @param albumInfo
     * @param mcFiles
     * @param o         {@link SZFolderInfo}
     * @return
     */
    private boolean insertFolderData(OEX_Rights rights, XML2JSON_Album albumInfo,
                                     List<ParserEngine.CommonFile> mcFiles, SZFolderInfo o) {
        boolean flag = false;
        String myProId = o.getMyProId();
        //final List<ParserEngine.CommonFile> mCommonFiles = mcFiles;
        String mAlbum_Id = AlbumDAOImpl.getInstance().findAlbumId(myProId);// 获取专辑ID
        if (mAlbum_Id == null || "".equals(mAlbum_Id)) {
            try {
                Right right = new Right();
                long currentTime = System.currentTimeMillis();
                right.setId(String.valueOf(currentTime));
                right.setPro_album_id("0");
                right.setRight_uid(rights.getContextMap().get("uid"));
                right.setRight_version(rights.getContextMap().get("version"));
                DateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale
                        .getDefault());
                String date = sDateFormat.format(new java.util.Date());
                right.setCreate_time(date);
                right.setAccount_id("1");
                right.setUsername(SZInitInterface.getUserName(true));
                // 4、 插入asset内容
                List<OEX_Asset> rightAssets = rights.getAgreement().getAssets();
                LinkedList<Asset> assets = new LinkedList<Asset>();
                for (int i = 0, count = rightAssets.size(); i < count; i++) {
                    Asset asset = new Asset();
                    asset.setId(String.valueOf(currentTime + i));
                    asset.setAsset_uid(rightAssets.get(i).getOdd_uid());
                    asset.setRight_id(right.getId());
                    asset.setCek_cipher_value(rightAssets.get(i).getCipheralue());
                    asset.setCek_encrypt_method(rightAssets.get(i).getEnc_algorithm());
                    asset.setCek_retrieval_key(rightAssets.get(i).getRetrieval_url());
                    asset.setDigest_method(rightAssets.get(i).getDigest_algorithm_key());
                    asset.setDigest_value(rightAssets.get(i).getDigest_algorithm_value());
                    asset.setCreate_time(date);
                    asset.setRight_version(right.getRight_version());
                    asset.setUsername(right.getUsername());
                    assets.add(asset);
                }
                int n = 0;
                // 3、 插入权限表及约束内容
                List<OEX_Permission> rightPermissions = rights.getAgreement().getPermission();
                for (int i = 0, count = rightPermissions.size(); i < count; i++) {
                    Permission permission = new Permission();
                    permission.setId(String.valueOf(currentTime + i));
                    int assetId = Integer.parseInt(rightPermissions.get(i).getAssent_id()
                            .substring(5)) - 1;
                    String assentId = assets.get(assetId).getId();
                    permission.setAsset_id(assentId);
                    permission.setCreate_time(date);
                    permission.setElement(String.valueOf(rightPermissions.get(i).getType()));
                    List<Map<String, String>> attributes = rightPermissions.get(i).getAttributes();
                    for (Map<String, String> map : attributes) {
                        for (Map.Entry<String, String> entry : map.entrySet()) {
                            n++;
                            Perconstraint perconstraint = new Perconstraint();
                            perconstraint.setId(String.valueOf(currentTime + n));
                            perconstraint.setElement(entry.getKey());
                            if ("datetime".equals(entry.getKey())) {
                                Perconattribute startPerconattribute = new Perconattribute();
                                Perconattribute endPerconattribute = new Perconattribute();
                                String start = rightPermissions.get(i).getStartTime();
                                String end = rightPermissions.get(i).getEndTime();
                                startPerconattribute.setElement("start");
                                startPerconattribute.setValue(start);
                                startPerconattribute.setCreate_time(date);
                                startPerconattribute.setPerconstraint_id(perconstraint.getId());
                                endPerconattribute.setElement("end");
                                endPerconattribute.setValue(end);
                                endPerconattribute.setCreate_time(date);
                                endPerconattribute.setPerconstraint_id(perconstraint.getId());
                                perconstraint.addPerconattributes(startPerconattribute);
                                perconstraint.addPerconattributes(endPerconattribute);
                            }
                            perconstraint.setPermission_id(permission.getId());
                            perconstraint.setValue(entry.getValue());
                            perconstraint.setCreate_time(date);
                            permission.addPerconstraint(perconstraint);
                        }
                    }
                    assets.get(assetId).addPermission(permission);
                }
                right.setAssets(assets);
                RightDAOImpl.getInstance().cascadedSave(right);

                // jsonObj变成局部变量
                JSONObject albObject = albumInfo.getInfoObj();
                SZAlbum album = new SZAlbum();
                album.setId(String.valueOf(currentTime));
                album.setName(albObject.getString("albumName"));
                album.setRight_id(albObject.getString("rid"));
                album.setProduct_id(albObject.getString("albumId"));
                album.setModify_time(date);
                album.setCategory(albObject.getString("albumCategory"));
                album.setPicture(albObject.getString("picture"));
                album.setItem_number(String.valueOf(albumInfo.getContentList().size() / 2));
                album.setUsername(SZInitInterface.getUserName(true));
                album.setMyproduct_id(myProId);
                album.setAuthor("");
                album.setPublish_date(o.getPublishDate());
                SZLog.d("insert", "album: " + album.toString());
                AlbumDAOImpl.getInstance().save(album);

                List<String> contents = albumInfo.getContentList();
                AlbumContentDAOImpl acDaoImpl = AlbumContentDAOImpl.getInstance();
                for (int i = 0, count = contents.size(); i < count; i += 2) {
                    SZAlbumContent albumContent = new SZAlbumContent();
                    for (int j = 0; j < assets.size(); j++) {
                        String Content_id = contents.get(i).replace("\"", "");
                        String odduid = assets.get(j).getAsset_uid();
                        if (Content_id.equals(odduid)) {
                            albumContent.setMypro_id(album.getMyproduct_id());
                            albumContent.setAlbum_id(album.getId());
                            albumContent.setModify_time(date);
                            albumContent.setName(contents.get(i + 1).replaceAll("\"", ""));
                            albumContent.setAsset_id(assets.get(j).getId());
                            albumContent.setContent_id(Content_id);
                            // 设置文件类型
                            for (int k = 0, size = mcFiles.size(); k < size; k++) {
                                ParserEngine.CommonFile file = mcFiles.get(k);
                                String fileName = file.filename;
                                String name = FileUtil.getNameFromFileName(fileName);
                                if (Content_id.equals(name)) {
                                    String extName = String.valueOf(file.filetype);
                                    albumContent.setFile_type(extName);
                                    albumContent.setFile_path(file.filepath);
                                }
                            }
                            //album.addAlbumContent(albumContent);
                            acDaoImpl.save(albumContent);
                            SZLog.d("insert", "albumContent: " + albumContent.toString());
                        }
                    }
                }
                //AlbumDAOImpl.getInstance().cascadedSave(album);
                flag = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return flag;
    }

    /**
     * 解析下载的单个文件
     *
     * @param context
     * @param o       {@link SZFileData}
     */
    public void parserFile(Context context, final SZFileData o) {
        SZLog.e("start parser file name: " + o.getName());
        //删除可能存在的记录
        DownData2DBManager.Builder().deleteByFileId(o.getFiles_id());

        SZInitInterface.checkFilePath();
        // 下载文件路径
        final String drmPath = new StringBuffer()
                .append(PathUtil.DEF_DOWNLOAD_PATH)
                .append(File.separator)
                .append(o.getFiles_id())
                .append(Fields._DRM).toString();
        // 解析后文件保存路径
        final String decodePath = new StringBuffer()
                .append(PathUtil.DEF_FILE_PATH)
                .append(File.separator)
                .append(o.getSharefolder_id()).toString();

        try {
            // 创建下载和解压目录
            FileUtil.createDirectory(drmPath);
            FileUtil.createDirectory(decodePath);

            lock();
            List<ParserEngine.CommonFile> list = ParserEngine.parserDRMFile(drmPath,
                    decodePath);
            if (list == null) {
                context.sendBroadcast(new Intent(Actions.ACTION_DOWNLOAD_ERROR)
                        .putExtra(K.TAG_FILEDATA, o));
                return;
            }
            XML2JSON_Album albumInfo = null;
            OEX_Rights rights = null;
            for (ParserEngine.CommonFile c : list) {
                if (c.filetype == FILEFormat.ALBUMINFO) {
                    albumInfo = ParserEngine.parserJSON(new File(c.filepath), list);
                } else if (c.filetype == FILEFormat.RIGHT) {
                    rights = ParserEngine.parserRight(new File(c.filepath));
                }
            }
            // 将专辑信息和权限插入表中
            if (albumInfo != null && rights != null) {
                if (insertFileData(rights, albumInfo, list, o)) {
                    SZLog.i("insert success");
                    for (ParserEngine.CommonFile c : list) {
                        if (c.filetype == FILEFormat.ALBUMINFO || c.filetype == FILEFormat.RIGHT) {
                            FileUtil.deleteFileWithPath(c.filepath);
                        }
                    }
                    FileUtil.deleteFileWithPath(drmPath);
                } else {
                    // 插入失败
                    SZLog.i("insert failed or already exist!");
                    context.sendBroadcast(new Intent(
                            Actions.ACTION_DOWNLOAD_ERROR).putExtra(
                            K.TAG_FILEDATA, o));
                }
            }
            // 解析完毕，发送广播通知更新
            context.sendBroadcast(new Intent(Actions.ACTION_FINISHED).putExtra(
                    K.TAG_FILEDATA, o));
            SZLog.e("end parser fileId: " + o.getFiles_id());
        } catch (Exception e) {
            e.printStackTrace();
            context.sendBroadcast(new Intent(Actions.ACTION_DOWNLOAD_ERROR)
                    .putExtra(K.TAG_FILEDATA, o));
        } finally {
            unlock();
        }
    }

    /**
     * 插入单个下载文件所对应的内容
     *
     * @param rights
     * @param albumInfo
     * @param mcFiles   {@link List<ParserEngine.CommonFile>}
     * @param o         {@link SZFileData}
     * @return
     */
    private boolean insertFileData(OEX_Rights rights, XML2JSON_Album albumInfo,
                                   List<ParserEngine.CommonFile> mcFiles, SZFileData o) {
        boolean flag;
        // SZAlbumContent ac = AlbumContentDAOImpl.getInstance()
        // .findAlbumContentByContentId(o.getFiles_id());
        AlbumContentDAOImpl acDaoImpl = AlbumContentDAOImpl.getInstance();
        boolean existData = acDaoImpl.existAlbumContent(o.getFiles_id());
        if (existData) return false;
        try {
            Right right = new Right();
            long currentTime = System.currentTimeMillis();
            right.setId(String.valueOf(currentTime));
            right.setPro_album_id("0");
            right.setRight_uid(rights.getContextMap().get("uid"));
            right.setRight_version(rights.getContextMap().get("version"));
            DateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale
                    .getDefault());
            String date = sDateFormat.format(new java.util.Date());
            right.setCreate_time(date);
            right.setAccount_id("1");
            right.setUsername(SZInitInterface.getUserName(true));
            // 4、 插入asset内容
            List<OEX_Asset> rightAssets = rights.getAgreement().getAssets();
            LinkedList<Asset> assets = new LinkedList<Asset>();
            for (int i = 0, count = rightAssets.size(); i < count; i++) {
                Asset asset = new Asset();
                asset.setId(String.valueOf(currentTime + i));
                asset.setAsset_uid(rightAssets.get(i).getOdd_uid());
                asset.setRight_id(right.getId());
                asset.setCek_cipher_value(rightAssets.get(i).getCipheralue());
                asset.setCek_encrypt_method(rightAssets.get(i).getEnc_algorithm());
                asset.setCek_retrieval_key(rightAssets.get(i).getRetrieval_url());
                asset.setDigest_method(rightAssets.get(i).getDigest_algorithm_key());
                asset.setDigest_value(rightAssets.get(i).getDigest_algorithm_value());
                asset.setCreate_time(date);
                asset.setRight_version(right.getRight_version());
                asset.setUsername(right.getUsername());
                assets.add(asset);
            }
            int n = 0;
            // 3、 插入权限表及约束内容
            List<OEX_Permission> rightPermissions = rights.getAgreement()
                    .getPermission();
            for (int i = 0, count = rightPermissions.size(); i < count; i++) {
                Permission permission = new Permission();
                permission.setId(String.valueOf(currentTime + i));
                int assetId = Integer.parseInt(rightPermissions.get(i).getAssent_id().substring
                        (5)) - 1;
                String assentId = assets.get(assetId).getId();
                permission.setAsset_id(assentId);
                permission.setCreate_time(date);
                permission.setElement(String.valueOf(rightPermissions.get(i).getType()));
                List<Map<String, String>> attributes = rightPermissions.get(i).getAttributes();
                for (Map<String, String> map : attributes) {
                    for (Map.Entry<String, String> entry : map.entrySet()) {
                        n++;
                        Perconstraint perconstraint = new Perconstraint();
                        perconstraint.setId(String.valueOf(currentTime + n));
                        perconstraint.setElement(entry.getKey());
                        if ("datetime".equals(entry.getKey())) {
                            Perconattribute startPerconattribute = new Perconattribute();
                            Perconattribute endPerconattribute = new Perconattribute();
                            String start = rightPermissions.get(i).getStartTime();
                            String end = rightPermissions.get(i).getEndTime();
                            startPerconattribute.setElement("start");
                            startPerconattribute.setValue(start);
                            startPerconattribute.setCreate_time(date);
                            startPerconattribute.setPerconstraint_id(perconstraint.getId());

                            endPerconattribute.setElement("end");
                            endPerconattribute.setValue(end);
                            endPerconattribute.setCreate_time(date);
                            endPerconattribute.setPerconstraint_id(perconstraint.getId());
                            perconstraint.addPerconattributes(startPerconattribute);
                            perconstraint.addPerconattributes(endPerconattribute);
                        }
                        perconstraint.setPermission_id(permission.getId());
                        perconstraint.setValue(entry.getValue());
                        perconstraint.setCreate_time(date);
                        permission.addPerconstraint(perconstraint);
                    }
                }
                assets.get(assetId).addPermission(permission);
            }
            right.setAssets(assets);
            RightDAOImpl.getInstance().cascadedSave(right);

            // jsonObj变成局部变量
            JSONObject albObject = albumInfo.getInfoObj();
            SZAlbum album = new SZAlbum();
            album.setId(String.valueOf(currentTime));
            album.setName(albObject.getString("albumName"));
            album.setRight_id(albObject.getString("rid"));
            album.setProduct_id(albObject.getString("albumId"));
            album.setModify_time(date);
            album.setCategory(albObject.getString("albumCategory"));
            album.setItem_number(String.valueOf(albumInfo.getContentList().size() / 2));
            album.setPicture(albObject.getString("picture"));
            album.setUsername(SZInitInterface.getUserName(true));
            album.setMyproduct_id(o.getSharefolder_id());
            album.setPublish_date(""); //TODO:
            album.setAuthor("");
            boolean existAlbum = AlbumDAOImpl.getInstance().existAlbum(o.getSharefolder_id());
            if (!existAlbum) {
                AlbumDAOImpl.getInstance().save(album);
                SZLog.d("insertFile", "album: " + album.toString());
            }
            List<String> contents = albumInfo.getContentList();
            for (int i = 0, count = contents.size(); i < count; i += 2) {
                SZAlbumContent albumContent = new SZAlbumContent();
                for (int j = 0; j < assets.size(); j++) {
                    String Content_id = contents.get(i).replace("\"", "");
                    String odduid = assets.get(j).getAsset_uid();
                    if (Content_id.equals(odduid)) {
                        albumContent.setAlbum_id(album.getId());
                        albumContent.setMypro_id(album.getMyproduct_id());
                        albumContent.setModify_time(date);
                        albumContent.setName(contents.get(i + 1).replaceAll("\"", ""));
                        albumContent.setAsset_id(assets.get(j).getId());
                        albumContent.setContent_id(Content_id);
                        // 设置文件类型和本地路径
                        for (ParserEngine.CommonFile file : mcFiles) {
                            String fileName = file.filename;
                            String name = FileUtil.getNameFromFileName(fileName);
                            if (Content_id.equals(name)) {
                                String extName = String.valueOf(file.filetype);
                                albumContent.setFile_type(extName);
                                albumContent.setFile_path(file.filepath);
                            }
                        }
                        acDaoImpl.save(albumContent);
                        SZLog.d("insertFile", "albumContent: " + albumContent.toString());
                        //album.addAlbumContent(albumContent);
                    }
                }
            }
            //AlbumDAOImpl.getInstance().cascadedSave(album);
            flag = true;
        } catch (Exception e) {
            e.printStackTrace();
            flag = false;
        }
        return flag;
    }

}
