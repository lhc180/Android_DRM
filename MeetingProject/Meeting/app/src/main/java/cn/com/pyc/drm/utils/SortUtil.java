package cn.com.pyc.drm.utils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cn.com.pyc.drm.bean.MeetingBean;

/**
 * @author 李巷阳
 * @version V1.0
 * @Description: (用一句话描述该文件做什么)
 * @date 2017/3/1 15:45
 */
public class SortUtil {

    public static void MeetingSort(List<MeetingBean> mDataList, final TypeSort typeSort){
        Collections.sort(mDataList, new Comparator<MeetingBean>() {
            /*
             * int compare(Student o1, Student o2) 返回一个基本类型的整型， 返回负数表示：o1 小于o2，
             * 返回0 表示：o1和o2相等， 返回正数表示：o1大于o2。
             */
            public int compare(MeetingBean o1, MeetingBean o2) {
                long timestamp_1= TimeUtil.getDateToTimestamp(o1.getStartTime());
                long timestamp_2= TimeUtil.getDateToTimestamp(o2.getStartTime());
                if(TypeSort.Sequence==typeSort){
                    if (timestamp_1> timestamp_2) {
                        return 1;
                    }
                }else if(TypeSort.Reverse==typeSort){
                    if (timestamp_1< timestamp_2) {
                        return 1;
                    }
                }
                if (timestamp_1 == timestamp_2) {
                    return 0;
                }
                return -1;
            }
        });
    }
    public enum TypeSort {
        Sequence, Reverse
    }

}
