package cn.com.pyc.drm.model;

/**
 * Created by hudq on 2017/3/31.
 */

public class DataModel extends BaseModel {

    private Data data;

    public void setData(Data data) {
        this.data = data;
    }

    public Data getData() {
        return data;
    }

    public static class Data {
        private String url;             //下载url
        private String fileInfo;        //加密证书
        private String logId;           //阅读日志id（接口数据存在时返回）

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getFileInfo() {
            return fileInfo;
        }

        public void setFileInfo(String fileInfo) {
            this.fileInfo = fileInfo;
        }

        public String getLogId() {
            return logId;
        }

        public void setLogId(String logId) {
            this.logId = logId;
        }
    }
}
