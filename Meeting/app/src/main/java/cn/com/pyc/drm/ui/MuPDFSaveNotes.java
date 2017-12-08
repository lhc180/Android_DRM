package cn.com.pyc.drm.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import cn.com.meeting.drm.R;
import cn.com.pyc.drm.bean.event.MuPDFBookMarkUploadEvent;
import cn.com.pyc.drm.dialog.NormalDialog;
import cn.com.pyc.drm.dialog.NormalDialog.NormalDialogCallBack;
import cn.com.pyc.drm.model.db.bean.Bookmark;
import cn.com.pyc.drm.model.db.practice.BookmarkDAOImpl;
import cn.com.pyc.drm.utils.OpenPageUtil;
import cn.com.pyc.drm.utils.TimeUtil;
import cn.com.pyc.drm.utils.UIHelper;
import cn.com.pyc.drm.widget.HighlightImageView;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

import de.greenrobot.event.EventBus;

/**
 * 保存备注： 返回，查看所有，如果有备注内容都保存
 */
public class MuPDFSaveNotes extends BaseActivity implements OnClickListener {

	@ViewInject(R.id.findall_notes)
	private TextView fi_notes;

	@ViewInject(R.id.save_notes)
	private TextView sa_notes;

	@ViewInject(R.id.edit_content)
	private EditText ed_content;

	@ViewInject(R.id.tv_notespages)
	private TextView tv_pages;

	@ViewInject(R.id.back)
	private HighlightImageView back;

	@ViewInject(R.id.tv_length_txt)
	private TextView tv_remark_size;

	@ViewInject(R.id.view)
	private View view;

	private int page_few;

	private String asset_id;

	private Bookmark bookmark;

	private String bookmark_ID;

	private String open_new_activity = "open_new_activity";

	private String back_activity = "back_activity";

	private String before_content;

	private String after_content;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_save_notes);
		init_Value();
		load_Data();
		init_View();
		setListener();

	}

	@Override
	protected void init_Value() {
		ViewUtils.inject(this);
		UIHelper.showTintStatusBar(this);
		EventBus.getDefault().register(this);// 注册事件

		page_few = getIntent().getIntExtra("page_few", 0);
		asset_id = getIntent().getStringExtra("asset_id");

	}

	@Override
	protected void load_Data() {
		bookmark_ID = asset_id + page_few;
		bookmark = BookmarkDAOImpl.getInstance().findBookmarkById(bookmark_ID);
	}

	@Override
	protected void init_View() {
		fi_notes.setVisibility(View.VISIBLE);
		sa_notes.setVisibility(View.VISIBLE);

		tv_pages.setText("编写第" + (page_few + 1) + "页备注");

		if (bookmark == null)
			return;
		before_content = bookmark.getContent();
		if (before_content == null)
			return;

		ed_content.setText(before_content);
		ed_content.setSelection(before_content.length());

	}

	/**
	 * @Description: (事件的注册)
	 * @author 李巷阳
	 * @date 2016-6-16 下午3:23:07
	 */
	private void setListener() {
		fi_notes.setOnClickListener(this);
		sa_notes.setOnClickListener(this);
		back.setOnClickListener(this);
		ed_content.addTextChangedListener(change);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.findall_notes:
			show_dialog(open_new_activity);
			break;
		case R.id.back:
			show_dialog(back_activity);
			break;
		case R.id.save_notes:
			saveRemark(getContent());
			break;
		default:
			break;
		}
	}

	private void show_dialog(final String type) {
		load_Data();
		if (bookmark !=null)
		before_content = bookmark.getContent();
		
		String ed_content_str = ed_content.getText().toString().trim();
		after_content = ed_content_str == null ? "" : ed_content_str;
		before_content = before_content == null ? "" : before_content.trim();

		if (!after_content.equals(before_content)) {
			new NormalDialog().dialog(MuPDFSaveNotes.this, "您确定更新当前备注内容吗？",null, new NormalDialogCallBack() {
				@Override
				public void getConfirm(View v) {
					if (open_new_activity.equals(type)) {
						if (saveRemark(after_content)) {
							OpenPageUtil.openMuPDFFindAllNotes(MuPDFSaveNotes.this, asset_id);
						}
					} else if (back_activity.equals(type)) {
						if (saveRemark(after_content)) {
							UploadAndFinish();
						}
					}
				}

				@Override
				public void getCancel(View v) {
					if (open_new_activity.equals(type)) {
						OpenPageUtil.openMuPDFFindAllNotes(MuPDFSaveNotes.this, asset_id);
					} else if (back_activity.equals(type)) {
						UploadAndFinish();
					}
				}
			});

		}else{
			if(open_new_activity.equals(type)){
				OpenPageUtil.openMuPDFFindAllNotes(MuPDFSaveNotes.this, asset_id);
			}else if(back_activity.equals(type)){
				UploadAndFinish();
			}
		
		}

	}





	private String getContent() {
		String content = ed_content.getText().toString().trim();
		return content;
	}

	private boolean saveRemark(String content) {
		if (content.length() > 500) {
			showToast("亲，每条备注正文上线为500字。");
			return false;
		}
		
		if (getContent() == null || "".equals(getContent())) {
			showToast(MuPDFSaveNotes.this, "备注不可为空。");
			return false;
		}
		if (getContent() != null && !"".equals(getContent())) {
			String currentdate = TimeUtil.getCurrentTime();
			Bookmark bm = new Bookmark();
			bm.setId(bookmark_ID);
			bm.setContent_ids(asset_id);
			bm.setContent(content);
			bm.setTime(currentdate);
			bm.setPagefew(page_few + "");
			Bookmark notes = BookmarkDAOImpl.getInstance().findBookmarkById(bookmark_ID);
			if (notes == null) {
				BookmarkDAOImpl.getInstance().save(bm);
			} else {
				BookmarkDAOImpl.getInstance().update(bm);
			}
			showToast(MuPDFSaveNotes.this, "备注已保存");
		} else {
			showToast(MuPDFSaveNotes.this, "保存失败，备注不可为空~");
		}
		return true;
	}

	// /**
	// * 当查询全部备注界面点击事件后，则关闭本界面。
	// *
	// * @param event
	// */
	// public void onEventMainThread(MuPDFBookMarkJPageEvent event) {
	// UploadAndFinish();
	// }

	/**   
	* @Description: (列表界面关闭后，则更新此添加界面内容) 
	* @author 李巷阳
	* @date 2016-6-16 下午4:36:34 
	*/
	public void onEventMainThread(MuPDFBookMarkUploadEvent event) {
		
		find_bookmark_upload();
	}




	// 更新和删除
	private void UploadAndFinish() {
		// 改变当前页的添加书签按钮的状态
		EventBus.getDefault().post(new MuPDFBookMarkUploadEvent());
		finish();
		UIHelper.finishOutAnim(this);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (resultCode) { // resultCode为回传的标记，我在B中回传的是RESULT_OK
		case RESULT_OK:
			Bundle b = data.getExtras();
			String is_finish=b.getString("de_finish");
			if(is_finish!=null && "finish".equals(is_finish))
			{
				finish();
			}else{
				asset_id = b.getString("asset_id");
				page_few = b.getInt("page_few");
				bookmark_ID = asset_id + page_few;
				tv_pages.setText("编写第" + (page_few + 1) + "页备注");
				find_bookmark_upload();
			}
			break;
		default:
			break;
		}
	}

	
	@Override
	public void onBackPressed() {
		show_dialog(back_activity);
	}
	
	
	private void find_bookmark_upload() {
		load_Data();
		if (bookmark == null)
			return;
		before_content = bookmark.getContent();
		if (before_content == null)
			return;
		ed_content.setText(before_content);
		ed_content.setSelection(before_content.length());
	}

	private TextWatcher change = new TextWatcher() {
		@Override
		public void afterTextChanged(Editable s) {
			if (s.length() > 0) {
				tv_remark_size.setVisibility(View.VISIBLE);
				tv_remark_size.setText(String.valueOf(s.length()) + MuPDFSaveNotes.this.getResources().getString(R.string.length_txt));
			} else {
				tv_remark_size.setVisibility(View.GONE);
			}
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
		}
	};



}
