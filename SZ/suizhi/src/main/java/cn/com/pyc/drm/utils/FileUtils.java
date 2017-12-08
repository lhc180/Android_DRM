package cn.com.pyc.drm.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件操作工具包
 */
public class FileUtils {

    // private static final double KB = 1024.0;
    // private static final double MB = KB * KB;
    // private static final double GB = KB * KB * KB;
    //
    // private static final String PRIMARY_FOLDER = "pref_key_primary_folder";
    // private static final String READ_ROOT = "pref_key_read_root";
    // // private static final String SYSTEM_SEPARATOR = File.separator;
    //
    //
    // // public static int CATEGORY_TAB_INDEX = 0;
    // // public static int SDCARD_TAB_INDEX = 1;
    //
    // // does not include sd card folder
    // private static String[] SysFileDirs = new String[] {
    // "miren_browser/imagecaches" };

    private static final String SDCardRoot = Environment
            .getExternalStorageDirectory().getAbsolutePath() + File.separator;
    private static final String SHOW_REAL_PATH = "pref_key_show_real_path";
    private static final String ANDROID_SECURE = SDCardRoot + ".android_secure";


    /**
     * 根据文件的编码获取文件缓冲流
     *
     * @param file
     * @return
     */
    public static Reader getFileBufferByEncode(File file) throws IOException {
        BufferedReader reader = null;
        FileInputStream fis = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(fis);
        bis.mark(4);
        byte[] first3bytes = new byte[3];
        //找到文档的前三个字节并自动判断文档类型。
        bis.read(first3bytes);
        bis.reset();
        if (first3bytes[0] == (byte) 0xEF
                && first3bytes[1] == (byte) 0xBB
                && first3bytes[2] == (byte) 0xBF) {
            reader = new BufferedReader(new InputStreamReader(bis, "utf-8"));
            DRMLog.e("utf-8");
        } else if (first3bytes[0] == (byte) 0xFF
                && first3bytes[1] == (byte) 0xFE) {
            reader = new BufferedReader(new InputStreamReader(bis, "unicode"));
            DRMLog.e("unicode");
        } else if (first3bytes[0] == (byte) 0xFE
                && first3bytes[1] == (byte) 0xFF) {
            reader = new BufferedReader(new InputStreamReader(bis, "utf-16be"));
            DRMLog.e("utf-16be");
        } else if (first3bytes[0] == (byte) 0xFF
                && first3bytes[1] == (byte) 0xFF) {
            reader = new BufferedReader(new InputStreamReader(bis, "utf-16le"));
            DRMLog.e("utf-16le");
        } else {
            reader = new BufferedReader(new InputStreamReader(bis, "gbk"));
            DRMLog.e("gbk");
        }
        return reader;
    }


    /**
     * 传入文件名以及字符串, 将字符串信息保存到文件中
     *
     * @param fileName
     * @param text
     */
    public static File writeTextToFile(final String fileName, final String text) {
        FileWriter fileWriter = null;
        File file = null;
        try {
            // 创建文件对象
            file = new File(fileName);
            // 向文件写入对象写入信息
            fileWriter = new FileWriter(file);
            // 写文件
            fileWriter.write(text);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileWriter != null) {
                    fileWriter.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

//    /**
//     * 以正确的编码读取文本文件中的字符串（解决歌词文件乱码问题）
//     *
//     * @param fileName 文件路径
//     * @return 文件中的文本内容
//     */
//    public static String readFile(String fileName) {
//        File file = new File(fileName);
//        FileInputStream fis = null;
//        BufferedInputStream bis = null;
//        BufferedReader reader;
//        String text = "";
//        try {
//            fis = new FileInputStream(file);
//            bis = new BufferedInputStream(fis);
//            bis.mark(4);
//            byte[] first3bytes = new byte[3];
//
//            //找到文档的前三个字节并自动判断文档类型。
//            bis.read(first3bytes);
//            bis.reset();
//            if (first3bytes[0] == (byte) 0xEF && first3bytes[1] == (byte) 0xBB
//                    && first3bytes[2] == (byte) 0xBF) {// utf-8
//
//                reader = new BufferedReader(new InputStreamReader(bis, "utf-8"));
//
//            } else if (first3bytes[0] == (byte) 0xFF
//                    && first3bytes[1] == (byte) 0xFE) {
//
//                reader = new BufferedReader(
//                        new InputStreamReader(bis, "unicode"));
//            } else if (first3bytes[0] == (byte) 0xFE
//                    && first3bytes[1] == (byte) 0xFF) {
//
//                reader = new BufferedReader(new InputStreamReader(bis,
//                        "utf-16be"));
//            } else if (first3bytes[0] == (byte) 0xFF
//                    && first3bytes[1] == (byte) 0xFF) {
//
//                reader = new BufferedReader(new InputStreamReader(bis,
//                        "utf-16le"));
//            } else {
//
//                reader = new BufferedReader(new InputStreamReader(bis, "GBK"));
//            }
//            String str = reader.readLine();
//
//            while (str != null) {
//                text = text + str + "/n";
//                str = reader.readLine();
//            }
//            System.out.println("text" + text);
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if (fis != null) {
//                try {
//                    fis.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//            if (bis != null) {
//                try {
//                    bis.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//
//        return text;
//    }

    /**
     * 获取文件扩展名
     * <p>
     * eg：askahj1145asd.drm
     * <p>
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
     * <p>
     * eg：askahj1145asd.drm
     * <p>
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
     * <p>
     * eg：/suizhi/file/askahj1145asd.drm
     * <p>
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

    /**
     * 从文件路径获取文件名
     * <p>
     * eg：/suizhi/file/acd123aaa.drm
     * <p>
     * 得到acd123aaa.drm
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
     * 根据文件绝对路径生成文件
     *
     * @param filePath 文件绝对路径
     * @return
     */
    public static File createFile(String filePath) throws IOException {
        File file = new File(filePath);
//        if (file.exists() && file.length() > 0L) {
//            //先对要删除的文件进行重命名，然后再删除。这样删除过程中的文件锁就加在另一个文件上了，不会影响再次创建的过程
//            final File to = new File(file.getAbsolutePath() + ".temp");
//            file.renameTo(to);
//            file.delete();
//            Log.i("createFile", "delete:" + filePath);
//        }

        if (file.exists()) return file;


        file.createNewFile();
        return file;
    }

    public static File createFile(String filePath, String fileName) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            file.mkdirs();
        }
        File file2 = new File(filePath, fileName);
        if (file2.isDirectory()) {
            file2.delete();
            DRMLog.e("文件夹:" + file2.getPath());
        } else if (file2.exists()) {
            return file2;
        }
        file2.createNewFile();
        return file2;
    }

    /**
     * 检查路径是否存在
     *
     * @param path
     * @return
     */
    public static boolean checkFilePathExists(String path) {
        return path != null && new File(path).exists();
    }

    /**
     * 新建目录
     *
     * @param directoryName 目录全路径
     * @return
     */
    public static boolean createDirectory(String directoryName) {
        if (checkFilePathExists(directoryName)) {
            return true;
        }
        if (TextUtils.isEmpty(directoryName))
            return false;
        DRMLog.i(directoryName);
        return new File(directoryName).mkdirs();
    }

    /**
     * 删除文件
     *
     * @param filePath
     */
    public static boolean deleteFileWithPath(String filePath) {
        if (!checkFilePathExists(filePath)) {
            return false;
        }
        SecurityManager checker = new SecurityManager();
        File f = new File(filePath);
        checker.checkDelete(filePath);
        if (f.isFile()) {
            DRMLog.i(filePath);
            return f.delete();
        }
        return false;
    }

    /**
     * 删除文件夹 param folderPath 文件夹完整绝对路径
     */
    // public static void delFolder(String folderPath)
    // {
    // try
    // {
    // delAllFile(folderPath); // 删除完里面所有内容
    // // String filePath = folderPath;
    // // filePath = filePath.toString();
    // java.io.File myFilePath = new java.io.File(folderPath);
    // myFilePath.delete(); // 删除空文件夹
    // } catch (Exception e)
    // {
    // e.printStackTrace();
    // }
    // }

    /**
     * 递归删除文件和文件夹
     */
    public static void deleteAllFile(File file) {
        if (file == null) return;
        if (file.isFile()) {
            file.delete();
            return;
        }
        if (file.isDirectory()) {
            File[] childFile = file.listFiles();
            if (childFile == null || childFile.length == 0) {
                file.delete();
                return;
            }
            for (File f : childFile) {
                deleteAllFile(f);
            }
            file.delete();
        }
    }

    /**
     * 递归删除文件和文件夹
     */
    public static void deleteAllFile(String path) {
        if (path == null) return;
        deleteAllFile(new File(path));
    }

    /**
     * 删除指定文件夹下所有文件 param path 文件夹完整绝对路径
     */
    @Deprecated
    public static void delAllFile(String path) {
        File file = new File(path);
        if (!file.exists()) return;
        if (!file.isDirectory()) return;

        String[] nameArray = file.list();
        if (nameArray == null) return;

        File tempFile = null;
        for (String fileName : nameArray) {
            tempFile = new File(path.endsWith(File.separator) ?
                    path + fileName : path + File.separator + fileName);
            if (tempFile.isFile()) {
                boolean result = tempFile.delete();
                DRMLog.d("delAllFile", result + ";" + tempFile.getAbsolutePath());
            }
            if (tempFile.isDirectory()) {
                delAllFile(path + File.separator + fileName);
            }
        }
//        for (int i = 0; i < tempList.length; i++) {
//            if (path.endsWith(File.separator)) {
//                temp = new File(path + tempList[i]);
//            } else {
//                temp = new File(path + File.separator + tempList[i]);
//            }
//            if (temp.isFile()) {
//                temp.delete();
//                DRMLog.d("delAllFile", temp.getAbsolutePath());
//            }
//            if (temp.isDirectory()) {
//                delAllFile(path + File.separator + tempList[i]);// 先删除文件夹里面的文件
//                // delFolder(path + "/" + tempList[i]);// 再删除空文件夹
//            }
//        }
        file.delete();
    }

    /**
     * 获取文件大小
     *
     * @param filePath
     * @return
     */
    public static long getFileSize(String filePath) {
        long size = 0;
        File file = new File(filePath);
        if (file.exists()) {
            size = file.length();
        }
        return size;
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

    /*
     * File status information. This class maps directly to the POSIX stat
     * structure.
     *
     * @hide
     */
//    public static final class FileStatus {
//        public int dev;
//        public int ino;
//        public int mode;
//        public int nlink;
//        public int uid;
//        public int gid;
//        public int rdev;
//        public long size;
//        public int blksize;
//        public long blocks;
//        public long atime;
//        public long mtime;
//        public long ctime;
//    }

    // /////////////////////////////////////////////////////////////////////////////
    // /////////////////////////////////////////////////////////////////////////////
    // /////////////////////////////////////////////////////////////////////////////
    // /////////////////////////////////////////////////////////////////////////////

    // /////////////////////////////////////////////////////////////////////////////
    // /////////////////////////////////////////////////////////////////////////////
    // /////////////////////////////////////////////////////////////////////////////
    // /////////////////////////////////////////////////////////////////////////////

    /**
     * Get the path for the file:/// only
     *
     * @param uri
     * @return
     */
    public static String getPath(String uri) {

        if (TextUtils.isEmpty(uri)) return null;
        if (uri.startsWith("file://") && uri.length() > "file://".length()) // >7
            return Uri.decode(uri.substring("file://".length())); // 7
        return Uri.decode(uri);
    }

    public static String getName(String uri) {
        String path = getPath(uri);
        if (path != null) return new File(path).getName();
        return null;
    }

    /**
     * 在SD卡上创建文件
     *
     * @param filename
     * @param dir
     * @return
     */
    public File createFileInSDCard(String filename, String dir)
            throws IOException {
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
    public File writeToSDCardInput(String path, String filename,
                                   InputStream inStream) {
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

    // public static FileInfo GetFileInfo(String filePath)
    // {
    // File lFile = new File(filePath);
    // if (!lFile.exists()) return null;
    // FileInfo lFileInfo = new FileInfo();
    // lFileInfo.canRead = lFile.canRead();
    // lFileInfo.canWrite = lFile.canWrite();
    // lFileInfo.isHidden = lFile.isHidden();
    // lFileInfo.fileName = getNameFromFilePath(filePath);
    // lFileInfo.ModifiedDate = lFile.lastModified();
    // lFileInfo.IsDir = lFile.isDirectory();
    // lFileInfo.filePath = filePath;
    // lFileInfo.fileSize = lFile.length();
    // return lFileInfo;
    // }

    // public static FileInfo GetFileInfo(File f, FilenameFilter filter,
    // boolean showHidden)
    // {
    // FileInfo lFileInfo = new FileInfo();
    // String filePath = f.getPath();
    // File lFile = new File(filePath);
    // lFileInfo.canRead = lFile.canRead();
    // lFileInfo.canWrite = lFile.canWrite();
    // lFileInfo.isHidden = lFile.isHidden();
    // lFileInfo.fileName = f.getName();
    // lFileInfo.ModifiedDate = lFile.lastModified();
    // lFileInfo.IsDir = lFile.isDirectory();
    // lFileInfo.filePath = filePath;
    // if (lFileInfo.IsDir)
    // {
    // int lCount = 0;
    // File[] files = lFile.listFiles(filter);
    // if (files == null) { return null; }
    // for (File child : files)
    // {
    // if ((!child.isHidden() || showHidden)
    // && isNormalFile(child.getAbsolutePath()))
    // {
    // lCount++;
    // }
    // }
    // lFileInfo.Count = lCount;
    //
    // } else
    // {
    //
    // lFileInfo.fileSize = lFile.length();
    //
    // }
    // return lFileInfo;
    // }

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

//    public static void updateActionModeTitle(ActionMode mode, Context context,
//                                             int selectedNum) {
//        if (mode != null) {
//            mode.setTitle(context.getString(R.string.multi_select_title,
//                    selectedNum));
//            if (selectedNum == 0) {
//                mode.finish();
//            }
//        }
//    }

    // public static String getSdDirectory()
    // {
    // return Environment.getExternalStorageDirectory().getPath();
    // }
    //
    // public static boolean shouldShowFile(String path)
    // {
    // return shouldShowFile(new File(path));
    // }

    // public static boolean shouldShowFile(File file)
    // {
    // boolean show = Settings.instance().getShowDotAndHiddenFiles();
    // if (show) return true;
    //
    // if (file.isHidden()) return false;
    //
    // if (file.getName().startsWith(".")) return false;
    //
    // String sdFolder = getSdDirectory();
    // for (String s : SysFileDirs)
    // {
    // if (file.getPath().startsWith(makePath(sdFolder, s))) return false;
    // }
    //
    // return true;
    // }

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
    // public static boolean containsPath(String path1, String path2)
    // {
    // String path = path2;
    // while (path != null)
    // {
    // if (path.equalsIgnoreCase(path1)) return true;
    //
    // if (path.equals(GlobalConsts.ROOT_PATH)) break;
    // path = new File(path).getParent();
    // }
    //
    // return false;
    // }
    //
    // public static boolean isReadRoot(Context context)
    // {
    // SharedPreferences settings = PreferenceManager
    // .getDefaultSharedPreferences(context);
    //
    // boolean isReadRootFromSetting = settings.getBoolean(READ_ROOT, false);
    // boolean isReadRootWhenSettingPrimaryFolderWithoutSdCardPrefix =
    // !getPrimaryFolder(
    // context).startsWith(getSdDirectory());
    //
    // return isReadRootFromSetting
    // || isReadRootWhenSettingPrimaryFolderWithoutSdCardPrefix;
    // }
    //
    // public static String getPrimaryFolder(Context context)
    // {
    // SharedPreferences settings = PreferenceManager
    // .getDefaultSharedPreferences(context);
    // String primaryFolder = settings.getString(PRIMARY_FOLDER, context
    // .getString(R.string.default_primary_folder,
    // GlobalConsts.ROOT_PATH));
    //
    // if (TextUtils.isEmpty(primaryFolder))
    // { // setting primary folder =
    // // empty("")
    // primaryFolder = GlobalConsts.ROOT_PATH;
    // }
    //
    // // it's remove the end char of the home folder setting when it with the
    // // '/' at the end.
    // // if has the backslash at end of the home folder, it's has minor bug at
    // // "UpLevel" function.
    // int length = primaryFolder.length();
    // if (length > 1
    // && File.separator.equals(primaryFolder.substring(length - 1)))
    // { // length
    // // =
    // // 1,
    // // ROOT_PATH
    // return primaryFolder.substring(0, length - 1);
    // } else
    // {
    // return primaryFolder;
    // }
    // }

    public static boolean showRealPath(Context context) {
        SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(context);
        return settings.getBoolean(SHOW_REAL_PATH, false);
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

    /*
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

    /*
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

    /*
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
     * 从文件路径获取文件的路径
     * <p>
     * eg：/suizhi/file/askahj1145asd.drm
     * <p>
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
     * 获取指定路径下文件夹内文件的大小，存入到map中。
     */
    protected Map<String, String> getPathSize() {
        // 读取文件夹
        File file = new File(PathUtil.getDRMPrefixPath());
        String test[];
        test = file.list();
        Map<String, String> map = new HashMap<String, String>();
        for (int i = 0; i < test.length; i++) {
            // String path = test[i];
            String isdrmpath = FileUtils.getPathFromFileDRM(test[i]);
            if (!"".equals(isdrmpath)) {
                String drmpath = PathUtil.getDRMPrefixPath() + File.separator
                        + isdrmpath + ".drm";
                long fileOffset = FileUtils.getFileSize(drmpath);
                String perSize = FormatUtil.formatSize(fileOffset);// 获取文件的大小。
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
                    File file1 = new File(defaultsavefilepath + File.separator
                            + isdrmpath);
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
     * 复制整个文件夹内容到另一个文件夹中
     *
     * @param srcFiles 源文件夹
     * @param desFiles 目标文件夹
     */
    public static void copyFile(File srcFiles, File desFiles, OnCopyProgressListener listener) {
        if (!srcFiles.exists()) {
            DRMLog.e("源文件路径缺失~");
            return;
        }
        if (!desFiles.exists()) // 如果文件夹不存在
            desFiles.mkdirs(); // 建立新的文件夹
        try {
            File[] fl = srcFiles.listFiles();
            for (int i = 0; i < fl.length; i++) {
                if (fl[i].isFile()) { // 如果是文件类型就复制文件
                    long mCurrentSize = 0;
                    int length = -1, mTempPercent = 0;
                    DRMLog.i("源文件路径：" + fl[i].getPath());
                    DRMLog.i("源文件名称：" + fl[i].getName());
                    FileInputStream fis = new FileInputStream(fl[i]);
                    FileOutputStream out = new FileOutputStream(new File(desFiles.getPath()
                            + File.separator + fl[i].getName()));
                    //long totalSize = fis.available();
                    long totalSize = fis.getChannel().size();
                    byte[] buffer = new byte[2048];
                    while ((length = fis.read(buffer)) != -1) {
                        out.write(buffer, 0, length);        // 复制文件内容
                        mCurrentSize += length;    //处理进度数据
                        int mPercentage = (int) (mCurrentSize * 100 / totalSize);
                        if (mPercentage > mTempPercent) {
                            //发送进度
                            DRMLog.d("copyFile", "progress(%): " + mPercentage);
                            if (listener != null) {
                                listener.onCopyProgress(mPercentage);
                            }
                        }
                        mTempPercent = mPercentage;
                    }
                    out.close(); // 关闭输出流
                    fis.close(); // 关闭输入流
                }
                if (fl[i].isDirectory()) { // 如果是文件夹类型
                    DRMLog.d("copyFile", "文件夹：" + fl[i].getName());
                    File des = new File(desFiles.getPath() + File.separator + fl[i].getName());
                    des.mkdirs(); // 在目标文件夹中创建相同的文件夹
                    copyFile(fl[i], des, listener); // 递归调用方法本身
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface OnCopyProgressListener {
        void onCopyProgress(int progress);
    }

    /**
     * 重命名目录
     *
     * @param fromDir
     * @param toDir
     */
    public static void renameDirectory(String fromDir, String toDir) {
        File from = new File(fromDir);
        if (!from.exists() || !from.isDirectory()) {
            System.out.println("Directory does not exist: " + fromDir);
            return;
        }
        File to = new File(toDir);
        if (from.renameTo(to))
            System.out.println("renameDirectory Success!");
        else
            System.out.println("renameDirectory failed!");
    }

}