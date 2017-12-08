package cn.com.pyc.drm.bean;

import java.io.Serializable;
import java.util.List;

/**
 * @Description: (用一句话描述该文件做什么)
 * @author 李巷阳
 * @date 2016-8-19 下午4:24:52
 * @version V1.0
 */
public class ReturnMeetingRecordDataBean implements Serializable {
	private String result;

	private String msg;

	private String token;

	private List<MeetingBean> data;

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

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public List<MeetingBean> getData() {
		return data;
	}

	public void setData(List<MeetingBean> data) {
		this.data = data;
	}

}
