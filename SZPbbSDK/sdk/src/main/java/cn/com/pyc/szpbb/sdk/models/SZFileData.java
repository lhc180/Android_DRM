package cn.com.pyc.szpbb.sdk.models;

import android.os.Parcel;
import android.os.Parcelable;

import cn.com.pyc.szpbb.common.DownloadState;

public class SZFileData implements Parcelable {
    private String id;
    private String files_id;
    private String name;
    private String sharefolder_id;
    private long last_modify_time;
    private long fileSize;
    /**当前集合中的索引位置**/
    private int position;
    private String ftpUrl;
    private int progress;
    private int taskState = DownloadState.INIT;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.files_id);
        dest.writeString(this.name);
        dest.writeString(this.sharefolder_id);
        dest.writeLong(this.last_modify_time);
        dest.writeLong(this.fileSize);
        dest.writeInt(this.position);
        dest.writeString(this.ftpUrl);
        dest.writeInt(this.progress);
        dest.writeInt(this.taskState);
    }

    public SZFileData() {
    }

    private SZFileData(Parcel in) {
        this.id = in.readString();
        this.files_id = in.readString();
        this.name = in.readString();
        this.sharefolder_id = in.readString();
        this.last_modify_time = in.readLong();
        this.fileSize = in.readLong();
        this.position = in.readInt();
        this.ftpUrl = in.readString();
        this.progress = in.readInt();
        this.taskState = in.readInt();
    }

    public static final Parcelable.Creator<SZFileData> CREATOR = new Parcelable.Creator<SZFileData>() {
        @Override
        public SZFileData createFromParcel(Parcel source) {
            return new SZFileData(source);
        }

        @Override
        public SZFileData[] newArray(int size) {
            return new SZFileData[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFiles_id() {
        return files_id;
    }

    public void setFiles_id(String files_id) {
        this.files_id = files_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSharefolder_id() {
        return sharefolder_id;
    }

    public void setSharefolder_id(String sharefolder_id) {
        this.sharefolder_id = sharefolder_id;
    }

    public long getLast_modify_time() {
        return last_modify_time;
    }

    public void setLast_modify_time(long last_modify_time) {
        this.last_modify_time = last_modify_time;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getTaskState() {
        return taskState;
    }

    public void setTaskState(int taskState) {
        this.taskState = taskState;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getProgress() {
        return progress;
    }

    public String getFtpUrl() {
        return ftpUrl;
    }

    public void setFtpUrl(String ftpUrl) {
        this.ftpUrl = ftpUrl;
    }

    @Override
    public String toString() {
        return "SZFileData{" +
                "id='" + id + '\'' +
                ", files_id='" + files_id + '\'' +
                ", name='" + name + '\'' +
                ", sharefolder_id='" + sharefolder_id + '\'' +
                ", last_modify_time=" + last_modify_time +
                ", fileSize=" + fileSize +
                ", position=" + position +
                ", taskState=" + taskState +
                '}';
    }
}
