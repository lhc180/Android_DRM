package cn.com.pyc.drm.model;

import java.util.ArrayList;
import java.util.List;

import cn.com.pyc.drm.common.DrmPat;
import cn.com.pyc.drm.utils.Util_;

public class SearchResultModel extends BaseModel {

    private SearchInfo data;

    public void setData(SearchInfo data) {
        this.data = data;
    }

    public SearchInfo getData() {
        return data;
    }

    public static class SearchInfo {
        private MyProduct myProducts;
        private SearchProduct searchProducts;
        private RecommendProduct recommendProducts;
        private String accessLogId;

        public MyProduct getMyProducts() {
            return myProducts;
        }

        public void setMyProducts(MyProduct myProducts) {
            this.myProducts = myProducts;
        }

        public SearchProduct getSearchProducts() {
            return searchProducts;
        }

        public void setSearchProducts(SearchProduct searchProducts) {
            this.searchProducts = searchProducts;
        }

        public RecommendProduct getRecommendProducts() {
            return recommendProducts;
        }

        public void setRecommendProducts(RecommendProduct recommendProducts) {
            this.recommendProducts = recommendProducts;
        }

        public void setAccessLogId(String accessLogId) {
            this.accessLogId = accessLogId;
        }

        public String getAccessLogId() {
            return accessLogId;
        }
    }

    /**
     * 推荐结果
     *
     * @author hudq
     */
    public static class RecommendProduct {
        private int totalPageNum;
        private List<SearchResult> items;

        public int getTotalPageNum() {
            return totalPageNum;
        }

        public void setTotalPageNum(int totalPageNum) {
            this.totalPageNum = totalPageNum;
        }

        public List<SearchResult> getItems() {
            if (items == null) return new ArrayList<>();
            return setMarkSource(items, DrmPat.RECOMMONED);
        }

        public void setItems(List<SearchResult> items) {
            this.items = items;
        }
    }

    /**
     * 搜索结果
     *
     * @author hudq
     */
    public static class SearchProduct {
        private int totalPageNum;
        private List<SearchResult> items;

        public int getTotalPageNum() {
            return totalPageNum;
        }

        public void setTotalPageNum(int totalPageNum) {
            this.totalPageNum = totalPageNum;
        }

        public List<SearchResult> getItems() {
            if (items == null) return new ArrayList<>();
            return setMarkSource(items, DrmPat.SEARCHED);
        }

        public void setItems(List<SearchResult> items) {
            this.items = items;
        }
    }

    /**
     * 我的购买产品
     *
     * @author hudq
     */
    public static class MyProduct {
        // private int pageSize;
        // private int finishNum;
        // private int startNum;
        private int totalPageNum;
        private List<SearchResult> items;

        public int getTotalPageNum() {
            return totalPageNum;
        }

        public void setTotalPageNum(int totalPageNum) {
            this.totalPageNum = totalPageNum;
        }

        public List<SearchResult> getItems() {
            if (items == null) return new ArrayList<>();
            return setMarkSource(items, DrmPat.BUYED);
        }

        public void setItems(List<SearchResult> items) {
            this.items = items;
        }
    }

    /**
     * 搜索结果
     *
     * @author hudq
     */
    public static class SearchResult {
        private String authors;
        private String picture_url;
        private String picture_ratio;
        private String preview;
        private String myProId;
        private String proId;
        private String sellerId;
        private String productName;
        private String category;

        private String onShelves_time;//上架时间
        private String grade; //等级
        private String sellerName;//店铺名称
        private String totalPrice;//总价
        private String discountPrice;//折扣价
        private String validity;//有效期类型：为空:没有设置有效期，daterange:设置有效期期限，days:设置天数

        private String use_start_time;//有效期开始时间
        private String use_end_time;//有效期结束时间
        private String useDays;//有效期天数
        private String productPv; //访问量

        private int source; // -1 购买的； 32 搜索结果；64 推荐

        public void setSource(int source) {
            this.source = source;
        }

        public int getSource() {
            return source;
        }

        public String getAuthors() {
            return authors;
        }

        public void setAuthors(String authors) {
            this.authors = authors;
        }

        public String getPicture_url() {
            return picture_url;
        }

        public void setPicture_url(String picture_url) {
            this.picture_url = picture_url;
        }

        public String getPicture_ratio() {
            return picture_ratio;
        }

        public void setPicture_ratio(String picture_ratio) {
            this.picture_ratio = picture_ratio;
        }

        public String getPreview() {
            return preview;
        }

        public void setPreview(String preview) {
            this.preview = preview;
        }

        public String getMyProId() {
            return myProId;
        }

        public void setMyProId(String myProId) {
            this.myProId = myProId;
        }

        public String getProId() {
            return proId;
        }

        public void setProId(String proId) {
            this.proId = proId;
        }

        public void setSellerId(String sellerId) {
            this.sellerId = sellerId;
        }

        public String getSellerId() {
            return sellerId;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        /**
         * 类别：MUSIC; VIDEO; BOOK
         */
        public String getCategory() {
            return Util_.getAlbumCategory(this.category);
        }

        /**
         * 中文类别：音乐；视频；图书
         */
        public String getCategoryName() {
            return category;
        }

        public String getOnShelves_time() {
            return onShelves_time;
        }

        public void setOnShelves_time(String onShelves_time) {
            this.onShelves_time = onShelves_time;
        }

        public String getGrade() {
            return grade;
        }

        public void setGrade(String grade) {
            this.grade = grade;
        }

        public String getSellerName() {
            return sellerName;
        }

        public void setSellerName(String sellerName) {
            this.sellerName = sellerName;
        }

        public String getTotalPrice() {
            return totalPrice;
        }

        public void setTotalPrice(String totalPrice) {
            this.totalPrice = totalPrice;
        }

        public String getDiscountPrice() {
            return discountPrice;
        }

        public void setDiscountPrice(String discountPrice) {
            this.discountPrice = discountPrice;
        }

        public String getValidity() {
            return validity;
        }

        public void setValidity(String validity) {
            this.validity = validity;
        }

        public String getUse_end_time() {
            return use_end_time;
        }

        public void setUse_end_time(String use_end_time) {
            this.use_end_time = use_end_time;
        }

        public String getUse_start_time() {
            return use_start_time;
        }

        public void setUse_start_time(String use_start_time) {
            this.use_start_time = use_start_time;
        }

        public String getUseDays() {
            return useDays;
        }

        public void setUseDays(String useDays) {
            this.useDays = useDays;
        }

        public String getProductPv() {
            return productPv;
        }

        public void setProductPv(String productPv) {
            this.productPv = productPv;
        }
    }

    /**
     * 设置对应的搜索来源
     *
     * @param targetItems
     * @param source
     * @return
     * @see DrmPat.BUYED
     * @see DrmPat.SEARCHED
     * @see DrmPat.RECOMMONED
     */
    private static List<SearchResult> setMarkSource(
            List<SearchResult> targetItems, int source) {
        List<SearchResult> results = new ArrayList<SearchResultModel.SearchResult>();
        for (SearchResult s : targetItems) {
            s.setSource(source);
            results.add(s);
        }
        targetItems.clear();
        targetItems = null;
        return results;
    }

}
