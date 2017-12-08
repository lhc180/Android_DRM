package cn.com.pyc.drm.model;

/**
 * Created by hudq on 2017/5/5.
 */

public class VerifyItemModel extends BaseModel {

    private ProductInfo data;

    public void setData(ProductInfo data) {
        this.data = data;
    }

    public ProductInfo getData() {
        return data;
    }
}
