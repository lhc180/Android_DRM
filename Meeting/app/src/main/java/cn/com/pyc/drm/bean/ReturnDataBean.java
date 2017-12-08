package cn.com.pyc.drm.bean;


/**
 * @Description: (用一句话描述该文件做什么)
 * @author 李巷阳
 * @date 2016-8-17 上午11:55:11
 * @version V1.0
 */
public class ReturnDataBean extends BaseBean {

	private verificationCodeData data;

	public void setData(verificationCodeData data) {
		this.data = data;
	}

	public verificationCodeData getData() {
		return data;
	}

	private static class verificationCodeData {
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
