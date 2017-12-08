package cn.com.pyc.drm.adapter;

import java.math.BigDecimal;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import cn.com.meeting.drm.R;
import cn.com.pyc.drm.model.db.bean.AlbumContent;
import cn.com.pyc.drm.model.right.ContentRight;
import cn.com.pyc.drm.utils.CommonUtil;
import cn.com.pyc.drm.utils.DRMLog;
import cn.com.pyc.drm.utils.MediaUtils;
import cn.com.pyc.drm.utils.TimeUtil;

/**
 * 专辑下文件列表
 * 
 * @author hudq
 * 
 */
public class Mupdf_AlbumList_Adapter extends BaseAdapter
{

	private String TAG = "MupdfAlbumListAdapter";
	private Context context;
	private List<AlbumContent> mlist;

	private int curPosition = -1;

	public void setCurrentPosition(int pos)
	{
		curPosition = pos;
	}

	public void setAlbumContentList(List<AlbumContent> mlist)
	{
		this.mlist = mlist;
	}

	// public List<AlbumContent> getAlbumContentList()
	// {
	// return mlist;
	// }

	public Mupdf_AlbumList_Adapter(Context context, List<AlbumContent> songlist)
	{
		this.context = context;
		this.mlist = songlist;
	}

	@Override
	public int getCount()
	{
		return mlist != null ? mlist.size() : 0;
	}

	@Override
	public Object getItem(int position)
	{
		return null;
	}

	@Override
	public long getItemId(int position)
	{
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		ViewHolder holder;
		if (convertView == null)
		{
			holder = new ViewHolder();
			convertView = View.inflate(context, R.layout.item_albumlist_adapter, null);
			holder.txtTitle = (TextView) convertView.findViewById(R.id.name_title);
			holder.startdate = (TextView) convertView.findViewById(R.id.start_date);
			holder.enddate = (TextView) convertView.findViewById(R.id.end_date);
			holder.valIv = (ImageView) convertView.findViewById(R.id.val_iv);
			convertView.setTag(holder);
		} else
		{
			holder = (ViewHolder) convertView.getTag();
		}
		if (position == curPosition)
		{
			holder.txtTitle.setTextColor(context.getResources().getColor(R.color.title_bar_bg_color));
			holder.enddate.setTextColor(context.getResources().getColor(R.color.title_bar_bg_color));
			holder.startdate.setTextColor(context.getResources().getColor(R.color.title_bar_bg_color));
			holder.valIv.setImageResource(R.drawable.ic_validate_time_select);
		} else
		{
			holder.txtTitle.setTextColor(context.getResources().getColor(R.color.black_bb));
			holder.enddate.setTextColor(context.getResources().getColor(R.color.gray));
			holder.startdate.setTextColor(context.getResources().getColor(R.color.gray));
			holder.valIv.setImageResource(R.drawable.ic_validate_time_nor);
		}
		List<ContentRight> rights = MediaUtils.getInstance().getPdfmediaRight();
	
		if (rights != null && rights.size() > 0)
		{
			ContentRight cr= rights.get(position);
			String odd_datetime_start = TimeUtil.getStringByLongMillsEveryminute(cr.odd_datetime_start);
			String odd_datetime_end = TimeUtil.getStringByLongMillsEveryminute(cr.odd_datetime_end);
			
		
			holder.enddate.setVisibility(View.VISIBLE);

			// 判断是否未到生效时间
			if(cr.IsEffectivetime){
				String available = CommonUtil.getLeftChangeTime(context, cr.availableTime);
				if ("00天00小时".equals(available))
				{
					// 已经过期
					holder.startdate.setText(context.getString(R.string.start_time, odd_datetime_start));
					holder.enddate.setText(context.getString(R.string.end_times,odd_datetime_end)+"  "+context.getString(R.string.over_time));
					holder.startdate.setTextColor(context.getResources().getColor(R.color.oldlace));
					holder.enddate.setTextColor(context.getResources().getColor(R.color.oldlace));
				} else if (context.getString(R.string.forever_time).equals(available))
				{
					// 永久
//					holder.startdate.setText(context.getString(R.string.start_time, odd_datetime_start));
//					holder.enddate.setText(context.getString(R.string.end_times,available));
					holder.startdate.setText(context.getString(R.string.dead_time, available));
					holder.enddate.setVisibility(View.GONE);
				} else
				{
					// 正常
					String odd_datetime_end0 =String.valueOf(cr.odd_datetime_end);
					String odd_datetime_end1=odd_datetime_end0.substring(0, 1);
				    int a = Integer.parseInt(odd_datetime_end1);
					if(a>3){
						holder.startdate.setText(context.getString(R.string.start_time, odd_datetime_start));
						holder.enddate.setText(context.getString(R.string.end_times,context.getString(R.string.forever_time)));
					}else{
						holder.startdate.setText(context.getString(R.string.start_time, odd_datetime_start));
						holder.enddate.setText(context.getString(R.string.end_times, odd_datetime_end));
					}
				}
			}else{
				// 判断如果结束时间大于2076年的时间戳，就代表是永久
				String odd_datetime_end0 =String.valueOf(cr.odd_datetime_end);
				String odd_datetime_end1=odd_datetime_end0.substring(0, 1);
			    int a = Integer.parseInt(odd_datetime_end1);
				if(a>3){
					holder.startdate.setText(context.getString(R.string.start_time, odd_datetime_start));
					holder.enddate.setText(context.getString(R.string.end_times,context.getString(R.string.forever_time)));
					holder.startdate.setTextColor(context.getResources().getColor(R.color.oldlace));
				}else{
					holder.startdate.setText(context.getString(R.string.start_time, odd_datetime_start));
					holder.enddate.setText(context.getString(R.string.end_times, odd_datetime_end));
					holder.startdate.setTextColor(context.getResources().getColor(R.color.oldlace));
				}
			}
		}
		String name = mlist.get(position).getName();
		holder.txtTitle.setText(name.replaceAll("\"", ""));
		return convertView;

	}

	// 复用控件
	static class ViewHolder
	{
		TextView txtTitle;
		TextView startdate;
		TextView enddate;
		ImageView valIv;
	}

}
