package cn.com.pyc.drm.fragment;

import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import cn.com.meeting.drm.R;
import cn.com.pyc.drm.adapter.Mupdf_Mark_Adapter;
import cn.com.pyc.drm.bean.event.MuPDFBookMarkUploadEvent;
import cn.com.pyc.drm.model.db.bean.Bookmark;
import cn.com.pyc.drm.model.db.practice.BookmarkDAOImpl;
import cn.com.pyc.drm.ui.MuPDFMuHomeActivity;
import cn.com.pyc.drm.utils.ConvertToUtils;
import cn.com.pyc.drm.utils.DeviceUtils;
import cn.com.pyc.drm.utils.MediaUtils;
import cn.com.pyc.drm.utils.UIHelper;
import de.greenrobot.event.EventBus;

/**
 * 书签
 * 
 */
@SuppressLint("InflateParams") public class FragmentBookMark extends BaseMupdfFragment
{

	private ListView label_listview;
	private View emptyView;
	private List<Bookmark> bookmarkList;
	private Mupdf_Mark_Adapter mlaAdapter;
	private Dialog dialog;
	private String[] fieldValues;
	private String[] fields;

	protected void onCreateView(Bundle savedInstanceState)
	{
		super.onCreateView(savedInstanceState);
		setContentView(R.layout.fragment_pdf_bookmark);
		List<Bookmark> bookmarks = getData();
		label_listview = (ListView) findViewById(R.id.label_listview);
		emptyView = findViewById(R.id.empty_layout);
		initAdapter(getActivity().getLayoutInflater(), bookmarks);
		label_listview.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				Bookmark bookmark = bookmarkList.get(position);
				String pagefew = bookmark.getPagefew();
				int pageInt = ConvertToUtils.toInt(pagefew);
				Intent mIntent = new Intent();
				mIntent.putExtra("page", pageInt);
				getActivity().setResult(Activity.RESULT_OK, mIntent);
				getActivity().finish();
				UIHelper.finishOutAnim(getActivity());
			}
		});

		label_listview.setOnItemLongClickListener(new OnItemLongClickListener()
		{

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
			{
				final Bookmark bookmark = bookmarkList.get(position);
				return showEditSelectDialog(bookmark);
			}
		});
	}

	/**
	 * 
	 * @param bookmark
	 *            编辑的数据源对象
	 * @return
	 */
	private boolean showEditSelectDialog(final Bookmark bookmark)
	{
		View dialogView = getActivity().getLayoutInflater().inflate(R.layout.dialog_pdf_lable, null);
		View delete_bookmark = dialogView.findViewById(R.id.delete_bookmark);
		View empty_bookmark = dialogView.findViewById(R.id.clear_bookmark);
		View edit_bookmark = dialogView.findViewById(R.id.edit_bookmark);
		// 编辑书签
		edit_bookmark.setOnClickListener(new OnClickListener()
		{
			private AlertDialog dlg;

			 public void onClick(View v)
			{
				dismiss();
				// 自定义内容的对话框
				AlertDialog.Builder builder = new Builder(getActivity());
				View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_pdf_lable_edit, null);
				final EditText edit_content = (EditText) view.findViewById(R.id.edit_content);
				Button edit_btn_positive = (Button) view.findViewById(R.id.dialog_edit_btn_positive);
				Button edit_btn_negative = (Button) view.findViewById(R.id.dialog_edit_btn_negative);
				edit_content.setText(bookmark.getContent());

				edit_btn_positive.setOnClickListener(new OnClickListener()
				{
					public void onClick(View v)
					{
						String edit_text = edit_content.getText().toString();
						// Bookmark bm = new Bookmark();
						// bm.setId(bookmark.getId());
						// bm.setContent_ids(bookmark.getContent_ids());
						// bm.setTime(bookmark.getTime());
						// bm.setPagefew(bookmark.getPagefew());
						bookmark.setContent(edit_text);
						BookmarkDAOImpl.getInstance().update(bookmark);

						mlaAdapter.notifyDataSetChanged();
						// initAdapter(getActivity().getLayoutInflater(),
						// getData());
						if (dlg != null)
						{
							dlg.dismiss();
						}
					}
				});
				edit_btn_negative.setOnClickListener(new OnClickListener()
				{
					public void onClick(View v)
					{
						if (dlg != null)
						{
							dlg.dismiss();
						}
					}
				});
				builder.setView(view);
				dlg = builder.create();
				dialog.setCanceledOnTouchOutside(false);
				dialog.setCancelable(false);
				dlg.show();
			}
		});
		// 删除书签
		delete_bookmark.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{

				dismiss();

				//Bookmark
				BookmarkDAOImpl.getInstance().deleteEntityByIdAndName(bookmark.getId(), Bookmark.class.getSimpleName());
				// initAdapter(getActivity().getLayoutInflater(), getData());
				bookmarkList.remove(bookmark);
				mlaAdapter.notifyDataSetChanged();

				// 改变当前页的添加书签按钮的状态
				EventBus.getDefault().post(new MuPDFBookMarkUploadEvent());

				Toast.makeText(getApplicationContext(), "删除成功", Toast.LENGTH_SHORT).show();
			}
		});
		// 清空书签
		empty_bookmark.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{

				dismiss();

				BookmarkDAOImpl.getInstance().DeleteBookMark(fieldValues[0]);
				// initAdapter(getActivity().getLayoutInflater(), getData());
				bookmarkList.clear();
				mlaAdapter.notifyDataSetChanged();
				showEmptyView();
				// 改变当前页的添加书签按钮的状态
				EventBus.getDefault().post(new MuPDFBookMarkUploadEvent());

				Toast.makeText(getApplicationContext(), "已清空全部", Toast.LENGTH_SHORT).show();
			}
		});
		dialog = new Dialog(getActivity(), R.style.transparentFrameWindowStyle);
		dialog.setContentView(dialogView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		Window window = dialog.getWindow();
		window.setWindowAnimations(R.style.main_menu_animstyle);
		WindowManager.LayoutParams wl = window.getAttributes();
		wl.x = 0;
		wl.y = DeviceUtils.getScreenSize(getActivity()).y;
		wl.width = ViewGroup.LayoutParams.MATCH_PARENT;
		wl.height = ViewGroup.LayoutParams.WRAP_CONTENT;
		dialog.onWindowAttributesChanged(wl);
		dialog.setCanceledOnTouchOutside(true);
		dialog.show();
		return true;
	}

	private void dismiss()
	{
		if (dialog != null)
		{
			dialog.dismiss();
		}
	}

	private void initAdapter(LayoutInflater mInflater, List<Bookmark> bookmarkList)
	{
		if (bookmarkList == null || bookmarkList.isEmpty())
		{
			showEmptyView();
			return;
		}
		if (mlaAdapter == null)
		{
			mlaAdapter = new Mupdf_Mark_Adapter(mInflater, bookmarkList);
			label_listview.setAdapter(mlaAdapter);
		} else
		{
			mlaAdapter.setData(bookmarkList);
		}
	}

	@SuppressWarnings("unchecked")
	private List<Bookmark> getData()
	{
		fields = new String[] { "content_ids" };
		fieldValues = new String[] { MediaUtils.getInstance().getPdfmediaList().get(MediaUtils.MPDF_CURRENTPOS).getAsset_id() };
		return bookmarkList = (List<Bookmark>) BookmarkDAOImpl.getInstance().findByQueryOrder(fields, fieldValues, Bookmark.class,"order by time desc");
	}

	private void showEmptyView()
	{
		MuPDFMuHomeActivity.setEmptyViews(label_listview, emptyView, getString(R.string.have_not_add_bookmark));
	}

}
