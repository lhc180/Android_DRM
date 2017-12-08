package cn.com.pyc.szpbb.sdk.models;

import android.os.Parcel;
import android.os.Parcelable;

import cn.com.pyc.szpbb.common.DownloadState;

/**
 * 要下载的信息. <br/>
 * 实现Parcelable接口<br/>
 * <br/>
 * 必须按照变量顺序写入和读取
 */
public class SZFolderInfo implements Parcelable {
    private String authors;
    private String category;
    private String myProId;
    private String picture_ratio;
    private String picture_url;

    private String preview;
    private String proId;
    private String publishDate;
    private long folderSize;
    private String productName;

    // 下载对应的ftpPath。没有即为空
    private String ftpUrl;
    // 任务状态，初始化
    private int taskState = DownloadState.INIT;
    // 下载的item位置
    private int position;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.authors);
        dest.writeString(this.category);
        dest.writeString(this.myProId);
        dest.writeString(this.picture_ratio);
        dest.writeString(this.picture_url);

        dest.writeString(this.preview);
        dest.writeString(this.proId);
        dest.writeString(this.publishDate);
        dest.writeLong(this.folderSize);
        dest.writeString(this.productName);

        dest.writeString(this.ftpUrl);
        dest.writeInt(this.taskState);
        dest.writeInt(this.position);
        // dest.writeInt(this.progress);
        // dest.writeLong(this.currentSize);
        // dest.writeLong(this.totalSize);
        // dest.writeString(this.theme);
    }

    public final static Parcelable.Creator<SZFolderInfo> CREATOR = new Creator<SZFolderInfo>() {

        @Override
        public SZFolderInfo[] newArray(int size) {

            return new SZFolderInfo[size];
        }

        @Override
        public SZFolderInfo createFromParcel(Parcel source) {
            SZFolderInfo o = new SZFolderInfo();
            o.setAuthors(source.readString());
            o.setCategory(source.readString());
            o.setMyProId(source.readString());
            o.setPicture_ratio(source.readString());
            o.setPicture_url(source.readString());
            o.setPreview(source.readString());
            o.setProId(source.readString());
            o.setPublishDate(source.readString());
            o.setFolderSize(source.readLong());
            o.setProductName(source.readString());
            o.setFtpUrl(source.readString());
            o.setTaskState(source.readInt());
            o.setPosition(source.readInt());
            return o;
        }
    };

    public String getAuthors() {
        return authors;
    }

    public void setAuthors(String authors) {
        this.authors = authors;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getMyProId() {
        return myProId;
    }

    public void setMyProId(String myProId) {
        this.myProId = myProId;
    }

    public String getPicture_ratio() {
        return picture_ratio;
    }

    public void setPicture_ratio(String picture_ratio) {
        this.picture_ratio = picture_ratio;
    }

    public String getPicture_url() {
        return picture_url;
    }

    public void setPicture_url(String picture_url) {
        this.picture_url = picture_url;
    }

    public String getPreview() {
        return preview;
    }

    public void setPreview(String preview) {
        this.preview = preview;
    }

    public String getProId() {
        return proId;
    }

    public void setProId(String proId) {
        this.proId = proId;
    }

    public String getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(String publishDate) {
        this.publishDate = publishDate;
    }

    public long getFolderSize() {
        return folderSize;
    }

    public void setFolderSize(long folderSize) {
        this.folderSize = folderSize;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getTaskState() {
        return taskState;
    }

    public void setTaskState(int taskState) {
        this.taskState = taskState;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void setFtpUrl(String ftpUrl) {
        this.ftpUrl = ftpUrl;
    }

    public String getFtpUrl() {
        return ftpUrl;
    }

}
