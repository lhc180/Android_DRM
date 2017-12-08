package cn.com.pyc.szpbb.demo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import cn.com.pyc.szpbb.sdk.SZDBInterface;
import cn.com.pyc.szpbb.sdk.SZHttpInterface;
import cn.com.pyc.szpbb.sdk.SZInitInterface;
import cn.com.pyc.szpbb.sdk.bean.SZBuildParams;
import cn.com.pyc.szpbb.sdk.callback.ISZCallBack;
import cn.com.pyc.szpbb.sdk.response.SZResponseLogin;
import cn.com.pyc.szpbb.sdk.response.SZResponseLoginout;
import cn.com.pyc.szpbb.sdk.response.request.SZRequestLogin;
import cn.com.pyc.szpbb.sdk.response.request.SZRequestLoginout;
import cn.com.pyc.szpbb.util.SZLog;
import cn.com.pyc.szpbb.util.SecurityUtil;


/**
 * 登录demo
 */
public class LoginActivity extends Activity {

    // UI references.
    private EditText mUserView;
    private EditText mPasswordView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mUserView = (EditText) findViewById(R.id.user);
        mPasswordView = (EditText) findViewById(R.id.password);

        if (!TextUtils.isEmpty(SZInitInterface.getToken())) {
            startActivity(new Intent(LoginActivity.this, ShareInfoActivity.class));
        }
    }

    public void login(View v) {
        requestLogin();
    }

    /**
     * 登录PBB服务器，并初始化参数
     * <p>
     * 按设备的分享可以无需登录。
     */
    private void requestLogin() {
        final SZRequestLogin params = new SZRequestLogin();
        params.username = mUserView.getText().toString().trim();
        params.password = SecurityUtil.encryptBASE64(mPasswordView.getText().toString());

        final ProgressDialog dlg = ProgressDialog.show(this, "登录", "正在登录...");
        SZHttpInterface.login(params, new ISZCallBack<SZResponseLogin>() {
            @Override
            public void onSuccess(SZResponseLogin response) {
                if (response.isResult()) {
                    SZLog.d("onSuccess", "登录成功");
                    //初始化参数
                    SZBuildParams p = new SZBuildParams.Builder()
                            .setBasePath("Android/data/SZPJDemo")
                            .setToken(response.getToken())
                            .setUserName(params.username)
                            .setPassword(params.password)
                            //.setShareId("1e634e82-6816-4bed-8f3e-b51ad97c42e4")
                            .create();
                    SZInitInterface.setParams(p);
                    // 登录成功，创建数据库、表！*建议异步操作执行最好。
                    SZDBInterface.openDB();
                    SZDBInterface.createTables();

                    startActivity(new Intent(LoginActivity.this, ShareInfoActivity.class));
                }
            }

            @Override
            public void onFinished() {
                SZLog.d("onFinished", "登录结束");
                dlg.dismiss();
            }

            @Override
            public void onFailed(String throwable) {
                SZLog.d("登录失败: " + throwable);
            }
        });
    }

    //注销
    public void loginout(View view) {
        final ProgressDialog dlg = ProgressDialog.show(this, "注销", "正在注销...");
        SZRequestLoginout p = new SZRequestLoginout();
        p.username = SZInitInterface.getUserName(true);
        p.token = SZInitInterface.getToken();
        SZHttpInterface.loginout(p, new ISZCallBack<SZResponseLoginout>() {
            @Override
            public void onSuccess(SZResponseLoginout response) {
                if (response.isResult()) {
                    SZLog.d("注销成功！");

                    //清除所有的缓存？??*建议异步操作执行最好。
                    SZInitInterface.deleteCacheFile();
                    SZDBInterface.deleteTableData();
                }
            }

            @Override
            public void onFailed(String throwable) {

            }

            @Override
            public void onFinished() {
                //注销切换账号需要关闭数据库后重新打开
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        SZInitInterface.clearSP();
                        SZDBInterface.closeDB();
                        dlg.dismiss();
                    }
                }, 3000);
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SZHttpInterface.cancelHttp(SZRequestLogin.class);
    }
}

