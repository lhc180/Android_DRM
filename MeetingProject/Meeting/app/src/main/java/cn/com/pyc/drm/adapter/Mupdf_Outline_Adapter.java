package cn.com.pyc.drm.adapter;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import cn.com.meeting.drm.R;
import cn.com.pyc.drm.utils.DRMUtil;

import com.artifex.mupdfdemo.OutlineItem;

//目录
public class Mupdf_Outline_Adapter extends BaseAdapter
{

	private List<OutlineItem> moutlineList;
	private Context context;
	private int currentposition = 0;

	public Mupdf_Outline_Adapter(Context context, List<OutlineItem> outlineList)
	{
		this.context = context;
		moutlineList = outlineList;
	}

	public int getCount()
	{
		if (moutlineList == null)
		{
			return 0;
		}
		return moutlineList.size();
	}

	public Object getItem(int arg0)
	{
		return moutlineList.get(arg0);
	}

	public long getItemId(int arg0)
	{
		return 0;
	}

	public View getView(int position, View convertView, ViewGroup parent)
	{
		ViewHolder holder;
		if (convertView == null)
		{
			holder = new ViewHolder();
			convertView = View.inflate(context, R.layout.item_pdf_outline, null);
			holder.txtTitle = (TextView) convertView.findViewById(R.id.title);
			holder.pageNum = (TextView) convertView.findViewById(R.id.page_num);
			convertView.setTag(holder);
		} else
		{
			holder = (ViewHolder) convertView.getTag();
		}
		currentposition = position + 1;
		OutlineItem item = moutlineList.get(position);
		if (currentposition == (DRMUtil.OutlinePosition))
		{
			holder.txtTitle.setTextColor(context.getResources().getColor(R.color.title_bar_bg_color));
			holder.pageNum.setTextColor(context.getResources().getColor(R.color.title_bar_bg_color));
		} else
		{
			holder.txtTitle.setTextColor(context.getResources().getColor(R.color.content_text_color_white));
			holder.pageNum.setTextColor(context.getResources().getColor(R.color.content_text_color_white));
		}
		holder.txtTitle.setText(item.title);
		holder.pageNum.setText(context.getString(R.string.page_n, (item.page + 1)));
		return convertView;
	}

	// 复用控件
	static class ViewHolder
	{
		TextView txtTitle;
		TextView pageNum;
	}

}
