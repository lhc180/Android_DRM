package cn.com.pyc.drm.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.com.pyc.drm.R;
import cn.com.pyc.drm.common.DrmPat;
import cn.com.pyc.drm.common.MusicMode;
import cn.com.pyc.drm.model.FileData;
import cn.com.pyc.drm.model.db.bean.Album;
import cn.com.pyc.drm.model.db.bean.AlbumContent;
import cn.com.pyc.drm.ui.LoginActivity;
import cn.com.pyc.drm.utils.help.DRMDBHelper;
import cn.com.pyc.drm.utils.help.MusicHelp;
import cn.com.pyc.drm.utils.manager.ActicityManager;

public class Util_ {

    /**
     * 检查登录
     *
     * @return
     */
    public static boolean checkLogin() {
        //String username = (String) SPUtils.get(DRMUtil.KEY_REMEMBER_NAME, "");
        String accountId = (String) SPUtils.get(DRMUtil.KEY_ACCOUNT_ID, "");
        String token = (String) SPUtils.get(DRMUtil.KEY_SUPER_TOKEN, "");
        return !TextUtils.isEmpty(accountId) && !TextUtils.isEmpty(token);
    }

    /**
     * 获取位置
     *
     * @param contentId
     * @param files
     * @return
     */
    public static int getContentIndex(String contentId, List<AlbumContent> files) {
        int startPos = 0;
        if (contentId == null) return startPos;
        contentId = contentId.replaceAll("\"", "");
        for (int i = 0, size = files.size(); i < size; i++) {
            AlbumContent ac = files.get(i);
            if (contentId.equals(ac.getContent_id())) {
                DRMLog.d("fileName[" + i + "]: " + ac.getName());
                startPos = i;
                break;
            }
        }
        return startPos;
    }

    public static int getFileIndex(String itemId, List<FileData> files) {
        int startPos = -1;
        if (itemId == null) return startPos;
        int size = files.size();
        for (int i = 0; i < size; i++) {
            FileData data = files.get(i);
            if (itemId.equals(data.getItemId())) {
                //DRMLog.d("fileName[" + i + "]: " + data.getContent_name());
                startPos = i;
                break;
            }
        }
        return startPos;
    }

    /**
     * 检查文件是否存在本地
     *
     * @param ac
     * @param filePath
     * @param callback
     */
    public static boolean checkFileExist(Context ac, String filePath,
                                         ViewUtil.DialogCallBack callback) {
        boolean exist = true;
        if (!FileUtils.checkFilePathExists(filePath)) {
            exist = false;
            DRMLog.e("文件不存在");
            ViewUtil.showSingleCommonDialog(ac, null,
                    ac.getString(R.string.fail_to_open_file),
                    ac.getString(R.string.close), callback);
        }
        return exist;
    }

    /**
     * 分集下载文件后，保存的专辑有重复，但myProId唯一
     *
     * @param albums
     * @return
     */
    public static List<Album> wipeRepeatAlbumData(List<Album> albums) {
        List<Album> list = new ArrayList<Album>();
        String id = null;
        for (Album album : albums) {
            if (!TextUtils.equals(id, album.getMyproduct_id())) {
                list.add(album);
            }
            id = album.getMyproduct_id();
        }
        return list;
    }

    /**
     * 根据类别，获取扩展名称
     *
     * @param category
     * @return
     */
    public static String getExtTypeName(String category) {
        String ext = "";
        if (TextUtils.isEmpty(category)) return ext;
        switch (category) {
            case DrmPat.MUSIC:
            case "音乐":
                ext = DrmPat.MP3;
                break;
            case DrmPat.BOOK:
            case "图书":
                ext = DrmPat.PDF;
                break;
            case DrmPat.VIDEO:
            case "视频":
                ext = DrmPat.MP4;
                break;
            default:
                break;
        }
        return ext;
    }

    /**
     * 返回的种类可能是中文类别
     *
     * @param category
     * @return
     */
    public static String getAlbumCategory(String category) {
        String nameCategory = "";
        if (TextUtils.isEmpty(category)) return nameCategory;
        switch (category) {
            case DrmPat.MUSIC:
            case "音乐":
                nameCategory = DrmPat.MUSIC;
                break;
            case DrmPat.BOOK:
            case "图书":
                nameCategory = DrmPat.BOOK;
                break;
            case DrmPat.VIDEO:
            case "视频":
                nameCategory = DrmPat.VIDEO;
                break;
            default:
                break;
        }
        return nameCategory;
    }

    /**
     * 设置listview的EmptyView，在加载数据为空或没有数据的时候使用
     *
     * @param lv
     * @param emptyView
     * @param tips
     */
    public static final void setEmptyViews(ListView lv, View emptyView, String tips) {
        if (lv == null)
            return;

        TextView tipTextView = ((TextView) emptyView.findViewById(R.id.empty_text));
        tipTextView.setTextColor(Color.parseColor("#666666"));
        tipTextView.setText(tips);
        lv.setEmptyView(emptyView);
    }


    /**
     * 重复登录了，token验证失败/登录已过期
     */
    public static void repeatLogin(Activity context) {
        ActicityManager.getInstance().remove(context);
        if (MusicMode.STATUS != MusicMode.Status.STOP) {
            MusicHelp.release(context);
        }
        DRMDBHelper.setInitData(context);
        Bundle bundle = new Bundle();
        bundle.putBoolean(DRMUtil.KEY_REPEAT_LOGIN, true);
        OpenPageUtil.openActivity(context, LoginActivity.class, bundle);
        context.finish();
    }

    //字段是否是对应的type
    public static boolean isValidField(String fieldName, Class fieldType) {
        return !fieldName.equalsIgnoreCase("serialVersionUID")
                && !fieldType.equals(Collections.class)
                && !fieldType.equals(Map.class)
                && !fieldType.equals(List.class)
                && !fieldType.equals(Set.class);
    }

    //移除已经下载存在的元素，返回没有下载的
    public static List<FileData> wipeDownloadData(List<FileData> sourceData,
                                                  List<AlbumContent> content) {
        List<FileData> tempData = new ArrayList<>(sourceData);
        for (FileData data : sourceData) {
            String itemId = data.getItemId();
            for (AlbumContent albumContent : content) {
                if (TextUtils.equals(itemId, albumContent.getContent_id())) {
                    tempData.remove(data);
                }
            }
        }
        return tempData;
    }

    //移除未下载的元素，返回已经下载的
    public static List<FileData> wipeNotDownloadData(List<FileData> sourceData,
                                                     List<AlbumContent> content) {
        List<FileData> tempData = new ArrayList<>(sourceData);
        List<FileData> unDownloadData = wipeDownloadData(sourceData, content);
        tempData.removeAll(unDownloadData);
        return tempData;
    }

}
