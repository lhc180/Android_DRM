package cn.com.pyc.szpbb.sdk.database.bean;

public class SZBookmark
{

	private String id = "";
	private String content_id;
	private long time;
	private String content;
	private int pagefew;

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public long getTime()
	{
		return time;
	}

	public String getContent_id()
	{
		return content_id;
	}

	public void setContent_id(String content_id)
	{
		this.content_id = content_id;
	}

	public void setTime(long time)
	{
		this.time = time;
	}

	public String getContent()
	{
		return content;
	}

	public void setContent(String content)
	{
		this.content = content;
	}

	public int getPagefew()
	{
		return pagefew;
	}

	public void setPagefew(int pagefew)
	{
		this.pagefew = pagefew;
	}

}
