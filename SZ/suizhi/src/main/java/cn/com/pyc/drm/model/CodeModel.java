package cn.com.pyc.drm.model;

import cn.com.pyc.drm.common.Code;

/**
 * Created by hudq on 2017/4/10.
 */

public class CodeModel {

    private String msg;
    private String code;
    private String data;

    /**
     * 成功，返回Code = "1000" ;
     *
     * @param code
     * @return
     */
    public boolean isYes(String code) {
        return Code.SUCCESS.equals(code);
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
