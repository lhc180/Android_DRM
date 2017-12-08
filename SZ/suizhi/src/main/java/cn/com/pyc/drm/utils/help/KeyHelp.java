package cn.com.pyc.drm.utils.help;

/**
 * Created by hudq on 2017/3/28.
 */

public class KeyHelp {
    //public static final int ALL_PAUSE = 1;
    //public static final int SOME_DOWNLOAD = 2;

    //下载文件相关
    public static final String DF_TASK_ID = "taskId";
    public static final String DF_FILEDATA = "FileData";
    public static final String DF_CURRENTSIZE = "currentSize";
    public static final String DF_PROGRESS = "progress";
    public static final String DF_ISLAST = "isLastProgress";
    public static final String DF_CODE = "code";

    //列表传递KEY
    public static final String KEY_FROM_CHECK = "from_check_share";
    public static final String KEY_MYPRO_ID = "myProId";
    public static final String KEY_PRO_NAME = "product_name";
    public static final String KEY_FILE_ID = "cur_fileId";
    public static final String KEY_PRO_URL = "album_pic";
    public static final String KEY_LRC_ID = "lrc_id";
    public static final String KEY_PRO_CATEGORY = "category";
    public static final String KEY_SAVE_CONTENT = "save_albumContent";
    public static final String KEY_FILE_LIST = "file_listData";

    //音乐悬浮控件相关，MusicViewService
    public static final String MVS_NAME = "mvs_product_name";
    public static final String MVS_MYPROID = "mvs_myproduct_id";
    public static final String MVS_MYPROURL = "mvs_product_url";
    public static final String MVS_FILE_LIST = "mvs_file_list";
    //public static final String MVS_FILEID = "mvs_file_id";
    public static final String MVS_OPTION = "mvs_option";

    //音乐播放相关，MusicPlayService
    public static final String MPS_OPTION = "mps_option";
    public static final String MPS_FILES = "mps_files";
    public static final String MPS_FILE_ID = "mps_fileId";
    public static final String MPS_MYPRODUCT_ID = "mps_myProductId";
    public static final String MPS_PRODUCT_URL = "mps_productUrl";
    public static final String MPS_PROGRESS = "mps_progress";

    //是否是从登录进入的标志
    public static final String LOGIN_FLAG = "login_flag";

    //点击我的内容进入注册界面的标志
    public static final String SWICH_CONTENT = "swich_content";
    //登录,注销,我的内容点击后进行登录的标识,根据标识的不同分别切换到设置界面,发现界面和我的内容界面
    public static final String SWICH_CONTENT_2 = "SWICH_CONTENT2";
}
