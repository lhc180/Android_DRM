package cn.com.pyc.drm.model;

/**
 * 登录成功返回数据
 * 
 * @author hudq
 * 
 */
public class LoginModel extends BaseModel {

	private LoginInfo data;

	public void setData(LoginInfo data) {
		this.data = data;
	}

	public LoginInfo getData() {
		if (data == null)
			return new LoginInfo();
		return data;
	}

	public static class LoginInfo {

		private boolean result;
		private String token;

		public boolean isResult() {
			return result;
		}

		public void setResult(boolean result) {
			this.result = result;
		}

		public String getToken() {
			return token;
		}

		public void setToken(String token) {
			this.token = token;
		}
	}

}
