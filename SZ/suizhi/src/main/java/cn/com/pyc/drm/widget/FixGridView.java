package cn.com.pyc.drm.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

public class FixGridView extends GridView {

	public FixGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public FixGridView(Context context) {
		super(context);
	}

	public FixGridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
				MeasureSpec.AT_MOST);
		super.onMeasure(widthMeasureSpec, expandSpec);
	}
}
