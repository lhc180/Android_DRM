package cn.com.pyc.drm.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.Button;
import cn.com.pyc.drm.widget.HighlightDrawable;

/**
 * Applies a pressed state color filter or disabled state alpha for the button's
 * background drawable.
 * 
 * @author shiki
 */
public class HightLightButton extends Button {

	public HightLightButton(Context context) {
		super(context);
	}

	public HightLightButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public HightLightButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public void setBackgroundDrawable(Drawable d) {
		// Replace the original background drawable (e.g. image) with a
		// LayerDrawable that
		// contains the original drawable.

		if (d == null) {
			super.setBackgroundDrawable(null);
		} else {
			HighlightDrawable layer = new HighlightDrawable(d);
			super.setBackgroundDrawable(layer);
		}
	}
}
