package cn.com.pyc.drm.bean.event;

/**
 * 处理页面UI相关
 */
public class ConductUIEvent extends BaseEvent {

    private int type;
    private boolean net;

    public ConductUIEvent(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setNet(boolean net) {
        this.net = net;
    }

    public boolean isNet() {
        return net;
    }
}
