package cn.com.pyc.drm.model.music;

import java.io.Serializable;

/**
 * Created by simaben on 2014/8/13.
 */
public class Mp3Info implements Serializable{
    /**
	 * 
	 */
	private static final long serialVersionUID = -22160797230924437L;
	public long id;
    public String title;
    public String artist;
    public String url;
    public long duration;
    public long size;
}
