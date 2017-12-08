package cn.com.pyc.drm.model;

import cn.com.pyc.drm.model.LoginModel.LoginInfo;

public class SigninReturnModel extends BaseModel {

	private String token;
	private SigninReturn data;
	
	
	
	
	public String getToken() {
		return token;
	}


	public void setToken(String token) {
		this.token = token;
	}



	public SigninReturn getData() {
		if (data == null)
			return new SigninReturn();
		return data;
	}


	public void setData(SigninReturn data) {
		this.data = data;
	}


	public static class SigninReturn {

		private String title;
		private String url;
		public String getTitle() {
			return title;
		}
		public void setTitle(String title) {
			this.title = title;
		}
		public String getUrl() {
			return url;
		}
		public void setUrl(String url) {
			this.url = url;
		}
		
	}
}
