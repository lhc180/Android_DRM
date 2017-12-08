package cn.com.pyc.drm.model;

import cn.com.pyc.drm.utils.manager.DownloadTaskManager;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * 要下载的文件信息 <br\>
 * 实现Parcelable接口，提高页面之间数据的传递效率<br\>
 * <br\>
 * <br\>
 * <br\>
 * *************Parcelable序列化操作,定义变量的顺序不可更改！写的顺序和读的顺序必须一致,需要的字段去掉注释即可**************
 * 
 */
public class DownloadInfo implements Parcelable
{

	private boolean active;
	private String authors;
	// private String aveDayPrice;
	// private String avePrintCost;
	// private String aveReviewRating;
	private String category;
	private String myProId;
	private String picture_ratio;
	private String picture_url;

	// private String preview;
	// private String proId;
	private String publishDate;
	// private String publishFirm;

	private String productName;

	// private boolean purchaseItemsIsEmpty;
	// private boolean sellByDay;
	// private boolean sellPrint;

	private String totalPrice;
	// private int totalReviewNum;

	// 任务状态，初始化:DownloadTaskManager.INIT
	private int taskState = DownloadTaskManager.INIT;
	// 下载的item位置
	private int position;
	// 下载保存进度
	private int progress;
	// 当前下载大小
	private long currentSize;
	// 总大小
	private long totalSize;

	@Override
	public int describeContents()
	{
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		/** 必须按照变量顺序,否则出错，但是Parcelable效率高 */
		
		dest.writeInt(this.active ? 1 : 0);
		dest.writeString(this.authors);
		// dest.writeString(this.aveDayPrice);
		// dest.writeString(this.avePrintCost);
		// dest.writeString(this.aveReviewRating);
		dest.writeString(this.category);
		dest.writeString(this.myProId);
		dest.writeString(this.picture_ratio);
		dest.writeString(this.picture_url);
		// dest.writeString(this.preview);
		// dest.writeString(this.proId);
		dest.writeString(this.publishDate);
		// dest.writeString(this.publishFirm);
		dest.writeString(this.productName);
		// dest.writeInt(this.purchaseItemsIsEmpty ? 1 : 0);
		// dest.writeInt(this.sellByDay ? 1 : 0);
		// dest.writeInt(this.sellPrint ? 1 : 0);
		dest.writeString(this.totalPrice);
		// dest.writeInt(this.totalReviewNum);
		dest.writeInt(this.taskState);
		dest.writeInt(this.position);
		dest.writeInt(this.progress);
		dest.writeLong(this.currentSize);
		dest.writeLong(this.totalSize);

	}

	public final static Parcelable.Creator<DownloadInfo> CREATOR = new Creator<DownloadInfo>()
	{

		@Override
		public DownloadInfo[] newArray(int size)
		{

			return new DownloadInfo[size];
		}

		@Override
		public DownloadInfo createFromParcel(Parcel source)
		{
			DownloadInfo o = new DownloadInfo();
			o.setActive((source.readInt() == 1) ? true : false);
			o.setAuthors(source.readString());
			// o.setAveDayPrice(source.readString());
			// o.setAvePrintCost(source.readString());
			// o.setAveReviewRating(source.readString());
			o.setCategory(source.readString());
			o.setMyProId(source.readString());
			o.setPicture_ratio(source.readString());
			o.setPicture_url(source.readString());
			// o.setPreview(source.readString());
			// o.setProId(source.readString());
			o.setPublishDate(source.readString());
			// o.setPublishFirm(source.readString());
			o.setProductName(source.readString());
			// o.setPurchaseItemsIsEmpty(source.readInt() == 1 ? true : false);
			// o.setSellByDay(source.readInt() == 1 ? true : false);
			// o.setSellPrint(source.readInt() == 1 ? true : false);
			o.setTotalPrice(source.readString());
			// o.setTotalReviewNum(source.readInt());
			o.setTaskState(source.readInt());
			o.setPosition(source.readInt());
			o.setProgress(source.readInt());
			o.setCurrentSize(source.readLong());
			o.setTotalSize(source.readLong());
			return o;
		}
	};
	
	public boolean isActive()
	{
		return active;
	}

	public void setActive(boolean active)
	{
		this.active = active;
	}

	public String getAuthors()
	{
		return authors;
	}

	public void setAuthors(String authors)
	{
		this.authors = authors;
	}

	public String getCategory()
	{
		return category;
	}

	public void setCategory(String category)
	{
		this.category = category;
	}

	public String getMyProId()
	{
		return myProId;
	}

	public void setMyProId(String myProId)
	{
		this.myProId = myProId;
	}

	public String getPicture_ratio()
	{
		return picture_ratio;
	}

	public void setPicture_ratio(String picture_ratio)
	{
		this.picture_ratio = picture_ratio;
	}

	public String getPicture_url()
	{
		return picture_url;
	}

	public void setPicture_url(String picture_url)
	{
		this.picture_url = picture_url;
	}
	
	public String getPublishDate()
	{
		return publishDate;
	}

	public void setPublishDate(String publishDate)
	{
		this.publishDate = publishDate;
	}

	public String getProductName()
	{
		return productName;
	}

	public void setProductName(String productName)
	{
		this.productName = productName;
	}

	public String getTotalPrice()
	{
		return totalPrice;
	}

	public void setTotalPrice(String totalPrice)
	{
		this.totalPrice = totalPrice;
	}

	public int getTaskState()
	{
		return taskState;
	}

	public void setTaskState(int taskState)
	{
		this.taskState = taskState;
	}

	public int getPosition()
	{
		return position;
	}

	public void setPosition(int position)
	{
		this.position = position;
	}

	public int getProgress()
	{
		return progress;
	}

	public void setProgress(int progress)
	{
		this.progress = progress;
	}

	public long getCurrentSize()
	{
		return currentSize;
	}

	public void setCurrentSize(long currentSize)
	{
		this.currentSize = currentSize;
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
