package cn.com.pyc.drm.model;

/**
 * Created by songyumei on 2017/10/27.
 */

public class AliPayModel {
    private String payType;
    private String orderString;
    private String mybill_id;
    private String bill_num;
    private String pay_money;

    public String getPayType() {
        return payType;
    }

    public void setPayType(String payType) {
        this.payType = payType;
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

    public String getOrderString() {
        return orderString;
    }

    public void setOrderString(String orderString) {
        this.orderString = orderString;
    }

    @Override
    public String toString() {
        return "AliPayModel{" +
                "payType='" + payType + '\'' +
                ", orderString='" + orderString + '\'' +
                ", mybill_id='" + mybill_id + '\'' +
                ", bill_num='" + bill_num + '\'' +
                ", pay_money='" + pay_money + '\'' +
                '}';
    }
}
