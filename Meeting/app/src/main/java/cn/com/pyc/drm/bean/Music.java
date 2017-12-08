package cn.com.pyc.drm.bean;

public class Music
{
	// 标题
	private String title;
	// 专辑ID
	private String albumId;
	// 文件权限ID
	private String productId;
	// 演唱者
	private String artist;
	// ID
	private String id;
	// 在SDcard上的播放路径
	private String path;
	// 时长
	private String duration;

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getAlbumId()
	{
		return this.albumId;
	}

	public void setAlbumId(String albumId)
	{
		this.albumId = albumId;
	}

	public String getProductId()
	{
		return this.productId;
	}

	public void setProductId(String productId)
	{
		this.productId = productId;
	}

	public String getArtist()
	{
		return artist;
	}

	public void setArtist(String artist)
	{
		this.artist = artist;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getPath()
	{
		return path;
	}

	public void setPath(String path)
	{
		this.path = path;
	}

	public String getDuration()
	{
		return duration;
	}

	public void setDuration(String duration)
	{
		this.duration = duration;
	}

}
