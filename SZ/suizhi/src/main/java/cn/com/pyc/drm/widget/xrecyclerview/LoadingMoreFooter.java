package cn.com.pyc.drm.widget.xrecyclerview;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.wang.avi.AVLoadingIndicatorView;

import cn.com.pyc.drm.R;

public class LoadingMoreFooter extends LinearLayout {

    private SimpleViewSwitcher progressCon;
    public final static int STATE_LOADING = 0;
    public final static int STATE_COMPLETE = 1;
    public final static int STATE_NOMORE = 2;
    private TextView mText;
    private String loadingHint;
    private String noMoreHint;
    private String loadingDoneHint;

    public LoadingMoreFooter(Context context) {
        super(context);
        initView();
    }

    /**
     * @param context
     * @param attrs
     */
    public LoadingMoreFooter(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public void setLoadingHint(String hint) {
        loadingHint = hint;
    }

    public void setNoMoreHint(String hint) {
        noMoreHint = hint;
    }

    public void setLoadingDoneHint(String hint) {
        loadingDoneHint = hint;
    }

    public void initView() {
        setGravity(Gravity.CENTER);
        setLayoutParams(new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        progressCon = new SimpleViewSwitcher(getContext());
        progressCon.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        //AVLoadingIndicatorView progressView = new AVLoadingIndicatorView(this.getContext());
        AVLoadingIndicatorView progressView = getAvLoadingView(this.getContext());
        progressView.setIndicatorColor(getContext().getResources().getColor(R.color
                .title_bg_color));
        //progressView.setIndicatorId(ProgressStyle.BallSpinFadeLoader);
        progressView.setIndicator(ProgressStyle.applyIndicator(ProgressStyle.BallRotate));
        progressCon.setView(progressView);

        addView(progressCon);
        mText = new TextView(getContext());
        mText.setTextColor(getContext().getResources().getColor(R.color.gray));
        mText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
        mText.setText(getContext().getString(R.string.xRecyclerView_loading));
        loadingHint = (String) getContext().getText(R.string.xRecyclerView_loading);
        noMoreHint = (String) getContext().getText(R.string.xRecyclerView_nomore_loading);
        loadingDoneHint = (String) getContext().getText(R.string.xRecyclerView_loading_done);
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        int margin = (int) getResources().getDimension(R.dimen.xrecycler_text_margin);
        layoutParams.setMargins(margin, margin, margin, margin);

        mText.setLayoutParams(layoutParams);
        addView(mText);

        int padding = (int) (margin * 0.5f);
        setPadding(padding, padding, padding, padding);
    }

    private AVLoadingIndicatorView getAvLoadingView(Context ctx) {
        View view = View.inflate(ctx, R.layout.avloading_layout, null);
        return ((AVLoadingIndicatorView) view.findViewById(R.id
                .listview_header_avloading));
    }

    public void setProgressStyle(int style) {
        if (style == ProgressStyle.SysProgress) {
            progressCon.setView(new ProgressBar(getContext(), null, android.R.attr
                    .progressBarStyle));
        } else {
            //AVLoadingIndicatorView progressView = new AVLoadingIndicatorView(this.getContext());
            AVLoadingIndicatorView progressView = getAvLoadingView(this.getContext());
            progressView.setIndicatorColor(getContext().getResources().getColor(R.color
                    .title_bg_color));
            //progressView.setIndicatorId(style);
            progressView.setIndicator(ProgressStyle.applyIndicator(style));
            progressCon.setView(progressView);
        }
    }

    public void setState(int state) {
        switch (state) {
            case STATE_LOADING:
                progressCon.setVisibility(View.VISIBLE);
                mText.setText(loadingHint);
                this.setVisibility(View.VISIBLE);
                break;
            case STATE_COMPLETE:
                mText.setText(loadingDoneHint);
                this.setVisibility(View.GONE);
                break;
            case STATE_NOMORE:
                mText.setText(noMoreHint);
                progressCon.setVisibility(View.GONE);
                this.setVisibility(View.VISIBLE);
                break;
        }
    }
}
