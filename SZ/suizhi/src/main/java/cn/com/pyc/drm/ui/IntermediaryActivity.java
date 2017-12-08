package cn.com.pyc.drm.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;

import org.xutils.common.Callback;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.com.pyc.drm.R;
import cn.com.pyc.drm.bean.event.BaseEvent;
import cn.com.pyc.drm.bean.event.ConductUIEvent;
import cn.com.pyc.drm.common.Constant;
import cn.com.pyc.drm.model.CheckMyProIdModel;
import cn.com.pyc.drm.utils.APIUtil;
import cn.com.pyc.drm.utils.DRMLog;
import cn.com.pyc.drm.utils.DataMergeUtil;
import cn.com.pyc.drm.utils.help.KeyHelp;
import cn.com.pyc.drm.utils.manager.ExecutorManager;
import cn.com.pyc.drm.utils.manager.HttpEngine;
import de.greenrobot.event.EventBus;

/**
 * 中间页面（迁移数据显示进度时使用）
 */
public class IntermediaryActivity extends BaseAssistActivity {
    private static final String TAG = "IntermediaryUI";
    private static final int MSG_PROGRESS = 1000;
    private static final int MSG_FINISHED = -1000;
    private ProgressBar pbProgress;
    private TextView tvIndex;

    private DataMergeUtil<?> mergeUtil;
    private Map<String, List<String>> idGroup;
    private List<String> idsSum = new ArrayList<>(); //所有的myProId集合

    private ExecHandler handler = new ExecHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitivity_intermediary);
        init();
        initView();
        checkUserMyProducts();
    }

    private void initView() {
        pbProgress = (ProgressBar) findViewById(R.id.intermediary_progress_btn);
        tvIndex = (TextView) findViewById(R.id.intermediary_progress_text);
    }

    private void init() {
        if (getWindow() != null) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        mergeUtil = new DataMergeUtil<>();
        idGroup = mergeUtil.getAllBuyMyProductIdMap();
        for (Map.Entry<String, List<String>> entry : idGroup.entrySet()) {
            idsSum.addAll(entry.getValue());
        }
    }

    //List集合转化成逗号分隔的字符串
    private String convertIds(List<String> ids) {
        StringBuilder builder = new StringBuilder();
        for (String id : ids) {
            builder.append(id).append(",");
        }
        return builder.toString();
    }

    //通过有效的myProId查找到有效的有户名称
    private Set<String> findValidUser(String[] validId) {
        Set<String> names = new LinkedHashSet<>();
        for (String id : validId) {
            if (id == null) continue;
            DRMLog.e("有效id: " + id);
            for (Map.Entry<String, List<String>> entry : idGroup.entrySet()) {
                if (entry.getValue().contains(id)) {
                    names.add(entry.getKey());
                }
            }
        }
        return names;
    }

    private void checkUserMyProducts() {
        showBgLoading(getString(R.string.please_waiting));
        Bundle bundle = new Bundle();
        bundle.putString("username", Constant.getName());
        bundle.putString("token", Constant.getToken());
        bundle.putString("myProIds", convertIds(idsSum));
        HttpEngine.post(APIUtil.checkAccountAndMyProductUrl(), bundle, new Callback
                .CommonCallback<String>() {
            @Override
            public void onSuccess(String s) {
                DRMLog.d(TAG, "check-result:" + s);
                CheckMyProIdModel model = JSON.parseObject(s, CheckMyProIdModel.class);
                if (model.isYes(model.getCode())) {
                    if (model.getData() != null) {
                        String[] idArray = model.getData().getMyProIds();
                        if (idArray != null && idArray.length > 0) {
                            moveData2Target(findValidUser(idArray));
                            return;
                        }
                        //TODO:失败同样回主界面
                    }
                } else {
                    showToast(model.getMsg());
                }
                openHomeUI();
            }

            @Override
            public void onError(Throwable throwable, boolean b) {
                DRMLog.d(TAG, "check-error:" + throwable.getMessage());
            }

            @Override
            public void onCancelled(CancelledException e) {
            }

            @Override
            public void onFinished() {
                hideBgLoading();
            }
        });
    }

    private void moveData2Target(Set<String> names) {
        ExecutorManager.getInstance().execute(new CopyDataThread(this, names));
    }

    private static class CopyDataThread implements Runnable {
        private WeakReference<Activity> reference;
        private Set<String> names;
        private int index = 0;

        CopyDataThread(IntermediaryActivity activity, Set<String> names) {
            reference = new WeakReference<Activity>(activity);
            this.names = names;
        }

        @Override
        public void run() {
            if (reference.get() == null) return;
            final IntermediaryActivity activity = (IntermediaryActivity) reference.get();
            if (names == null || names.isEmpty()) {
                activity.handler.sendEmptyMessage(MSG_FINISHED);
                return;
            }
            for (String validName : names) {
                index++;
                DRMLog.d("有效用户名：" + validName);
//                activity.mergeUtil.copyFileFromTargetFolder(validName, new FileUtils
//                        .OnCopyProgressListener() {
//                    @Override
//                    public void onCopyProgress(int progress) {
//                        Message msg = Message.obtain();
//                        msg.what = MSG_PROGRESS;
//                        msg.arg1 = progress;
//                        msg.arg2 = index;
//                        activity.handler.sendMessage(msg);
//                    }
//                });
//                activity.mergeUtil.insertDataFromTargetDB(activity, validName);
            }
            activity.handler.removeMessages(MSG_PROGRESS);
            activity.handler.sendEmptyMessageDelayed(MSG_FINISHED, 600L);
        }
    }

    private static class ExecHandler extends Handler {
        private WeakReference<IntermediaryActivity> reference;

        private ExecHandler(IntermediaryActivity activity) {
            reference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            IntermediaryActivity activity = reference.get();
            if (activity == null) return;
            if (msg.what == MSG_PROGRESS) {
                activity.pbProgress.setProgress(msg.arg1);
                activity.tvIndex.setText("正在更新第" + (msg.arg2 + 1) + "条数据: " + msg.arg1 + "%");
            } else if (msg.what == MSG_FINISHED) {
                //完成检查，去主页面
                //activity.openActivity(HomeActivity.class);
                //activity.finish();
                activity.openHomeUI();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (handler != null && handler.hasMessages(MSG_PROGRESS)) {
            showToast(getString(R.string.please_waiting));
            return;
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }


    private void openHomeUI() {
        EventBus.getDefault().post(new ConductUIEvent(BaseEvent.Type.UI_HOME_FINISH));
        //activity.openActivity(HomeActivity.class);
        startActivity(new Intent(this, HomeActivity.class)
                .putExtra(KeyHelp.LOGIN_FLAG, Constant.LOGIN_TYPE));
        finish();
    }
}
