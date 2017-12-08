package cn.com.pyc.drm.adapter;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import cn.com.meeting.drm.R;
import cn.com.pyc.drm.bean.MechanismBean;
import cn.com.pyc.drm.utils.SPUtils;

/**
 * @author 李巷阳
 * @version V1.0
 * @Description: (机构的adapter适配器)
 * @date 2017/1/19 15:13
 */
public class JGAdapter extends Rv_SimpleAdapter<MechanismBean> {
    private String PhoneNumber;
    /**
     * 构造函数
     */
    public JGAdapter(Context context, List<MechanismBean> datas) {
        super(context,R.layout.item_mechanism, datas);
    }
    /**
     * 构造函数
     */
    public JGAdapter(Context context, List<MechanismBean> datas, String phoneNumber) {
        super(context,R.layout.item_mechanism, datas);
        PhoneNumber = phoneNumber;
    }
    @Override
    protected void convert(Rv_BaseViewHolder viewHoder, MechanismBean item) {
       ImageView iv_tick = viewHoder.getImageView(R.id.iv_tick);
       TextView tv_mechanismname= viewHoder.getTextView(R.id.tv_mechanismname);
        if(PhoneNumber!=null){
            String Mechanism = (String) SPUtils.get(PhoneNumber, "");// 获取机构号码
            String AddressAndNameStr=item.getServerAddress()+","+item.getServerName()+","+item.getSZUserName();
            if(Mechanism.equals(AddressAndNameStr)){
                iv_tick.setVisibility(View.VISIBLE);
            }else{
                iv_tick.setVisibility(View.INVISIBLE);
            }
        }
        tv_mechanismname.setText(item.getServerName());
    }
}
