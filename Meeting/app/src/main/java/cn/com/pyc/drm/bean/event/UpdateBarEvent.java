package cn.com.pyc.drm.bean.event;

public class UpdateBarEvent extends BaseEvent
{

	private boolean isShowBar;

	public UpdateBarEvent(boolean isShowBar)
	{
		super();
		this.isShowBar = isShowBar;
	}

	public boolean isShowBar()
	{
		return isShowBar;
	}

}
