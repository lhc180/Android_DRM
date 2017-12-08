package cn.com.pyc.drm.ui;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import cn.com.pyc.drm.R;
import cn.com.pyc.drm.utils.APIUtil;
import cn.com.pyc.drm.utils.OpenPageUtil;
import cn.com.pyc.drm.utils.help.UIHelper;

/**
 * 关于我们
 *
 *  @author hudq update
 */
public class SZInfoActivity extends BaseActivity implements OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sz_about);
        initView();
        loadData();
    }

    @Override
    protected void initView() {
        UIHelper.showTintStatusBar(this);
        findViewById(R.id.back_img).setOnClickListener(this);
        ((TextView) findViewById(R.id.title_tv)).setText(getString(R.string.sz_info));
        ((TextView) findViewById(R.id.content_sz_text)).setText(getString(R.string.sz_introduce));
        //final String verEnv = BuildConfig.DEBUG ? "_debug" : "_release";
        //final String dateEnv = TimeUtil.getCurrentTime(".yyyyMMdd");
        //final String version = CommonUtil.getAppVersionName(this);
        ((TextView) findViewById(R.id.tv_version)).setText(getString(R.string.app_name) + "\t" +
                getString(R.string.app_building));
        ((TextView) findViewById(R.id.tv_number)).setText(getString(R.string.sz_phone_label,
                getString(R.string.sz_phone)));
        ((TextView) findViewById(R.id.tv_official_website)).setText(getString(R.string
                .sz_address_label, getString(R.string.sz_address)));

        findViewById(R.id.rel_customer_cell).setOnClickListener(this);
        findViewById(R.id.rel_web_page).setOnClickListener(this);

    }

    private int click = 0;
    private long currentTime = System.currentTimeMillis();

    @Override
    protected void loadData() {
        //调试
        findViewById(R.id.tv_version).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                click++;
                if (System.currentTimeMillis() - currentTime > 600) {
                    click = 0;
                }
                currentTime = System.currentTimeMillis();
                if (click == 7) {
                    showToast("API: " + APIUtil.getHost());
                    click = 0;
                }
            }
        });
    }

    @Override
    protected void getValue() {
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_img:
                UIHelper.finishActivity(this);
                break;
            case R.id.rel_customer_cell:
                OpenPageUtil.openSystemDialPage(this, getString(R.string.sz_phone));
                UIHelper.startInAnim(this);
                break;
            case R.id.rel_web_page:
                OpenPageUtil.openBrowserOfSystem(this, getString(R.string.sz_address));
                UIHelper.startInAnim(this);
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        UIHelper.finishActivity(this);
    }
}
