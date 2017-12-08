package cn.com.pyc.drm.bean.event;

/**
 * 同步正在下载文件的更新进度
 *
 * @author hudq
 */
@Deprecated
public class FileProgressStateEvent extends BaseEvent {

    private String fileId;

    public FileProgressStateEvent(String fileId) {
        super();
        this.fileId = fileId;
    }

    public String getFileId() {
        return fileId;
    }
}
