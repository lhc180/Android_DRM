package cn.com.pyc.drm.model.db.bean;

import java.io.Serializable;

public class Album implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5802822985954317061L;

	private String id = "";
	private String name;
	private String right_id;
	private String product_id;
	private String modify_time;
	private String category;
	private String item_number;
	/** myProId */
	private String myproduct_id;
	private String author;
	private String username;
	private String picture;
	private String picture_ratio;
	private String publishDate;
	// 买家更新时间，既买家更新时间，item修改时间
	private String save_Last_add_time;
	// 卖家最后更新文件时间，存到本地。
	private String save_Last_modify_time;


	// 专辑里文件
	//private List<AlbumContent> albumContents = new LinkedList<AlbumContent>();

	public String getAuthor()
	{
		return author;
	}

	public void setAuthor(String author)
	{
		this.author = author;
	}

	public void setPicture_ratio(String picture_ratio)
	{
		this.picture_ratio = picture_ratio;
	}

	public String getPicture_ratio()
	{
		return picture_ratio;
	}

	public String getPublishDate()
	{
		return publishDate;
	}

	public void setPublishDate(String publishDate)
	{
		this.publishDate = publishDate;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getCategory()
	{
		return category;
	}

	public void setCategory(String category)
	{
		this.category = category;
	}

	public String getModify_time()
	{
		return modify_time;
	}

	public void setModify_time(String modify_time)
	{
		this.modify_time = modify_time;
	}

	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getItem_number()
	{
		return item_number;
	}

	public void setItem_number(String item_number)
	{
		this.item_number = item_number;
	}

	public String getProduct_id()
	{
		return product_id;
	}

	public void setProduct_id(String product_id)
	{
		this.product_id = product_id;
	}

	public String getRight_id()
	{
		return right_id;
	}

	public void setRight_id(String right_id)
	{
		this.right_id = right_id;
	}

	public String getPicture()
	{
		return picture;
	}

	public void setPicture(String picture)
	{
		this.picture = picture;
	}

	/** getmyProId */
	public String getMyproduct_id()
	{
		return myproduct_id;
	}

	public void setMyproduct_id(String myproduct_id)
	{
		this.myproduct_id = myproduct_id;
	}

//	public List<AlbumContent> getAlbumContents()
//	{
//		return albumContents;
//	}
//
//	public void setAlbumContents(List<AlbumContent> albumContents)
//	{
//		this.albumContents = albumContents;
//	}
//
//	public void addAlbumContent(AlbumContent albumContent)
//	{
//		this.albumContents.add(albumContent);
//	}

	public String getSave_Last_add_time()
	{
		return save_Last_add_time;
	}

	public void setSave_Last_add_time(String save_Last_add_time)
	{
		this.save_Last_add_time = save_Last_add_time;
	}

	public String getSave_Last_modify_time()
	{
		return save_Last_modify_time;
	}

	public void setSave_Last_modify_time(String save_Last_modify_time)
	{
		this.save_Last_modify_time = save_Last_modify_time;
	}

	@Override
	public String toString()
	{
		return "Album [id=" + id + ", name=" + name + ", category=" + category
				+ ", modify_time=" + modify_time + ", username=" + username
				+ ", item_number=" + item_number + ", product_id=" + product_id
				+ ", right_id=" + right_id + ", picture=" + picture
				+ ", myproduct_id=" + myproduct_id + ", author=" + author
				+ ", picture_ratio=" + picture_ratio + ", publishDate="
				+ publishDate + ", save_Last_add_time=" + save_Last_add_time
				+ ", save_Last_modify_time=" + save_Last_modify_time
				//+ ", albumContents=" + albumContents
				+ "]";
	}

}
