package cn.com.pyc.drm.model.db.bean;

import java.io.Serializable;

public class AlbumContent implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -9148974058794534252L;
	private String id = "";
	private String name;
	private String content_id;
	private String album_id;
	private String modify_time;
	private String asset_id;
	
	public String getId(){
		return id;
	}
	public void setId(String id){
		this.id = id;
	}
	public String getName(){
		return name;
	}
	public void setName(String name){
		this.name = name;
	}
	public String getContent_id(){
		return content_id;
	}
	public void setContent_id(String content_id){
		this.content_id = content_id;
	}
	public String getAlbum_id(){
		return album_id;
	}
	public void setAlbum_id(String album_id){
		this.album_id = album_id;
	}
	public String getModify_time(){
		return modify_time;
	}
	public void setAsset_id(String asset_id){
		this.asset_id = asset_id;
	}
	public String getAsset_id(){
		return asset_id;
	}
	public void setModify_time(String modify_time){
		this.modify_time = modify_time;
	}
	@Override
	public String toString() {
		return "AlbumContent [id=" + id + ", name=" + name + ", content_id="
				+ content_id + ", album_id=" + album_id + ", modify_time="
				+ modify_time + ", asset_id=" + asset_id + "]";
	}
}
