package cn.com.pyc.drm.model.db.bean;

import java.io.Serializable;

public class Bookmark implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 4882535015404578943L;
	private String id = "";// 唯一id
	private String content_ids;
	private String time;
	private String content;
	private String pagefew;

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getTime()
	{
		return time;
	}

	public String getContent_ids()
	{
		return content_ids;
	}

	public void setContent_ids(String content_ids)
	{
		this.content_ids = content_ids;
	}

	public void setTime(String time)
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

	public String getPagefew() {
		return pagefew;
	}

	public void setPagefew(String pagefew) {
		this.pagefew = pagefew;
	}




}
