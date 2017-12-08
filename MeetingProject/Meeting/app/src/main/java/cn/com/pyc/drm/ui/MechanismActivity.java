package cn.com.pyc.drm.ui;


import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.google.zxing.client.android.CaptureActivity;
import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;
import com.jcodecraeer.xrecyclerview.XRecyclerView.LoadingListener;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

import java.util.List;

import cn.com.meeting.drm.R;
import cn.com.pyc.drm.adapter.JGAdapter;
import cn.com.pyc.drm.adapter.Rv_BaseAdapter;
import cn.com.pyc.drm.bean.BaseBean;
import cn.com.pyc.drm.bean.MechanismBean;
import cn.com.pyc.drm.bean.ReturnMechanismDataBean;
import cn.com.pyc.drm.common.Constant;
import cn.com.pyc.drm.utils.CommonUtil;
import cn.com.pyc.drm.utils.DRMUtil;
import cn.com.pyc.drm.utils.OpenPageUtil;
import cn.com.pyc.drm.utils.SPUtils;
import cn.com.pyc.drm.utils.StringUtil;
import cn.com.pyc.drm.utils.UIHelper;
import cn.com.pyc.drm.utils.ViewUtil;
import cn.com.pyc.drm.utils.ViewUtil.DialogCallBack;
import cn.com.pyc.drm.utils.manager.ActicityManager;
import cn.com.pyc.drm.utils.manager.RequestHttpManager;
import cn.com.pyc.drm.widget.HighlightImageView;


/**
 * @author 李巷阳
 * @version V1.0
 * @Description: (机构列表)
 * @date 2016-8-15 下午5:35:09
 */
public class MechanismActivity extends BaseActivity implements View.OnClickListener, Rv_BaseAdapter.OnItemClickListener{
    @ViewInject(R.id.title_tv)
    private TextView mtv_title_tv;

    @ViewInject(R.id.lv_mechanism)
    private XRecyclerView mlv_mechanism;

    @ViewInject(R.id.tv_add_hints)
    private TextView mtv_add_hints;

    @ViewInject(R.id.explain)
    private TextView mtv_explain;

    @ViewInject(R.id.view)
    private View mview;

    @ViewInject(R.id.rl_prompt)
    private RelativeLayout mrl_prompt;

    private JGAdapter mAdapter;

    private List<MechanismBean> listStr;

    @ViewInject(R.id.opt_img)
    private HighlightImageView mopt_img;

    @ViewInject(R.id.back_img)
    private HighlightImageView back_img;

    private int resultCode = 1;

    private String PhoneNumber = "";

    private AsyncTask<String, String, String> mLoginTask;

    private String isForActivity;

    /**
     * @author 李巷阳
     * @date 2016-8-15 下午5:36:01
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mechanism);
        init_Value();
        init_View();
        showloading_load_data();
        initListener();
    }

    /**
     * @author 李巷阳
     * @date 2016-10-27 下午5:01:47
     */
    private void showloading_load_data() {
        showBgLoading("正在加载...");
        initdata();
    }

    /**
     * @author 李巷阳
     * @date 2016-8-15 下午5:35:54
     */
    @Override
    protected void init_Value() {
        // TODO Auto-generated method stub
        ViewUtils.inject(this);
        ActicityManager.getInstance().add(this);
        // 自定义状态栏
        UIHelper.showTintStatusBar(this);
        isForActivity = getIntent().getStringExtra("isForActivity");
        if (Constant.isForLoginActivity.equals(isForActivity)) {
            mtv_explain.setText("选择您要进入的机构");
        }
    }

    /**
     * @author 李巷阳
     * @date 2016-8-15 下午5:35:54
     */
    @Override
    protected void init_View() {
        // TODO Auto-generated method stub
        mtv_title_tv.setText("机构列表");
        mopt_img.setVisibility(View.VISIBLE);

        LinearLayoutManager LinearManager = new LinearLayoutManager(this);
        LinearManager.setOrientation(LinearLayoutManager.VERTICAL);
        mlv_mechanism.setLayoutManager(LinearManager);

        mlv_mechanism.setRefreshProgressStyle(ProgressStyle.BallSpinFadeLoader);
        mlv_mechanism.setLaodingMoreProgressStyle(ProgressStyle.BallRotate);
        mlv_mechanism.setArrowImageView(R.drawable.iconfont_downgrey);

    }

    /**
     * @author 李巷阳
     * @date 2016-8-19 上午11:36:04
     */
    private void initdata() {
        // TODO Auto-generated method stub
        PhoneNumber = (String) SPUtils.get(DRMUtil.KEY_PHONE_NUMBER, "");
        new AsyncTask<String, String, String>() {
            protected String doInBackground(String... params) {
                String return_str = RequestHttpManager.getWSOrganizationName(params[0]);
                return return_str;
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    try{
                        ReturnMechanismDataBean bmd = JSON.parseObject(result, ReturnMechanismDataBean.class);
                        if ("true".equals(bmd.getResult())) {
                            listStr = bmd.getToken();
                            if (listStr.size() > 0) {
                                setVisible();
                                setAdapter(listStr, PhoneNumber);
                            } else {
                                setInvisible();
                            }
                        } else {
                            UIHelper.showToast(getApplicationContext(), bmd.getMsg());
                        }
                    }catch (Exception ex){
                        UIHelper.showToast(getApplicationContext(), "获取数据失败");
                    }

                } else {
                    UIHelper.showToast(getApplicationContext(), "获取数据失败");
                }
                hideBgLoading();
                mlv_mechanism.refreshComplete();
            }
        }.execute(PhoneNumber);
    }

    /**
     * @Description: (用一句话描述该文件做什么)
     * @author 李巷阳
     * @date 2016-8-15 下午5:46:51
     */
    private void initListener() {
        // TODO Auto-generated method stub
        back_img.setOnClickListener(this);
        mopt_img.setOnClickListener(this);


        mlv_mechanism.setLoadingListener(new LoadingListener() {

            @Override
            public void onRefresh() {
                initdata();
            }

            @Override
            public void onLoadMore() {
            }
        });


    }

    /**
     * @author 李巷阳
     * @date 2016-8-16 上午11:20:54
     */
    private void setVisible() {
        mrl_prompt.setVisibility(View.VISIBLE);
        mlv_mechanism.setVisibility(View.VISIBLE);
        mview.setVisibility(View.VISIBLE);
        mtv_add_hints.setVisibility(View.GONE);

    }

    /**
     * @author 李巷阳
     * @date 2016-8-16 上午11:19:15
     */
    private void setInvisible() {
        mrl_prompt.setVisibility(View.GONE);
        mlv_mechanism.setVisibility(View.GONE);
        mview.setVisibility(View.GONE);
        mtv_add_hints.setVisibility(View.VISIBLE);
    }

    private void setAdapter(List<MechanismBean> liststr, String PhoneNumber) {
        if (mAdapter == null) {
            mAdapter = new JGAdapter(getApplication(), liststr, PhoneNumber);
            mlv_mechanism.setAdapter(mAdapter);
        } else {
            mAdapter.refreshData(listStr);
        }
        mAdapter.setOnItemClickListener(this);
    }

    /**
     * @author 李巷阳
     * @date 2016-8-17 下午3:26:50
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_img:
                UIHelper.finishActivity(this);
                finish();// 此处一定要调用finish()方法

                break;
            case R.id.opt_img:
                // 扫码登录
                SPUtils.remove(DRMUtil.KEY_LOGIN_TOKEN);
                OpenPageUtil.openZXingCode(MechanismActivity.this);
                UIHelper.startInAnim(MechanismActivity.this);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constant.REQUEST_CODE_SCAN: {
                // 扫码登录获取产品信息
                if (resultCode == CaptureActivity.RESULT_OK && data != null) {
                    Bundle bundle = data.getExtras();
                    String DataSource = bundle.getString(Constant.DECODED_CONTENT_KEY);
                    saveScanResult(DataSource);
                }
            }
            break;
            case Constant.SCANHISTORY_CODE: {
                // 扫码登录获取产品信息
                if (resultCode == Activity.RESULT_OK) {
                    MechanismActivity.this.finish();
                }
            }
            break;
        }
    }

    /**
     * 保存扫描到的用户名称
     */
    private void saveScanResult(final String DataSource) {
        // 判断是否是机构二维码
        final String GetCompany = StringUtil.getStringByColonHalf(DataSource, "?op", "=");
        if (TextUtils.isEmpty(GetCompany) && !"GetCompany".equals(GetCompany)) {
            showToast(getString(R.string.scan_qr_code_error));
            showMsgDialog(DataSource);
            return;
        }
        cancelTask();
        mLoginTask = new AsyncTask<String, String, String>() {
            protected void onPreExecute() {
                showBgLoading("载入中...");
            }

            ;

            @Override
            protected String doInBackground(String... params) {
                if (isCancelled()) return null;
                String return_str = RequestHttpManager.getCompany(DataSource);
                return return_str;
            }

            protected void onPostExecute(String result) {
                hideBgLoading();
                final String address = StringUtil.getStringByColon(result, "Address", ",");
                final String servername = StringUtil.getStringByColon(result, "ServerName", ",");
                final String username = StringUtil.getStringByColon(result, "SZUserName", ",");

                if (TextUtils.isEmpty(address) || TextUtils.isEmpty(servername) || TextUtils.isEmpty(username)) {
                    showToast(getString(R.string.scan_qr_code_error));
                    showMsgDialog(DataSource);
                    return;
                }
                if (result != null) {
                    // TODO
                    final MechanismBean bb = JSON.parseObject(result, MechanismBean.class);

                    getWsoganizationg(bb);

                } else {
                    UIHelper.showToast(getApplicationContext(), "机构创建失败");
                }

            }

        };
        mLoginTask.execute();

    }

    /**
     * 添加机构
     *
     * @author 李巷阳
     * @date 2016-8-25 下午12:00:38
     */
    private void getWsoganizationg(final MechanismBean bb) {
        cancelTask();
        mLoginTask = new AsyncTask<String, String, String>() {
            protected void onPreExecute() {
                showBgLoading("载入中...");
            }

            ;

            @Override
            protected String doInBackground(String... params) {
                if (isCancelled()) return null;
                String return_str = RequestHttpManager.getWSOrganizationName(PhoneNumber, bb.getServerAddress(), bb.getServerName(), bb.getSZUserName());
                return return_str;
            }

            protected void onPostExecute(String result) {
                hideBgLoading();
                if (result != null) {
                    BaseBean bb = JSON.parseObject(result, BaseBean.class);
                    if ("true".equals(bb.getResult())) {
                        showloading_load_data();
                    }
                    UIHelper.showToast(getApplicationContext(), bb.getMsg());
                } else {
                    UIHelper.showToast(getApplicationContext(), "机构创建失败");
                }

            }

            ;
        };
        mLoginTask.execute();
    }

    ;

    /**
     * 非自定义登录的url，显示结果对话框
     *
     * @param result
     */
    private void showMsgDialog(final String result) {
        ViewUtil.showCommonDialog(this, getString(R.string.scanning_result), result, getString(R.string.copy), new DialogCallBack() {
            @Override
            public void onConfirm() {
                CommonUtil.copyText(MechanismActivity.this, result);
                showToast(getString(R.string.copy_to_clip));
            }
        });
    }

    // 取消任务
    private void cancelTask() {
        if (mLoginTask != null && (!mLoginTask.isCancelled() || mLoginTask.getStatus() == AsyncTask.Status.RUNNING)) {
            mLoginTask.cancel(true);
            mLoginTask = null;
        }
    }

    /**
     * @author 李巷阳
     * @date 2016-8-25 上午11:55:03
     */
    @Override
    protected void load_Data() {
        // TODO Auto-generated method stub

    }
    // adapter item 点击事件
    @Override
    public void onItemClick(View view, int position) {
        MechanismBean mmechanis_data = listStr.get(position - 1);
        if (TextUtils.isEmpty(mmechanis_data.getServerAddress()) || TextUtils.isEmpty(mmechanis_data.getServerName()) || TextUtils.isEmpty(mmechanis_data.getSZUserName())) {
            showToast("机构数据异常");
            return;
        }
        String AddressAndNameStr = mmechanis_data.getServerAddress() + "," + mmechanis_data.getServerName() + "," + mmechanis_data.getSZUserName();
        if (!"".equals(PhoneNumber)) {
            SPUtils.save(PhoneNumber, AddressAndNameStr);
        }
        // 切换时，更新端口号
        String[] hostAndPortString = StringUtil.getHostAndPortByResult(mmechanis_data.getServerAddress());
        if (hostAndPortString != null) {
            // 保存主机名。eg： video.suizhi.net
            SPUtils.save(DRMUtil.SCAN_FOR_HOST, hostAndPortString[0]);
            // 保存端口号。eg：8657
            SPUtils.save(DRMUtil.SCAN_FOR_PORT, hostAndPortString[1]);
        }

        Bundle bundle = new Bundle();
        bundle.putSerializable("MechanismData", mmechanis_data);

        // 判断 ： 如果我的会议跳转过来，则直接返回，否则则跳转
        if ("ForScanHistoryActivity".equals(isForActivity)) {
            Intent mIntent = new Intent();
            mIntent.putExtras(bundle);
            setResult(DRMUtil.RefreshMyMeetingActivity, mIntent);
        } else {
            openActivity(MyMeetingActivityPro.class, bundle);
            UIHelper.startInAnim(MechanismActivity.this);
        }
        finish();
    }
}
