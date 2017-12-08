package cn.com.pyc.drm.adapter;

import java.util.ArrayList;
import java.util.List;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import cn.com.meeting.drm.R;
import cn.com.pyc.drm.common.Constant;
import cn.com.pyc.drm.model.db.bean.Album;
import cn.com.pyc.drm.ui.BaseActivity;
import cn.com.pyc.drm.utils.CommonUtil;
import cn.com.pyc.drm.utils.ConvertToUtils;
import cn.com.pyc.drm.utils.StringUtil;

import com.android.maxwin.view.ScaleImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * 本地adapter
 * 
 * @author qd
 * 
 */
public class DownloadLocalAdapter extends BaseAdapter {

	private BaseActivity context;
	private List<Album> albums = new ArrayList<>();

	public DownloadLocalAdapter(BaseActivity context, List<Album> albums) {
		this.context = context;
		this.albums = albums;
	}

	public List<Album> getAlbums() {
		return albums;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return albums.size();
	}

	@Override
	public Album getItem(int position) {
		// TODO Auto-generated method stub
		return albums.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewLocalHolder localHolder = null;
		if (convertView == null) {
			convertView = View.inflate(context, R.layout.item_download_main_local, null);
			localHolder = new ViewLocalHolder();
			localHolder.image = (ScaleImageView) convertView.findViewById(R.id.image_local);
			localHolder.name = (TextView) convertView.findViewById(R.id.name_local);
			localHolder.tips = (TextView) convertView.findViewById(R.id.tips_local);
			localHolder.types = (TextView) convertView.findViewById(R.id.types_local);
			convertView.setTag(localHolder);
		} else {
			localHolder = (ViewLocalHolder) convertView.getTag();
		}
		Album album = albums.get(position);
		// 设置图片
		double ratio = ConvertToUtils.toDouble(album.getPicture_ratio(), 1.0);
		int imgWidth = (Constant.screenWidth - CommonUtil.dip2px(context, 16)) / 4;
		localHolder.image.setImageWidth(imgWidth);
		// 系数 picture_ratio = width/height
		localHolder.image.setImageHeight((int) (imgWidth / ratio));
		// 本地数据
		showLocalData(context, album, localHolder);
		return convertView;
	}

	// 本地数据
	private void showLocalData(BaseActivity context, Album album, ViewLocalHolder localHolder) {
		localHolder.name.setText(album.getName());
		// tips
		if ("".equals(StringUtil.formatAuthors(album.getAuthor()))) {
			localHolder.tips.setVisibility(View.GONE);
		} else {
			localHolder.tips.setText(StringUtil.formatAuthors(album.getAuthor()));
			localHolder.tips.setVisibility(View.VISIBLE);
		}
		String type = album.getCategory();
		String num = album.getItem_number();
		if (type != null) {
			switch (type) {
			case "MUSIC":
				localHolder.types.setText(context.getString(R.string.music_count, num));
				break;
			case "BOOK":
				localHolder.types.setText(context.getString(R.string.book_count, num));
				break;
			case "VIDEO":
				localHolder.types.setText(context.getString(R.string.video_count, num));
				break;
			}
		}
		if (TextUtils.isEmpty(album.getPicture()) || "null".equalsIgnoreCase(album.getPicture())) {
			localHolder.image.setBackground(context.getResources().getDrawable(R.drawable.ic_cover_page));
		} else {
			ImageLoader.getInstance().displayImage(album.getPicture(), localHolder.image, Constant.options, null);
		}
	}

	static class ViewLocalHolder {
		ScaleImageView image;
		TextView name;
		TextView tips;
		TextView types;
	}

}
