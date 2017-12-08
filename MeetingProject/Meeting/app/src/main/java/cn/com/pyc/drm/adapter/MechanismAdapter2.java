package cn.com.pyc.drm.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import cn.com.meeting.drm.R;
import cn.com.pyc.drm.bean.MechanismBean;
import cn.com.pyc.drm.utils.SPUtils;

/**
 * @Description: (用一句话描述该文件做什么)
 * @author 李巷阳
 * @date 2016-10-27 下午3:43:40
 * @version V1.0
 */
public class MechanismAdapter2 extends Adapter<RecyclerView.ViewHolder> {

	private List<MechanismBean> liststr;

	private String PhoneNumber;

	private Context context;

	private LayoutInflater mLayoutInflater;

	/**
	 * @param liststr
	 * @param phoneNumber
	 * @param context
	 */
	public MechanismAdapter2( Context context,List<MechanismBean> liststr, String phoneNumber) {
		super();
		this.liststr = liststr;
		PhoneNumber = phoneNumber;
		this.context = context;
		mLayoutInflater = LayoutInflater.from(context);

	}

	/**
	 * @author 李巷阳
	 * @date 2016-10-27 下午3:44:26
	 */
	@Override
	public int getItemCount() {
		return liststr == null ? 0 : liststr.size();
	}

	/**
	 * @author 李巷阳
	 * @date 2016-10-27 下午3:44:26
	 */
	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		MyHolder mholder=(MyHolder)holder;
		MechanismBean mechanismbean=liststr.get(position);
		String Mechanism = (String) SPUtils.get(PhoneNumber, "");// 获取机构号码
		String AddressAndNameStr=mechanismbean.getServerAddress()+","+mechanismbean.getServerName()+","+mechanismbean.getSZUserName();
		if(Mechanism.equals(AddressAndNameStr)){
			mholder.iv_tick.setVisibility(View.VISIBLE);
		}else{
			mholder.iv_tick.setVisibility(View.INVISIBLE);
		}
		mholder.tv_mechanismname.setText(mechanismbean.getServerName());
	}

	/**
	 * @author 李巷阳
	 * @date 2016-10-27 下午3:44:26
	 */
	@Override
	public ViewHolder onCreateViewHolder(ViewGroup arg0, int arg1) {
		return new MyHolder(mLayoutInflater.inflate(R.layout.item_mechanism, null));
	}

	public void setData(List<MechanismBean> listM) {
		this.liststr = listM;
		notifyDataSetChanged();
	}

	private class MyHolder extends RecyclerView.ViewHolder {

		private ImageView iv_tick;
		private TextView tv_mechanismname;

		/**
		 */
		public MyHolder(View convertView) {
			super(convertView);
			if (null == convertView)
				throw new IllegalArgumentException("convertView require not null.");
			iv_tick = (ImageView) convertView.findViewById(R.id.iv_tick);
			tv_mechanismname = (TextView) convertView.findViewById(R.id.tv_mechanismname);

		}
	}

}
