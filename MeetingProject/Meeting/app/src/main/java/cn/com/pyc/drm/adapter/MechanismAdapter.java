package cn.com.pyc.drm.adapter;

import java.util.List;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import cn.com.meeting.drm.R;
import cn.com.pyc.drm.bean.MechanismBean;
import cn.com.pyc.drm.common.App;
import cn.com.pyc.drm.utils.SPUtils;

/**
 * @Description: (用一句话描述该文件做什么)
 * @author 李巷阳
 * @date 2016-8-15 下午6:05:49
 * @version V1.0
 */
public class MechanismAdapter extends BaseAdapter {

	private List<MechanismBean> liststr;

	private String PhoneNumber;
	/**
	 * @param liststr
	 */
	public MechanismAdapter(List<MechanismBean> liststr,String PhoneNumber) {
		this.liststr = liststr;
		this.PhoneNumber = PhoneNumber;
	}

	/**
	 * @Description: (用一句话描述该文件做什么)
	 * @author 李巷阳
	 * @date 2016-8-15 下午6:06:18
	 */
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		if(liststr!=null){
			return liststr.size();
		}else{
			return 0;
		}
	
	}
	public void setData(List<MechanismBean> listM){
		this.liststr=listM;
		notifyDataSetChanged();
	}
	/**
	 * @Description: (用一句话描述该文件做什么)
	 * @author 李巷阳
	 * @date 2016-8-15 下午6:06:18
	 */
	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return liststr.get(position);
	}

	/**
	 * @Description: (用一句话描述该文件做什么)
	 * @author 李巷阳
	 * @date 2016-8-15 下午6:06:18
	 */
	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	/**
	 * @Description: (用一句话描述该文件做什么)
	 * @author 李巷阳
	 * @date 2016-8-15 下午6:06:18
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		// TODO Auto-generated method stub
		View view;
		ViewHolder holder;
		MechanismBean sh=(MechanismBean) getItem(position);
		if(convertView!=null){
			view = convertView;
			holder = (ViewHolder) view.getTag();
		}else{
			view = View.inflate(App.getInstance(), R.layout.item_mechanism, null);
			holder = new ViewHolder();
			holder.tv_meetingName = (TextView) view.findViewById(R.id.tv_meetingName);
			holder.iv_tick = (ImageView) view.findViewById(R.id.iv_tick);

			view.findViewById(R.id.iv_tick);
			view.setTag(holder);
		}
		String Mechanism = (String) SPUtils.get(PhoneNumber, "");// 获取机构号码
		String AddressAndNameStr=sh.getServerAddress()+","+sh.getServerName()+","+sh.getSZUserName();
		if(Mechanism.equals(AddressAndNameStr)){
			holder.iv_tick.setVisibility(View.VISIBLE);
		}else{
			holder.iv_tick.setVisibility(View.INVISIBLE);
		}
		holder.tv_meetingName.setText(sh.getServerName());
		return view;
	
	}
	private class ViewHolder {
		
		TextView tv_meetingName;
		ImageView iv_tick;
	}

}
