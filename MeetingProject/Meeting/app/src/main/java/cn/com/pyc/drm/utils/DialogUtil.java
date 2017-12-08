package cn.com.pyc.drm.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.com.meeting.drm.R;
import cn.com.pyc.drm.adapter.JGAdapter;
import cn.com.pyc.drm.ui.MyMeetingActivityPro;

/**
 * @author 李巷阳
 * @version V1.0
 * @Description: (机构对话框)
 * @date 2017/2/27 17:16
 */
public class DialogUtil {


    private static Dialog dialog_lv;
    /**
     * 切换机构的dialog
     * 显示对话框列表
     *
     * **/
    public static void showDialog_lv(final MyMeetingActivityPro mContext, JGAdapter baseAdapter) {
        dialog_lv =  new Dialog(mContext, R.style.SZ_LoadBgDialog);
        View view = View.inflate(mContext, R.layout.dialog_server_lv, null);
        TextView mTv_prompt= (TextView) view.findViewById(R.id.tv_prompt);
        RecyclerView mLv_control = (RecyclerView) view.findViewById(R.id.lv_control);
        RelativeLayout rl_add_mechanism = (RelativeLayout) view.findViewById(R.id.rl_add_mechanism);
        rl_add_mechanism.setVisibility(View.VISIBLE);
        rl_add_mechanism.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.OnClick_Dialog_Listener(v);
            }
        });
        LinearLayoutManager LinearManager = new LinearLayoutManager(mContext);
        LinearManager.setOrientation(LinearLayoutManager.VERTICAL);
        mLv_control.setLayoutManager(LinearManager);
        mLv_control.setAdapter(baseAdapter);

        dialog_lv.setContentView(view);
        dialog_lv.setCancelable(false);
        dialog_lv.setCanceledOnTouchOutside(true);
        dialog_lv.show();
    }

  /*  *//**
     * 切换机构的dialog
     * 显示对话框列表
     *
     * **//*
    public static void showDialog_lv2(final MyMeetingActivityPro mContext, JGAdapter baseAdapter) {

        mLv_control.setAdapter(baseAdapter);
    }*/
    /**
     * 切换机构的dialog
     * 显示对话框列表
     *
     * **/
    public static void showDialog_lverrir(final MyMeetingActivityPro mContext) {

        dialog_lv =  new Dialog(mContext, R.style.SZ_LoadBgDialog);
        View view = View.inflate(mContext, R.layout.dialog_server_lv, null);
        TextView mTv_prompt= (TextView) view.findViewById(R.id.tv_prompt);
        RecyclerView mLv_control = (RecyclerView) view.findViewById(R.id.lv_control);
        RelativeLayout rl_add_mechanism = (RelativeLayout) view.findViewById(R.id.rl_add_mechanism);
        rl_add_mechanism.setVisibility(View.VISIBLE);
        rl_add_mechanism.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.OnClick_Dialog_Listener(v);
            }
        });
        LinearLayoutManager LinearManager = new LinearLayoutManager(mContext);
        LinearManager.setOrientation(LinearLayoutManager.VERTICAL);
        mLv_control.setLayoutManager(LinearManager);

        dialog_lv.setContentView(view);
        dialog_lv.setCancelable(false);
        dialog_lv.setCanceledOnTouchOutside(true);
        dialog_lv.show();
    }
    /** 关闭对话框 **/
    public static void hideDialog_lv(){
        if (dialog_lv != null) {
            dialog_lv.dismiss();
        }
    }


    /** 显示对话框列表提示 **/
    public static void showDialog_Prompt(Activity mactivity, String str_confirm, String str_cancel, String content_prompt, final onDialogPromptListener listener){
        AlertDialog.Builder builder = new AlertDialog.Builder(mactivity,AlertDialog.THEME_HOLO_LIGHT);
        View view = View.inflate(mactivity, R.layout.dialog_user, null);

        TextView tv_info=(TextView) view.findViewById(R.id.Cancellation);//提示信息
        LinearLayout confirm = (LinearLayout) view.findViewById(R.id.confirm);// 确定
        LinearLayout cancel = (LinearLayout) view.findViewById(R.id.cancel);// 取消
        TextView tv_confirm = (TextView) view.findViewById(R.id.exitBtn0);// 确定textview
        TextView tv_cancel = (TextView) view.findViewById(R.id.go_shopping_btn);// 确定textview
        tv_info.setText(content_prompt);
        tv_confirm.setText(str_confirm);
        tv_cancel.setText(str_cancel);
        builder.setView(view);
        final AlertDialog dialog = builder.create();
        dialog.show();
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.Onclick_Dialog_Confirm_Listener(v);

//                Bundle bundle=new Bundle();
//                bundle.putString("phone_number", getPhoneNo());
//                openActivity(LoginMenuActivity.class,bundle);
//                finish();
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.Onclick_Dialog_Cancel_Listener(v);
                if (dialog != null)
                    dialog.dismiss();
            }
        });
    }



    public interface OnDialogMechanismListener {
        void OnClick_Dialog_Listener(View v);
    }


    public interface onDialogPromptListener{

        void Onclick_Dialog_Confirm_Listener(View v);

        void Onclick_Dialog_Cancel_Listener(View v);

    }

}
