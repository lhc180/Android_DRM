package cn.com.pyc.szpbb.util;

import java.io.File;
import java.io.IOException;

public class FileUtil {
    /**
     * 根据文件绝对路径生成文件
     *
     * @param filePath 文件绝对路径
     * @return
     */
    public static File createFile(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            file.createNewFile();
        }
        return file;
    }

    /**
     * 检查文件是否存在
     *
     * @param path
     * @return
     */
    public static boolean checkFileExist(String path) {
        return checkFileExist(new File(path));
    }

    /**
     * 检查文件是否存在
     *
     * @param file
     * @return
     */
    public static boolean checkFileExist(File file) {
        return file != null && file.exists();
    }

    /**
     * 新建目录
     *
     * @param directoryName 目录全路径名
     * @return
     */
    public static boolean createDirectory(String directoryName) {
        if (checkFileExist(directoryName)) {
            return true;
        }
        SZLog.i("create directory: " + directoryName);
        return !"".equals(directoryName) && new File(directoryName).mkdirs();
    }

    /***
     * 获取文件名（不带扩展名）
     * <p>
     * <p>
     * eg：abcdef.jpg; return "abcdef"
     *
     * @param fileName
     * @return
     */
    public static String getNameFromFileName(String fileName) {
        if (fileName == null) {
            return "";
        }
        int dotPosition = fileName.lastIndexOf('.');
        if (dotPosition != -1) {
            return fileName.substring(0, dotPosition);
        }
        return "";
    }

    /**
     * 获取文件扩展名
     * <p>
     * eg：abcdef.jpg; return "jpg"
     *
     * @param fileName
     * @return
     */
    public static String getExtNameFromFileName(String fileName) {
        if (fileName == null) {
            return "";
        }
        int dotPosition = fileName.lastIndexOf('.');
        if (dotPosition != -1) {
            return fileName.substring(dotPosition + 1);
        }
        return "";
    }

    /**
     * 从文件路径获取文件名
     * <p>
     * eg：/sz/file/abcdef.drm
     * <p>
     * 得到abcdef.drm
     *
     * @param filePath
     * @return
     */
    public static String getNameFromFilePath(String filePath) {
        if (filePath == null) {
            return "";
        }
        int pos = filePath.lastIndexOf('/');
        if (pos != -1) {
            return filePath.substring(pos + 1);
        }
        return "";
    }

    /**
     * 删除指定文件夹下所有文件
     *
     * @param path 文件夹完整绝对路径
     */
    public static void deleteAllFile(String path) {
        File file = new File(path);
        if (!file.exists()) return;
        if (!file.isDirectory()) return;

        String[] tempList = file.list();
        if (tempList == null) return;
        File temp = null;
        for (String s : tempList) {
            if (path.endsWith(File.separator)) {
                temp = new File(path + s);
            } else {
                temp = new File(path + File.separator + s);
            }
            if (temp.isFile()) {
                temp.delete();
                SZLog.i("delete: " + temp.getAbsolutePath());
            }
            if (temp.isDirectory()) {
                deleteAllFile(path + File.separator + s);// 先删除文件夹里面的文件
            }
        }
        file.delete();
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
            SZLog.i("deleteFileWithPath:" + filePath);
            f.delete();
            return true;
        }
        return false;
    }

    /**
     * 获取文件长度
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

}
