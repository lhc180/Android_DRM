package cn.com.pyc.drm.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import cn.com.meeting.drm.R;
import cn.com.pyc.drm.adapter.Mupdf_Outline_Adapter;
import cn.com.pyc.drm.bean.event.MuPDFBookMarkUploadEvent;
import cn.com.pyc.drm.utils.CommonUtil;
import cn.com.pyc.drm.utils.DRMUtil;
import cn.com.pyc.drm.utils.UIHelper;
import cn.com.pyc.drm.widget.HighlightImageView;

import com.artifex.mupdfdemo.OutlineItem;

import de.greenrobot.event.EventBus;

public class MuPDFOutlineActivity extends BaseActivity implements OnClickListener {

	private List<OutlineItem> outlineList;
	private HighlightImageView back;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		CommonUtil.fullScreen(true, this);
		init_Value();
		init_View();
	}

	@Override
	protected void init_View() {
		// TODO Auto-generated method stub
		setContentView(R.layout.fragment_pdf_outline);
		ListView lv = (ListView) findViewById(R.id.outline_listview);
		View emptyView = findViewById(R.id.empty_layout);
		back = (HighlightImageView) findViewById(R.id.back);
		
		
		back.setOnClickListener(this);
		
		if (null == outlineList || outlineList.isEmpty()) {
			showToast(R.string.have_not_outline);
			return;
		}
		Mupdf_Outline_Adapter lineAdapter = new Mupdf_Outline_Adapter(this,
				outlineList);
		lv.setAdapter(lineAdapter);
		if (DRMUtil.OutlinePosition != 0) {
			lv.setSelection(DRMUtil.OutlinePosition - 1);
		} else {
			lv.setSelection(DRMUtil.OutlinePosition);
		}

		
		lv.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				Intent mIntent = new Intent();
				mIntent.putExtra("page", outlineList.get(position).page);
				setResult(Activity.RESULT_OK, mIntent);
				finish();
				UIHelper.finishOutAnim(MuPDFOutlineActivity.this);
			}
		});
	}

	@Override
	protected void load_Data() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void init_Value() {
		outlineList = (ArrayList<OutlineItem>) getIntent()
				.getSerializableExtra("outline");

	}
	
	private void UploadAndFinish() {
		// 改变当前页的添加书签按钮的状态
		finish();
		UIHelper.finishOutAnim(this);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			UploadAndFinish() ;
			
			break;

		default:
			break;
		}
	}
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		// 改变当前页的添加书签按钮的状态
		UploadAndFinish() ;
		
	}
}
