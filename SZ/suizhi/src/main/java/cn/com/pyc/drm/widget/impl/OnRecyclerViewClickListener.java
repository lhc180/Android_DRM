package cn.com.pyc.drm.widget.impl;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

@Deprecated
public class OnRecyclerViewClickListener implements RecyclerView.OnItemTouchListener {

    private GestureDetector detector;
    private onItemClickListener mListener;

    public interface onItemClickListener {
        void onItemClick(View itemView, int position);
    }

    public OnRecyclerViewClickListener(Context context,
                                       final RecyclerView recyclerView, onItemClickListener
                                               listener) {
        this.mListener = listener;
        detector = new GestureDetector(context,
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onSingleTapUp(MotionEvent e) {
                        View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                        if (child != null && mListener != null) {
                            //mListener.onItemClick(child, recyclerView.getChildPosition(child));
                            mListener.onItemClick(child, recyclerView.getChildAdapterPosition
                                    (child));
                            return true;
                        }
                        return super.onSingleTapUp(e);
                    }
                });
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView arg0, MotionEvent arg1) {
        if (detector.onTouchEvent(arg1)) {
            return true;
        }
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView arg0, MotionEvent arg1) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
    }
}
