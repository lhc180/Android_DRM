package cn.com.pyc.drm.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import cn.com.pyc.drm.R;
import cn.com.pyc.drm.common.DrmPat;
import cn.com.pyc.drm.common.ViewHolder;
import cn.com.pyc.drm.model.db.bean.AlbumContent;
import cn.com.pyc.drm.model.right.SZContent;
import cn.com.pyc.drm.utils.ValidDateUtil;
import cn.com.pyc.drm.utils.help.KeyHelp;
import cn.com.pyc.drm.utils.help.UIHelper;
import cn.com.pyc.drm.utils.manager.ActicityManager;
import cn.com.pyc.drm.widget.ToastShow;

/**
 * 展示已下载文件list视图
 *
 * @author hudq
 */
public class ListAlbumContentActivity extends BaseActivity implements
        OnClickListener, OnItemClickListener {
    String myProId;
    String productName;
    String fileId;
    String category;
    String album_pic;
    List<AlbumContent> contents;
    private ListView mListView;
    private MyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_albumcontent);
        getValue();
        initView();
        loadData();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void getValue() {
        Intent intent = getIntent();
        myProId = intent.getStringExtra(KeyHelp.KEY_MYPRO_ID);
        productName = intent.getStringExtra(KeyHelp.KEY_PRO_NAME);
        fileId = intent.getStringExtra(KeyHelp.KEY_FILE_ID);
        category = intent.getStringExtra(KeyHelp.KEY_PRO_CATEGORY);
        album_pic = intent.getStringExtra(KeyHelp.KEY_PRO_URL); // 音乐的时候使用
        contents = (List<AlbumContent>) intent.getSerializableExtra(KeyHelp.KEY_SAVE_CONTENT);

        if (contents == null || category == null) {
            finish();
            ToastShow.getToast().showFail(getApplicationContext(), "无资源记录");
            return;
        }
    }

    @Override
    protected void initView() {
        UIHelper.showTintStatusBar(this);
        ActicityManager.getInstance().add(this);
        ((TextView) findViewById(R.id.title_tv)).setText(productName);
        findViewById(R.id.back_img).setOnClickListener(this);
        mListView = (ListView) findViewById(R.id.ac_listview);
        mListView.setOnItemClickListener(this);
    }

    @Override
    protected void loadData() {
        adapter = new MyAdapter(this, contents);
        mListView.setAdapter(adapter);
        adapter.setContentId(fileId);
        adapter.notifyDataSetChanged();
    }

    private class MyAdapter extends BaseAdapter {
        private Context mContext;
        private List<AlbumContent> contents;
        private String contentId;

        private int selectColorId;
        private int unSelectNameColorId;
        private int unSelectTimeColorId;
        private Drawable selectDrawable;
        private Drawable unSelectDrawable;

        public void setContentId(String contentId) {
            this.contentId = contentId;
        }

        private MyAdapter(Context mContext, List<AlbumContent> contents) {
            super();
            this.mContext = mContext;
            this.contents = contents;

            Resources resources = this.mContext.getResources();
            this.selectColorId = resources.getColor(R.color.brilliant_blue);
            this.unSelectNameColorId = resources.getColor(R.color.black_aa);
            this.unSelectTimeColorId = resources.getColor(R.color.gray);
            this.selectDrawable = resources.getDrawable(R.drawable.ic_validate_time_select);
            this.unSelectDrawable = resources.getDrawable(R.drawable.ic_validate_time_nor);
        }

        @Override
        public int getCount() {
            return contents != null ? contents.size() : 0;
        }

        @Override
        public AlbumContent getItem(int position) {
            return contents.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(mContext, R.layout.item_albumcontent_list, null);
            }
            TextView tvName = ViewHolder.get(convertView, R.id.tv_ac_name);
            TextView tvTime = ViewHolder.get(convertView, R.id.tv_ac_status);
            TextView tvLabel = ViewHolder.get(convertView, R.id.tv_ac_status_lable);
            ImageView ivTime = ViewHolder.get(convertView, R.id.iv_ac_status);

            AlbumContent ac = contents.get(position);
            boolean equals = TextUtils.equals(contentId, ac.getContent_id());
            tvName.setTextColor(equals ? selectColorId : unSelectNameColorId);
            tvTime.setTextColor(equals ? selectColorId : unSelectTimeColorId);
            tvLabel.setTextColor(equals ? selectColorId : unSelectTimeColorId);
            ivTime.setImageDrawable(equals ? selectDrawable : unSelectDrawable);

            tvName.setText(ac.getName());
            SZContent szcont = new SZContent(ac.getAsset_id());
            if (szcont.isInEffective()) {
                //文件未生效
                tvTime.setText(mContext.getString(R.string.file_ineffective));
            } else {
                tvTime.setText(ValidDateUtil.getValidTime(mContext,
                        szcont.getAvailbaleTime(), szcont.getOdd_datetime_end()));
            }
            return convertView;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_img:
                UIHelper.finishActivity(this);
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        UIHelper.finishActivity(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActicityManager.getInstance().remove(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        AlbumContent ac = (AlbumContent) mListView.getItemAtPosition(position);
        if (ac == null) return;
        SZContent szContent = new SZContent(ac.getAsset_id());
        if (!szContent.checkOpen()) {
            showToast(getString(R.string.file_expired));
            return;
        }
        if (szContent.isInEffective()) {
            showToast(getString(R.string.file_ineffective));
            return;
        }

        adapter.setContentId(ac.getContent_id());
        adapter.notifyDataSetChanged();
        OpenPage(ac);
    }

    private void OpenPage(AlbumContent ac) {
        Bundle bundle = new Bundle();
        bundle.putString(KeyHelp.KEY_MYPRO_ID, myProId);
        bundle.putString(KeyHelp.KEY_PRO_NAME, productName);
        bundle.putString(KeyHelp.KEY_FILE_ID, ac.getContent_id());
        ////bundle.putSerializable(KeyHelp.KEY_SAVE_CONTENT, (ArrayList<AlbumContent>) this
        // .contents);
        switch (this.category) {
            case DrmPat.BOOK: {
                Intent data = new Intent();
                data.putExtras(bundle);
                setResult(Activity.RESULT_OK, data);
                // OpenPageUtil.openActivity(this, MuPDFActivity.class, bundle);
                UIHelper.finishActivity(ListAlbumContentActivity.this);
            }
            break;
            default:
                break;
        }
    }

}
