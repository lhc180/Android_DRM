package cn.com.pyc.szpbb.demo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import cn.com.pyc.szpbb.demo.adapter.FolderListAdapter;
import cn.com.pyc.szpbb.sdk.SZHttpInterface;
import cn.com.pyc.szpbb.sdk.SZInitInterface;
import cn.com.pyc.szpbb.sdk.callback.ISZCallBack;
import cn.com.pyc.szpbb.sdk.models.SZFolderInfo;
import cn.com.pyc.szpbb.sdk.response.SZResponseGetShareInfo;
import cn.com.pyc.szpbb.sdk.response.request.SZRequestGetShareInfo;

/**
 * 分享信息demo
 */
public class ShareInfoActivity extends Activity implements AdapterView.OnItemClickListener {

    private ListView mListView;
    private TextView infoText;

    private FolderListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_info);
        mListView = (ListView) findViewById(R.id.share_listView);
        infoText = (TextView) findViewById(R.id.share_info);
        mListView.setOnItemClickListener(this);

        requestShareInfo();
    }

    private void requestShareInfo() {
        final ProgressDialog dlg = ProgressDialog.show(this, "提示", "正在加载分享信息...");
        SZRequestGetShareInfo params = new SZRequestGetShareInfo();
        params.token = SZInitInterface.getToken();
        params.shareId = "1e634e82-6816-4bed-8f3e-b51ad97c42e4";
        SZHttpInterface.getShareInfo(params, new ISZCallBack<SZResponseGetShareInfo>() {
            @Override
            public void onSuccess(SZResponseGetShareInfo response) {
                if (response.isResult()) {
                    SZResponseGetShareInfo.ShareInfo info = response.getData();
                    StringBuilder sb = new StringBuilder();
                    sb.append("分享人：" + info.getOwner() + "\n")
                            .append("主题：" + info.getTheme() + "\n")
                            .append("附言：" + info.getMessage() + "\n")
                            .append("设备：剩余" + (info.getMax_device_num() - info
                                    .getReceive_device_num()) + "台" + "\n")
                            .append("时限：" + getShareTime(info) + "\n");
                    infoText.setText(sb.toString());

                    List<SZFolderInfo> folders = response.getPageInfo().getItems();
                    adapter = new FolderListAdapter(ShareInfoActivity.this, folders);
                    mListView.setAdapter(adapter);
                }
            }

            @Override
            public void onFailed(String throwable) {
            }

            @Override
            public void onFinished() {
                dlg.dismiss();
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        SZFolderInfo folder = adapter.getItem(position);
        startActivity(new Intent(this, FileListActivity.class).putExtra("folder_id", folder
                .getMyProId()));
    }

    private String getShareTime(SZResponseGetShareInfo.ShareInfo data) {
        if (data.isUnlimmit()) {
            return "永久有效";
        } else if (data.isDayRange()) {
            return data.getShare_time().replace("/", "至");
        } else {
            return "从第一次绑定起计" + data.getShare_time() + "天";
        }
    }
}
