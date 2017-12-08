package cn.com.pyc.drm.common;

public interface ConstantValue
{
	/**
	 * 播放
	 */
	int OPTION_PLAY = 1;
	/**
	 * 暂停
	 */
	int OPTION_PAUSE = 2;
	/**
	 * 停止
	 */
	int OPTION_STOP = 3;
	/**
	 * 继续播放
	 */
	int OPTION_CONTINUE = 4;

	/**
	 * 让Activity更新进度
	 */
	int MSG_UPDATE_PROCESS = 5;

	/**
	 * 进度的改变
	 */
	int OPTION_CHANGE = 6;

	// ///////////播放模式///////////////////
	/**
	 * 列表循环
	 */
	int CIRCLE = 7;
	/**
	 * 单曲循环
	 */
	int SINGLE_R = 8;
	/**
	 * 随机播放
	 */
	int RANDOM = 9;
	/**
	 * 顺序播放 1次
	 */
	int ORDER = 10;

	// ///////////播放模式///////////////////

	/**
	 * 播放完成
	 */
	int FINISHED = 11;
	/**
	 * 释放资源
	 */
	int RELEASE = 12;

	// 播放前释放资源
	// int OPTION_PREPLAY = 13;

	/**
	 * 获取歌曲时间
	 */
	int OBTAIN_TIME = 14;

	
	
	/**
	 * 开启下载方法。
	 */
	int START_DOWNLOAD = 0xa1;

	/**
	 * 解析下载的drm文件
	 */
	int RESOLVE_DOWNLOAD = 0xa2;

	/**
	 * 创建数据库
	 */
	int CREATE_DB = 0xa3;

}
