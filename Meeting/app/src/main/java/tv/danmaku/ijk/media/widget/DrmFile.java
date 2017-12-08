package tv.danmaku.ijk.media.widget;

import java.util.Locale;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * *************Parcelable序列化操作,定义变量的顺序不可更改！
 * 
 * @author hudq
 * 
 */
public class DrmFile implements Parcelable
{

	private String filePath;
	// private int MimeType;

	private String publicKey;
	private String privateKey;

	private String artist = "DRM"; // 音乐则为艺术家；视频则为导演
	private String validity = "0天剩余0天"; // 有效期信息
	private String publisher = "DRM"; // 发布者
	private int duration; // 时长
	private int curPos = 0; // 记录当前播放时间

	private String title; // 标题-根据filePath获得
	private String format; // 格式-根据filePath获得

	private String asset_id;

	private String odd_datetime_end;
	private String odd_datetime_start;
	public Boolean IsEffectivetime;// 是否在生效时间之内
	private String myProId;



	public String getOdd_datetime_start() {
		return odd_datetime_start;
	}

	public void setOdd_datetime_start(String odd_datetime_start) {
		this.odd_datetime_start = odd_datetime_start;
	}

	public Boolean getIsEffectivetime() {
		return IsEffectivetime;
	}

	public void setIsEffectivetime(Boolean isEffectivetime) {
		IsEffectivetime = isEffectivetime;
	}

	public void setMyProId(String myProId)
	{
		this.myProId = myProId;
	}

	public String getMyProId()
	{
		return myProId;
	}

	public String getOdd_datetime_end()
	{
		return odd_datetime_end;
	}

	public void setOdd_datetime_end(String odd_datetime_end)
	{
		this.odd_datetime_end = odd_datetime_end;
	}

	// 明文
	public DrmFile(String filePath)
	{
		this.filePath = filePath;
	}

	// 密文
	public DrmFile(String filePath, String publicKey, String privateKey)
	{
		this.filePath = filePath;
		this.publicKey = publicKey;
		this.privateKey = privateKey;
	}

	// public boolean isCipherFile()
	// {
	// return canDecrypt();
	// }

	// public boolean canEncrypt()
	// {
	// return !TextUtils.isEmpty(publicKey) && !isCipherFile(); //
	// 公钥是针对用户的；不能对密文解密
	// }

	// public boolean canDecrypt()
	// {
	// return !TextUtils.isEmpty(privateKey); // 私钥是与文件一一对应的
	// }

	/*-************************
	 * Getter and Setter
	 *************************/

	public String getFilePath()
	{
		return filePath;
	}

	public void setFilePath(String filePath)
	{
		this.filePath = filePath;
	}

	public String getPublicKey()
	{
		return publicKey;
	}

	public void setPublicKey(String publicKey)
	{
		this.publicKey = publicKey;
	}

	public String getPrivateKey()
	{
		return privateKey;
	}

	public void setPrivateKey(String privateKey)
	{
		this.privateKey = privateKey;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getArtist()
	{
		return artist;
	}

	public void setArtist(String artist)
	{
		this.artist = artist;
	}

	public String getValidity()
	{
		return validity;
	}

	public void setValidity(String validity)
	{
		this.validity = validity;
	}

	public String getPublisher()
	{
		return publisher;
	}

	public void setPublisher(String publisher)
	{
		this.publisher = publisher;
	}

	public String getFormat()
	{
		return filePath.substring(filePath.lastIndexOf(".") + 1).toUpperCase(Locale.ENGLISH);
	}

	public void setFormat(String format)
	{
		this.format = format;
	}

	public int getDuration()
	{
		return duration;
	}

	public void setDuration(int duration)
	{
		this.duration = duration;
	}

	public String getAsset_id()
	{
		return asset_id;
	}

	public void setAsset_id(String asset_id)
	{
		this.asset_id = asset_id;
	}

	public int getCurPos()
	{
		return curPos;
	}

	public void setCurPos(int curPos)
	{
		this.curPos = curPos;
	}

	@Override
	public String toString()
	{
		return "DrmFile [filePath=" + filePath + ", publicKey=" + publicKey + ", privateKey=" + privateKey + ", artist=" + artist + ", validity=" + validity
				+ ", publisher=" + publisher + ", duration=" + duration + ", curPos=" + curPos + ", title=" + title + ", format=" + format + ", asset_id="
				+ asset_id + ", odd_datetime_end=" + odd_datetime_end + ", myProId=" + myProId + "]";
	}

	@Override
	public int describeContents()
	{
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		
		
		
		dest.writeString(this.filePath);

		dest.writeString(this.publicKey);
		dest.writeString(this.privateKey);

		dest.writeString(this.artist);
		dest.writeString(this.validity);
		dest.writeString(this.publisher);
		dest.writeInt(this.duration);
		dest.writeInt(this.curPos);

		dest.writeString(this.title);
		dest.writeString(this.format);

		dest.writeString(this.asset_id);

		dest.writeString(this.odd_datetime_end);
		dest.writeString(this.myProId);
		
		dest.writeString(this.odd_datetime_start);
		dest.writeByte((byte) (this.IsEffectivetime ? 1 : 0));     //if myBoolean == true, byte == 1  

	}

	public final static Parcelable.Creator<DrmFile> CREATOR = new Creator<DrmFile>()
	{

		@Override
		public DrmFile[] newArray(int size)
		{
			return new DrmFile[size];
		}

		@Override
		public DrmFile createFromParcel(Parcel source)
		{
			String filePath = source.readString();
			String publicKey = source.readString();
			String privateKey = source.readString();

			DrmFile df = new DrmFile(filePath, publicKey, privateKey);
			df.setArtist(source.readString());
			df.setValidity(source.readString());
			df.setPublisher(source.readString());
			df.setDuration(source.readInt());
			df.setCurPos(source.readInt());

			df.setTitle(source.readString());
			df.setFormat(source.readString());
			
			df.setAsset_id(source.readString());
			df.setOdd_datetime_end(source.readString());
			df.setMyProId(source.readString());
			df.setOdd_datetime_start(source.readString());
			df.setIsEffectivetime(source.readByte() != 0);
			return df;
		}
	};

}
