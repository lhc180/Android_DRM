package cn.com.pyc.drm.bean;

import com.lidroid.xutils.db.annotation.Column;
import com.lidroid.xutils.db.annotation.Id;
import com.lidroid.xutils.db.annotation.Table;

/**
 * @author 李巷阳
 * @version V1.0
 * @Description: (保存二维码扫描机构的历史记录)
 * @date 2017/3/7 15:28
 */
@Table(name = "ScanMechanismHistory")
public class ScanMechanismHistory {


    // id，必须存在列
    @Id(column = "id")
    private int id;
    // 机构名
    @Column(column = "ServerName")
    private String ServerName;
    // 机构地址
    @Column(column = "ServerAddress")
    private String ServerAddress;
    // 绥知用户名
    @Column(column = "SZUserName")
    private String SZUserName;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getServerName() {
        return ServerName;
    }

    public void setServerName(String serverName) {
        ServerName = serverName;
    }

    public String getServerAddress() {
        return ServerAddress;
    }

    public void setServerAddress(String serverAddress) {
        ServerAddress = serverAddress;
    }

    public String getSZUserName() {
        return SZUserName;
    }

    public void setSZUserName(String SZUserName) {
        this.SZUserName = SZUserName;
    }
}
