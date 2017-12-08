package cn.com.pyc.drm.model.db.bean;

/**
 * 文件的更新记录
 * Created by hudq on 2017/2/16.
 */

public class AlbumContentHistory {

    private String itemId;
    private String collectionId; //外键，对应AlbumContent中collectionId;
    private String content_name;
    private long content_size;
    private String content_format;
    private int page_num;
    private double length;
    private String version;
    private String versionInfo;
    private long play_progress;

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getVersionInfo() {
        return versionInfo;
    }

    public void setVersionInfo(String versionInfo) {
        this.versionInfo = versionInfo;
    }

    public String getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(String collectionId) {
        this.collectionId = collectionId;
    }

    public String getContent_name() {
        return content_name;
    }

    public void setContent_name(String content_name) {
        this.content_name = content_name;
    }

    public long getContent_size() {
        return content_size;
    }

    public void setContent_size(long content_size) {
        this.content_size = content_size;
    }

    public String getContent_format() {
        return content_format;
    }

    public void setContent_format(String content_format) {
        this.content_format = content_format;
    }

    public int getPage_num() {
        return page_num;
    }

    public void setPage_num(int page_num) {
        this.page_num = page_num;
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setPlay_progress(long play_progress) {
        this.play_progress = play_progress;
    }

    public long getPlay_progress() {
        return play_progress;
    }
}
