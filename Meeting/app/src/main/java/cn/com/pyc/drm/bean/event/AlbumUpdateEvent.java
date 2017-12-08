package cn.com.pyc.drm.bean.event;

import cn.com.pyc.drm.model.DownloadInfo;

/**
 * 专辑需要更新时候的事件（会议系统）
 * 
 * @author hudq
 * 
 */
public class AlbumUpdateEvent extends BaseEvent
{

	private String myProId;
	private DownloadInfo o;

	public AlbumUpdateEvent(DownloadInfo o, String myProId)
	{
		super();
		this.o = o;
		this.myProId = myProId;
	}

	public DownloadInfo getO()
	{
		return o;
	}

	public String getMyProId()
	{
		return myProId;
	}

}
