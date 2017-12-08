package cn.com.pyc.drm.bean.event;

public class MusicSwitchNameEvent extends BaseEvent
{

	private String musicName;
	private boolean hasPermitted;

	public MusicSwitchNameEvent(String musicName, boolean hasPermitted)
	{
		super();
		this.musicName = musicName;
		this.hasPermitted = hasPermitted;
	}

	public String getMusicName()
	{
		return musicName;
	}

	public boolean isHasPermitted()
	{
		return hasPermitted;
	}

}
