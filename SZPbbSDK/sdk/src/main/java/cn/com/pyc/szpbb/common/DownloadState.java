package cn.com.pyc.szpbb.common;

/**
 * 下载状态类
 */
public final class DownloadState
{
	/** 开始，默认值 */
	public static final int INIT = 0x0;
	/** 等待下载 */
	public static final int WAITING = 0x1;
	/** 连接 */
	public static final int CONNECTING = 0x2;
	/** 暂停 */
	public static final int PAUSE = 0x3;
	/** 下载中 */
	public static final int DOWNLOADING = 0x4;
	/** 下载异常 */
	public static final int DOWNLOAD_ERROR = 0x5;
	/** 下载完成 */
	public static final int FINISHED = 0x6;
	/** 正在验证 */
	public static final int CHECKING = 0x10;
	/** 等待验证 */
	public static final int WAITING_CHECKING = 0x11;

}
