package cn.com.pyc.drm.model;

/**
 * Created by hudaqiang on 2017/7/6.
 */

public class CheckMyProIdModel extends BaseModel {

    private CheckIdData data;

    public void setData(CheckIdData data) {
        this.data = data;
    }

    public CheckIdData getData() {
        return data;
    }

    public static class CheckIdData {
        private String[] myProIds;

        public void setMyProIds(String[] myProIds) {
            this.myProIds = myProIds;
        }

        public String[] getMyProIds() {
            return myProIds;
        }
    }
}
