package cn.com.pyc.drm.model;

import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
/**
 * Created by simaben on 2014/9/1.
 */
public class AlbumListItem {

    public static final String AUTHORITY = "drm_albums";
    public static final Uri CONTENT_ALBUM_URI = Uri.parse("content://" + AUTHORITY + "/album");

    public long _id;
    public String albumName;
    public String albumId;
    public String albumPicture;
    public String albumRid;
    public String albumCategory;

    public static final String[] PROJECTION = new String[]{
    		BaseColumns._ID ,
            "albumName",
            "albumId",
            "picture",
            "rid",
            "albumCategory"
    };

    public AlbumListItem(Cursor cursor) {
        _id = cursor.getInt(cursor.getColumnIndexOrThrow(BaseColumns._ID));
        albumName = cursor.getString(cursor.getColumnIndexOrThrow( "albumName"));
        albumId = cursor.getString(cursor.getColumnIndexOrThrow( "albumId"));
        albumPicture = cursor.getString(cursor.getColumnIndexOrThrow("picture"));
        albumRid = cursor.getString(cursor.getColumnIndexOrThrow("rid"));
        albumCategory = cursor.getString(cursor.getColumnIndexOrThrow("albumCategory"));
    }

}
