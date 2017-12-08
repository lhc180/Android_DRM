package cn.com.pyc.szpbb.sdk.response;

/**
 * 基础响应类
 *
 * @author hudq
 */
public class SZResponse {

    private String code;
    private String msg;
    private boolean result;


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

    /**
     * true成功！！！
     *
     * @return boolean
     */
    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

}
