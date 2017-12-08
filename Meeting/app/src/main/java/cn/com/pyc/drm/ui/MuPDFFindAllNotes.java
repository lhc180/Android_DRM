package cn.com.pyc.drm.ui;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;
import cn.com.meeting.drm.R;
import cn.com.pyc.drm.adapter.Mupdf_Mark_Adapter;
import cn.com.pyc.drm.bean.event.MuPDFBookMarkUploadEvent;
import cn.com.pyc.drm.dialog.NormalDialog;
import cn.com.pyc.drm.dialog.NormalDialog.NormalDialogCallBack;
import cn.com.pyc.drm.model.db.bean.Bookmark;
import cn.com.pyc.drm.model.db.practice.BookmarkDAOImpl;
import cn.com.pyc.drm.utils.UIHelper;
import cn.com.pyc.drm.widget.HighlightImageView;
import cn.com.pyc.drm.widget.SwipeMenuCreatorView;
import cn.com.pyc.drm.widget.SwipeMenuCreatorView.SwipeMenuCreatorViewCallBack;

import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

import de.greenrobot.event.EventBus;

public class MuPDFFindAllNotes extends BaseActivity implements OnClickListener {

	@ViewInject(R.id.label_listview)
	private SwipeMenuListView lb_listview;

	@ViewInject(R.id.back)
	private HighlightImageView back;

	@ViewInject(R.id.findall_title)
	private TextView findall_title;

	@ViewInject(R.id.empty_layout)
	private View emptyView;

	@ViewInject(R.id.empty_text)
	private TextView emptyTextView;

	private String[] fieldValues;
	private String[] fields;
	private List<Bookmark> bookmarkList;
	private String asset_id;
	private Mupdf_Mark_Adapter mlaAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_findall_notes);
		init_Value();
		load_Data();
		init_View();
		initListener();
	}

	@Override
	protected void init_Value() {
		ViewUtils.inject(this);
		UIHelper.showTintStatusBar(this);
	}

	@Override
	protected void load_Data() {
		asset_id = getIntent().getStringExtra("asset_id");
		bookmarkList = getData();
		sort_list();
	}

	@Override
	protected void init_View() {

		initAdapter(getLayoutInflater(), bookmarkList);
		findall_title.setVisibility(View.VISIBLE);

		SwipeMenuCreatorView.open(MuPDFFindAllNotes.this, lb_listview, bookmarkList, new SwipeMenuCreatorViewCallBack() {
			public void getOnClickListener(final int position) {
				final Bookmark b = bookmarkList.get(position);
				int PageFew = Integer.parseInt(b.getPagefew());
				new NormalDialog().dialog(MuPDFFindAllNotes.this, MuPDFFindAllNotes.this.getString(R.string.delete_ScanHistory) + (PageFew + 1) + " 页备注?",null, new NormalDialogCallBack() {

					public void getConfirm(View v) {
						bookmarkList.remove(position);
						delBookmark(b);
					}

					@Override
					public void getCancel(View v) {
					}
				});
			}
		});

	}

	/**
	 * @Description: (初始化事件)
	 * @author 李巷阳
	 * @date 2016-6-16 下午5:24:30
	 */
	private void initListener() {
		back.setOnClickListener(this);
		lb_listview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Bookmark bookmark = bookmarkList.get(position);
				Intent intent = new Intent();
				Bundle bundle = new Bundle();
				bundle.putString("asset_id", bookmark.getContent_ids());
				bundle.putInt("page_few", Integer.parseInt(bookmark.getPagefew()));
				intent.putExtras(bundle);
				Bookmark booknotes = BookmarkDAOImpl.getInstance().findBookmarkById(bookmark.getId());

				setResult(RESULT_OK, intent);
				finish();
				UIHelper.finishOutAnim(MuPDFFindAllNotes.this);
			}
		});
	}

	@SuppressWarnings("unchecked")
	private List<Bookmark> getData() {
		if (asset_id != null) {
			fields = new String[] { "content_ids" };
			fieldValues = new String[] { asset_id };
			return bookmarkList = (List<Bookmark>) BookmarkDAOImpl.getInstance().findByQueryOrder(fields, fieldValues, Bookmark.class, "order by pagefew ASC");
		}
		return null;
	}

	private void delBookmark(final Bookmark b) {
		BookmarkDAOImpl.getInstance().DeleteBookMarkById(b.getId());
		mlaAdapter.notifyDataSetChanged();
		showToast(MuPDFFindAllNotes.this, "删除成功");
		EventBus.getDefault().post(new MuPDFBookMarkUploadEvent());
		Intent intent = new Intent();
		Bundle bundle = new Bundle();
		bundle.putString("de_finish", "finish");
		intent.putExtras(bundle);
		setResult(RESULT_OK, intent);

	}

	private void sort_list() {
		Collections.sort(bookmarkList, new Comparator<Bookmark>() {
			/*
			 * int compare(Student o1, Student o2) 返回一个基本类型的整型， 返回负数表示：o1 小于o2，
			 * 返回0 表示：o1和o2相等， 返回正数表示：o1大于o2。
			 */
			public int compare(Bookmark o1, Bookmark o2) {
				int page1 = Integer.parseInt(o1.getPagefew());
				int page2 = Integer.parseInt(o2.getPagefew());

				// 按照学生的年龄进行升序排列
				if (page1 > page2) {
					return 1;
				}
				if (page1 == page2) {
					return 0;
				}
				return -1;
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			UploadAndFinish();
			break;

		default:
			break;
		}
	}

	private void UploadAndFinish() {
		// 改变当前页的添加书签按钮的状态
		EventBus.getDefault().post(new MuPDFBookMarkUploadEvent());
		finish();
		UIHelper.finishOutAnim(this);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		// 改变当前页的添加书签按钮的状态
		UploadAndFinish();
	}

	private void initAdapter(LayoutInflater mInflater, List<Bookmark> bookmarkList) {
		if (bookmarkList == null || bookmarkList.isEmpty()) {
			// showToast(MuPDFFindAllNotes.this, "没有加载到数据");
			emptyTextView.setText("暂无备注信息");
			lb_listview.setEmptyView(emptyView);
			return;
		}
		if (mlaAdapter == null) {
			mlaAdapter = new Mupdf_Mark_Adapter(mInflater, bookmarkList);
			lb_listview.setAdapter(mlaAdapter);
		} else {
			mlaAdapter.setData(bookmarkList);
		}
	}
}
