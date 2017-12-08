package cn.com.pyc.drm.bean.event;

/**
 * EventBus 事件总线，数据表创建。。。
 * 
 * @author hudq
 * 
 */
public class DBMakerEvent extends BaseEvent
{

	private boolean maker;

	public boolean isMaker()
	{
		return maker;
	}

	public DBMakerEvent(boolean maker)
	{
		super();
		this.maker = maker;
	}

}
