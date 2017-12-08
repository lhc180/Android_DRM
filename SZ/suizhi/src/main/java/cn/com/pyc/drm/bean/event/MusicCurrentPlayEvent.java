package cn.com.pyc.drm.bean.event;

/**
 * 当前歌曲的id
 */
public class MusicCurrentPlayEvent extends BaseEvent {

    private String fileId;
    private String fileName;

    public MusicCurrentPlayEvent(String fileId,String fileName) {
        super();
        this.fileId = fileId;
        this.fileName = fileName;
    }

    public String getFileId() {
        return fileId;
    }

    public String getFileName() {
        return fileName;
    }
}
