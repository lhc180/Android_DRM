package cn.com.pyc.szpbb.sdk.response;

import java.util.ArrayList;
import java.util.List;

import cn.com.pyc.szpbb.sdk.models.SZFolderInfo;

public class SZResponseGetShareInfo extends SZResponse {

    private ShareInfo data;
    private SharePageInfo pageInfo;

    public ShareInfo getData() {
        return data;
    }

    public void setData(ShareInfo data) {
        this.data = data;
    }

    public SharePageInfo getPageInfo() {
        return pageInfo;
    }

    public void setPageInfo(SharePageInfo pageInfo) {
        this.pageInfo = pageInfo;
    }

    public static class ShareInfo {
        private long create_time;
        private int filecount;
        private boolean iswithdraw;
        private long last_modify_time;
        private int limit_num;
        private int max_user_num;
        private String message;
        private String myshare_id;
        private String url;
        private String owner;
        private int receive_device_num;
        private long receive_time;
        private int receive_user_num;
        private boolean received;
        private String share_mode;
        private String share_time_type;
        private String share_time;
        private String theme;
        private int max_device_num;

        public long getCreate_time() {
            return create_time;
        }

        public void setCreate_time(long create_time) {
            this.create_time = create_time;
        }

        public int getFilecount() {
            return filecount;
        }

        public void setFilecount(int filecount) {
            this.filecount = filecount;
        }

        public boolean isIswithdraw() {
            return iswithdraw;
        }

        public void setIswithdraw(boolean iswithdraw) {
            this.iswithdraw = iswithdraw;
        }

        public long getLast_modify_time() {
            return last_modify_time;
        }

        public void setLast_modify_time(long last_modify_time) {
            this.last_modify_time = last_modify_time;
        }

        public int getLimit_num() {
            return limit_num;
        }

        public void setLimit_num(int limit_num) {
            this.limit_num = limit_num;
        }

        public int getMax_user_num() {
            return max_user_num;
        }

        public void setMax_user_num(int max_user_num) {
            this.max_user_num = max_user_num;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getMyshare_id() {
            return myshare_id;
        }

        public void setMyshare_id(String myshare_id) {
            this.myshare_id = myshare_id;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getOwner() {
            return owner;
        }

        public void setOwner(String owner) {
            this.owner = owner;
        }

        public int getReceive_device_num() {
            return receive_device_num;
        }

        public void setReceive_device_num(int receive_device_num) {
            this.receive_device_num = receive_device_num;
        }

        public long getReceive_time() {
            return receive_time;
        }

        public void setReceive_time(long receive_time) {
            this.receive_time = receive_time;
        }

        public int getReceive_user_num() {
            return receive_user_num;
        }

        public void setReceive_user_num(int receive_user_num) {
            this.receive_user_num = receive_user_num;
        }

        public boolean isReceived() {
            return received;
        }

        public void setReceived(boolean received) {
            this.received = received;
        }

        public String getShare_mode() {
            return share_mode;
        }

        public void setShare_mode(String share_mode) {
            this.share_mode = share_mode;
        }

        public String getShare_time_type() {
            return share_time_type;
        }

        public void setShare_time_type(String share_time_type) {
            this.share_time_type = share_time_type;
        }

        public String getShare_time() {
            return share_time;
        }

        public void setShare_time(String share_time) {
            this.share_time = share_time;
        }

        public String getTheme() {
            return theme;
        }

        public void setTheme(String theme) {
            this.theme = theme;
        }

        public int getMax_device_num() {
            return max_device_num;
        }

        public void setMax_device_num(int max_device_num) {
            this.max_device_num = max_device_num;
        }

        /**
         * 永久有效
         *
         * @return
         */
        public boolean isUnlimmit() {
            return "unlimit".equals(this.share_time_type);
        }

        /**
         * 从某日期至某日期
         *
         * @return
         */
        public boolean isDayRange() {
            return "dayrange".equals(this.share_time_type);
        }

        /**
         * 从第一次打开起，n天内有效
         *
         * @return
         */
        public boolean isDays() {
            return "days".equals(this.share_time_type);
        }
    }

    public static class SharePageInfo {
        private int pageSize;
        private int currentPageNum;
        private int totalNum;
        // private int totalBookNum;
        // private int totalMusicNum;
        // private int totalVideoNum;
        // private int totalAllCategoryNum;
        private int totalPageNum;
        private List<SZFolderInfo> items;

        public int getPageSize() {
            return pageSize;
        }

        public void setPageSize(int pageSize) {
            this.pageSize = pageSize;
        }

        public int getCurrentPageNum() {
            return currentPageNum;
        }

        public void setCurrentPageNum(int currentPageNum) {
            this.currentPageNum = currentPageNum;
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

        public List<SZFolderInfo> getItems() {
            return items;
        }

        public void setItems(List<SZFolderInfo> items) {
            //this.items = items;
            this.items = getMyData(items);
        }

        private List<SZFolderInfo> getMyData(List<SZFolderInfo> datas) {
            List<SZFolderInfo> myDatas = new ArrayList<>();
            for (int i = 0; i < datas.size(); i++) {
                SZFolderInfo data = datas.get(i);
                data.setPosition(i);
                myDatas.add(data);
            }
            return myDatas;
        }

    }

}
