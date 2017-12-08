package cn.com.pyc.szpbb.demo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.List;

import cn.com.pyc.szpbb.common.Code;
import cn.com.pyc.szpbb.common.Fields;
import cn.com.pyc.szpbb.demo.adapter.FileListAdapter;
import cn.com.pyc.szpbb.sdk.SZDownloadInterface;
import cn.com.pyc.szpbb.sdk.SZFileInterface;
import cn.com.pyc.szpbb.sdk.SZHttpInterface;
import cn.com.pyc.szpbb.sdk.SZInitInterface;
import cn.com.pyc.szpbb.sdk.callback.ISZCallBack;
import cn.com.pyc.szpbb.sdk.database.bean.SZAlbumContent;
import cn.com.pyc.szpbb.sdk.download.SZDownloadFileSubscriber;
import cn.com.pyc.szpbb.sdk.models.SZFileData;
import cn.com.pyc.szpbb.sdk.response.SZResponseBindShare;
import cn.com.pyc.szpbb.sdk.response.SZResponseDownloadCheck;
import cn.com.pyc.szpbb.sdk.response.SZResponseGetShareFile;
import cn.com.pyc.szpbb.sdk.response.request.SZRequestBindShare;
import cn.com.pyc.szpbb.sdk.response.request.SZRequestDownloadCheck;
import cn.com.pyc.szpbb.sdk.response.request.SZRequestGetShareFile;
import cn.com.pyc.szpbb.util.SZLog;

/**
 * 文件列表demo
 */
public class FileListActivity extends Activity implements AdapterView.OnItemClickListener {

    private ListView mListView;
    private FileListAdapter adapter;
    private String folderId;

    //提示语可自定义
    private SZDownloadFileSubscriber subScriber = new SZDownloadFileSubscriber() {
        @Override
        protected void downloadProgress(SZFileData data, int progress, long currentSize) {
            //下载进度
            adapter.updateItemView(data);
        }

        @Override
        protected void downloadFailed(SZFileData data) {
            //下载失败
            adapter.updateItemView(data);
        }

        @Override
        protected void waiting(SZFileData data) {
            //等待下载
            adapter.updateItemView(data);
        }

        @Override
        protected void connecting(SZFileData data) {
            //连接中
            adapter.updateItemView(data);
        }

        @Override
        protected void downloadFinished(SZFileData data) {
            //下载完成
            adapter.updateItemView(data);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_list);
        SZDownloadInterface.register(this, subScriber);

        folderId = getIntent().getStringExtra("folder_id");
        SZLog.d("folderId = " + folderId);

        mListView = (ListView) findViewById(R.id.data_listView);
        mListView.setOnItemClickListener(this);

        loadFileList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SZDownloadInterface.unregister(this, subScriber);
    }

    /**
     * 请求文件列表
     */
    private void loadFileList() {
        final ProgressDialog dlg = ProgressDialog.show(this, "提示", "加载文件列表...");
        SZRequestGetShareFile params = new SZRequestGetShareFile();
        params.shareFolderId = folderId;//"6b7e8efa-a313-4e1d-9081-626342be3861";
        SZHttpInterface.getShareFile(params, new ISZCallBack<SZResponseGetShareFile>() {
            @Override
            public void onSuccess(SZResponseGetShareFile response) {
                if (response.isResult()) {
                    List<SZFileData> datas = response.getData();
                    adapter = new FileListAdapter(mListView, datas);
                    mListView.setAdapter(adapter);
                }
            }

            @Override
            public void onFailed(String throwable) {
                SZLog.e("获取文件列表失败：" + throwable);
            }

            @Override
            public void onFinished() {
                dlg.dismiss();
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        SZFileData data = adapter.getItem(position);
        SZAlbumContent result = SZFileInterface.findAlbumContent(data.getFiles_id());
        if (result != null) {
            //已下载，打开文件
            if (Fields.PDF.equals(result.getFile_type())) {
                Intent intent = new Intent(this, PDFDocActivity.class);
                intent.putExtra("SZAlbumContent", result);
                startActivity(intent);
            }
        } else {
            //没有下载，则开启下载
            download(data);
        }
    }

    /**
     * 下载
     *
     * @param data 文件item数据
     */
    private void download(final SZFileData data) {
        if (SZDownloadInterface.isStartTask(data)) {
            //暂停任务
            SZDownloadInterface.stopTask(this, data);
            //:暂停,更新UI
            adapter.updateItemView(data);
            return;
        }

        //校验文件
        SZRequestDownloadCheck params = new SZRequestDownloadCheck();
        params.fileId = data.getFiles_id();
        params.shareFolderId = data.getSharefolder_id();
        params.shareId = "1e634e82-6816-4bed-8f3e-b51ad97c42e4"; //分享的shareId;
        params.token = SZInitInterface.getToken();
        SZHttpInterface.downloadCheck(params, new ISZCallBack<SZResponseDownloadCheck>() {
            @Override
            public void onSuccess(SZResponseDownloadCheck response) {
                responseHandle(data, response.getCode(), response.getFtpUrl());
            }

            @Override
            public void onFailed(String throwable) {
            }

            @Override
            public void onFinished() {
            }
        });
    }

    private void responseHandle(SZFileData data, String code, String ftpUrl) {
        if (Code._1001.equals(code)) {
            if (ftpUrl == null || !ftpUrl.startsWith("ftp://")) {
                SZLog.e("下载文件地址有误！");
            } else {
                SZLog.e("开启下载");
                data.setFtpUrl(ftpUrl); //设置ftpUrl
                SZDownloadInterface.startTask(this, data);
            }
        } else if (Code._8002.equals(code)) {
            SZLog.e("请先绑定分享！");
            bindShare(data);
        } else if (Code._8001.equals(code)) {
            SZLog.e("请先领取分享！");
            //TODO:调用接收分享接口 SZHttpInterface.receiveShare();
        } else if (Code._8006.equals(code)) {
            SZLog.e("登录状态已失效，请重新登录！");
            //TODO:去登录
        } else if (Code._8007.equals(code)) {
            SZLog.e("正在加密文件，请稍候");
            //TODO：可以重新请求校验文件接口downloadCheck.
        } else if (Code._9007.equals(code)) {
            SZLog.e("您还未登录");
            //TODO:去登录
        } else {
            SZLog.e("其他错误处理：自定义提示给用户！");
        }
    }

    private void bindShare(final SZFileData data) {
        AlertDialog builder = new AlertDialog.Builder(this)
                .setMessage("请先绑定分享")
                .setPositiveButton("绑定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        bind(data);
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).create();
        builder.show();
    }

    /**
     * 绑定分享
     *
     * @param data
     */
    private void bind(final SZFileData data) {
        final ProgressDialog dlg = ProgressDialog.show(this, "绑定分享", "正在绑定...");
        SZRequestBindShare params = new SZRequestBindShare();
        params.fileId = data.getFiles_id();
        params.shareFolderId = data.getSharefolder_id();
        params.shareId = "1e634e82-6816-4bed-8f3e-b51ad97c42e4"; //分享的shareId;
        params.token = SZInitInterface.getToken();
        SZHttpInterface.bindShare(params, new ISZCallBack<SZResponseBindShare>() {
            @Override
            public void onSuccess(SZResponseBindShare response) {
                responseHandle(data, response.getCode(), response.getFtpUrl());
            }

            @Override
            public void onFailed(String throwable) {
                SZLog.e("绑定失败:" + throwable);
            }

            @Override
            public void onFinished() {
                dlg.dismiss();
            }
        });
    }

}
