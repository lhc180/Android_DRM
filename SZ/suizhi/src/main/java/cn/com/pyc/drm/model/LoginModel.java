package cn.com.pyc.drm.model;

/**
 * 登录成功返回数据
 *
 * @author hudq
 */
public class LoginModel extends BaseModel {

    private LoginInfo data;

    public void setData(LoginInfo data) {
        this.data = data;
    }

    public LoginInfo getData() {
        if (data == null) return new LoginInfo();
        return data;
    }

    public static class LoginInfo {

        private String token;
        private String username; //登陆成功会返回超级账户名，后续接口请求都用这个做username
        private String accountId;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public void setAccountId(String accountId) {
            this.accountId = accountId;
        }

        public String getAccountId() {
            return accountId;
        }
    }

}
