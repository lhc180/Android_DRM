package cn.com.pyc.drm.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import cn.com.pyc.drm.R;
import cn.com.pyc.drm.bean.event.MusicCurrentPlayEvent;
import cn.com.pyc.drm.bean.event._ColorPixEvent;
import cn.com.pyc.drm.utils.DRMLog;
import cn.com.pyc.drm.utils.help.BitmapPixHelp;
import cn.com.pyc.drm.utils.help.ImageLoadHelp;
import cn.com.pyc.drm.utils.manager.MusicUIShowManager;

/**
 * 音乐的图片展现界面
 *
 * @author lixiangyang
 */
public class FragmentMusicImg extends BaseFragment {

    private TextView albumNameText;
    private TextView musicNameText;

    private String imageUrl;            //专辑图片地址
    private String albumName;           //专辑名称

    //歌曲切换了，修改名称
    public void onEventMainThread(MusicCurrentPlayEvent event) {
        if (musicNameText != null) {
            musicNameText.setText(event.getFileName());
        }
    }

    //获取到了背景的颜色值的通知
    public void onEventMainThread(_ColorPixEvent event) {
        BitmapPixHelp._Color _color = event.getColor();
        boolean isLightBg = BitmapPixHelp.isLightColor(_color);
        if (musicNameText != null) {
            musicNameText.setTextColor(MusicUIShowManager.getInstance()._1_textColor(isLightBg));
        }
        if (albumNameText != null) {
            albumNameText.setTextColor(MusicUIShowManager.getInstance()._2_textColor(isLightBg));
        }
        DRMLog.d("emt", "fg img light is " + isLightBg);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            this.albumName = bundle.getString(BaseFragment.MUSIC_ALBUM_NAME);
            this.imageUrl = bundle.getString(BaseFragment.MUSIC_IMAGE_URL);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_music_img, container, false);
        ImageView ivImage = (ImageView) rootView.findViewById(R.id.music_albumImage);
        ImageLoadHelp.loadImage(ivImage, imageUrl);
        albumNameText = (TextView) rootView.findViewById(R.id.music_albumName);
        albumNameText.setText(albumName);
        musicNameText = (TextView) rootView.findViewById(R.id.music_name);
        super.onCreateView(inflater, container, savedInstanceState);
        return rootView;
    }

}
