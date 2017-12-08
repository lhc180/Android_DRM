package cn.com.pyc.drm.utils;

import java.util.Locale;

import android.util.Log;

/**
 * 判断文件类型
 * 
 * @author hudq
 * 
 */
public class FileTypeUtil
{
	// 视频
	public static final int VIDEOMP4 = 0x64;
	// 音频
	public static final int AUDIOMP3 = 0xc8;
	// pdf
	public static final int FILEPDF = 0x12c;

	// 其他
	public static final int HTML = 0x190;
	public static final int TEXT = 0x1f4;
	public static final int ZIP = 0x258;

	public static final int REQUEST_CODE_ScanHistory=0x408;
	
    public static final int RESULT_OK           = -1;
	// 不支持app播放的
	public static final int UNSORPPORT = -1;
	public static final int UNSORPPORT_VIDEO = 0x3e8;
	public static final int UNSORPPORT_AUDIO = 0x7d0;
	public static final int UNSORPPORT_PDF = 0xbb8;
	
	
	

	/**
	 * 获取文件类型
	 * 
	 * @param paramString
	 * @return
	 */
	private static final int getFileType(String paramString)
	{
		int typeCode = UNSORPPORT;
		if ((paramString == null) || (paramString.trim().length() < 1))
		{
			typeCode = UNSORPPORT;
			return typeCode;
		} else
		{
			// 123abc.mp/
			// 123abc.mp4
			int j = paramString.length() - 1;
			if (paramString.charAt(j) == '/') // 47
			{
				int k = paramString.length() - 1;
				paramString = paramString.substring(0, k);
			}
			String str = paramString.toLowerCase(Locale.getDefault());

			if ((str.endsWith(".mp4")))
				typeCode = VIDEOMP4;
			else if ((str.endsWith(".mp3")))
				typeCode = AUDIOMP3;
			else if ((str.endsWith(".pdf")))
				typeCode = FILEPDF;
			else
			{
				// if (isUnsupportedVideoTypes(str)) typeCode =
				// UNSORPPORT_VIDEO;
				// if (isUnsupportedAudioTypes(str)) typeCode =
				// UNSORPPORT_AUDIO;
				// if (isUnsupportedFileTypes(str)) typeCode = UNSORPPORT_PDF;
				typeCode = UNSORPPORT;
			}

			Log.v("", "typecode: " + typeCode);
			return typeCode;
		}
	}

	/**
	 * 是否是音频
	 * 
	 * @param paramString
	 * @return
	 */
	public static final boolean isAudioFile(String paramString)
	{
		int i = getFileType(paramString);
		return i == AUDIOMP3;
	}

	public static final boolean isVideoFile(String paramString)
	{
		int i = getFileType(paramString);
		return i == VIDEOMP4;
	}

	public static final boolean isPdfFile(String paramString)
	{
		int i = getFileType(paramString);
		return i == FILEPDF;
	}

	public static final boolean isUnsupportedVideoTypes(String paramString)
	{
		if ((paramString.endsWith(".3gp")) || (paramString.endsWith(".mkv"))
				|| (paramString.endsWith(".avi")) || (paramString.endsWith("rm"))
				|| (paramString.endsWith("rmvb")) || (paramString.endsWith(".mpg"))
				|| (paramString.endsWith(".mpeg")) || (paramString.endsWith(".asf"))
				|| (paramString.endsWith(".flv")))
		{
			return true;
		} else
		{
			return false;
		}
	}

	public static final boolean isUnsupportedAudioTypes(String paramString)
	{
		if ((paramString.endsWith(".wma")) || (paramString.endsWith(".wmv"))
				|| (paramString.endsWith(".amr")) || (paramString.endsWith(".ogg")))
		{
			return true;
		} else
		{
			return false;
		}
	}

	public static final boolean isUnsupportedFileTypes(String str)
	{
		if ((str.endsWith(".html")) || (str.endsWith(".htm")) || (str.endsWith(".jsp"))
				|| (str.endsWith(".asp")) || (str.endsWith(".txt")) || (str.endsWith(".text"))
				|| (str.endsWith(".ini")) || (str.endsWith(".properties"))
				|| (str.endsWith(".prop")) || (str.endsWith(".xml")) || (str.endsWith(".zip"))
				|| (str.endsWith(".tar")) || (str.endsWith(".gz")) || (str.endsWith(".rar"))
				|| (str.endsWith(".7z")))
		{
			return true;
		} else
		{
			return false;
		}
	}
}
