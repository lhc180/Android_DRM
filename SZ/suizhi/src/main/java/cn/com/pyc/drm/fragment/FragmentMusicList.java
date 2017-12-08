package cn.com.pyc.drm.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.wang.avi.AVLoadingIndicatorView;

import java.util.List;

import cn.com.pyc.drm.R;
import cn.com.pyc.drm.bean.DrmFile;
import cn.com.pyc.drm.bean.event.MusicCurrentPlayEvent;
import cn.com.pyc.drm.bean.event._ColorPixEvent;
import cn.com.pyc.drm.common.MusicMode;
import cn.com.pyc.drm.common.ViewHolder;
import cn.com.pyc.drm.service.MusicPlayService;
import cn.com.pyc.drm.utils.DRMLog;
import cn.com.pyc.drm.utils.ValidDateUtil;
import cn.com.pyc.drm.utils.help.BitmapPixHelp;
import cn.com.pyc.drm.utils.help.KeyHelp;
import cn.com.pyc.drm.utils.help.MusicHelp;
import cn.com.pyc.drm.utils.manager.MusicUIShowManager;
import cn.com.pyc.drm.widget.ToastShow;

/**
 * fragment music 列表
 *
 * @author hudaqiang
 */
public class FragmentMusicList extends BaseFragment implements AdapterView.OnItemClickListener {

    private Activity mActivity;
    private List<DrmFile> contents;

    private ListView mListView;
    private MusicListAdapter mAdapter;

    //当前歌曲切换了
    public void onEventMainThread(MusicCurrentPlayEvent event) {
        if (mAdapter != null) {
            mAdapter.setCurrentItemId(event.getFileId());
        }
    }

    //获取到了背景的颜色值的通知
    public void onEventMainThread(_ColorPixEvent event) {
        BitmapPixHelp._Color _color = event.getColor();
        boolean isLightBg = BitmapPixHelp.isLightColor(_color);
        if (mAdapter != null) {
            mAdapter.setUnSelectNameColorId(MusicUIShowManager.getInstance()._1_textColor(isLightBg));
            mAdapter.setUnSelectTimeColorId(MusicUIShowManager.getInstance()._2_textColor(isLightBg));
            mAdapter.notifyDataSetChanged();
        }
        DRMLog.d("emt", "fg list light is " + isLightBg);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Bundle bundle = getArguments();
            contents = bundle.getParcelableArrayList(BaseFragment.MUSIC_CONTENT_LIST);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_music_list, container, false);
        mListView = (ListView) rootView.findViewById(R.id.music_listview);
        mAdapter = new MusicListAdapter(mActivity, contents);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        super.onCreateView(inflater, container, savedInstanceState);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mActivity = null;
        mListView.setOnItemClickListener(null);
        if (contents != null)
            contents.clear();
    }

    private class MusicListAdapter extends BaseAdapter {

        private Context ctx;
        private List<DrmFile> contents;
        private String currentItemId;

        private int selectColorId;
        private int unSelectNameColorId;
        private int unSelectTimeColorId;
        private Drawable selectDrawable;
        private Drawable unSelectDrawable;

        private void setCurrentItemId(String currentItemId) {
            if (TextUtils.equals(this.currentItemId, currentItemId))
                return;
            this.currentItemId = currentItemId;
            notifyDataSetChanged();
        }

        public void setUnSelectNameColorId(int unSelectNameColorId) {
            this.unSelectNameColorId = unSelectNameColorId;
        }

        public void setUnSelectTimeColorId(int unSelectTimeColorId) {
            this.unSelectTimeColorId = unSelectTimeColorId;
        }

        private MusicListAdapter(Context ctx, List<DrmFile> contents) {
            this.ctx = ctx;
            this.contents = contents;

            Resources resources = this.ctx.getResources();
            this.selectColorId = resources.getColor(R.color.brilliant_blue);
            this.unSelectNameColorId = resources.getColor(R.color.white_smoke);
            this.unSelectTimeColorId = Color.parseColor("#dbdbdb");//resources.getColor(R.color
            // .gray);
            this.selectDrawable = resources.getDrawable(R.drawable.ic_validate_time_select);
            this.unSelectDrawable = resources.getDrawable(R.drawable.ic_validate_time_nor);
        }

        @Override
        public int getCount() {
            return contents != null ? contents.size() : 0;
        }

        @Override
        public DrmFile getItem(int position) {
            return contents.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, android.view.View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = View.inflate(ctx, R.layout.item_music_list, null);
            }
            TextView tvName = ViewHolder.get(convertView, R.id.music_list_name);
            TextView tvTime = ViewHolder.get(convertView, R.id.music_list_text_status);
            ImageView ivTime = ViewHolder.get(convertView, R.id.music_list_img_status);
            AVLoadingIndicatorView avLoading = ViewHolder.get(convertView, R.id.music_list_working);
            DrmFile ac = contents.get(position);
            tvName.setText(ac.getFileName());
            //SZContent szCont = new SZContent(ac.getAsset_id());
            tvTime.setText(ValidDateUtil.getValidTime(ctx, ac.getValidityTime(), ac
                    .getEndDatetime()));

            boolean equals = TextUtils.equals(currentItemId, ac.getFileId());
            tvName.setTextColor(equals ? selectColorId : unSelectNameColorId);
            tvTime.setTextColor(equals ? selectColorId : unSelectTimeColorId);
            ivTime.setImageDrawable(equals ? selectDrawable : unSelectDrawable);
            avLoading.setVisibility(equals ? View.VISIBLE : View.GONE);

            return convertView;
        }
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mAdapter == null) return;
        DrmFile ac = mAdapter.getItem(position);
        if (!ac.isCheckOpen()) {
            ToastShow.getToast().showBusy(mActivity, getString(R.string.file_expired));
            return;
        }
        if (ac.isInEffective()) {
            ToastShow.getToast().showBusy(mActivity, getString(R.string.file_ineffective));
            return;
        }
        if (MusicHelp.isSameMusic(ac.getFileId())) return;

        MusicHelp.stop(mActivity);
        mAdapter.setCurrentItemId(ac.getFileId());

        Intent intent = new Intent(mActivity, MusicPlayService.class);
        intent.putExtra(KeyHelp.MPS_FILE_ID, ac.getFileId());
        intent.putExtra(KeyHelp.MPS_OPTION, MusicMode.Status.PLAY);
        mActivity.startService(intent);
    }
}
