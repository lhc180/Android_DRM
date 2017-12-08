package cn.com.pyc.drm.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

import java.util.List;

import cn.com.meeting.drm.R;
import cn.com.pyc.drm.adapter.MHAdapter;
import cn.com.pyc.drm.adapter.Rv_BaseAdapter;
import cn.com.pyc.drm.bean.MechanismBean;
import cn.com.pyc.drm.bean.MeetingBean;
import cn.com.pyc.drm.utils.CommonUtil;
import cn.com.pyc.drm.utils.OpenPageUtil;
import cn.com.pyc.drm.utils.UIHelper;
import cn.com.pyc.drm.utils.manager.ActicityManager;
import cn.com.pyc.drm.widget.HighlightImageView;
import cn.com.pyc.drm.widget.SearchEditText;

/**
 * @author 李巷阳
 * @version V1.0
 * @Description: (搜索界面)
 * @date 2017/3/3 14:47
 */
public class SearchProductActivity extends BaseActivity implements Rv_BaseAdapter.OnItemClickListener {

    @ViewInject(R.id.back_img)
    private HighlightImageView mBack_img;

    @ViewInject(R.id.title_tv)
    private TextView mTitle_tv;

    @ViewInject(R.id.search_edittext)
    private SearchEditText mSearch_edittext;

    @ViewInject(R.id.search_canceltext)
    private TextView mSearch_canceltext;

    @ViewInject(R.id.lv_scanhistory)
    private RecyclerView mLv_scanhistory;

    private MechanismBean mmechanismbean;

    private InputMethodManager imManager;
    private String keyWords;
    private List<MeetingBean> meetingBeanList;
    private List<MeetingBean> search_meetingBeanList;
    private MHAdapter mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_product);
        init_Value();
        init_View();
    }

    @OnClick({R.id.back_img,R.id.search_canceltext})
    public void ViewClick(View v)
    {
        switch (v.getId()){
            case R.id.back_img:
                finishUI();
                break;
            case R.id.search_canceltext:
                finishUI();
                break;
        }
    }


    @Override
    protected void init_View() {
        ViewUtils.inject(this);
        ActicityManager.getInstance().add(this);
        UIHelper.showTintStatusBar(this); // 自定义状态栏
        LinearLayoutManager LinearManager = new LinearLayoutManager(this);
        LinearManager.setOrientation(LinearLayoutManager.VERTICAL);
        mLv_scanhistory.setLayoutManager(LinearManager);

        mTitle_tv.setText(mmechanismbean!=null?mmechanismbean.getServerName():"");
        mSearch_edittext.requestFocus();
        mSearch_edittext.setCancelTextView(mSearch_canceltext);
        mSearch_edittext.setOnSearchClickListener(new SearchEditText.OnSearchClickListener()
        {
            @Override
            public void onSearchClick(View view)
            {
                keyWords = ((EditText) view).getText().toString().trim();
                search_meetingBeanList =  CommonUtil.searchList(meetingBeanList,keyWords);
                if(search_meetingBeanList==null || search_meetingBeanList.size()==0)
                {
                    showToast("对不起,您搜索的内容未找到。");
                    return;
                }
                setAdapter(search_meetingBeanList);
            }
        });
    }



    /**
     *  设置我的会议的数据适配器
     * @param meetingRecordData
     */
    private void setAdapter(List<MeetingBean> meetingRecordData) {
        if (mAdapter == null) {
            mAdapter = new MHAdapter(getApplicationContext(), meetingRecordData);
            mLv_scanhistory.setAdapter(mAdapter);
        } else {
            mAdapter.refreshData(meetingRecordData);
        }
        mAdapter.setOnItemClickListener(this);
    }

    @Override
    protected void load_Data() {

    }

    @Override
    protected void init_Value() {
        mmechanismbean = (MechanismBean) getIntent().getSerializableExtra("MechanismData");
        meetingBeanList = (List<MeetingBean>) getIntent().getSerializableExtra("MeetingDataList");
    }


    @Override
    public void onBackPressed() {
        finishUI();
    }

    private void finishUI() {
        InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        im.hideSoftInputFromWindow(getCurrentFocus().getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        this.finish();
        overridePendingTransition(android.R.anim.fade_in,
                android.R.anim.fade_out);
    }

    @Override
    public void onItemClick(View view, int position) {
        final MeetingBean mData = search_meetingBeanList.get(position);
        OpenPageUtil.openDownloadMainByMyMeeting(SearchProductActivity.this, mData, mmechanismbean);
    }


}
