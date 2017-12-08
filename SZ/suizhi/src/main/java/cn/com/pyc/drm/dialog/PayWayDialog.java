package cn.com.pyc.drm.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import cn.com.pyc.drm.R;
import cn.com.pyc.drm.utils.DeviceUtils;

/**
 * 选择支付方式对话框 <br/>
 * <p>
 * <br/>
 * <p>
 * 显示内容可设定
 */
public class PayWayDialog extends Dialog {

    private TextView weChat;
    private TextView aliPay;
    private OnPayWayDialogClick callBack;

    public PayWayDialog(Context context, OnPayWayDialogClick callBack) {
        super(context, R.style.LoadBgDialog);
        this.callBack = callBack;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_payway);
        weChat = (TextView) findViewById(R.id.payway_wechat);
        aliPay = (TextView) findViewById(R.id.payway_alipay);
        TextView title = (TextView) findViewById(R.id.payway_title);
        // 设置对话框的宽度
        if (title.getLayoutParams() != null) {
            int width = DeviceUtils.getScreenSize(this.getContext()).x;
            title.getLayoutParams().width = (int) (width * 0.73);
        }
        setCanceledOnTouchOutside(false);

        weChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callBack != null) {
                    callBack.onWeChat();
                }
                dismiss();
            }
        });
        aliPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callBack != null) {
                    callBack.onAliPay();
                }
                dismiss();
            }
        });
    }

    public interface OnPayWayDialogClick {
        void onWeChat();

        void onAliPay();
    }


}
