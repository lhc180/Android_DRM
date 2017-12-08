package cn.com.pyc.drm.adapter;

import java.util.List;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import cn.com.meeting.drm.R;
import cn.com.pyc.drm.bean.ScanHistory;
import cn.com.pyc.drm.common.App;
import cn.com.pyc.drm.utils.TimeUtil;

public class ScanHistoryAdapter extends BaseAdapter{


	private List<ScanHistory> listSH;
	
	public ScanHistoryAdapter(List<ScanHistory> listSH) {
		super();
		this.listSH = listSH;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		if(listSH!=null){
			return listSH.size();
		}else{
			return 0;
		}
	
	}
	public void setData(List<ScanHistory> listSH){
		this.listSH=listSH;
		notifyDataSetChanged();
	}
	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return listSH.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		View view;
		ViewHolder holder;
		ScanHistory sh=(ScanHistory) getItem(position);
		if(convertView!=null){
			view = convertView;
			holder = (ViewHolder) view.getTag();
		}else{
			view = View.inflate(App.getInstance(), R.layout.item_scanhistory, null);
			holder = new ViewHolder();
			holder.tv_meetingName = (TextView) view.findViewById(R.id.tv_meetingName);
			holder.tv_meetingtime = (TextView) view.findViewById(R.id.tv_meetingtime);
			view.setTag(holder);
		}
		
		holder.tv_meetingName.setText(sh.getMeetingName());
		holder.tv_meetingtime.setText(sh.getCreateTime());
		return view;
	}

	static class ViewHolder {
	
		TextView tv_meetingName;
		TextView tv_meetingtime;
	}

	
	
	
	
}
