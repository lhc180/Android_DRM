package cn.com.pyc.szpbb.sdk.bean;

import cn.com.pyc.szpbb.common.K;
import cn.com.pyc.szpbb.sdk.SZInitInterface;
import cn.com.pyc.szpbb.util.PathUtil;
import cn.com.pyc.szpbb.util.SPUtil;
import cn.com.pyc.szpbb.util.SecurityUtil;

/**
 * 初始化构建参数
 * <p>
 * Created by hudq on 2016/12/12.
 */
public class SZBuildParams {

    private String basePath;
    private String userName;
    private String password;
    private String shareId;
    private String token;

    public void init() {
        SPUtil.save(K.FIELDS_BASE_PATH, this.basePath);
        SPUtil.save(K.FIELDS_SHARE, this.shareId);
        SPUtil.save(K.FIELDS_NAME, this.userName);
        SPUtil.save(K.FIELDS_PWD, SecurityUtil.encryptBASE64(this.password));
        SPUtil.save(K.FIELDS_TOKEN, this.token);

        //创建文件保存目录
        //SZInitInterface.createFilePath();
        PathUtil.createSaveFilePath(SZInitInterface.getUserName(false));
    }

    private SZBuildParams() {
    }

    private SZBuildParams(Builder builder) {
        this.basePath = builder.basePath;
        this.userName = builder.userName;
        this.password = builder.password;
        this.shareId = builder.shareId;
        this.token = builder.token;
    }


    public static class Builder {
        private String basePath;
        private String userName;
        private String password;
        private String shareId;
        private String token;

        /**
         * 设置文件保存路径,eg: test1; Android/data/test1<br/>
         * <p>
         *
         * @param basePath 路径别名，不设置或设置为null或""则使用默认存储路径;<br/>
         *                 默认存储偏移路径"Android/data/PBB.Guest/"
         * @return this
         */
        public Builder setBasePath(String basePath) {
            this.basePath = basePath;
            return this;
        }

        /**
         * 设置用户名
         *
         * @param userName 用户名，一般是手机号
         * @return this
         */
        public Builder setUserName(String userName) {
            this.userName = userName;
            return this;
        }

        /**
         * 设置用户密码，一般不建议设置
         *
         * @param password 密码
         * @return this
         */
        public Builder setPassword(String password) {
            this.password = password;
            return this;
        }

        /**
         * 设置分享id,一条分享对应一个分享id <br/>
         * 不设置shareId，则数据库名称使用用户名
         *
         * @param shareId 分享id
         * @return this
         */
        public Builder setShareId(String shareId) {
            this.shareId = shareId;
            return this;
        }

        /**
         * 设置用户令牌,由接口请求PBB服务端返回。
         *
         * @param token 令牌
         * @return this
         */
        public Builder setToken(String token) {
            this.token = token;
            return this;
        }

        public SZBuildParams create() {
            return new SZBuildParams(this);
        }
    }
}
