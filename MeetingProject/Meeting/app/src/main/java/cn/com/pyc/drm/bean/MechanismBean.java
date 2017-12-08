package cn.com.pyc.drm.bean;

import java.io.Serializable;

/**
 * @Description: (用一句话描述该文件做什么)
 * @author 李巷阳
 * @date 2016-8-18 下午5:29:29
 * @version V1.0
 */
public class MechanismBean implements Serializable{

	private String ServerName;// 机构名
	private String ServerAddress;// 机构地址
	private String SZUserName;// 绥知用户名

	public MechanismBean(){

	}
	public MechanismBean(String serverName, String serverAddress, String SZUserName) {
		ServerName = serverName;
		ServerAddress = serverAddress;
		this.SZUserName = SZUserName;
	}

	public String getServerAddress() {
		return ServerAddress;
	}

	public void setServerAddress(String serverAddress) {
		ServerAddress = serverAddress;
	}

	public String getServerName() {
		return ServerName;
	}

	public void setServerName(String serverName) {
		ServerName = serverName;
	}



	public String getSZUserName() {
		return SZUserName;
	}

	public void setSZUserName(String sZUserName) {
		SZUserName = sZUserName;
	}

	@Override
	public String toString() {
		return "MechanismBean{" +
				"ServerName='" + ServerName + '\'' +
				", ServerAddress='" + ServerAddress + '\'' +
				", SZUserName='" + SZUserName + '\'' +
				'}';
	}
}
