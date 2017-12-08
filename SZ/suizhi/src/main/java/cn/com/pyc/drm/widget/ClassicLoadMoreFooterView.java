package cn.com.pyc.drm.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.aspsine.swipetoloadlayout.SwipeLoadMoreTrigger;
import com.aspsine.swipetoloadlayout.SwipeTrigger;
import com.wang.avi.AVLoadingIndicatorView;

import cn.com.pyc.drm.R;
import cn.com.pyc.drm.utils.DRMLog;
import cn.com.pyc.drm.utils.ViewUtil;

public class ClassicLoadMoreFooterView extends FrameLayout implements
		SwipeLoadMoreTrigger, SwipeTrigger
{

	private TextView tvLoadMore;
	private AVLoadingIndicatorView progressBar;

	private int mFooterHeight;
	private String pullLoadText;
	private String releaseLoadText;
	private String loadingText;

	public ClassicLoadMoreFooterView(Context context)
	{
		this(context, null, 0);
	}

	public ClassicLoadMoreFooterView(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
	}

	public ClassicLoadMoreFooterView(Context context, AttributeSet attrs,
			int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
		mFooterHeight = getResources().getDimensionPixelOffset(
				R.dimen.swipe_footer_height);
		DRMLog.v("mFooterHeight = " + mFooterHeight);
		pullLoadText = getContext().getString(
				R.string.xlistview_footer_hint_normal);
		releaseLoadText = getContext().getString(
				R.string.xlistview_footer_hint_ready);
		loadingText = getContext().getString(
				R.string.xlistview_footer_hint_loadmore);
	}

	@Override
	protected void onFinishInflate()
	{
		super.onFinishInflate();
		tvLoadMore = (TextView) findViewById(R.id.classic_footer_hint_textview);
		progressBar = (AVLoadingIndicatorView) findViewById(R.id.classic_footer_progressbar);
	}

	@Override
	public void onLoadMore()
	{
		tvLoadMore.setText(loadingText);
		ViewUtil.showWidget(progressBar);
	}

	@Override
	public void onPrepare()
	{
		tvLoadMore.setText(pullLoadText);
	}

	@Override
	public void onMove(int y, boolean isComplete, boolean automatic)
	{
		if (!isComplete)
		{
			ViewUtil.hideWidget(progressBar);
			if (Math.abs(y) >= mFooterHeight)
			{
				tvLoadMore.setText(releaseLoadText);
			} else
			{
				tvLoadMore.setText(pullLoadText);
			}
		}
	}

	@Override
	public void onRelease()
	{
	}

	@Override
	public void onComplete()
	{
		ViewUtil.hideWidget(progressBar);
	}

	@Override
	public void onReset()
	{
		tvLoadMore.setText(pullLoadText);
		ViewUtil.hideWidget(progressBar);
	}
}
