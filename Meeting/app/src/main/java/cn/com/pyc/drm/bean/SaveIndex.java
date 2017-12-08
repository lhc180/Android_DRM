package cn.com.pyc.drm.bean;

import com.lidroid.xutils.db.annotation.Column;
import com.lidroid.xutils.db.annotation.Id;
import com.lidroid.xutils.db.annotation.Table;

/**
 * 保存的索引表 <br\>
 * 
 * 代码混淆时候注解必须
 * 
 * @author qd
 * 
 */

@Table(name = "SaveIndex")
public class SaveIndex
{

	// id，必须存在列
	@Id(column = "id")
	private int id;
	// 保存的阅读进度索引
	@Column(column = "positonIndex")
	private String positonIndex;
	// 专辑对应的myProId
	@Column(column = "myProId")
	private String myProId;
	// 保存的时间
	@Column(column = "time")
	private long time;
	// 保存索引对应的文件类型
	@Column(column = "fileType")
	private String fileType;

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public String getPositonIndex()
	{
		return positonIndex;
	}

	public void setPositonIndex(String positonIndex)
	{
		this.positonIndex = positonIndex;
	}

	public String getMyProId()
	{
		return myProId;
	}

	public void setMyProId(String myProId)
	{
		this.myProId = myProId;
	}

	public long getTime()
	{
		return time;
	}

	public void setTime(long time)
	{
		this.time = time;
	}

	public void setFileType(String fileType)
	{
		this.fileType = fileType;
	}

	public String getFileType()
	{
		return fileType;
	}

}
