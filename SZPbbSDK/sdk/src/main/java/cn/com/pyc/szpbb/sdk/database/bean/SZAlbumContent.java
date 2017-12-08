package cn.com.pyc.szpbb.sdk.database.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class SZAlbumContent implements Parcelable {
    private String id = "";
    private String mypro_id;
    private String name;
    private String content_id;
    private String album_id;
    private String modify_time;
    private String asset_id;
    private String file_type;
    private String file_path;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.mypro_id);
        dest.writeString(this.name);
        dest.writeString(this.content_id);
        dest.writeString(this.album_id);
        dest.writeString(this.modify_time);
        dest.writeString(this.asset_id);
        dest.writeString(this.file_type);
        dest.writeString(this.file_path);
    }

    public SZAlbumContent() {
    }

    private SZAlbumContent(Parcel in) {
        this.id = in.readString();
        this.mypro_id = in.readString();
        this.name = in.readString();
        this.content_id = in.readString();
        this.album_id = in.readString();
        this.modify_time = in.readString();
        this.asset_id = in.readString();
        this.file_type = in.readString();
        this.file_path = in.readString();
    }

    public static final Parcelable.Creator<SZAlbumContent> CREATOR = new Parcelable
            .Creator<SZAlbumContent>() {
        @Override
        public SZAlbumContent createFromParcel(Parcel source) {
            return new SZAlbumContent(source);
        }

        @Override
        public SZAlbumContent[] newArray(int size) {
            return new SZAlbumContent[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMypro_id() {
        return mypro_id;
    }

    public void setMypro_id(String mypro_id) {
        this.mypro_id = mypro_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent_id() {
        return content_id;
    }

    public void setContent_id(String content_id) {
        this.content_id = content_id;
    }

    public String getAlbum_id() {
        return album_id;
    }

    public void setAlbum_id(String album_id) {
        this.album_id = album_id;
    }

    public String getModify_time() {
        return modify_time;
    }

    public void setModify_time(String modify_time) {
        this.modify_time = modify_time;
    }

    public String getAsset_id() {
        return asset_id;
    }

    public void setAsset_id(String asset_id) {
        this.asset_id = asset_id;
    }

    public String getFile_type() {
        return file_type;
    }

    public void setFile_type(String file_type) {
        this.file_type = file_type;
    }

    public String getFile_path() {
        return file_path;
    }

    public void setFile_path(String file_path) {
        this.file_path = file_path;
    }

    @Override
    public String toString() {
        return "SZAlbumContent{" +
                "id='" + id + '\'' +
                ", mypro_id='" + mypro_id + '\'' +
                ", name='" + name + '\'' +
                ", content_id='" + content_id + '\'' +
                ", album_id='" + album_id + '\'' +
                ", modify_time='" + modify_time + '\'' +
                ", asset_id='" + asset_id + '\'' +
                ", file_type='" + file_type + '\'' +
                ", file_path='" + file_path + '\'' +
                '}';
    }
}
