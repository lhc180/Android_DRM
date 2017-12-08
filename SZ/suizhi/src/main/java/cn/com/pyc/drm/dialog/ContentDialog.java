package cn.com.pyc.drm.dialog;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import cn.com.pyc.drm.R;
import cn.com.pyc.drm.utils.DeviceUtils;

/**
 * 带电视机开关效果的弹出对话框 <br/>
 * 
 * <br/>
 * 
 * 显示内容可设定
 */
public class ContentDialog extends TVAnimDialog implements
		android.view.View.OnClickListener
{

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

	protected ContentDialog(Context context, boolean cancelable,
			OnCancelListener cancelListener)
	{
		super(context, cancelable, cancelListener);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_content);
		positive = (Button) findViewById(R.id.deldlg_btn_positive);
		negative = (Button) findViewById(R.id.deldlg_btn_negative);
		TextView contentTv = (TextView) findViewById(R.id.deldlg_tv_content);
		TextView titleTv = (TextView) findViewById(R.id.deldlg_tv_title);
		// 设置对话框的宽度
		if (titleTv.getLayoutParams() != null)
		{
			int width = DeviceUtils.getScreenSize(this.getContext()).x;
			titleTv.getLayoutParams().width = (int) (width * 0.73);
		}
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
		case R.id.deldlg_btn_positive:
			if (l != null)
			{
				l.onClick(DIALOG_CONFIRM);
			}
			break;
		case R.id.deldlg_btn_negative:
			if (l != null)
			{
				l.onClick(DIALOG_CANCEL);
			}
			break;
		}
		dismiss();
	}

}
