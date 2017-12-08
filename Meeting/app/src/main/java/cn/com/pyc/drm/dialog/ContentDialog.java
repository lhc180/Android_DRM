package cn.com.pyc.drm.dialog;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import cn.com.meeting.drm.R;

/**
 * 带电视机开关效果的弹出对话框 <br/>
 * 
 * <br/>
 * 
 * 显示内容可设定
 */
public class ContentDialog extends TVAnimDialog implements android.view.View.OnClickListener
{

	private String TAG = ContentDialog.class.getSimpleName();
	private Button positive;
	private Button negative;

	private String content;

	public ContentDialog(Context context)
	{
		super(context);
	}

	public ContentDialog(Context context, String content)
	{
		super(context);
		this.content = content;
	}

	public ContentDialog(Context context, int theme)
	{
		super(context, theme);
	}

	protected ContentDialog(Context context, boolean cancelable, OnCancelListener cancelListener)
	{
		super(context, cancelable, cancelListener);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_content);
		positive = (Button) findViewById(R.id.dialog_delete_btn_positive);
		negative = (Button) findViewById(R.id.dialog_delete_btn_negative);
		TextView contentTv = (TextView) findViewById(R.id.tv_deldlg_content);
		//TextView titleTv = (TextView) findViewById(R.id.tv_deldlg_title);
		if (!TextUtils.isEmpty(content))
		{
			contentTv.setText(content);
		}
		positive.setOnClickListener(this);
		negative.setOnClickListener(this);
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
		case R.id.dialog_delete_btn_positive:
			// setDialogId(DRMUtil.DIALOG_CONFIRM);
			if (l != null)
			{
				l.onClick(DIALOG_CONFIRM);
			}
			break;

		case R.id.dialog_delete_btn_negative:
			// setDialogId(DRMUtil.DIALOG_CANCEL);
			if (l != null)
			{
				l.onClick(DIALOG_CANCEL);
			}
			break;
		}
		dismiss();
	}

}
