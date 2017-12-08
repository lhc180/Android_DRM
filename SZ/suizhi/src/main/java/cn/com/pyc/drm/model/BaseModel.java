package cn.com.pyc.drm.model;

import cn.com.pyc.drm.common.Code;

/**
 * 基础类型model
 *
 * @author hudq
 */
public class BaseModel {
    private boolean result;
    private String msg;
    private String code;

    /**
     * 成功，返回Code = "1000" ;
     *
     * @param code
     * @return
     */
    public boolean isYes(String code) {
        return Code.SUCCESS.equals(code);
    }

    /**
     * 成功，isResult = true;
     *
     * @return
     */
    public boolean isSuccess() {
        return this.result;
    }

    public String getMsg() {
        return this.msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    @Override
    public String toString() {
        return "BaseModel{" +
                "result=" + result +
                ", msg='" + msg + '\'' +
                ", code='" + code + '\'' +
                '}';
    }
}
