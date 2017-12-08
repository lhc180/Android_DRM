package tv.danmaku.ijk.media.widget;

import java.util.Locale;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

public class Util
{

	public static String toTime(long millseconds)
	{
		if (millseconds <= 0)
		{
			return "0秒";
		}
		millseconds /= 1000;
		int min = (int) (millseconds / 60);
		int sec = (int) (millseconds % 60);
		String str = "";
		if (min > 0)
		{
			str += min + "分";
		}
		if (sec > 0)
		{
			str += sec + "秒";
		} else
		{
			str += "钟";
		}
		return str;
	}

	// 将进度显示规范化
	public static String formateTime(long position)
	{
		// 毫秒--->秒。
		int totalSeconds = (int) (position / 1000);

		int seconds = totalSeconds % 60;
		int minutes = (totalSeconds / 60) % 60;
		int hours = totalSeconds / 3600;

		if (hours > 0)
		{
			return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds).toString();
		} else
		{
			return String.format(Locale.US, "%02d:%02d", minutes, seconds).toString();
		}
	}

	/**
	 * 动画
	 * 
	 * @author qd
	 * 
	 */
	public static final class AnimationUtil
	{
		public enum Location
		{
			Left, Top, Right, Bottom;
		}

		private static final int DURATION = 200;

		public static Animation translate(View animView, boolean autoStart, boolean show, Location location)
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
			TranslateAnimation ta = new TranslateAnimation(typeSelf, fromX, typeSelf, toX, typeSelf, fromY, typeSelf, toY);
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

		public static Animation scale(View animView, boolean autoStart, boolean show, Location location, float pivot)
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
			ScaleAnimation sa = new ScaleAnimation(fromX, toX, fromY, toY, typeSelf, pivotX, typeSelf, pivotY);
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

		public static Animation rotate(View animView, boolean autoStart, boolean show, int edge)
		{
			// rotate不适合这个类
			return null;
		}

		public static void group(View animView, boolean show, Animation... anims)
		{
			if (animView == null || anims == null || anims.length == 0)
			{
				return;
			}

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
		private static class GoneViewListener implements android.view.animation.Animation.AnimationListener
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

	/**
	 * view 显示
	 * 
	 */
	public static final class ViewUtil
	{
		public static boolean isShown(View v)
		{
			return v.getVisibility() == View.VISIBLE;
		}

		public static void visible(View v)
		{
			v.setVisibility(View.VISIBLE);
		}

		public static void gone(View v)
		{
			v.setVisibility(View.GONE);
		}

		public static void invisible(View v)
		{
			v.setVisibility(View.INVISIBLE);
		}
	}

	/**
	 * 
	 * 获取屏幕宽高
	 * 
	 */
	public static final class ScreenUtil
	{
		public static DisplayMetrics getScreenRect(Context context)
		{
			DisplayMetrics dm = new DisplayMetrics();
			((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(dm);
			return dm;
		}

		public static int getScreenWidth(Context context)
		{
			return getScreenRect(context).widthPixels;
		}

		public static int getScreenHeight(Context context)
		{
			return getScreenRect(context).heightPixels;
		}
	}

}
