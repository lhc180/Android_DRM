package cn.com.pyc.szpbb.sdk.bean;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

/**
 * 存储当前用户下载文件夹的数据
 */
@Table(name = "DownFolderData")
public class DownData
{
	// id，必须存在列
	@Column(name = "id", isId = true)
	private int id;

	@Column(name = "folderId")
	private String folderId; // 专辑id(一个专辑对应多个文件)

	@Column(name = "folderName")
	private String folderName;

	@Column(name = "totalSize")
	private long totalSize;// 文件大小

	@Column(name = "ftpPath")
	private String ftpPath; // 下载路径

	@Column(name = "localPath")
	private String localPath; // 本地路径

	@Column(name = "completeSize")
	private long completeSize;// 已完成大小

	@Column(name = "progress")
	private int progress; // 进度

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public String getFolderId()
	{
		return folderId;
	}

	public void setFolderId(String folderId)
	{
		this.folderId = folderId;
	}

	public String getFtpPath()
	{
		return ftpPath;
	}

	public void setFtpPath(String ftpPath)
	{
		this.ftpPath = ftpPath;
	}

	public String getLocalPath()
	{
		return localPath;
	}

	public void setLocalPath(String localPath)
	{
		this.localPath = localPath;
	}

	public long getCompleteSize()
	{
		return completeSize;
	}

	public void setCompleteSize(long completeSize)
	{
		this.completeSize = completeSize;
	}

	public int getProgress()
	{
		return progress;
	}

	public void setProgress(int progress)
	{
		this.progress = progress;
	}

	public String getFolderName()
	{
		return folderName;
	}

	public void setFolderName(String folderName)
	{
		this.folderName = folderName;
	}

	public long getTotalSize()
	{
		return totalSize;
	}

	public void setTotalSize(long totalSize)
	{
		this.totalSize = totalSize;
	}

}
