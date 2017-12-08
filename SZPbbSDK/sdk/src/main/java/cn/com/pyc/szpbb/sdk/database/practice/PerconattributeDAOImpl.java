package cn.com.pyc.szpbb.sdk.database.practice;

import cn.com.pyc.szpbb.sdk.database.bean.Perconattribute;
import cn.com.pyc.szpbb.sdk.database.dbBase.BaseDAOPracticeImpl;


public class PerconattributeDAOImpl extends BaseDAOPracticeImpl<Perconattribute> implements
        PerconattributeDAO {
    public static PerconattributeDAOImpl getInstance() {
        return new PerconattributeDAOImpl();
    }

    private PerconattributeDAOImpl() {
    }
}
