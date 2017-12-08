package cn.com.pyc.drm.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

public class UnderLineTextView extends TextView
{

	private Paint paint = new Paint();
	private int lineHeight = 0;

	public UnderLineTextView(Context context)
	{
		super(context);
		init();
	}

	public UnderLineTextView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init();
	}

	public UnderLineTextView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		init();
	}

	private void init()
	{
		Resources resources = getResources();
		lineHeight = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 1f, resources.getDisplayMetrics());

		paint.setColor(0xffC8C8C8);
		paint.setAntiAlias(true);
		paint.setStyle(Style.FILL_AND_STROKE);
		paint.setStrokeWidth(0f);
		paint.setStrokeCap(Cap.ROUND); // 设置画笔线帽的样式 取值有三种 Cap.ROUND
										// 圆形线帽，Cap.SQUARE 方形线帽，Cap.BUTT 无线帽
		paint.setStrokeJoin(Join.ROUND); // 设置线段连接处​​​的连接模式，取值有：Join.MITER（结合处为锐角）、Join.Round(结合处为圆弧)、Join.BEVEL(结合处为直线)
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);

		canvas.drawRect(0, getHeight()- lineHeight, getWidth(), getHeight() + lineHeight,
				paint);
	}

	@Override
	public void setPadding(int left, int top, int right, int bottom)
	{
		super.setPadding(left, top, right, bottom + lineHeight);
	}

}
