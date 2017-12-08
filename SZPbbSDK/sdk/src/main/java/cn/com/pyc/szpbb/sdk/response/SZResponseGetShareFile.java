package cn.com.pyc.szpbb.sdk.response;

import java.util.ArrayList;
import java.util.List;

import cn.com.pyc.szpbb.sdk.models.SZFileData;

public class SZResponseGetShareFile extends SZResponse {

    private List<SZFileData> data;

    public List<SZFileData> getData() {
        return data;
    }

    public void setData(List<SZFileData> data) {
        //this.data = data;
        this.data = getMyData(data);
    }

    private List<SZFileData> getMyData(List<SZFileData> datas) {
        List<SZFileData> myDatas = new ArrayList<>();
        for (int i = 0; i < datas.size(); i++) {
            SZFileData data = datas.get(i);
            data.setPosition(i);
            myDatas.add(data);
        }
        return myDatas;
    }

}
