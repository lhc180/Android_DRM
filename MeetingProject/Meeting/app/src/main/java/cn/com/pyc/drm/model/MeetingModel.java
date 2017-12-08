package cn.com.pyc.drm.model;

import java.util.ArrayList;

/**
 * Created by songyumei on 2017/8/22.
 */

public class MeetingModel {
    private MeetingName data;
    private boolean result;
//    {"data":{"id":"","verify":"","url":"","verifyurl":"","client_url":"","exact_createTime":"","name":"nullvalue","title":"nullvalue","createTime":"nullvalue"},"result":false}
    /*public MeetingName getData() {
        return data;
    }

    public void setData(MeetingName data) {
        this.data = data;
    }*/

    public MeetingName getData() {
        return data;
    }

    public void setData(MeetingName data) {
        this.data = data;
    }

    public boolean getResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public static class MeetingName {
        private String client_url;
        private String exact_createTime;
        private String id;
        private String url;
        private String verify;
        private String verifyurl;
        private String name;
        private String title;
        private String createTime;

        public String getClient_url() {
            return client_url;
        }

        public void setClient_url(String client_url) {
            this.client_url = client_url;
        }

        public String getVerifyurl() {
            return verifyurl;
        }

        public void setVerifyurl(String verifyurl) {
            this.verifyurl = verifyurl;
        }

        public String getVerify() {
            return verify;
        }

        public void setVerify(String verify) {
            this.verify = verify;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getExact_createTime() {
            return exact_createTime;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getCreateTime() {
            return createTime;
        }

        public void setCreateTime(String createTime) {
            this.createTime = createTime;
        }

        public void setExact_createTime(String exact_createTime) {
            this.exact_createTime = exact_createTime;
        }
    }
}
