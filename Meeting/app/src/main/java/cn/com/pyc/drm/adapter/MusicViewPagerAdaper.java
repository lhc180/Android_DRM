package cn.com.pyc.drm.adapter;

import cn.com.pyc.drm.fragment.FragmentMusicImg;
import cn.com.pyc.drm.fragment.FragmentMusicLyc;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class MusicViewPagerAdaper extends FragmentPagerAdapter
{

	// 图片和歌词
	private Fragment[] fs = new Fragment[2];
	private String imgUrl;
	private String name;

	public static final String IMGPATH_TAG = "image_path";
	public static final String NAME_TAG = "name";

	public MusicViewPagerAdaper(FragmentManager fm, String imgUrl, String name)
	{
		super(fm);
		this.imgUrl = imgUrl;
		this.name = name;
	}

	@Override
	public Fragment getItem(int arg0)
	{
		switch (arg0)
		{
		case 0:
		{
			fs[0] = new FragmentMusicImg();
			Bundle bundle = new Bundle();
			bundle.putString(IMGPATH_TAG, imgUrl);
			bundle.putString(NAME_TAG, name);
			fs[0].setArguments(bundle);
			return fs[0];
		}
		case 1:
		{
			fs[1] = new FragmentMusicLyc();
			Bundle bundle = new Bundle();
			bundle.putString(IMGPATH_TAG, imgUrl);
			fs[1].setArguments(bundle);
			return fs[1];
		}
		}
		return fs[0];
	}

	@Override
	public int getCount()
	{
		return fs.length;
	}

}
