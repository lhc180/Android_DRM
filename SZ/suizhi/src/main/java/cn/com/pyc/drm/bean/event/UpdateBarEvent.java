package cn.com.pyc.drm.bean.event;

public class UpdateBarEvent extends BaseEvent {

    private boolean isShow;
    private int type = Type.UPDATE_SETTING_BAR;

    public UpdateBarEvent(boolean isShow, int type) {
        super();
        this.isShow = isShow;
        this.type = type;
    }

    public boolean isShow() {
        return isShow;
    }

    public int getType() {
        return type;
    }
}
