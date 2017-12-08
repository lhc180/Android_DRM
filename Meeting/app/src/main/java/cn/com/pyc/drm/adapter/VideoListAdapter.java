package cn.com.pyc.drm.adapter;

import java.util.List;

import tv.danmaku.ijk.media.widget.DrmFile;
import tv.danmaku.ijk.media.widget._ColorText;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import cn.com.meeting.drm.R;

public class VideoListAdapter extends BaseAdapter {
	private Context context;
	private List<DrmFile> drmFiles;
	private int curPos;
	private _ColorText colorText;

	public VideoListAdapter(Context context, List<DrmFile> drmFiles) {
		this.context = context;
		this.drmFiles = drmFiles;
	}

	@Override
	public int getCount() {


		return drmFiles != null ? drmFiles.size() : 0;
	}

	@Override
	public Object getItem(int position) {

		return drmFiles.get(position);
	}

	@Override
	public long getItemId(int position) {

		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(context).inflate(
					R.layout.item_video_lists, null);
			holder.videoName = (TextView) convertView
					.findViewById(R.id.alt_txt_title);
			holder.enddate = (TextView) convertView
					.findViewById(R.id.end_date);
			holder.startdate = (TextView) convertView
					.findViewById(R.id.start_date);
			holder.icon = (ImageView) convertView
					.findViewById(R.id.img_validate);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		DrmFile df = drmFiles.get(position);
		if (position == curPos) {
			holder.videoName.setTextColor(context.getResources().getColor(
					R.color.title_bar_bg_color));
			holder.startdate.setTextColor(context.getResources().getColor(
					R.color.title_bar_bg_color));
			holder.enddate.setTextColor(context.getResources().getColor(
					R.color.title_bar_bg_color));
			holder.icon.setImageResource(R.drawable.ic_validate_time_select);
		} else {
			holder.videoName.setTextColor(context.getResources().getColor(
					R.color.content_text_color_white));
			holder.startdate.setTextColor(context.getResources().getColor(
					R.color.content_text_color_white));
			holder.enddate.setTextColor(context.getResources().getColor(
					R.color.content_text_color_white));
			holder.icon.setImageResource(R.drawable.ic_validate_time_nor);
		}
		holder.videoName.setText(df.getTitle().replaceAll("\"", ""));
		if (df.getIsEffectivetime()) {
			String available = df.getValidity();
			if ("00天00小时".equalsIgnoreCase(available)) {
				// 已经过期
				holder.startdate.setText(context.getString(R.string.start_time,
						df.getOdd_datetime_start()));
				holder.enddate.setText(context.getString(R.string.end_times,
						df.getOdd_datetime_end()) + "  "+
						context.getString(R.string.over_time));
				holder.startdate.setTextColor(context.getResources().getColor(
						R.color.oldlace));
				holder.enddate.setTextColor(context.getResources().getColor(
						R.color.oldlace));
			} else if (context.getString(R.string.forever_time).equals(
					available)) {
				// 永久
				holder.startdate.setText(context.getString(R.string.dead_time,
						available));
				holder.enddate.setVisibility(View.GONE);
			} else {
				// 正常
				holder.startdate.setText(context.getString(R.string.start_time,
						df.getOdd_datetime_start()));
				holder.enddate.setText(context.getString(R.string.end_times,
						df.getOdd_datetime_end()));
			}
		} else {
			// 未到生效时间
			String odd_datetime_end0 = String.valueOf(df.getOdd_datetime_end());
			String odd_datetime_end1 = odd_datetime_end0.substring(0, 1);
			int a = Integer.parseInt(odd_datetime_end1);
			if (a > 3) {
				holder.startdate.setText(context.getString(R.string.start_time,
						df.getOdd_datetime_start()));
				holder.enddate.setText(context.getString(R.string.end_times,
						context.getString(R.string.forever_time)));
				holder.startdate.setTextColor(context.getResources().getColor(
						R.color.oldlace));
			} else {
				holder.startdate.setText(context.getString(R.string.start_time,
						df.getOdd_datetime_start()));
				holder.enddate.setText(context.getString(R.string.end_times,
						df.getOdd_datetime_end()));
				holder.startdate.setTextColor(context.getResources().getColor(
						R.color.oldlace));
			}
		}
		return convertView;
	}

	/**
	 * 当前位置
	 * 
	 * @param pos
	 */
	public void setCurPosition(int position) {
		curPos = position;
		notifyDataSetChanged();
	}

	static class ViewHolder {
		TextView videoName;
		TextView startdate;
		TextView enddate;
		ImageView icon;
	}
}
