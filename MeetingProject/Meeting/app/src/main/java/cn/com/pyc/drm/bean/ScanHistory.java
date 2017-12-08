package cn.com.pyc.drm.bean;

import com.lidroid.xutils.db.annotation.Column;
import com.lidroid.xutils.db.annotation.Id;
import com.lidroid.xutils.db.annotation.Table;

import java.io.Serializable;

/**
 * 保存的二维码扫描历史记录 <br\>
 * 
 * 代码混淆时候注解必须
 * 
 * @author qd
 * 
 */

@Table(name = "ScanHistory")
public class ScanHistory implements Serializable{


	// id，必须存在列
	@Id(column = "id")
	private int id;
	// 会议ID
	@Column(column = "meetingId")
	private String meetingId;
	// 会议路径
	@Column(column = "ScanDataSource")
	private String ScanDataSource;
	// 会议类型
	@Column(column = "meetingType")
	private String meetingType;
	// 会议名称
	@Column(column = "meetingName")
	private String meetingName;
	// 用户名称
	@Column(column = "username")
	private String username;
	// 保存的时间
	@Column(column = "time")
	private long time;
	// 判断是否第三方那个登陆
	@Column(column = "verify")
	private boolean verify;
	
	// 判断是否第三方那个登陆
	@Column(column = "isverify")
	private String isverifys;
		
	// 第三方登陆url路径
	@Column(column = "verify_url")
	private String verify_url;
	// 第三方登陆url路径
	@Column(column = "Vote_title")
	private String Vote_title;
	// 第三方登陆url路径
	@Column(column = "Vote_url")
	private String Vote_url;
	// 主界面显示的host主机名
	@Column(column = "client_url")
	private String client_url;
	// 会议创建时间
	@Column(column = "createTime")
	private String createTime;





	

	public String getClient_url() {
		return client_url;
	}
	public void setClient_url(String client_url) {
		this.client_url = client_url;
	}
	public String getIsverifys() {
		return isverifys;
	}
	public void setIsverifys(String isverifys) {
		this.isverifys = isverifys;
	}
	public boolean isVerify() {
		return verify;
	}
	public void setVerify(boolean verify) {
		this.verify = verify;
	}
	public String getVote_url() {
		return Vote_url;
	}
	public void setVote_url(String vote_url) {
		Vote_url = vote_url;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getVote_title() {
		return Vote_title;
	}
	public void setVote_title(String vote_title) {
		Vote_title = vote_title;
	}
	

	public String getVerify_url() {
		return verify_url;
	}
	public void setVerify_url(String verify_url) {
		this.verify_url = verify_url;
	}
	public String getMeetingName() {
		return meetingName;
	}
	public void setMeetingName(String meetingName) {
		this.meetingName = meetingName;
	} 
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getMeetingId() {
		return meetingId;
	}
	public void setMeetingId(String meetingId) {
		this.meetingId = meetingId;
	}

	public String getScanDataSource() {
		return ScanDataSource;
	}
	public void setScanDataSource(String scanDataSource) {
		ScanDataSource = scanDataSource;
	}
	public String getMeetingType() {
		return meetingType;
	}
	public void setMeetingType(String meetingType) {
		this.meetingType = meetingType;
	}
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	public String getCreateTime() {
		return createTime;
	}
	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
	@Override
	public String toString() {
		return "ScanHistory [id=" + id + ", meetingId=" + meetingId + ", ScanDataSource=" + ScanDataSource + ", meetingType=" + meetingType + ", meetingName=" + meetingName + ", username=" + username + ", time=" + time + ", verify=" + verify + ", isverifys=" + isverifys + ", verify_url=" + verify_url + ", Vote_title=" + Vote_title + ", Vote_url=" + Vote_url + ", client_url=" + client_url + ", createTime=" + createTime + "]";
	}

	


}
