package cn.com.pyc.drm.ui.view;

import cn.com.pyc.drm.bean.ScanHistory;

/**   
 * @Description: (用一句话描述该文件做什么) 
 * @author 李巷阳
 * @date 2016-10-18 下午5:55:10 
 * @version V1.0   
 */
public interface ILoginView {
	

    // 获取用户输入的账号
    String getUserName();
    // 获取用户输入的密码
    String getPassWord();
    // 登陆过程出现的异常
    void showError(String string);
    // 登陆成功
    void showSuccess(String string);
    // 开启进度加载框
    void showBgLoading(String string);
    // 关闭进度加载框
    void hideBgLoading();
    // 弹出Toast
    void showToast(String str);
    // 非自定义登录的url，显示结果对话框
    void showMsgDialog(String str);
    // 跳转第三方登陆activity
    void openScanLoginVerificationActivity(ScanHistory sh);
    // 跳转主界面
    void openDownloadMainByScaning2(ScanHistory sh);
    
    
}
