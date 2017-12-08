package cn.com.pyc.drm.utils;

import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

public class AnimationUtil
{
	
	public enum Location
	{
		Left, Top, Right, Bottom;
	}

	private static final int DURATION = 200;

	public static Animation translate(View animView, boolean autoStart,
			boolean show, Location location)
	{
		// 默认不用translate 移动都是相对于左上角的那个点
		float fromX = 0.0f;
		float fromY = 0.0f;
		float toX = 0.0f;
		float toY = 0.0f;
		// 左侧
		switch (location)
		{
		case Left:
			fromX = show ? -1.0f : 0.0f;
			toX = show ? 0.0f : -1.0f;
			break;

		case Top:
			fromY = show ? -1.0f : 0.0f;
			toY = show ? 0.0f : -1.0f;
			break;

		case Right:
			fromX = show ? 1.0f : 0.0f;
			toX = show ? 0.0f : 1.0f;
			break;

		case Bottom:
			fromY = show ? 1.0f : 0.0f;
			toY = show ? 0.0f : 1.0f;
			break;

		default:
			break;
		}

		final int typeSelf = Animation.RELATIVE_TO_SELF;
		TranslateAnimation ta = new TranslateAnimation(typeSelf, fromX,
				typeSelf, toX, typeSelf, fromY, typeSelf, toY);
		ta.setInterpolator(new AccelerateInterpolator()); // 加速
		ta.setDuration(DURATION);
		ta.setFillAfter(true);

		// 开始动画
		if (animView != null)
		{
			ta.setAnimationListener(new GoneViewListener(animView, show));
			if (show)
			{
				animView.setVisibility(View.VISIBLE); // visible的情况下动画才能执行

			}
			if (autoStart)
			{
				animView.startAnimation(ta);
			}
		}
		return ta;
	}

	public static Animation alpha(View animView, boolean autoStart, boolean show)
	{
		AlphaAnimation aa = null;
		if (show)
		{
			aa = new AlphaAnimation(0.0f, 1.0f);
		} else
		{
			aa = new AlphaAnimation(1.0f, 0.0f);
		}

		aa.setDuration(DURATION);
		aa.setFillAfter(true);
		// 开始动画
		if (animView != null)
		{
			aa.setAnimationListener(new GoneViewListener(animView, show));
			if (show)
			{
				animView.setVisibility(View.VISIBLE); // visible的情况下动画才能执行

			}
			if (autoStart)
			{
				animView.startAnimation(aa);
			}
		}
		return aa;
	}

	public static Animation scale(View animView, boolean autoStart,
			boolean show, Location location, float pivot)
	{
		float fromX = 0.0f;
		float fromY = 0.0f;
		float toX = 0.0f;
		float toY = 0.0f;
		float pivotX = 0.0f;
		float pivotY = 0.0f;

		// 最终状态
		if (show)
		{
			toX = 1.0f;
			toY = 1.0f;
		} else
		{
			fromX = 1.0f;
			fromY = 1.0f;
		}

		switch (location)
		{
		case Left:
			pivotX = 0.0f;
			pivotY = pivot;
			break;

		case Top:
			pivotX = pivot;
			pivotY = 0.0f;
			break;

		case Right:
			pivotX = 1.0f;
			pivotY = pivot;
			break;

		case Bottom:
			pivotX = pivot;
			pivotY = 1.0f;
			break;
		default:
			break;
		}

		final int typeSelf = Animation.RELATIVE_TO_SELF;
		ScaleAnimation sa = new ScaleAnimation(fromX, toX, fromY, toY,
				typeSelf, pivotX, typeSelf, pivotY);
		sa.setDuration(DURATION);
		sa.setFillAfter(true);
		sa.setInterpolator(new AccelerateInterpolator());
		// 开始动画
		if (animView != null)
		{
			sa.setAnimationListener(new GoneViewListener(animView, show));
			if (show)
			{
				animView.setVisibility(View.VISIBLE); // visible的情况下动画才能执行

			}
			if (autoStart)
			{
				animView.startAnimation(sa);
			}
		}
		return sa;
	}

	public static Animation rotate(View animView, boolean autoStart,
			boolean show, int edge)
	{
		// rotate不适合这个类
		return null;
	}

	public static void group(View animView, boolean show, Animation... anims)
	{
		if (animView == null || anims == null || anims.length == 0) { return; }

		AnimationSet as = new AnimationSet(false);
		for (Animation a : anims)
		{
			as.addAnimation(a);
		}
		as.setDuration(DURATION);
		if (!show)
		{
			as.setAnimationListener(new GoneViewListener(animView, show));
		}
		animView.startAnimation(as);
	}

	/**
	 * 注意：只有在View是visible的情况下，startAnimation才会执行动画，
	 * 所以这里没有所谓的VisibleViewListener，只能Gone
	 */
	private static class GoneViewListener implements
			android.view.animation.Animation.AnimationListener
	{
		private final View goneView;
		private final boolean show;

		public GoneViewListener(View goneView, boolean show)
		{
			this.goneView = goneView;
			this.show = show;
		}

		@Override
		public void onAnimationStart(Animation animation)
		{
		}

		@Override
		public void onAnimationEnd(Animation animation)
		{

			if (!show)
			{
				goneView.setVisibility(View.GONE);

			}
			goneView.clearAnimation(); // 注意，clear之后再gone就不会有焦点了

		}

		@Override
		public void onAnimationRepeat(Animation animation)
		{
		}
	}
}
