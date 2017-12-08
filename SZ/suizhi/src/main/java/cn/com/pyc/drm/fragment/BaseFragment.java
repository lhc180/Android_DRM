package cn.com.pyc.drm.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.greenrobot.event.EventBus;

/**
 * 音乐显示页面基础类
 */
public abstract class BaseFragment extends Fragment {

    public static final String MUSIC_MYPRO_ID = "music_myPro_id";
    public static final String MUSIC_ALBUM_NAME = "music_album_name";
    public static final String MUSIC_IMAGE_URL = "music_image_url";
    public static final String MUSIC_CONTENT_LIST = "music_content_list";
    public static final String MUSIC_FILE_ID = "music_file_id";
    public static final String MUSIC_LRC_ID = "music_lrc_id";

    public static final int TAG_IMAGE = 0;          //图片
    public static final int TAG_LRC = 1;            //歌词
    public static final int TAG_PREVIEW = 2;        //简介
    public static final int TAG_LIST = 3;           //列表


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable
            Bundle savedInstanceState) {
        EventBus.getDefault().registerSticky(this);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        EventBus.getDefault().unregister(this);
        super.onDestroyView();
    }


    public static BaseFragment newInstance(int tag, Bundle bundle) {
        if (tag == TAG_IMAGE) {
            FragmentMusicImg fragmentMusicImg = new FragmentMusicImg();
            fragmentMusicImg.setArguments(bundle);
            return fragmentMusicImg;
        } else if (tag == TAG_LRC) {
            FragmentMusicLrc fragmentMusicLrc = new FragmentMusicLrc();
            fragmentMusicLrc.setArguments(bundle);
            return fragmentMusicLrc;
        } else if (tag == TAG_PREVIEW) {
            FragmentMusicPreview fragmentMusicPreview = new FragmentMusicPreview();
            fragmentMusicPreview.setArguments(bundle);
            return fragmentMusicPreview;
        } else if (tag == TAG_LIST) {
            FragmentMusicList fragmentMusicList = new FragmentMusicList();
            fragmentMusicList.setArguments(bundle);
            return fragmentMusicList;
        } else {
            throw new IllegalArgumentException("params 'tag' not right.");
        }
    }

}
