package cn.com.pyc.drm.adapter;

import java.util.List;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import cn.com.meeting.drm.R;
import cn.com.pyc.drm.bean.ReturnMeetingRecordDataBean.databean;

/**
 * @Description: (用一句话描述该文件做什么)
 * @author 李巷阳
 * @date 2016-10-25 下午5:07:59
 * @version V1.0
 */
public class MymeetingHistoryAdapter extends Adapter<RecyclerView.ViewHolder> {
	private LayoutInflater mLayoutInflater;
	private Context context;
	private List<databean> listSH;

	/**
	 * @param context
	 * @param listSH
	 */
	public MymeetingHistoryAdapter(Context context, List<databean> listSH) {
		super();
		this.context = context;
		this.listSH = listSH;
		mLayoutInflater = LayoutInflater.from(context);
	}

	/**
	 * @author 李巷阳
	 * @date 2016-10-25 下午5:08:16
	 */
	@Override
	public int getItemCount() {
		return listSH == null ? 0 : listSH.size();
	}

	/**
	 * @author 李巷阳
	 * @date 2016-10-25 下午5:08:16
	 */
	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		databean sh = (databean) listSH.get(position);
		((MyHolder) holder).tv_meetingName.setText(sh.getMeetingName());
		((MyHolder) holder).tv_meetingtime.setText(sh.getStartTime());
	}

	/**
	 * @author 李巷阳
	 * @date 2016-10-25 下午5:08:16
	 */
	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new MyHolder(mLayoutInflater.inflate(R.layout.item_mymeeting_record, parent, false));
	}

	// 设定Holder
	private class MyHolder extends RecyclerView.ViewHolder {
		private TextView tv_meetingName;
		private TextView tv_meetingtime;

		/**
		 * @param arg0
		 */
		public MyHolder(View convertView) {
			super(convertView);
			if (null == convertView)
				throw new IllegalArgumentException("convertView require not null.");
			tv_meetingName = (TextView) convertView.findViewById(R.id.tv_meetingName);
			tv_meetingtime = (TextView) convertView.findViewById(R.id.tv_meetingtime);
		}
	}
	
	public void setData(List<databean> listSH){
		this.listSH=listSH;
		notifyDataSetChanged();
	}
}
