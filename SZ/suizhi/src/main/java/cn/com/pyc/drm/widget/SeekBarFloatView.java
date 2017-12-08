package cn.com.pyc.drm.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import cn.com.pyc.drm.R;
import cn.com.pyc.drm.utils.ViewUtil;

/**
 * SeekBar带浮动显示框的View
 * <p>
 * Created by hudq on 2017/3/22.
 */

public class SeekBarFloatView extends RelativeLayout {

    private SeekBar mSeekBar;
    private TextView mTextView;

    private OnSeekBarFloatViewChangeListener mListener;

    public SeekBarFloatView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SeekBarFloatView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initView(context);

        // 设置内容
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SeekBarFloatView);
        float textSize = ta.getFloat(R.styleable.SeekBarFloatView_sbf_textSize, 0);
        int textColor = ta.getColor(R.styleable.SeekBarFloatView_sbf_textColor, Color.WHITE);
        int bgResId = ta.getResourceId(R.styleable.SeekBarFloatView_sbf_textBackground, R
                .drawable.ic_share_pos);

        mTextView.setTextSize(textSize);
        mTextView.setTextColor(textColor);
        mTextView.setBackgroundDrawable(getResources().getDrawable(bgResId));
        mTextView.setVisibility(View.INVISIBLE);


        ta.recycle(); // 防止多个自定义控件使用相同的参数
    }


    private void initView(Context context) {
        setGravity(Gravity.CENTER_HORIZONTAL);
        View view = View.inflate(context, R.layout.float_seekbar_layout, this); // this:挂载的布局
        mSeekBar = (SeekBar) view.findViewById(R.id.float_pageSlider);
        mTextView = (TextView) view.findViewById(R.id.float_textSlider);
    }

    public void setMax(int max) {
        if (mSeekBar != null) {
            mSeekBar.setMax(max);
        }
    }

    public void setProgress(int progress) {
        if (mSeekBar != null) {
            mSeekBar.setProgress(progress);
        }
    }

    public int getMax() {
        return mSeekBar != null ? mSeekBar.getMax() : 0;
    }

    public int getProgress() {
        return mSeekBar != null ? mSeekBar.getProgress() : 0;
    }

    /**
     * 设置FloatView显示Text
     *
     * @param text
     */
    public void setFloatViewText(String text) {
        if (mTextView != null) {
            mTextView.setText(text);
        }
    }

    /**
     * 设置FloatView点击事件监听
     *
     * @param listener
     */
    public void setOnClickFloatTextListener(OnClickListener listener) {
        if (listener == null) return;
        if (mTextView != null) {
            mTextView.setOnClickListener(listener);
        }
    }

    public void setOnSeekBarFloatViewChangeListener(OnSeekBarFloatViewChangeListener listener) {
        if (listener == null) return;
        if (mSeekBar != null) {
            mListener = listener;
            mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (mTextView != null) {
                        Rect bgRect = seekBar.getProgressDrawable().getBounds();
                        float width = (progress * (bgRect.width() - seekBar.getPaddingLeft() -
                                seekBar.getPaddingRight())) / seekBar.getMax() +
                                seekBar.getX() - mTextView.getWidth() / 2;
                        mTextView.setX(width);
                    }

                    mListener.onProgressChanged(seekBar, progress, fromUser);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                    removeCallbacks(cancelBack);
                    ViewUtil.showWidget(mTextView);

                    mListener.onStartTrackingTouch(seekBar);
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                    postDelayed(cancelBack, 1500);

                    mListener.onStopTrackingTouch(seekBar);
                }
            });
        }
    }

    private Runnable cancelBack = new Runnable() {
        @Override
        public void run() {
            if (mTextView != null) {
                mTextView.startAnimation(AnimationUtils
                        .loadAnimation(getContext(), android.R.anim.fade_out));
                ViewUtil.inVisibleWidget(mTextView);
            }
        }
    };


    public interface OnSeekBarFloatViewChangeListener {
        void onStopTrackingTouch(SeekBar seekBar);

        void onStartTrackingTouch(SeekBar seekBar);

        void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser);
    }
}
