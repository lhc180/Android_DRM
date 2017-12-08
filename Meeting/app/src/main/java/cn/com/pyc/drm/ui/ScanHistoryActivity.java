package cn.com.pyc.drm.ui;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.com.meeting.drm.R;
import cn.com.pyc.drm.adapter.ScanHistoryAdapter;
import cn.com.pyc.drm.bean.ScanHistory;
import cn.com.pyc.drm.bean.UpdateVersionBean;
import cn.com.pyc.drm.bean.event.SettingActivityCloseEvent;
import cn.com.pyc.drm.common.Constant;
import cn.com.pyc.drm.model.MeetingNameModel;
import cn.com.pyc.drm.model.MeetingNameModel.MeetingName;
import cn.com.pyc.drm.utils.CommonUtil;
import cn.com.pyc.drm.utils.DRMLog;
import cn.com.pyc.drm.utils.DRMUtil;
import cn.com.pyc.drm.utils.DeviceUtils;
import cn.com.pyc.drm.utils.OpenPageUtil;
import cn.com.pyc.drm.utils.SPUtils;
import cn.com.pyc.drm.utils.StringUtil;
import cn.com.pyc.drm.utils.UIHelper;
import cn.com.pyc.drm.utils.ViewUtil;
import cn.com.pyc.drm.utils.manager.ActicityManager;
import cn.com.pyc.drm.utils.manager.RequestHttpManager;
import cn.com.pyc.drm.utils.manager.ScanHistoryDBManager;
import cn.com.pyc.drm.utils.manager.XMLParserVersionManager;
import cn.com.pyc.drm.utils.manager.XMLParserVersionManager.OnCheckResultListener;
import cn.com.pyc.drm.widget.HighlightImageView;

import com.alibaba.fastjson.JSON;
import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.baoyz.swipemenulistview.SwipeMenuListView.OnMenuItemClickListener;
import com.baoyz.swipemenulistview.SwipeMenuListView.OpenOrCloseListener;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.view.annotation.ViewInject;

import de.greenrobot.event.EventBus;

public class ScanHistoryActivity extends BaseActivity {

	@ViewInject(R.id.lv_history)
	private SwipeMenuListView mlistview;

	@ViewInject(R.id.title_tv)
	private TextView tv_teb; // 标题

	@ViewInject(R.id.back_img)
	private HighlightImageView back_img; // 返回按钮

	@ViewInject(R.id.empty_layout)
	// R.id.empty_text
	private View emptyView;

	@ViewInject(R.id.empty_text)
	private TextView empty_text;

	private List<ScanHistory> listSH;

	private ScanHistoryAdapter SHAdapter;

	private boolean isStop = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scanhistory);
		init_Value();
		init_View();
		load_Data();
		checkVersion();
	}

	@Override
	protected void init_View() {
		tv_teb.setText("历史会议记录");

		mlistview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				open(position);
			}
		});

		back_img.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finishUI();
			}
		});

		// step 1. create a MenuCreator
		SwipeMenuCreator creator = new SwipeMenuCreator() {

			@Override
			public void create(SwipeMenu menu) {
				// create "delete" item
				SwipeMenuItem deleteItem = new SwipeMenuItem(ScanHistoryActivity.this);
				// set item background
				deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9, 0x3F, 0x25)));
				// set item width
				deleteItem.setWidth(CommonUtil.dip2px(ScanHistoryActivity.this, 90));
				// set a icon
				deleteItem.setIcon(R.drawable.ic_delete);
				// add to menu
				menu.addMenuItem(deleteItem);
			}
		};

		// set creator
		mlistview.setMenuCreator(creator);

		// step 2. listener item click event
		mlistview.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public void onMenuItemClick(final int position, SwipeMenu menu, int index) {
				final ScanHistory sh = listSH.get(position);
				switch (index) {
				case 0:
					AlertDialog.Builder builder = new Builder(ScanHistoryActivity.this);
					View view = View.inflate(getApplicationContext(), R.layout.dialog_isdelete, null);
					LinearLayout confirm = (LinearLayout) view.findViewById(R.id.confirm);// 确定
					LinearLayout cancel = (LinearLayout) view.findViewById(R.id.cancel);// 取消
					TextView tView = (TextView) view.findViewById(R.id.Cancellation);
					final CheckBox check_isDeleteFile = (CheckBox) view.findViewById(R.id.check_isDeleteFile);
					tView.setText(getString(R.string.delete_ScanHistory) + sh.getMeetingName() + " ?");
					builder.setView(view);
					final AlertDialog clearDlg = builder.create();
					clearDlg.show();
					confirm.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							if (clearDlg != null && clearDlg.isShowing()) {
								clearDlg.dismiss();
							}
							clearing(sh, check_isDeleteFile);
						}
					});
					cancel.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							if (clearDlg != null)
								clearDlg.dismiss();
						}
					});
					break;
				}
			}
		});

		mlistview.setOnOpenOrCloseListener(new OpenOrCloseListener() {
			// isOpen =false 为打开，=true 为关闭
			@Override
			public void isOpen(boolean isOpen) {
			}
		});
	}

	private void finishUI() {
		UIHelper.finishActivity(this);
		ActicityManager.getInstance().remove(this);
	}

	protected void clearing(ScanHistory sh, CheckBox check_isDeleteFile) {
		boolean isDelete = ScanHistoryDBManager.Builder(ScanHistoryActivity.this).deleteByMeetingID(sh.getMeetingId());
		if (isDelete) {
			listSH = ScanHistoryDBManager.Builder(this).findAllScanHistory();
			setAdapter(listSH);
			tos.show(getApplicationContext(), 1, "删除成功");
			if (listSH == null || listSH.isEmpty()) {
				empty_text.setText("当前历史会议记录为空");
				emptyView.setVisibility(View.VISIBLE);
			}
		} else {
			tos.show(getApplicationContext(), 0, "删除失败");
		}
	}

	protected void open(int position) {
		if (!CommonUtil.isNetConnect(ScanHistoryActivity.this)) {
			tos.showBusy(getApplicationContext(), "亲，网络连接不可用。");
			return;
		}
		// showBgLoading("登录中...");
		// Constant.LoginConfig.type = DrmPat.LOGIN_SCANING;
		SPUtils.remove(DRMUtil.KEY_LOGIN_TOKEN);
		SPUtils.remove(DRMUtil.KEY_MEETINGID);
		ScanHistory sh = listSH.get(position);
		String[] hostAndPortString = StringUtil.getHostAndPortByResult(sh.getScanDataSource());
		if (hostAndPortString != null) {
			// 保存主机名。eg： video.suizhi.net
			SPUtils.save(DRMUtil.SCAN_FOR_HOST, hostAndPortString[0]);
			// 保存端口号。eg：8657
			SPUtils.save(DRMUtil.SCAN_FOR_PORT, hostAndPortString[1]);
		} else {
			hideBgLoading();
			showToast("扫描地址有误...");
		}
		// 保存用户名
		SPUtils.save(DRMUtil.KEY_VISIT_NAME, sh.getUsername());
		SPUtils.save(DRMUtil.KEY_MEETINGID, sh.getMeetingId());
		getMeetingName(sh.getUsername(), sh.getMeetingId(), sh.getMeetingType(), sh.getScanDataSource());
	}

	private void getMeetingName(final String username, final String MeetingId, final String MeetingType, final String DataSource) {
		RequestHttpManager.init().postData(DRMUtil.getMeetingNameUrl(), username, MeetingId, new RequestCallBack<String>() {
			public void onSuccess(ResponseInfo<String> arg0) {
				DRMLog.d("history", "success: " + arg0.result);
				MeetingNameModel model = JSON.parseObject(arg0.result, MeetingNameModel.class);
				if (model.isSuccess()) {
					MeetingName o = model.getData();
					if (o == null) {
						hideBgLoading();
						showToast("请求服务器失败");
						return;
					}
					if ("true".equals(o.getVerify()) || "false".equals(o.getVerify())) {
						hideBgLoading();
						showToast("对不起,您的二维码已经过期，请扫描最新二维码。");
						return;
					}
					ScanHistory sh = new ScanHistory();
					sh.setMeetingId(MeetingId);
					sh.setScanDataSource(DataSource);
					sh.setMeetingType(MeetingType);
					sh.setUsername(username);
					sh.setIsverifys(o.getVerify());
					sh.setVerify_url(o.getVerifyurl() + "&DevicesId=" + DeviceUtils.getIMEI(ScanHistoryActivity.this));
					sh.setVote_title(o.getTitle());
					sh.setMeetingName(o.getName());
					sh.setVote_url(o.getUrl());
					sh.setClient_url(o.getClient_url());
					sh.setCreateTime(o.getCreateTime());

					// verify判断是否需要第三方验证。
					ScanHistoryDBManager.Builder(ScanHistoryActivity.this).saveScanHistory(sh);
					if (!Constant.no_verify.equals(o.getVerify())) {
						Intent intent = new Intent(ScanHistoryActivity.this, ScanLoginVerificationActivity.class);
						Bundle bundle = new Bundle();
						bundle.putSerializable("MeetingDetails", sh);
						intent.putExtras(bundle);
						hideBgLoading();
						startActivityForResult(intent, Constant.SCANHISTORY_CODE);
					} else {
						SPUtils.save(DRMUtil.VOTE_URL, o.getUrl());
						SPUtils.save(DRMUtil.KEY_PHONE_NUMBER, "");
						OpenPageUtil.openDownloadMainByScaning2(ScanHistoryActivity.this, sh);
						ScanHistoryActivity.this.setResult(Activity.RESULT_OK, null);
						EventBus.getDefault().post(new SettingActivityCloseEvent());
						hideBgLoading();
						finish();
					}
				} else {
					showToast("服务器异常");
					hideBgLoading();
				}
			}

			@Override
			public void onFailure(HttpException arg0, String arg1) {
				showToast("连接服务器失败");
				hideBgLoading();
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {

		case Constant.SCANHISTORY_CODE: {
			// 扫码登录获取产品信息
			if (resultCode == Activity.RESULT_OK) {
				ScanHistoryActivity.this.setResult(Activity.RESULT_OK, null);
				ScanHistoryActivity.this.finish();
			}
		}
			break;
		}
	}

	private void setAdapter(List<ScanHistory> listSH) {
		if (SHAdapter == null) {
			SHAdapter = new ScanHistoryAdapter(listSH);
			mlistview.setAdapter(SHAdapter);
		} else {
			SHAdapter.setData(listSH);
		}
	}

	@Override
	protected void load_Data() {
		listSH = ScanHistoryDBManager.Builder(this).findAllScanHistory();
		if (listSH == null || listSH.isEmpty()) {
			empty_text.setText("当前历史会议记录为空");
			emptyView.setVisibility(View.VISIBLE);
			return;
		}
		setAdapter(listSH);
	}

	@Override
	protected void init_Value() {
		ViewUtils.inject(this);
		ActicityManager.getInstance().add(this);
		UIHelper.showTintStatusBar(this);
	}

	/**
	 * 
	 * @Description: (检测版本号是否更新)
	 * @author 李巷阳
	 * @date 2016-9-9 下午3:53:43
	 */
	private void checkVersion() {
		if (!hasNet())
			return;

		if (isStop)
			return;

		XMLParserVersionManager.getInstance().checkUpdate(this, new OnCheckResultListener() {
			@Override
			public void onResult(boolean hasNewVersion, Object result) {
				if (hasNewVersion) {
					final UpdateVersionBean o = (UpdateVersionBean) result;
					mHandler.postDelayed(new Runnable() {
						@Override
						public void run() {
							if (!isStop)
								ViewUtil.showUpdateDialog(ScanHistoryActivity.this, o, !isStop);
						}
					}, 2000);
				}
			}
		});
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
		};
	};

	private boolean hasNet() {
		return CommonUtil.isNetConnect(this);
	}

	@Override
	protected void onStart() {
		super.onStart();
		isStop = false;
	}

	@Override
	protected void onStop() {
		super.onStop();
		isStop = true;
	}

	/**
	 * @Description: (后退键)
	 * @author 李巷阳
	 * @date 2016-6-17 下午6:08:02
	 */
	@Override
	public void onBackPressed() {
		finishUI();
	}
}
