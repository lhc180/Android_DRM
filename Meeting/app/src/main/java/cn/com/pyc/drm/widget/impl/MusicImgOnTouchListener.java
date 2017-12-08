package cn.com.pyc.drm.widget.impl;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import cn.com.meeting.drm.R;
import cn.com.pyc.drm.common.Constant;
import cn.com.pyc.drm.ui.MusicHomeActivity;
import cn.com.pyc.drm.utils.DRMUtil;

/**
 * music icon的move touch
 * 
 */
public class MusicImgOnTouchListener implements OnTouchListener
{

	private Activity mContext;
	private int lastX, lastY;
	private long currentTimeMillis;
	private long oldTimeMillis;
	// 状态栏的高度
	private int mStatusBarHeight = 0;

	public int getStatusBarHeight()
	{
		return mStatusBarHeight;
	}

	public MusicImgOnTouchListener(Activity context)
	{
		this.mContext = context;
	}

	// 带状态栏高度的构造方法
	public MusicImgOnTouchListener(Activity context, int mStatusBarHeight)
	{
		this.mContext = context;
		this.mStatusBarHeight = mStatusBarHeight;
	}

	@SuppressLint("ClickableViewAccessibility") @Override
	public boolean onTouch(View v, MotionEvent event)
	{
		switch (event.getAction())
		{
		case MotionEvent.ACTION_DOWN:
			// 获取圆心到x,y的距离
			lastX = (int) event.getRawX();
			lastY = (int) event.getRawY();
			oldTimeMillis = System.currentTimeMillis();
			break;
		case MotionEvent.ACTION_MOVE:
			// 获取在移动过程中，x 和 y的变化值。
			int dx = (int) event.getRawX() - lastX;
			int dy = (int) event.getRawY() - lastY;
			int left = v.getLeft() + dx;
			int top = v.getTop() + dy;
			int right = v.getRight() + dx;
			int bottom = v.getBottom() + dy;
			// 设置不能出界
			if (left < 0)
			{
				left = 0;
				right = left + v.getWidth();
			}

			if (right > Constant.screenWidth)
			{
				right = Constant.screenWidth;
				left = right - v.getWidth();
			}
			if (top < 0)
			{
				top = 0;
				bottom = top + v.getHeight();
			}
			if (bottom > Constant.screenHeight - mStatusBarHeight)
			{
				bottom = Constant.screenHeight - mStatusBarHeight;
				top = bottom - v.getHeight();
			}
			// 保存left和top值到内存
			//DRMUtil.Music_Image_left = left;
			//DRMUtil.Music_Image_top = top;
			v.layout(left, top, right, bottom);
			lastX = (int) event.getRawX();
			lastY = (int) event.getRawY();
			break;
		case MotionEvent.ACTION_UP:
			currentTimeMillis = System.currentTimeMillis();
			if ((currentTimeMillis - oldTimeMillis) < 101)
			{
				if (DRMUtil.Music_Album_Playing != null)
				{
					Bundle bundle = new Bundle();
					bundle.putSerializable("Album", DRMUtil.Music_Album_Playing);
					Intent intent = new Intent(mContext,
							MusicHomeActivity.class);
					intent.putExtras(bundle);
					mContext.startActivity(intent);
					mContext.overridePendingTransition(
							R.anim.activity_music_open, R.anim.fade_out_scale);
				}
			}
			break;
		}
		return true;
	}

}
