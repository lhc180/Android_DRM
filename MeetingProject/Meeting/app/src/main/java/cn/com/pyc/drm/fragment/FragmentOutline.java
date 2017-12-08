package cn.com.pyc.drm.fragment;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import cn.com.meeting.drm.R;
import cn.com.pyc.drm.adapter.Mupdf_Outline_Adapter;
import cn.com.pyc.drm.ui.MuPDFMuHomeActivity;
import cn.com.pyc.drm.utils.DRMUtil;
import cn.com.pyc.drm.utils.UIHelper;

import com.artifex.mupdfdemo.OutlineItem;

/**
 * 目录
 * 
 */
public class FragmentOutline extends BaseMupdfFragment
{

	// public static final String INTENT_INT_INDEX = "intent_int_index";
	public static final String KEY_OUTLINES = "key_outlines";
	private List<OutlineItem> outlineList;

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Bundle bundle = this.getArguments();
		if (bundle != null)
			this.outlineList = (List<OutlineItem>) bundle.getSerializable(KEY_OUTLINES);
	}

	protected void onCreateView(Bundle savedInstanceState)
	{
		super.onCreateView(savedInstanceState);
		setContentView(R.layout.fragment_pdf_outline);
		ListView lv = (ListView) findViewById(R.id.outline_listview);
		View emptyView = findViewById(R.id.empty_layout);
		if (null == outlineList || outlineList.isEmpty())
		{
			MuPDFMuHomeActivity.setEmptyViews(lv, emptyView, getString(R.string.have_not_outline));
			return;
		}
		Mupdf_Outline_Adapter lineAdapter = new Mupdf_Outline_Adapter(getActivity(), outlineList);
		lv.setAdapter(lineAdapter);
		
		if (DRMUtil.OutlinePosition != 0)
		{
			lv.setSelection(DRMUtil.OutlinePosition - 1);
		} else
		{
			lv.setSelection(DRMUtil.OutlinePosition);
		}
		lv.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				Intent mIntent = new Intent();
				mIntent.putExtra("page", outlineList.get(position).page);
				getActivity().setResult(Activity.RESULT_OK, mIntent);
				getActivity().finish();
				UIHelper.finishOutAnim(getActivity());
			}
		});
	}

	public void onDestroyView()
	{
		super.onDestroyView();
	}
}
