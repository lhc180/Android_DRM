package cn.com.pyc.drm.model.dbBase;

import java.util.UUID;

public abstract class SZBaseDataBean implements SZBaseBean {
	private static final long serialVersionUID = 6766950262685798336L;
	public SZBaseDataBean(){
		setId(UUID.randomUUID().toString());
	}
}
