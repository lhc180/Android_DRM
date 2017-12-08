package cn.com.pyc.drm.adapter;

import android.content.Context;
import android.widget.TextView;

import java.util.List;

import cn.com.meeting.drm.R;
import cn.com.pyc.drm.bean.MeetingBean;

/**
 * @author 李巷阳
 * @version V1.0
 * @Description: (我的会议的adapter适配器)
 * @date 2017/1/18 11:56
 */
public class MHAdapter extends Rv_SimpleAdapter<MeetingBean> {


    /**
     * 构造函数
     *
     * @param context     上下文
     * @param datas       数据源
     */
    public MHAdapter(Context context, List<MeetingBean> datas) {
        super(context, R.layout.item_mymeeting_record, datas);
    }

    @Override
    protected void convert(Rv_BaseViewHolder viewHoder, MeetingBean item) {
        TextView tv_meetingName = (TextView) viewHoder.getTextView(R.id.tv_meetingName);
        TextView tv_meetingtime = (TextView) viewHoder.getView(R.id.tv_meetingtime);
        tv_meetingName.setText(item.getMeetingName());
        tv_meetingtime.setText(item.getStartTime());
    }
}
