package cn.com.pyc.drm.bean.event;

/**
 * EventBus 事件总线，处理基础类
 *
 * @author hudq
 */

public class BaseEvent {
    public static class Type {
        public static final byte UI_LOGINGUIDE_FINISH = 1;          //结束LoginGuideUI
        public static final byte UI_HOME_FINISH = 2;                //结束HomeUI
        public static final byte UI_LOGIN_FINISH = 3;               //结束登录界面

        public static final byte UI_BROWSER_CLEAR = 5;              //清除BrowserUI缓存
        public static final byte UI_HOME_TAB_1 = 6;                 //切换到主页我的内容

        public static final byte UPDATE_SETTING_BAR = 10;           //setting页面ProgressBar
        public static final byte UPDATE_MAIN_DOT = 11;              //home页面提示dot
        public static final byte UPDATE_SETTING_CACHE = 12;         //设置页面缓存大小显示

        public static final byte FILE_DOWNLOAD = 20;                //文件下载
        public static final byte FILE_UPDATE = 21;                  //文件更新
        public static final byte FILE_SELECT = 22;                  //文件选中与否
        public static final byte FILE_UN_SELECT = 23;               //文件选中与否
        public static final byte FILE_CANCEL = 24;                  //文件下载取消

        public static final byte UI_DIS_LOGIN = 31;                  //切换到发现界面
        public static final byte UI_HOME_TAB_1_LOGIN = 32;           //切换到我的内容
        public static final byte UI_HOME_TAB_3_LOGIN = 33;           //切换到个人中心

        public static final byte UI_SETTING_UPDATE = 40;             //通知个人中心刷新界面
        public static final byte UPDATE_DISCOVER = 41;               //更新发现界面

    }


}
