package cn.com.pyc.drm.bean;

/**
 * App版本的更新model
 */
public class VersionBean {

    private String version;         //版本号
    private String url;             //下载url

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}
