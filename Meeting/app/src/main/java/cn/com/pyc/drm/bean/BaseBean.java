package cn.com.pyc.drm.bean;

/**
 * 基础类型model
 * 
 */
public class BaseBean
{

	private String result;

	private String msg;

	private String token;
	


	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getMsg()
	{
		return this.msg;
	}

	public void setMsg(String msg)
	{
		this.msg = msg;


	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}


}
