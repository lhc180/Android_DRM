package cn.com.pyc.drm.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import cn.com.pyc.drm.fragment.BaseFragment;

public class MusicViewPagerAdapter extends FragmentPagerAdapter {

    // 图片,歌词,简介，列表
    public static final int FRAGMENT_NUM = 4;

//    private String myProId;
//    private String albumName;
//    private String imageUrl;
//    private String fileId;
//    private String lrcId;
//    private List<DrmFile> contents;

    private Bundle mBundle;

    public MusicViewPagerAdapter(FragmentManager fm, Bundle bundle) {
        super(fm);
        if (bundle == null)
            throw new IllegalArgumentException("bundle required init.");

        mBundle = bundle;

//        this.albumName = bundle.getString(BaseFragment.MUSIC_ALBUM_NAME);
//        this.imageUrl = bundle.getString(BaseFragment.MUSIC_IMAGE_URL);
//        this.contents = bundle.getParcelableArrayList(BaseFragment.MUSIC_CONTENT_LIST);
//
//        this.myProId = bundle.getString(BaseFragment.MUSIC_MYPRO_ID);
//        this.fileId = bundle.getString(BaseFragment.MUSIC_FILE_ID);
//        this.lrcId = bundle.getString(BaseFragment.MUSIC_LRC_ID);
    }

    @Override
    public Fragment getItem(int arg0) {
        switch (arg0) {
            case 0:
//                return BaseFragment.newInstanceImage(albumName, imageUrl);
                return BaseFragment.newInstance(BaseFragment.TAG_IMAGE, mBundle);
            case 1:
//                return BaseFragment.newInstanceLrc(myProId, lrcId);
                return BaseFragment.newInstance(BaseFragment.TAG_LRC, mBundle);
            case 2:
//                return BaseFragment.newInstancePreview(fileId);
                return BaseFragment.newInstance(BaseFragment.TAG_PREVIEW, mBundle);
            case 3:
//                return BaseFragment.newInstanceList(contents);
                return BaseFragment.newInstance(BaseFragment.TAG_LIST, mBundle);
            default:
                Log.e("", "Fragment getItem error");
                break;
        }
        return BaseFragment.newInstance(BaseFragment.TAG_IMAGE, mBundle);
    }

    @Override
    public int getCount() {
        return FRAGMENT_NUM;
    }

}
