package cn.com.pyc.drm.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description: (用一句话描述该文件做什么)
 * @author 李巷阳
 * @date 2016-8-18 下午5:26:41
 * @version V1.0
 */
public class ReturnMechanismDataBean {
	private String result;

	private String msg;

	private List<MechanismBean> token;

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public List<MechanismBean> getToken() {
		if (token == null)
			token = new ArrayList<>();
		return token;
	}

	public void setToken(List<MechanismBean> token) {
		this.token = token;
	}
	
	
	
	
	
}
