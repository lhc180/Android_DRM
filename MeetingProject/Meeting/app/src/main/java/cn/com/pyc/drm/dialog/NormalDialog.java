package cn.com.pyc.drm.dialog;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.com.meeting.drm.R;
import cn.com.pyc.drm.ui.MuPDFSaveNotes;

/**
 * @Description: (共用对话框)
 * @author 李巷阳
 * @date 2016-6-16 下午3:52:19
 * @version V1.0
 */
public class NormalDialog {

	public AlertDialog dialog(Context content, String title , String prompt_message,final NormalDialogCallBack ndcb) {
		AlertDialog.Builder builder = new Builder(content);
		View view = View.inflate(content, R.layout.dialog_isdelete, null);
		LinearLayout confirm = (LinearLayout) view.findViewById(R.id.confirm);// 确定
		LinearLayout cancel = (LinearLayout) view.findViewById(R.id.cancel);// 取消
		TextView tView = (TextView) view.findViewById(R.id.Cancellation);
		TextView p_message = (TextView) view.findViewById(R.id.Prompt_message);
		final CheckBox check_isDeleteFile = (CheckBox) view.findViewById(R.id.check_isDeleteFile);
		tView.setText(title);
		if(prompt_message==null)
		{
			p_message.setVisibility(View.GONE);
		}else{
			p_message.setVisibility(View.VISIBLE);
			p_message.setText(prompt_message);
		}
		builder.setView(view);
		final AlertDialog clearDlg = builder.create();
		clearDlg.show();
		confirm.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (clearDlg != null && clearDlg.isShowing()) {
					clearDlg.dismiss();
				}
				ndcb.getConfirm(v);
			}
		});
		cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (clearDlg != null)
					clearDlg.dismiss();
				ndcb.getCancel(v);
			}
		});
		return clearDlg;
	}

	public interface NormalDialogCallBack {

		public void getConfirm(View v);

		public void getCancel(View v);
	}

}
