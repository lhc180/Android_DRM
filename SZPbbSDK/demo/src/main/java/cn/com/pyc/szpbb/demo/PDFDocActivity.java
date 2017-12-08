package cn.com.pyc.szpbb.demo;

import android.app.Activity;
import android.os.Bundle;
import android.widget.RelativeLayout;
import android.widget.Toast;

import cn.com.pyc.szpbb.coreui.SZPDFHelpInterface;
import cn.com.pyc.szpbb.coreui.SZPDFInterface;
import cn.com.pyc.szpbb.coreui.widget.SZPDFCore;
import cn.com.pyc.szpbb.coreui.widget.SZPDFDocView;
import cn.com.pyc.szpbb.coreui.widget.SZPDFPageAdapter;
import cn.com.pyc.szpbb.sdk.SZFileInterface;
import cn.com.pyc.szpbb.sdk.authentication.SZContentPermission;
import cn.com.pyc.szpbb.sdk.database.bean.SZAlbumContent;
import cn.com.pyc.szpbb.util.SZLog;

/**
 * pdf demo
 * <p>
 * method:<br/>
 * 1.UI相关：{@link SZPDFInterface} <br/>
 * (1)  init(); <br/>
 * (2)  destroy(); <br/>
 * (3)  isValid(); <br/>
 * (4)  authenticate(); <br/>
 * <p>
 * 2.管理工具相关：{@link SZPDFHelpInterface} <br/>
 * (1)  getTotalPages(); <br/>
 * (2)  getCurrentPage(); <br/>
 * (3)  setCurrentPage(); <br/>
 * (4)  saveBookmark(); <br/>
 * (5)  getBookmark(); <br/>
 * (6)  removeBookmark(); <br/>
 * (7)  getCatalogs(); <br/>
 */
public class PDFDocActivity extends Activity {

    private SZPDFCore core;
    private SZPDFDocView docView;

    private RelativeLayout mContainer;
    private SZAlbumContent currentFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf);
        mContainer = (RelativeLayout) findViewById(R.id.activity_pdfbase);
        currentFile = getIntent().getParcelableExtra("SZAlbumContent");

        init(currentFile);

    }

    private void init(SZAlbumContent ac) {
        //获取文件的权限
        SZContentPermission permission = SZFileInterface.getPermission(ac);
        boolean canOpen = permission.checkOpen();
        SZLog.i("是否有效：" + canOpen);
        SZLog.i("过期时间：" + permission.getExpireTime());
        SZLog.i("剩余时间：" + permission.getSurplusTime());
        if (!canOpen) {
            Toast.makeText(this, "文件无效，无法打开！", Toast.LENGTH_SHORT).show();
            return;
        }


        try {
            core = (SZPDFCore) getLastNonConfigurationInstance();
            if (core == null) {
                core = SZPDFInterface.init(this, ac.getFile_path());
                String clipherKey = permission.getCipherValue();
                if (SZPDFInterface.isValid(core) && SZPDFInterface.authenticate(core, clipherKey)) {
                    //创建视图
                    createDocView();
                    //添加视图到页面
                    mContainer.addView(docView);
                }
            } else {
                //创建视图,添加视图到页面
                mContainer.addView(createDocView());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建视图
     *
     * @return
     */
    private SZPDFDocView createDocView() {
        docView = new SZPDFDocView(this) {
            @Override
            protected void onPageChanged(int page) {
                SZLog.v("当前第" + (page + 1) + "页");
                SZLog.v("总共" + SZPDFHelpInterface.getTotalPages(core) + "页");
            }

            @Override
            protected void onTapMainArea() {
                SZLog.d("点击中心区域可控制显示自定义的菜单");
            }
        };
        SZPDFPageAdapter adapter = new SZPDFPageAdapter(this, core);
        docView.setAdapter(adapter);
        return docView;
    }


    /**
     * 对应getLastNonConfigurationInstance();
     *
     * @return obj
     */
//    @Override
//    public Object onRetainNonConfigurationInstance() {
//        SZPDFCore myCore = core;
//        core = null;
//        return myCore;
//    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        SZPDFInterface.destroy(core, docView);
    }
}
