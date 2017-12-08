package cn.com.pyc.drm.model;

/**
 * 基础类型model
 * 
 * @author qd
 * 
 */
public class BaseModel {

	private boolean result;

	private String msg;
	/**
	 * 成功
	 * 
	 * @return
	 */
	public boolean isSuccess() {
		return this.result;
	}

	public String getMsg(){
		return this.msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}

	public boolean isResult() {
		return result;
	}

	public void setResult(boolean result) {
		this.result = result;
	}

}
