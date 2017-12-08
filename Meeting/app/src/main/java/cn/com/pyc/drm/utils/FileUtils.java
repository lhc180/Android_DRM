package cn.com.pyc.drm.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.ActionMode;
import cn.com.meeting.drm.R;
import cn.com.pyc.drm.common.AppException;
import cn.com.pyc.drm.common.CrashHandler;
import cn.com.pyc.drm.model.xml.XML2JSON_Album;
import cn.com.pyc.drm.model.xml.OEX_Rights;
import net.file.FileInfo;
import net.file.GlobalConsts;
import net.file.Settings;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.*;

/**
 * 文件操作工具包
 */
@SuppressLint({ "SdCardPath", "RtlHardcoded" }) public class FileUtils {

	private static final String LOG_TAG = "FileUtils";

	private static final double KB = 1024.0;
	private static final double MB = KB * KB;
	private static final double GB = KB * KB * KB;

	private static final String PRIMARY_FOLDER = "pref_key_primary_folder";
	private static final String READ_ROOT = "pref_key_read_root";
	private static final String SHOW_REAL_PATH = "pref_key_show_real_path";
	// private static final String SYSTEM_SEPARATOR = File.separator;

	private static final String ANDROID_SECURE = "/mnt/sdcard/.android_secure";

	// public static int CATEGORY_TAB_INDEX = 0;
	// public static int SDCARD_TAB_INDEX = 1;

	// does not include sd card folder
	private static String[] SysFileDirs = new String[] { "miren_browser/imagecaches" };

	private String SDCardRoot = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;

	/**
	 * 获取文件扩展名
	 * 
	 * eg：askahj1145asd.drm
	 * 
	 * 得到drm
	 * 
	 * @param filename
	 * @return
	 */
	public static String getExtFromFileName(String filename) {
		if (filename == null) {
			return "";
		}

		int dotPosition = filename.lastIndexOf('.');
		if (dotPosition != -1) {
			return filename.substring(dotPosition + 1);
		}
		return "";
	}

	/***
	 * 获取文件名（不带扩展名）
	 * 
	 * eg：askahj1145asd.drm
	 * 
	 * 得到askahj1145asd
	 * 
	 * @param filename
	 * @return
	 */
	public static String getNameFromFileName(String filename) {
		if (filename == null) {
			return "";
		}

		int dotPosition = filename.lastIndexOf('.');
		if (dotPosition != -1) {
			return filename.substring(0, dotPosition);
		}
		return "";
	}

	/**
	 * 从文件路径获取文件的上层路径
	 * 
	 * eg：/suizhi/file/askahj1145asd.drm
	 * 
	 * 得到/suizhi/file
	 * 
	 * @param filepath
	 * @return
	 */
	public static String getPathFromFilePath(String filepath) {
		if (filepath == null) {
			return "";
		}

		int pos = filepath.lastIndexOf('/');
		if (pos != -1) {
			return filepath.substring(0, pos);
		}
		return "";
	}

	/*
	 * eg：/suizhi/file/askahj1145asd.drm
	 * 
	 * 得到askahj1145asd.drm
	 * 
	 * @param filepath
	 * 
	 * @return
	 */
	// @Deprecated (同getNameFromFilePath)
	// public static String getPathlastFilePath(String filepath)
	// {
	// if (filepath == null)
	// {
	// return "";
	// }
	//
	// int pos = filepath.lastIndexOf('/');
	// if (pos != -1)
	// {
	// return filepath.substring(pos + 1, filepath.length());
	// }
	// return "";
	// }

	/**
	 * 从文件路径获取文件的路径
	 * 
	 * eg：/suizhi/file/askahj1145asd.drm
	 * 
	 * 得到/suizhi/file/askahj1145asd
	 * 
	 * @param filepath
	 * @return
	 */
	public static String getPathFromFileDRM(String filepath) {
		if (filepath == null) {
			return "";
		}

		int pos = filepath.lastIndexOf(".drm");
		if (pos != -1) {
			return filepath.substring(0, pos);
		}
		return "";
	}

	/**
	 * 从文件路径获取文件名
	 * 
	 * eg：/suizhi/file/askahj1145asd.drm
	 * 
	 * 得到askahj1145asd.drm
	 * 
	 * @param filepath
	 * @return
	 */
	public static String getNameFromFilePath(String filepath) {
		if (filepath == null) {
			return "";
		}

		int pos = filepath.lastIndexOf('/');
		if (pos != -1) {
			return filepath.substring(pos + 1);
		}
		return "";
	}

	/**
	 * 获取文件夹下子文件得名字
	 * 
	 * eg：/suizhi/file/abc
	 * 
	 * 得到abc
	 * 
	 * @param filepath
	 * @return
	 */
	public static ArrayList getSubunitFileName(String filePath) {
		ArrayList al_Subunit_file = new ArrayList<String>();
		File f = new File(filePath);
		File[] files = f.listFiles();// 列出所有文件
		// 将所有文件存入list中
		if (files != null) {
			int count = files.length;// 文件个数
			for (int i = 0; i < count; i++) {
				File file = files[i];
				al_Subunit_file.add(file.getName());
			}
		}

		return al_Subunit_file;

	}

	public static FileInfo GetFileInfo(String filePath) {
		File lFile = new File(filePath);
		if (!lFile.exists())
			return null;
		FileInfo lFileInfo = new FileInfo();
		lFileInfo.canRead = lFile.canRead();
		lFileInfo.canWrite = lFile.canWrite();
		lFileInfo.isHidden = lFile.isHidden();
		lFileInfo.fileName = getNameFromFilePath(filePath);
		lFileInfo.ModifiedDate = lFile.lastModified();
		lFileInfo.IsDir = lFile.isDirectory();
		lFileInfo.filePath = filePath;
		lFileInfo.fileSize = lFile.length();
		return lFileInfo;
	}

	public static FileInfo GetFileInfo(File f, FilenameFilter filter, boolean showHidden) {
		FileInfo lFileInfo = new FileInfo();
		String filePath = f.getPath();
		File lFile = new File(filePath);
		lFileInfo.canRead = lFile.canRead();
		lFileInfo.canWrite = lFile.canWrite();
		lFileInfo.isHidden = lFile.isHidden();
		lFileInfo.fileName = f.getName();
		lFileInfo.ModifiedDate = lFile.lastModified();
		lFileInfo.IsDir = lFile.isDirectory();
		lFileInfo.filePath = filePath;
		if (lFileInfo.IsDir) {
			int lCount = 0;
			File[] files = lFile.listFiles(filter);
			if (files == null) {
				return null;
			}
			for (File child : files) {
				if ((!child.isHidden() || showHidden) && isNormalFile(child.getAbsolutePath())) {
					lCount++;
				}
			}
			lFileInfo.Count = lCount;

		} else {

			lFileInfo.fileSize = lFile.length();

		}
		return lFileInfo;
	}

	public static boolean isNormalFile(String fullName) {
		return !fullName.equals(ANDROID_SECURE);
	}

	// public static String formatDateString(Context context, long time)
	// {
	// DateFormat dateFormat =
	// android.text.format.DateFormat.getDateFormat(context);
	// DateFormat timeFormat =
	// android.text.format.DateFormat.getTimeFormat(context);
	// Date date = new Date(time);
	// return dateFormat.format(date) + " " + timeFormat.format(date);
	// }

	public static void updateActionModeTitle(ActionMode mode, Context context, int selectedNum) {
		if (mode != null) {
			mode.setTitle(context.getString(R.string.multi_select_title, selectedNum));
			if (selectedNum == 0) {
				mode.finish();
			}
		}
	}

	public static String getSdDirectory() {
		return Environment.getExternalStorageDirectory().getPath();
	}

	public static boolean shouldShowFile(String path) {
		return shouldShowFile(new File(path));
	}

	public static boolean shouldShowFile(File file) {
		boolean show = Settings.instance().getShowDotAndHiddenFiles();
		if (show)
			return true;

		if (file.isHidden())
			return false;

		if (file.getName().startsWith("."))
			return false;

		String sdFolder = getSdDirectory();
		for (String s : SysFileDirs) {
			if (file.getPath().startsWith(makePath(sdFolder, s)))
				return false;
		}

		return true;
	}

	/**
	 * 根据path1和path2创建路径 path1/path2
	 * 
	 * @param path1
	 * @param path2
	 * @return
	 */
	public static String makePath(String path1, String path2) {
		if (path1.endsWith(File.separator))
			return path1 + path2;

		return path1 + File.separator + path2;
	}

	// if path1 contains path2
	public static boolean containsPath(String path1, String path2) {
		String path = path2;
		while (path != null) {
			if (path.equalsIgnoreCase(path1))
				return true;

			if (path.equals(GlobalConsts.ROOT_PATH))
				break;
			path = new File(path).getParent();
		}

		return false;
	}

	public static boolean isReadRoot(Context context) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

		boolean isReadRootFromSetting = settings.getBoolean(READ_ROOT, false);
		boolean isReadRootWhenSettingPrimaryFolderWithoutSdCardPrefix = !getPrimaryFolder(context).startsWith(getSdDirectory());

		return isReadRootFromSetting || isReadRootWhenSettingPrimaryFolderWithoutSdCardPrefix;
	}

	public static String getPrimaryFolder(Context context) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		String primaryFolder = settings.getString(PRIMARY_FOLDER, context.getString(R.string.default_primary_folder, GlobalConsts.ROOT_PATH));

		if (TextUtils.isEmpty(primaryFolder)) { // setting primary folder =
												// empty("")
			primaryFolder = GlobalConsts.ROOT_PATH;
		}

		// it's remove the end char of the home folder setting when it with the
		// '/' at the end.
		// if has the backslash at end of the home folder, it's has minor bug at
		// "UpLevel" function.
		int length = primaryFolder.length();
		if (length > 1 && File.separator.equals(primaryFolder.substring(length - 1))) { // length
																						// =
																						// 1,
																						// ROOT_PATH
			return primaryFolder.substring(0, length - 1);
		} else {
			return primaryFolder;
		}
	}

	public static boolean showRealPath(Context context) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		return settings.getBoolean(SHOW_REAL_PATH, false);
	}

	/**
	 * 格式化显示文件大小 eg:2G;2.3M（小数位不为0显示一位小数，小数位为0不显示小数位）
	 * 
	 * @param size
	 * @return
	 */
	public static String convertStorage(long size) {
		long kb = 1024;
		long mb = kb * 1024;
		long gb = mb * 1024;

		if (size >= gb) {
			return String.format(Locale.getDefault(),"%.1fG", (float) size / gb);
		} else if (size >= mb) {
			float f = (float) size / mb;
			return String.format(Locale.getDefault(),f > 100 ? "%.0fM" : "%.1fM", f);
		} else if (size >= kb) {
			float f = (float) size / kb;
			return String.format(Locale.getDefault(),f > 100 ? "%.0fKB" : "%.1fKB", f);
		} else
			return String.format(Locale.getDefault(),"%dB", size);
	}

	/**
	 * 带一位小数的格式化显示 eg:2.1M,2.0G
	 * 
	 * @param size
	 * @return
	 */
	public static String showFileSize(long size) {
		String fileSize;
		if (size < KB)
			fileSize = size + "B";
		else if (size < MB)
			fileSize = String.format("%.1f", size / KB) + "KB";
		else if (size < GB)
			fileSize = String.format("%.1f", size / MB) + "M";
		else
			fileSize = String.format("%.1f", size / GB) + "G";

		return fileSize;
	}

	/**
	 * 转换文件大小
	 * 
	 * @param fileS
	 * @return B/KB/MB/GB 带两位小数
	 */
	public static String formatFileSize(long fileS) {
		java.text.DecimalFormat df = new java.text.DecimalFormat("#.00");
		String fileSizeString = "";
		if (fileS < 1024) {
			fileSizeString = df.format((double) fileS) + "B";
		} else if (fileS < 1048576) {
			fileSizeString = df.format((double) fileS / 1024) + "KB";
		} else if (fileS < 1073741824) {
			fileSizeString = df.format((double) fileS / 1048576) + "M";
		} else {
			fileSizeString = df.format((double) fileS / 1073741824) + "G";
		}
		return fileSizeString;
	}

	/**
	 * 获取目录文件大小
	 * 
	 * @param dir
	 * @return
	 */
	public static long getDirSize(File dir) {
		if (dir == null) {
			return 0;
		}
		if (!dir.isDirectory()) {
			return 0;
		}
		long dirSize = 0;
		File[] files = dir.listFiles();
		for (File file : files) {
			if (file.isFile()) {
				dirSize += file.length();
			} else if (file.isDirectory()) {
				dirSize += file.length();
				dirSize += getDirSize(file); // 递归调用继续统计
			}
		}
		return dirSize;
	}

	/**
	 * Get the path for the file:/// only
	 * 
	 * @param uri
	 * @return
	 */
	public static String getPath(String uri) {

		if (TextUtils.isEmpty(uri))
			return null;
		if (uri.startsWith("file://") && uri.length() > "file://".length()) // >7
			return Uri.decode(uri.substring("file://".length())); // 7
		return Uri.decode(uri);
	}

	public static String getName(String uri) {
		String path = getPath(uri);
		if (path != null)
			return new File(path).getName();
		return null;
	}

	/**
	 * 根据文件绝对路径生成文件
	 * 
	 * @param filePath
	 *            文件绝对路径
	 * @return
	 */
	public static File createFile(String filePath) {
		File file = new File(filePath);
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return file;
	}

	/**
	 * 根据文件绝对路径获取文件名
	 * 
	 * @param filePath
	 * @return
	 */
	// public static String getFileName(String filePath)
	// {
	// if (StringUtil.isEmpty(filePath))
	// return "";
	// return filePath.substring(filePath.lastIndexOf(File.separator) + 1);
	// }

	/**
	 * 根据文件的绝对路径获取文件名但不包含扩展名
	 * 
	 * @param filePath
	 * @return
	 */
	// public static String getFileNameNoFormat(String filePath)
	// {
	// if (StringUtil.isEmpty(filePath))
	// {
	// return "";
	// }
	// int point = filePath.lastIndexOf('.');
	// return filePath.substring(filePath.lastIndexOf(File.separator) + 1,
	// point);
	// }

	/**
	 * 获取文件扩展名
	 * 
	 * @param fileName
	 * @return
	 */
	// public static String getFileFormat(String fileName)
	// {
	// if (StringUtil.isEmpty(fileName))
	// return "";
	//
	// int point = fileName.lastIndexOf('.');
	// return fileName.substring(point + 1);
	// }

	/**
	 * 计算SD卡的剩余空间
	 * 
	 * @return 返回-1，说明没有安装sd卡
	 */
	// public static long getFreeDiskSpace()
	// {
	// String status = Environment.getExternalStorageState();
	// long freeSpace = 0;
	// if (status.equals(Environment.MEDIA_MOUNTED))
	// {
	// try
	// {
	// File path = Environment.getExternalStorageDirectory();
	// StatFs stat = new StatFs(path.getPath());
	// long blockSize = stat.getBlockSizeLong();
	// long availableBlocks = stat.getAvailableBlocksLong();
	// freeSpace = availableBlocks * blockSize / 1024;
	// } catch (Exception e)
	// {
	// e.printStackTrace();
	// }
	// } else
	// {
	// return -1;
	// }
	// return (freeSpace);
	// }

	/**
	 * 获取文件大小
	 * 
	 * @param filePath
	 * @return
	 */
	public static long getFileSize(String filePath) {
		long size = 0;

		File file = new File(filePath);
		if (file != null && file.exists()) {
			size = file.length();
		}
		return size;
	}

	/**
	 * 检查路径是否存在
	 * 
	 * @param path
	 * @return
	 */
	public static boolean checkFilePathExists(String path) {
		return new File(path).exists();
	}

	/**
	 * 新建目录
	 * 
	 * @param directoryName
	 *            目录全路径
	 * @return
	 */
	public static boolean createDirectory(String directoryName) {
		if (checkFilePathExists(directoryName)) {
			return true;
		}
		DRMLog.i("create directory: " + directoryName);
		return !directoryName.equals("") ? new File(directoryName).mkdirs() : false;
	}

	/**
	 * 删除文件
	 * 
	 * @param filePath
	 */
	public static boolean deleteFileWithPath(String filePath) {
		SecurityManager checker = new SecurityManager();
		File f = new File(filePath);
		checker.checkDelete(filePath);
		if (f.isFile()) {
			DRMLog.i("deleteFileWithPath", filePath);
			f.delete();
			return true;
		}
		return false;
	}

	/**
	 * 删除文件夹 param folderPath 文件夹完整绝对路径
	 */

	public static void delFolder(String folderPath) {
		try {
			delAllFile(folderPath); // 删除完里面所有内容
			String filePath = folderPath;
			filePath = filePath.toString();
			java.io.File myFilePath = new java.io.File(filePath);
			myFilePath.delete(); // 删除空文件夹
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 删除指定文件夹下所有文件
	 * 
	 * @param path
	 *            文件夹完整绝对路径
	 */

	public static boolean delAllFile(String path) {
		boolean flag = false;
		File file = new File(path);
		if (!file.exists()) {
			return flag;
		}
		if (!file.isDirectory()) {
			return flag;
		}
		String[] tempList = file.list();
		File temp = null;
		for (int i = 0; i < tempList.length; i++) {
			DRMLog.i("delAllFile:" + tempList[i]);
			if (path.endsWith(File.separator)) {
				temp = new File(path + tempList[i]);
			} else {
				temp = new File(path + File.separator + tempList[i]);
			}
			if (temp.isFile()) {
				temp.delete();
			}
			if (temp.isDirectory()) {
				delAllFile(path + "/" + tempList[i]);// 先删除文件夹里面的文件
				delFolder(path + "/" + tempList[i]);// 再删除空文件夹
				flag = true;
			}
		}
		return flag;
	}

	public static void checkPermission(Map<String, String> map) {
		// check permission
	}

	/**
	 * File status information. This class maps directly to the POSIX stat
	 * structure.
	 * 
	 * @hide
	 */
	public static final class FileStatus {
		public int dev;
		public int ino;
		public int mode;
		public int nlink;
		public int uid;
		public int gid;
		public int rdev;
		public long size;
		public int blksize;
		public long blocks;
		public long atime;
		public long mtime;
		public long ctime;
	}

	// /////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////

	/**
	 * drm文件构成 drm分为有头文件和资源文件两部分构成，中间通过32byte的md5做分割 <br/>
	 * 前8byte通过一个long类型变量，记录了整个头文件的长度 <br/>
	 * 每个头文件又分为3部分: <br/>
	 * 40个byte记录文件名， 8个byte记录一个long类型变量，表示文件的起点 <br/>
	 * 8个byte记录一个long类型变量，表示文件的终点 <br/>
	 * 前8byte的long类型变量 /(40+8+8) = 头文件的个数，每个头文件都对应着一个资源文件，所以也就得到了资源文件的个数 <br/>
	 * <br/>
	 * 
	 * 解析文件，得到资源文件
	 * 
	 * @param drmPath
	 *            被解析文件路径
	 * @param decodePath
	 *            解析后保存文件路径
	 */
	public static List<CommonFile> parserDRMFile(String drmPath, String decodePath) {

		List<CommonFile> files = new ArrayList<CommonFile>();

		File file = new File(drmPath);

		if (!checkFilePathExists(drmPath)) {
			Log.i(LOG_TAG, "文件不存在");
			return null;
		}
		FileOutputStream fos = null;
		FileInputStream fis = null;
		byte[] eightBytes = new byte[8];
		byte[] fileMd5Bytes = new byte[32];
		byte[] headFileNameBytes = new byte[40];
		int bufferLength = 1024 * 1024;
		try {
			// 头文件解析
			fis = new FileInputStream(file);

			fis.read(eightBytes);
			long len1 = DRMUtil.bytes2Long(eightBytes);
			int count = (int) (len1 / (headFileNameBytes.length + eightBytes.length + eightBytes.length));

			long[] filestarts = new long[count];
			long[] fileends = new long[count];
			for (int i = 0; i < count; i++) {
				CommonFile f = new CommonFile();
				fis.read(headFileNameBytes);
				String filename = new String(headFileNameBytes).trim();
				f.filetype = getFileType(filename);
				f.filename = filename;
				f.filepath = decodePath + File.separator + filename;
				fis.read(eightBytes);
				filestarts[i] = DRMUtil.bytes2Long(eightBytes);
				fis.read(eightBytes);
				fileends[i] = DRMUtil.bytes2Long(eightBytes);
				files.add(f);

				DRMLog.e(LOG_TAG, "filename: " + f.filename);
			}

			fis.read(fileMd5Bytes);

			// 资源文件解析
			for (int i = 0; i < count; i++) {
				File f = new File(decodePath, files.get(i).filename);
				if (!f.exists()) {
					DRMLog.i("文件绝对路径：" + f.getAbsolutePath());
					f.createNewFile();
				}
				fos = new FileOutputStream(f);
				int len = -1;
				byte[] buffer = null;
				if (fileends[i] > bufferLength) {
					int sum = (int) ((fileends[i] - fileMd5Bytes.length) / bufferLength);
					buffer = new byte[bufferLength];
					while ((len = fis.read(buffer)) != -1) {
						fos.write(buffer, 0, len);
						// fos.flush();
						sum--;
						if (sum == 0) {
							buffer = new byte[(int) ((fileends[i] - fileMd5Bytes.length) % bufferLength)];
							fis.read(buffer);
							fos.write(buffer);
							// fos.flush();
							fis.read(fileMd5Bytes);
							break;
						}
					}

				} else {
					buffer = new byte[(int) (fileends[i] - fileMd5Bytes.length)];
					fis.read(buffer);
					fos.write(buffer);
					// fos.flush();
					fis.read(fileMd5Bytes);
				}

				if (fos != null) {
					try {
						fos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {

			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return files;
	}

	public static OEX_Rights parserRight(File name) throws AppException {
		DRMLog.i("parserRight path: " + name.getAbsolutePath());
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(name);
			return PullXMLReader.readXML(fis);
		} catch (FileNotFoundException e) {
			throw AppException.file(e);
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 解析albumInfo.xml实际内容为JSON格式的字符
	 * 
	 * @param albumInfoFile
	 *            albumInfo文件
	 * 
	 * @param list
	 *            CommonFile文件
	 * @return
	 * @throws AppException
	 */
	public static XML2JSON_Album parserJSON(File albumInfoFile, List<FileUtils.CommonFile> list) throws AppException {

		StringBuffer sb = new StringBuffer();
		XML2JSON_Album albumInfo = new XML2JSON_Album();
		// 获得权限文件获得albumInfo.xml文件
		FileInputStream reader = null;
		try {

			reader = new FileInputStream(albumInfoFile);
			byte[] bytes = new byte[1024];
			while ((reader.read(bytes)) != -1) {
				sb.append(new String(bytes));
			}

			// 将字符串转成JSON对象
			JSONObject rootObj = new JSONObject(sb.toString());
			JSONObject contentNames = rootObj.getJSONObject("contentNames");

			ArrayList<String> al = new ArrayList<String>();
			for (int x = 0; x < list.size(); x++) {
				if (x > 1) {
					String filename = list.get(x).filename.split("\\.")[0];
					String contentname = contentNames.getString(filename);
					al.add("\"" + filename + "\"");
					al.add("\"" + contentname + "\"");
				}
			}
			// 对contentNames只能用字符串方式解析，因为key值不是固定的
			albumInfo.setContentList(al);
			albumInfo.setInfoList(DRMUtil.parserJSONToArrayList(rootObj.getString("infos")));
			albumInfo.setInfoObj(rootObj.getJSONObject("infos"));

			// 将字符串转成JSON对象
			// JSONObject rootObj = new JSONObject(sb.toString());
			// 对contentNames只能用字符串方式解析，因为key值不是固定的
			// albumInfo.setContentList(DRMUtil.parserJSONToArrayList(rootObj.getString("contentNames")));
			// albumInfo.setInfoList(DRMUtil.parserJSONToArrayList(rootObj.getString("infos")));
			// albumInfo.setInfoObj(rootObj.getJSONObject("infos"));
			// System.out.println(albumInfo);
			// System.out.println(albumInfo);

		} catch (FileNotFoundException e) {
			throw AppException.file(e);
		} catch (IOException e) {
			throw AppException.io(e);
		} catch (JSONException e) {
			throw AppException.json(e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return albumInfo;
	}

	/**
	 * 获取文件类型
	 * 
	 * @param fileName
	 * @return
	 */
	public static CommonFile.FILETYPE getFileType(String fileName) {
		String type = getExtFromFileName(fileName);
		if ("pdf".equals(type)) {
			return CommonFile.FILETYPE.PDF;
		} else if ("xml".equals(type)) {
			if (fileName.contains("albumInfo")) {
				return CommonFile.FILETYPE.ALBUMINFO;
			} else {
				return CommonFile.FILETYPE.RIGHT;
			}
		} else if ("mp3".equals(type)) {
			return CommonFile.FILETYPE.MP3;
		} else if ("mp4".equals(type)) {
			return CommonFile.FILETYPE.MP4;
		} else if ("drm".equals(type)) {
			return CommonFile.FILETYPE.DRM;
		}
		return CommonFile.FILETYPE.UNDEFINITION;
	}

	// /////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////

	/**
	 * 在SD卡上创建文件
	 * 
	 * @param filename
	 * @param dir
	 * @return
	 */
	public File createFileInSDCard(String filename, String dir) throws IOException {
		File file = new File(SDCardRoot + dir + File.separator + filename);
		file.createNewFile();
		return file;
	}

	/**
	 * 在SD卡上创建目录
	 * 
	 * @param dir
	 * @return
	 */
	public File createDirInSDCard(String dir) {
		File dirFile = new File(SDCardRoot + dir + File.separator);
		dirFile.mkdir();
		return dirFile;
	}

	/**
	 * 判断文件在SD卡上是否存在
	 * 
	 * @param filename
	 * @param path
	 * @return
	 */
	public boolean isFileExist(String filename, String path) {
		File file = new File(SDCardRoot + path + File.separator + filename);
		return file.exists();
	}

	/**
	 * 将一个InputStream里面的数据写入到SD卡中
	 * 
	 * @param path
	 * @param filename
	 * @param inStream
	 * @return
	 */
	public File writeToSDCardInput(String path, String filename, InputStream inStream) {
		File file = null;
		OutputStream outStrem = null;
		try {
			createDirInSDCard(path); // 创建目录
			file = createFileInSDCard(filename, path); // 创建文件
			outStrem = new FileOutputStream(file); // 得到文件输出流
			byte[] buffer = new byte[4 * 1024]; // 定义一个缓冲区
			int len;
			while ((len = inStream.read(buffer)) != -1) {
				outStrem.write(buffer, 0, len);
			}
			outStrem.flush(); // 确保数据写入到磁盘当中
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				outStrem.close(); // 关闭输出流
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return file;
	}

	/*
	 * public List<Mp3Info> getMp3Infos(String path) { List<Mp3Info> mp3Infos =
	 * new ArrayList<Mp3Info>(); File file = new File(SDCardRoot +
	 * File.separator + path); File[] files = file.listFiles(); FileUtils
	 * fileUtils = new FileUtils(); for (int i = 0; i < files.length; i++) { if
	 * (files[i].getName().endsWith("mp3")) { Mp3Info mp3Info = new Mp3Info();
	 * mp3Info.setTitle(files[i].getName()); mp3Info.setSize(files[i].length());
	 * String temp[] = mp3Info.getTitle().split("\\."); String lrcName = temp[0]
	 * + ".lrc"; if (fileUtils.isFileExist(lrcName, "/mp3")) {
	 * mp3Info.setLrcTitle(lrcName); } mp3Infos.add(mp3Info); } } return
	 * mp3Infos; }
	 */

	public static class CommonFile {
		public String filename;
		public String filepath;
		public FILETYPE filetype;
		public long filestart;
		public long fileSize;

		public enum FILETYPE {
			DRM, RIGHT, PDF, MP3, MP4, TXT, ALBUMINFO, UNDEFINITION;// *.drm文件，权限文件，各种资源文件，albumInfo文件
		}

	}

	/**
	 * 获取指定路径下文件夹内文件的大小，存入到map中。
	 */
	private Map<String, String> getPathSizi() {
		// 读取文件夹
		File file = new File(DRMUtil.DEFAULT_SAVE_FILE_DOWNLOAD_PATH);
		String test[];
		test = file.list();
		Map<String, String> map = new HashMap<String, String>();
		for (int i = 0; i < test.length; i++) {
			// String path = test[i];
			String isdrmpath = FileUtils.getPathFromFileDRM(test[i]);
			if (!"".equals(isdrmpath)) {
				String drmpath = DRMUtil.DEFAULT_SAVE_FILE_DOWNLOAD_PATH + File.separator + isdrmpath + ".drm";
				long fileOffset = FileUtils.getFileSize(drmpath);
				String perSize = FileUtils.convertStorage(fileOffset);// 获取文件的大小。
				map.put(isdrmpath, perSize);
			}
		}
		return map;
	}

	// 获取本地文件存在文件夹以及存在drm文件。
	public static ArrayList<String> getDownloadFile(String defaultsavefilepath) {
		ArrayList<String> al = new ArrayList<String>();
		// 读取文件夹
		File file = new File(defaultsavefilepath);
		if (file.exists()) {
			String test[];
			test = file.list();
			// 遍历数组。
			for (int i = 0; i < test.length; i++) {
				String pathdrm = test[i];
				String isdrmpath = FileUtils.getPathFromFileDRM(pathdrm);
				if (!"".equals(isdrmpath)) {
					File file1 = new File(defaultsavefilepath + File.separator + isdrmpath);
					if (file1.exists()) {
						al.add(isdrmpath);
					}
				}
			}
			return al;
		} else {
			return null;
		}
	}
	
	/**
	 * 检查sd卡上存储的日志文件
	 */
	public static void checkSDCardCrashLog() {
		// sdcard/Meeting/crash/
		String path = DRMUtil.getDefaultSDCardRootPath() + "crash" + File.separator;
		File dir = new File(path);
		if (!dir.exists())
			return;
		if (!dir.isDirectory())
			return;
		File[] files = dir.listFiles();
		if (files == null)
			return;
		int fileCount = files.length;
		DRMLog.i("log count: " + fileCount);
		if (fileCount > 20) {
			clearCrashLogs(files);
		}

	}
	
	// 清除保存的log
	public static void clearCrashLogs(File[] files) {
		for (File file : files) {
			if (file != null && file.isFile()) {
				try {
					// name: 2015-11-19-16-52-39-453
					String fileName = FileUtils.getNameFromFileName(file.getName());
					Date date = TimeUtil.getDateFromDateString(fileName, CrashHandler.LOGPATTERN);
					if (new Date().after(date)) {
						// 删除除今天之前记录的log
						file.delete();
					}
				} catch (Exception e) {
					file.delete();
				}
			}
		}
	}
	
	
	
	

}