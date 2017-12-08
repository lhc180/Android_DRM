package cn.com.pyc.drm.model;

/**
 * Created by songyumei on 2017/10/26.
 * 微信支付
 */

public class PayModel {

    private String payType;
    private String prepayid;
    private String noncestr;
    private String timestamp;
    private String sign;
    private String mybill_id;
    private String bill_num;
    private String pay_money;

    public String getPayType() {
        return payType;
    }

    public void setPayType(String payType) {
        this.payType = payType;
    }

    public String getPrepayid() {
        return prepayid;
    }

    public void setPrepayid(String prepayid) {
        this.prepayid = prepayid;
    }

    public String getNoncestr() {
        return noncestr;
    }

    public void setNoncestr(String noncestr) {
        this.noncestr = noncestr;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getMybill_id() {
        return mybill_id;
    }

    public void setMybill_id(String mybill_id) {
        this.mybill_id = mybill_id;
    }

    public String getBill_num() {
        return bill_num;
    }

    public void setBill_num(String bill_num) {
        this.bill_num = bill_num;
    }

    public String getPay_money() {
        return pay_money;
    }

    public void setPay_money(String pay_money) {
        this.pay_money = pay_money;
    }

    @Override
    public String toString() {
        return "PayModel{" +
                "payType='" + payType + '\'' +
                ", prepayid='" + prepayid + '\'' +
                ", noncestr='" + noncestr + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", sign='" + sign + '\'' +
                ", mybill_id='" + mybill_id + '\'' +
                ", bill_num='" + bill_num + '\'' +
                ", pay_money='" + pay_money + '\'' +
                '}';
    }
}
