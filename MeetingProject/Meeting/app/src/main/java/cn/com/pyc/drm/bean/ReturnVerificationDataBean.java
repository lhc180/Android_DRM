package cn.com.pyc.drm.bean;

/**
 * @author 李巷阳
 * @version V1.0
 * @Description: (用一句话描述该文件做什么)
 * @date 2017/3/15 17:22
 */
public class ReturnVerificationDataBean extends BaseBean {


    private String code;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    private verificationCodeData data;


    public void setData(verificationCodeData data) {
        this.data = data;
    }

    public verificationCodeData getData() {
        return data;
    }

    private static class verificationCodeData {
        private String title;
        private String url;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

    }

}
