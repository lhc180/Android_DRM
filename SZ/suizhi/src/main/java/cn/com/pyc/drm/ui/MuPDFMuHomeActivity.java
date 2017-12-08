package cn.com.pyc.drm.ui;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.artifex.mupdfdemo.OutlineItem;
import com.indicators.view.indicator.IndicatorViewPager;
import com.indicators.view.indicator.IndicatorViewPager.IndicatorFragmentPagerAdapter;
import com.indicators.view.indicator.IndicatorViewPager.OnIndicatorPageChangeListener;
import com.indicators.view.indicator.ScrollIndicatorView;
import com.indicators.view.indicator.slidebar.ColorBar;
import com.indicators.view.indicator.transition.OnTransitionTextListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import cn.com.pyc.drm.R;
import cn.com.pyc.drm.fragment.FragmentPDFBookmark;
import cn.com.pyc.drm.fragment.FragmentPDFOutline;
import cn.com.pyc.drm.utils.DeviceUtils;
import cn.com.pyc.drm.utils.help.UIHelper;

/**
 * 目录，书签home-activity
 *
 * @author hudq  update
 */
public class MuPDFMuHomeActivity extends BaseFragmentActivity {
    private IndicatorViewPager indicatorViewPager;
    private LayoutInflater inflate;
    private String[] tabname;
    private List<Fragment> muluList = new ArrayList<Fragment>();
    private int tabWidth;
    private MyAdapter adapter;

    /**
     * 当前选中页位置
     */
    private int mCurrentPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outline_tab);
        UIHelper.showTintStatusBar(this, getResources().getColor(R.color.bkg_lightdark));
        getWidths();
        getValue();
        ViewPager viewPager = (ViewPager) findViewById(R.id.moretab_viewPager);
        ScrollIndicatorView indicator = (ScrollIndicatorView) findViewById(R.id.moretab_indicator);
        int selectColorId = R.color.title_bg_color;
        int unSelectColorId = R.color.white;
        indicator.setScrollBar(new ColorBar(this, getResources().getColor(
                selectColorId), 5));
        indicator.setOnTransitionListener(new OnTransitionTextListener()
                .setColorId(this, selectColorId, unSelectColorId));
        viewPager.setOffscreenPageLimit(2);
        indicatorViewPager = new IndicatorViewPager(indicator, viewPager);
        inflate = LayoutInflater.from(getApplicationContext());
        adapter = new MyAdapter(getSupportFragmentManager());
        indicatorViewPager.setAdapter(adapter);

        setListener();
    }

    private void setListener() {
        indicatorViewPager
                .setOnIndicatorPageChangeListener(new OnIndicatorPageChangeListener() {
                    @Override
                    public void onIndicatorPageChange(int preItem,
                                                      int currentItem) {
                        mCurrentPosition = currentItem;
                    }
                });

        findViewById(R.id.iv_back).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                UIHelper.finishActivity(MuPDFMuHomeActivity.this);
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        int mCurrentOrientation = getResources().getConfiguration().orientation;
        if (mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT) {
            getWidths();
            // // ReaderView.mScale = MuPDFActivity.sScaleVertical;
            // ReaderView.HORIZONTAL_SCROLLING = true;
            adapter.notifyDataSetChanged();
        } else if (mCurrentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            getWidths();
            // // ReaderView.mScale = MuPDFActivity.sScaleHorizontal;
            // ReaderView.HORIZONTAL_SCROLLING = false;
            adapter.notifyDataSetChanged();
        }
        indicatorViewPager.setCurrentItem(mCurrentPosition, true);
    }

    private void getWidths() {
        Point outSize = DeviceUtils.getScreenSize(this);
        tabWidth = outSize.x / 2;
    }

    private void getValue() {
        tabname = new String[]{getString(R.string.directory_label),
                getString(R.string.bookmark_label)};
        Intent intent = getIntent();
        @SuppressWarnings("unchecked")
        List<OutlineItem> outline = (ArrayList<OutlineItem>) intent
                .getSerializableExtra("outline_list");
        String asset_id = intent.getStringExtra("asset_content_id");

        FragmentPDFOutline fol = new FragmentPDFOutline();
        Bundle args = new Bundle();
        args.putSerializable(FragmentPDFOutline.KEY_OUTLINES,
                (Serializable) outline);
        fol.setArguments(args);
        muluList.add(fol);

        Bundle args2 = new Bundle();
        args2.putString("asset_id", asset_id);
        FragmentPDFBookmark fbm = new FragmentPDFBookmark();
        fbm.setArguments(args2);
        muluList.add(fbm);
    }

    private class MyAdapter extends IndicatorFragmentPagerAdapter {

        public MyAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public int getCount() {
            return tabname.length;
        }

        @Override
        public View getViewForTab(int position, View convertView,
                                  ViewGroup container) {
            if (convertView == null) {
                convertView = inflate.inflate(R.layout.tab_top_text, container,
                        false);
            }
            TextView textView = (TextView) convertView;
            textView.setText(tabname[position]);
            textView.setWidth(tabWidth);
            return convertView;
        }

        @Override
        public Fragment getFragmentForPage(int position) {
            return muluList.get(position);
        }
    }

    ;

    /**
     * 监听Back键按下事件,方法2: 注意: 返回值表示:是否能完全处理该事件 在此处返回false,所以会继续传播该事件.
     * 在具体项目中此处的返回值视情况而定.
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            UIHelper.finishActivity(this);
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 设置listview的EmptyView，在加载数据为空或没有数据的时候使用
     *
     * @param lv
     * @param emptyView
     * @param tips
     */
    public static final void setEmptyViews(ListView lv, View emptyView,
                                           String tips) {
        TextView tipTextView = ((TextView) emptyView
                .findViewById(R.id.empty_text));
        tipTextView.setTextColor(Color.parseColor("#666666"));
        tipTextView.setText(tips);
        emptyView.findViewById(R.id.empty_image).setVisibility(View.GONE);
        lv.setEmptyView(emptyView);
    }

}
