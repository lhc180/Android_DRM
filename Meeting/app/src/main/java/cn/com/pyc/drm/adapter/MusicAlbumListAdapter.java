package cn.com.pyc.drm.adapter;

import java.util.List;

import cn.com.meeting.drm.R;
import cn.com.pyc.drm.model.db.bean.AlbumContent;
import cn.com.pyc.drm.model.right.ContentRight;
import cn.com.pyc.drm.utils.CommonUtil;
import cn.com.pyc.drm.utils.DRMLog;
import cn.com.pyc.drm.utils.MediaUtils;
import cn.com.pyc.drm.utils.TimeUtil;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 音乐专辑下文件列表
 * 
 * @author hudq
 * 
 */
public class MusicAlbumListAdapter extends BaseAdapter {

	private List<AlbumContent> infos;

	private LayoutInflater mInflater;

	private Context context;

	private List<ContentRight> contentRights;

	public MusicAlbumListAdapter(Context context, List<AlbumContent> infos,
			List<ContentRight> rights) {
		super();
		this.context = context;
		this.infos = infos;
		this.contentRights = rights;
		mInflater = LayoutInflater.from(context);

	}

	@Override
	public int getCount() {

		return infos.size();
	}

	@Override
	public AlbumContent getItem(int position) {

		return infos.get(position);
	}

	@Override
	public long getItemId(int position) {

		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		AlbumContent music = infos.get(position);
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.item_albumlist_adapter,
					null);
			holder.txtTitle = (TextView) convertView
					.findViewById(R.id.name_title);
			holder.startdate = (TextView) convertView
					.findViewById(R.id.start_date);
			holder.enddate = (TextView) convertView.findViewById(R.id.end_date);
			holder.valIv = (ImageView) convertView.findViewById(R.id.val_iv);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		if (position == MediaUtils.MUSIC_CURRENTPOS) {
			holder.txtTitle.setTextColor(context.getResources().getColor(
					R.color.title_bar_bg_color));
			holder.startdate.setTextColor(context.getResources().getColor(
					R.color.title_bar_bg_color));
			holder.enddate.setTextColor(context.getResources().getColor(
					R.color.title_bar_bg_color));
			holder.valIv.setImageResource(R.drawable.ic_validate_time_select);
		} else {
			holder.txtTitle.setTextColor(context.getResources().getColor(
					R.color.black_bb));
			holder.startdate.setTextColor(context.getResources().getColor(
					R.color.gray));
			holder.enddate.setTextColor(context.getResources().getColor(
					R.color.gray));
			holder.valIv.setImageResource(R.drawable.ic_validate_time_nor);
		}
		holder.txtTitle.setText(music.getName().replaceAll("\"", ""));
		List<ContentRight> rights = MediaUtils.getInstance().getMediaRight();
		String available_start = TimeUtil
				.getStringByLongMillsEveryminute(rights.get(position).availableTime);

		if (rights != null && rights.size() > 0) {
			ContentRight cr = rights.get(position);
			String odd_datetime_start = TimeUtil
					.getStringByLongMillsEveryminute(cr.odd_datetime_start);
			String odd_datetime_end = TimeUtil
					.getStringByLongMillsEveryminute(cr.odd_datetime_end);
			holder.enddate.setVisibility(View.VISIBLE);

			if (cr.IsEffectivetime) {
				String available = CommonUtil.getLeftChangeTime(context,
						cr.availableTime);
				if ("00天00小时".equals(available)) {
					// 已经过期
					holder.startdate.setText(context.getString(
							R.string.start_time, odd_datetime_start));
					holder.enddate.setText(context.getString(
							R.string.end_times, odd_datetime_end )+ "  "+
							context.getString(R.string.over_time));
					holder.startdate.setTextColor(context.getResources()
							.getColor(R.color.oldlace));
					holder.enddate.setTextColor(context.getResources()
							.getColor(R.color.oldlace));
				} else if (context.getString(R.string.forever_time).equals(
						available)) {
					// 永久
					// holder.startdate.setText(context.getString(R.string.start_time,
					// odd_datetime_start));
					// holder.enddate.setText(context.getString(R.string.end_times,available));
					holder.startdate.setText(context.getString(
							R.string.dead_time, available));
					holder.enddate.setVisibility(View.GONE);
				} else {
					// 未到生效时间
					String odd_datetime_end0 = String
							.valueOf(cr.odd_datetime_end);
					String odd_datetime_end1 = odd_datetime_end0
							.substring(0, 1);
					int a = Integer.parseInt(odd_datetime_end1);
					if (a > 3) {
						holder.startdate.setText(context.getString(
								R.string.start_time, odd_datetime_start));
						holder.enddate.setText(context.getString(
								R.string.end_times, R.string.forever_time));
					} else {
						holder.startdate.setText(context.getString(
								R.string.start_time, odd_datetime_start));
						holder.enddate.setText(context.getString(
								R.string.end_times, odd_datetime_end));
					}
				}
			} else {
				// 未到生效时间
				String odd_datetime_end0 = String.valueOf(cr.odd_datetime_end);
				String odd_datetime_end1 = odd_datetime_end0.substring(0, 1);
				int a = Integer.parseInt(odd_datetime_end1);
				if (a > 3) {
					holder.startdate.setText(context.getString(
							R.string.start_time, odd_datetime_start));
					holder.enddate.setText(context.getString(
							R.string.end_times, R.string.forever_time));
					holder.startdate.setTextColor(context.getResources().getColor(R.color.oldlace));
				} else {
					holder.startdate.setText(context.getString(
							R.string.start_time, odd_datetime_start));
					holder.enddate.setText(context.getString(
							R.string.end_times, odd_datetime_end));
					holder.startdate.setTextColor(context.getResources()
							.getColor(R.color.oldlace));
				}
			}
		}
		return convertView;
	}

	// 复用控件
	static class ViewHolder {
		TextView txtTitle;
		TextView startdate;
		TextView enddate;
		ImageView valIv;
	}

}
