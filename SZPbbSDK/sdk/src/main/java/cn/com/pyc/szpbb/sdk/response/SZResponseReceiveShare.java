package cn.com.pyc.szpbb.sdk.response;

public class SZResponseReceiveShare extends SZResponse {
    private String ftpUrl;

    public void setFtpUrl(String ftpUrl) {
        this.ftpUrl = ftpUrl;
    }

    public String getFtpUrl() {
        return ftpUrl;
    }
}
