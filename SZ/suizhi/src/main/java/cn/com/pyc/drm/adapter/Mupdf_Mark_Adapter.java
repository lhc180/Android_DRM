package cn.com.pyc.drm.adapter;

import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import cn.com.pyc.drm.R;
import cn.com.pyc.drm.model.db.bean.Bookmark;
import cn.com.pyc.drm.utils.ConvertToUtils;

/**
 * 书签编辑选项
 * 
 */
public class Mupdf_Mark_Adapter extends BaseAdapter {

	private List<Bookmark> bookmarkList;
	private LayoutInflater mInflater;

	public Mupdf_Mark_Adapter(LayoutInflater mInflater,
			List<Bookmark> bookmarkList) {
		super();
		this.bookmarkList = bookmarkList;
		this.mInflater = mInflater;
	}

	public int getCount() {
		if (bookmarkList == null) {
			return 0;
		}
		return bookmarkList.size();
	}

	@Override
	public Bookmark getItem(int position) {
		return bookmarkList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		Bookmark bookmark = getItem(position);
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.item_pdf_mark, null);
			holder.content = (TextView) convertView.findViewById(R.id.content);
			holder.pagefew = (TextView) convertView.findViewById(R.id.pagefew);
			holder.time = (TextView) convertView.findViewById(R.id.time);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		// int page = Integer.parseInt(bookmark.getPagefew());
		int page = ConvertToUtils.toInt(bookmark.getPagefew());
		holder.content.setText(bookmark.getContent());
		holder.pagefew.setText("第" + (++page) + "页");
		holder.time.setText(bookmark.getTime());
		return convertView;
	}

	static class ViewHolder {
		TextView content;
		TextView pagefew;
		TextView time;
	}

	public void setData(List<Bookmark> bookmarkList2) {
		this.bookmarkList = bookmarkList2;
		notifyDataSetChanged();
	}

}
