package cn.com.pyc.drm.widget;

import java.util.List;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.com.meeting.drm.R;
import cn.com.pyc.drm.model.db.bean.Bookmark;
import cn.com.pyc.drm.ui.MuPDFFindAllNotes;
import cn.com.pyc.drm.utils.CommonUtil;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.baoyz.swipemenulistview.SwipeMenuListView.OnMenuItemClickListener;
import com.baoyz.swipemenulistview.SwipeMenuListView.OpenOrCloseListener;

/**   
 * @Description: (滑动的通用类) 
 * @author 李巷阳
 * @date 2016-6-16 下午5:27:59 
 * @version V1.0   
 */
public class SwipeMenuCreatorView {
	
	
	public static void open(final Context context,SwipeMenuListView lb_listview,final List<Bookmark> bookmarkList,final SwipeMenuCreatorViewCallBack smcv){
		
		// step 1. create a MenuCreator
		SwipeMenuCreator creator = new SwipeMenuCreator()
		{
			@Override
			public void create(SwipeMenu menu)
			{
				// create "delete" item
				SwipeMenuItem deleteItem = new SwipeMenuItem(context);
				// set item background
				deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9, 0x3F, 0x25)));
				// set item width
				deleteItem.setWidth(CommonUtil.dip2px(context, 90));
				// set a icon
				deleteItem.setIcon(R.drawable.ic_delete);
				// add to menu
				menu.addMenuItem(deleteItem);
			}
		};
		
		// set creator
				lb_listview.setMenuCreator(creator);

				// step 2. listener item click event
				lb_listview.setOnMenuItemClickListener(new OnMenuItemClickListener()
				{
					@Override
					public void onMenuItemClick(final int position, SwipeMenu menu, int index)
					{
	
						switch (index) {
						case 0:
							smcv.getOnClickListener(position);
						}
					}
				});
				lb_listview.setOnOpenOrCloseListener(new OpenOrCloseListener()
				{
					// isOpen =false 为打开，=true 为关闭
					@Override
					public void isOpen(boolean isOpen)
					{
					}
				});
	}
	
	
	public interface SwipeMenuCreatorViewCallBack{
		
		public void getOnClickListener(int position);
		
	}
	
	
	

}
