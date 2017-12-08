package cn.com.pyc.drm.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.widget.TextView;

import cn.com.pyc.drm.R;

/**
 * 闪闪发光的显示字体的textview
 * 
 * @author hudq
 * 
 */
public class SplashTextView extends TextView
{

	private LinearGradient mLinearGradient;
	private Matrix mGradientMatrix;
	private Paint mPaint;
	private int mViewWidth = 0;
	private int mTranslate = 0;

	private int[] mColors;

	private boolean mAnimating = true;

	public void setAnimating(boolean mAnimating)
	{
		this.mAnimating = mAnimating;
		setTextColor(getResources().getColor(R.color.black_aa));
	}

	public SplashTextView(Context context)
	{
		this(context, null, 0);
	}

	public SplashTextView(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
	}

	public SplashTextView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		final TypedArray ta = context.obtainStyledAttributes(attrs,
				R.styleable.SplashTextView, defStyle, 0);
		int colorType = ta.getInteger(R.styleable.SplashTextView_color_type, 0);
		switch (colorType)
		{
			case 1:
				// 自定义颜色
				mColors = new int[] { 0x665190D7, 0xFF5190D7, 0x665190D7 };
				break;

			default:
				// 默认白色闪烁
				mColors = new int[] { 0x44FFFFFF, 0xFFFFFFFF, 0x44FFFFFF };
				break;
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		super.onSizeChanged(w, h, oldw, oldh);
		if (mViewWidth == 0)
		{
			mViewWidth = getMeasuredWidth();
			if (mViewWidth > 0)
			{
				mPaint = getPaint();
				mLinearGradient = new LinearGradient(-mViewWidth, 0, 0, 0,
						mColors, new float[] { 0.0f, 0.5f, 1.0f },
						Shader.TileMode.CLAMP);
				mPaint.setShader(mLinearGradient);
				mGradientMatrix = new Matrix();
			}
		}
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		if (mAnimating && mGradientMatrix != null)
		{
			mTranslate += mViewWidth / 10;
			if (mTranslate > 2 * mViewWidth)
			{
				mTranslate = -mViewWidth;
			}
			mGradientMatrix.setTranslate(mTranslate, 0);
			mLinearGradient.setLocalMatrix(mGradientMatrix);
			postInvalidateDelayed(50);
		}
	}

}
