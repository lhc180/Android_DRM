package cn.com.pyc.drm.bean;

import java.util.List;

/**
 * @Description: (用一句话描述该文件做什么)
 * @author 李巷阳
 * @date 2016-8-19 下午4:24:52
 * @version V1.0
 */
public class ReturnMeetingRecordDataBean {
	private String result;

	private String msg;

	private String token;

	private List<databean> data;

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

	public List<databean> getData() {
		return data;
	}

	public void setData(List<databean> data) {
		this.data = data;
	}

	public class databean {

		private String MeetingID;
		private String MeetingName;
		private String MeetingType;
		private String StartTime;
		private String EndTime;
		private String State;
		private String MeetingState;
		private String SuiZhiCode;
		private String Verify;
		private String IsPublic;
		private String QRCode;
		private String url;
		private String client_url;
		private String VerifyUrl;
		
		
	
		public String getVerifyUrl() {
			return VerifyUrl;
		}

		public void setVerifyUrl(String verifyUrl) {
			VerifyUrl = verifyUrl;
		}

		public String getClient_url() {
			return client_url;
		}

		public void setClient_url(String client_url) {
			this.client_url = client_url;
		}

		public String getQRCode() {
			return QRCode;
		}

		public void setQRCode(String qRCode) {
			QRCode = qRCode;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getMeetingID() {
			return MeetingID;
		}

		public void setMeetingID(String meetingID) {
			MeetingID = meetingID;
		}

		public String getMeetingName() {
			return MeetingName;
		}

		public void setMeetingName(String meetingName) {
			MeetingName = meetingName;
		}

		public String getMeetingType() {
			return MeetingType;
		}

		public void setMeetingType(String meetingType) {
			MeetingType = meetingType;
		}

		public String getStartTime() {
			return StartTime;
		}

		public void setStartTime(String startTime) {
			StartTime = startTime;
		}

		public String getEndTime() {
			return EndTime;
		}

		public void setEndTime(String endTime) {
			EndTime = endTime;
		}

		public String getState() {
			return State;
		}

		public void setState(String state) {
			State = state;
		}

		public String getMeetingState() {
			return MeetingState;
		}

		public void setMeetingState(String meetingState) {
			MeetingState = meetingState;
		}

		public String getSuiZhiCode() {
			return SuiZhiCode;
		}

		public void setSuiZhiCode(String suiZhiCode) {
			SuiZhiCode = suiZhiCode;
		}

		public String getVerify() {
			return Verify;
		}

		public void setVerify(String verify) {
			Verify = verify;
		}

		public String getIsPublic() {
			return IsPublic;
		}

		public void setIsPublic(String isPublic) {
			IsPublic = isPublic;
		}

	}

}
