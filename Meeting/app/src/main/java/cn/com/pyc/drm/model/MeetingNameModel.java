package cn.com.pyc.drm.model;

public class MeetingNameModel extends BaseModel
{

	private MeetingName data;

	public void setData(MeetingName data)
	{
		this.data = data;
	}

	public MeetingName getData()
	{
		return data;
	}

	public static class MeetingName
	{
		private String id;
		private String name;
		private String verify;
		private String url;
		private String title;
		private String verifyurl;
		private String client_url;
		private String createTime;
		
		
		
		public String getClient_url() {
			return client_url;
		}

		public void setClient_url(String client_url) {
			this.client_url = client_url;
		}

		public String getVerifyurl() {
			return verifyurl;
		}

		public void setVerifyurl(String verifyurl) {
			this.verifyurl = verifyurl;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}
		
	

		public String getVerify() {
			return verify;
		}

		public void setVerify(String verify) {
			this.verify = verify;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getId()
		{
			return id;
		}

		public void setId(String id)
		{
			this.id = id;
		}

		public String getName()
		{
			return name;
		}

		public void setName(String name)
		{
			this.name = name;
		}

		public String getCreateTime() {
			return createTime;
		}

		public void setCreateTime(String createTime) {
			this.createTime = createTime;
		}

		
	}

}
