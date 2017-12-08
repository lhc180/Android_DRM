package cn.com.pyc.szpbb.sdk.database.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class SZAlbum implements Parcelable {
    private String id = "";
    private String name;
    private String category;
    private String modify_time;
    private String username;
    private String item_number;
    private String product_id;
    private String right_id;
    private String picture;
    /**
     * myProId
     */
    private String myproduct_id;
    private String author;
    private String publish_date;
    //private List<SZAlbumContent> albumContents = new LinkedList<SZAlbumContent>();


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.name);
        dest.writeString(this.category);
        dest.writeString(this.modify_time);
        dest.writeString(this.username);
        dest.writeString(this.item_number);
        dest.writeString(this.product_id);
        dest.writeString(this.right_id);
        dest.writeString(this.picture);
        dest.writeString(this.myproduct_id);
        dest.writeString(this.author);
        dest.writeString(this.publish_date);
        //dest.writeTypedList(this.albumContents);
    }

    public SZAlbum() {
    }

    protected SZAlbum(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
        this.category = in.readString();
        this.modify_time = in.readString();
        this.username = in.readString();
        this.item_number = in.readString();
        this.product_id = in.readString();
        this.right_id = in.readString();
        this.picture = in.readString();
        this.myproduct_id = in.readString();
        this.author = in.readString();
        this.publish_date = in.readString();
        //this.albumContents = in.createTypedArrayList(SZAlbumContent.CREATOR);
    }

    public static final Parcelable.Creator<SZAlbum> CREATOR = new Parcelable.Creator<SZAlbum>() {
        @Override
        public SZAlbum createFromParcel(Parcel source) {
            return new SZAlbum(source);
        }

        @Override
        public SZAlbum[] newArray(int size) {
            return new SZAlbum[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getModify_time() {
        return modify_time;
    }

    public void setModify_time(String modify_time) {
        this.modify_time = modify_time;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getItem_number() {
        return item_number;
    }

    public void setItem_number(String item_number) {
        this.item_number = item_number;
    }

    public String getProduct_id() {
        return product_id;
    }

    public void setProduct_id(String product_id) {
        this.product_id = product_id;
    }

    public String getRight_id() {
        return right_id;
    }

    public void setRight_id(String right_id) {
        this.right_id = right_id;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getMyproduct_id() {
        return myproduct_id;
    }

    public void setMyproduct_id(String myproduct_id) {
        this.myproduct_id = myproduct_id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPublish_date() {
        return publish_date;
    }

    public void setPublish_date(String publish_date) {
        this.publish_date = publish_date;
    }

    //    public List<SZAlbumContent> getAlbumContents() {
//        return albumContents;
//    }
//
//    public void setAlbumContents(List<SZAlbumContent> albumContents) {
//        this.albumContents = albumContents;
//    }
//
//    public void addAlbumContent(SZAlbumContent albumContent) {
//        this.albumContents.add(albumContent);
//    }

    @Override
    public String toString() {
        return "SZAlbum{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", modify_time='" + modify_time + '\'' +
                ", username='" + username + '\'' +
                ", item_number='" + item_number + '\'' +
                ", product_id='" + product_id + '\'' +
                ", right_id='" + right_id + '\'' +
                ", picture='" + picture + '\'' +
                ", myproduct_id='" + myproduct_id + '\'' +
                ", author='" + author + '\'' +
                ", publish_date='" + publish_date + '\'' +
                '}';
    }
}
