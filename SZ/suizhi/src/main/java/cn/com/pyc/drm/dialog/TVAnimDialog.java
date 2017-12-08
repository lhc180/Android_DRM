package cn.com.pyc.drm.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import cn.com.pyc.drm.R;

/**
 * <b>电视机开关效果的Dialog</b></br>
 * 
 * <br>
 * 取自优酷视频客户端退出时电视机关闭动画效果，返向推出打开效果，继承该类就带有该动画效果</br>
 */
public class TVAnimDialog extends Dialog
{

	// content提示对话框取消
	public static final int DIALOG_CANCEL = 0x99;
	// content提示对话框确定
	public static final int DIALOG_CONFIRM = 0x97;

	protected OnTVAnimDialogClickListener l;

	public TVAnimDialog(Context context)
	{
		super(context, R.style.TVAnimDialog);// 此处附上Dialog样式
	}

	public TVAnimDialog(Context context, int theme)
	{
		super(context, theme);
	}

	protected TVAnimDialog(Context context, boolean cancelable, OnCancelListener cancelListener)
	{
		super(context, cancelable, cancelListener);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		//if (getWindow() != null)
		//	getWindow().setWindowAnimations(R.style.TVAnimDialogWindowAnim);
	}

	public void setOnTVAnimDialogClickListener(OnTVAnimDialogClickListener l)
	{
		this.l = l;
	}

	/**
	 * 用于监听点击对话框按钮的接口
	 */
	public interface OnTVAnimDialogClickListener
	{
		void onClick(int dialogId);
	}
}
