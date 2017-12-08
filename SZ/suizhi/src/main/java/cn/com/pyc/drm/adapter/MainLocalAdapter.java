package cn.com.pyc.drm.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.maxwin.view.ScaleImageView;

import java.util.ArrayList;
import java.util.List;

import cn.com.pyc.drm.R;
import cn.com.pyc.drm.common.Constant;
import cn.com.pyc.drm.model.db.bean.Album;
import cn.com.pyc.drm.ui.BaseActivity;
import cn.com.pyc.drm.utils.CommonUtil;
import cn.com.pyc.drm.utils.ConvertToUtils;
import cn.com.pyc.drm.utils.help.ImageLoadHelp;

/**
 * 本地adapter
 *
 * @author qd
 */
public class MainLocalAdapter extends BaseAdapter {

    private BaseActivity context;
    private List<Album> albums = new ArrayList<>();
    private int imgWidth;
    //private Drawable coverDrawable;

    public MainLocalAdapter(BaseActivity context, List<Album> albums) {
        this.context = context;
        this.albums = albums;
        imgWidth = (Constant.screenWidth - CommonUtil.dip2px(context, 16)) / 2;
        //coverDrawable = this.context.getResources().getDrawable(R.drawable.ic_cover_page);
    }

    public List<Album> getAlbums() {
        return albums;
    }

    @Override
    public int getCount() {
        return albums.size();
    }

    @Override
    public Album getItem(int position) {
        return albums.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewLocalHolder localHolder = null;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.item_main_local, null);
            localHolder = new ViewLocalHolder();
            localHolder.image = (ScaleImageView) convertView.findViewById(R.id.image_local);
            localHolder.name = (TextView) convertView.findViewById(R.id.name_local);
            localHolder.tips = (TextView) convertView.findViewById(R.id.tips_local);
            convertView.setTag(localHolder);
        } else {
            localHolder = (ViewLocalHolder) convertView.getTag();
        }
        Album album = albums.get(position);
        double ratio = ConvertToUtils.toDouble(album.getPicture_ratio(), 1.0);
        localHolder.image.setImageWidth(imgWidth);
        localHolder.image.setImageHeight((int) (imgWidth / ratio)); // 系数
        ImageLoadHelp.loadImage(localHolder.image,album.getPicture());
        //ImageLoader.getInstance().displayImage(album.getPicture(), localHolder.image);
        localHolder.name.setText(album.getName());
        localHolder.tips.setText(album.getAuthor());
        return convertView;
    }

    // 本地数据
//    @Deprecated
//    private void showLocalData(BaseActivity context, Album album, ViewLocalHolder localHolder) {
//        localHolder.name.setText(album.getName());
//        localHolder.tips.setText(album.getAuthor());
//        String type = album.getCategory();
    // String num = album.getItem_number();
//        if (type != null) {
//            switch (type) {
//                case DrmPat.MUSIC:
//                    // localHolder.types.setText(context.getString(R.string.music_count,
//                    // num));
//                    localHolder.types.setText(context
//                            .getString(R.string.category_music));
//                    break;
//                case DrmPat.BOOK:
//                    // localHolder.types.setText(context.getString(R.string.book_count,
//                    // num));
//                    localHolder.types.setText(context
//                            .getString(R.string.category_book));
//                    break;
//                case DrmPat.VIDEO:
//                    // localHolder.types.setText(context.getString(R.string.video_count,
//                    // num));
//                    localHolder.types.setText(context
//                            .getString(R.string.category_video));
//                    break;
//            }
//        }
//        if (StringUtil.isEmptyOrNull(album.getPicture())) {
//            localHolder.image.setBackgroundDrawable(coverDrawable);
//        } else {
//            ImageLoader.getInstance().displayImage(album.getPicture(), localHolder.image);
//        }
//    }

    private static class ViewLocalHolder {
        ScaleImageView image;
        TextView name;
        TextView tips;
    }

}
