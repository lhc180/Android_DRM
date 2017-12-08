package cn.com.pyc.drm.bean.event;

public class MuPDFBookMarkJPageEvent extends BaseEvent{
	private int pages;
	
	

	public MuPDFBookMarkJPageEvent(int pages) {
		super();
		this.pages = pages;
	}



	public int getPages() {
		return pages;
	}



	public void setPages(int pages) {
		this.pages = pages;
	}

	
}
