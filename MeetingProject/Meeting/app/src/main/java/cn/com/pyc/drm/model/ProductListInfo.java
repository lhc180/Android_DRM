package cn.com.pyc.drm.model;

import java.util.List;

/**
 * 产品列表
 * 
 * @author hudq
 */
public class ProductListInfo {
	
	private int currentPageNum;
	private int pageSize;
	private int totalNum;
	private int totalPageNum;
	private List<DownloadInfo> items;

	public int getCurrentPageNum() {
		return currentPageNum;
	}

	public void setCurrentPageNum(int currentPageNum) {
		this.currentPageNum = currentPageNum;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getTotalNum() {
		return totalNum;
	}

	public void setTotalNum(int totalNum) {
		this.totalNum = totalNum;
	}

	public int getTotalPageNum() {
		return totalPageNum;
	}

	public void setTotalPageNum(int totalPageNum) {
		this.totalPageNum = totalPageNum;
	}

	public List<DownloadInfo> getItems() {
		return items;
	}

	public void setItems(List<DownloadInfo> items) {
		this.items = items;
	}

	// public static RemoteResponseEntity mappingObject(String json) {
	// Gson gson = new Gson();
	// return gson.fromJson(json, RemoteResponseEntity.class);
	// }
}
