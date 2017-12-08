package cn.com.pyc.drm.widget;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class RecyclerViewClickListener implements
		RecyclerView.OnItemTouchListener
{

	private GestureDetector detector;
	private onItemClickListener mListener;

	public interface onItemClickListener
	{
		void onItemClick(View itemView, int position);
	}

	public RecyclerViewClickListener(Context context,
			final RecyclerView recyclerView, onItemClickListener listener)
	{
		this.mListener = listener;
		detector = new GestureDetector(context,
				new GestureDetector.SimpleOnGestureListener()
				{
					@Override
					public boolean onSingleTapUp(MotionEvent e)
					{
						View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
						if (child != null && mListener != null)
						{
							mListener.onItemClick(child,
									recyclerView.getChildPosition(child));
							return true;
						}
						return super.onSingleTapUp(e);
					}
				});
	}

	@Override
	public boolean onInterceptTouchEvent(RecyclerView arg0, MotionEvent arg1)
	{
		if (detector.onTouchEvent(arg1)) return true;
		return false;
	}

	@Override
	public void onTouchEvent(RecyclerView arg0, MotionEvent arg1)
	{

	}

	/**   
	* @author 李巷阳
	* @date 2016-10-25 下午5:38:26 
	*/
	@Override
	public void onRequestDisallowInterceptTouchEvent(boolean arg0) {
		
	}

}
