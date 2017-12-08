package cn.com.pyc.drm.bean.event;

public class UpdateDotsEvent extends BaseEvent
{
	private boolean show;

	public UpdateDotsEvent(boolean show)
	{
		this.show = show;
	}

	public boolean isShow()
	{
		return show;
	}

	public void setShow(boolean show)
	{
		this.show = show;
	}

}
