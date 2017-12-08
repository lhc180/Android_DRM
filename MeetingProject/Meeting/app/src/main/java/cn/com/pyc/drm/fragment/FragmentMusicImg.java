package cn.com.pyc.drm.fragment;

import java.io.File;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import cn.com.meeting.drm.R;
import cn.com.pyc.drm.adapter.MusicViewPagerAdaper;
import cn.com.pyc.drm.utils.DRMLog;
import cn.com.pyc.drm.utils.DRMUtil;
import cn.com.pyc.drm.utils.FileUtils;

import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * 音乐的图片展现界面
 * 
 * @author lixiangyang
 * 
 */
public class FragmentMusicImg extends BaseFragment
{

	private View rootView;
	private static TextView tvName;
	private static ImageView ivImage;

	private String imgUrl;
	private String name;
	private boolean isImgLoaded = false;

	public boolean isImgLoaded()
	{
		return isImgLoaded;
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Bundle bundle = this.getArguments();
		if (bundle != null)
		{
			this.imgUrl = bundle.getString(MusicViewPagerAdaper.IMGPATH_TAG);
			this.name = bundle.getString(MusicViewPagerAdaper.NAME_TAG);
		}
		DRMLog.i("imgUrl = " + imgUrl);
		DRMLog.i("name = " + name);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		rootView = inflater.inflate(R.layout.fragment_music_img, null, false);
		tvName = (TextView) rootView.findViewById(R.id.textMusic_name);
		ivImage = (ImageView) rootView.findViewById(R.id.albumImageView);
		// init ui data
		String fileName = FileUtils.getNameFromFilePath(imgUrl);
		String filePath = DRMUtil.DEFAULT_HIGH_SPEED_FUZZY_PATH + File.separator + fileName;
		File file = new File(filePath);
		if (file.exists())
		{
			isImgLoaded = true;
			ImageLoader.getInstance().displayImage("file://" + filePath, ivImage);
		}

		if (name != null)
		{
			tvName.setText(name.replaceAll("\"", ""));
		}
		return rootView;
	}

	/**
	 * ImageUtils.getGaussambiguity()下载图片成功后调用<br>
	 * 通过url设置音乐专辑图片
	 * 
	 * @param image
	 */
	public void setImageWithUrl(String imageUrl)
	{
		String fileName = FileUtils.getNameFromFilePath(imageUrl);
		String filePath = DRMUtil.DEFAULT_HIGH_SPEED_FUZZY_PATH + File.separator + fileName;
		File file = new File(filePath);
		if (ivImage != null && !isImgLoaded)
		{
			if (file.exists())
			{
				ImageLoader.getInstance().displayImage("file://" + filePath, ivImage);
			} else
			{
				ImageLoader.getInstance().displayImage(imageUrl, ivImage);
			}
		}
	}

	/**
	 * 本地加载音乐专辑图片
	 * 
	 * @param filePath
	 *            本地缓存图片路径
	 */
	public void setImageWithFilePath(String filePath)
	{
		if (ivImage != null && !isImgLoaded)
		{
			ImageLoader.getInstance().displayImage("file://" + filePath, ivImage);
		}
	}

	/**
	 * 设置音乐名称
	 * 
	 * @param name
	 */
	public void switchMusicName(String name)
	{
		if (tvName != null)
			tvName.setText(name.replaceAll("\"", ""));
	}

}
