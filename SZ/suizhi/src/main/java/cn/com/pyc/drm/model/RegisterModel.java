package cn.com.pyc.drm.model;

/**
 * Created by songyumei on 2017/7/18.
 */

public class RegisterModel {
    private Boolean result;
    private String msg;
    private int code;

    public Boolean getResult() {
        return result;
    }

    public void setResult(Boolean result) {
        this.result = result;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
