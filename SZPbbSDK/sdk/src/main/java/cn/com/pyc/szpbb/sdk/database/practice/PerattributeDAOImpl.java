package cn.com.pyc.szpbb.sdk.database.practice;

import cn.com.pyc.szpbb.sdk.database.bean.Perattribute;
import cn.com.pyc.szpbb.sdk.database.dbBase.BaseDAOPracticeImpl;


public class PerattributeDAOImpl extends BaseDAOPracticeImpl<Perattribute> implements
        PerattributeDAO {

    public static PerattributeDAOImpl getInstance() {
        return new PerattributeDAOImpl();
    }

    private PerattributeDAOImpl() {
    }
}
