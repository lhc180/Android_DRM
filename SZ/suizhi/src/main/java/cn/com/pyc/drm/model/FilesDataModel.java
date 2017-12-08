package cn.com.pyc.drm.model;

import java.util.ArrayList;
import java.util.List;

public class FilesDataModel extends BaseModel
{

	private List<FileData> data;

	public void setData(List<FileData> data)
	{
		this.data = data;
	}

	public List<FileData> getData()
	{
		if (data == null) data = new ArrayList<>();
		return data;
	}

}
