package cn.com.pyc.drm.model.db.practice;

import cn.com.pyc.drm.model.db.bean.AlbumContentHistory;
import cn.com.pyc.drm.model.dbBase.SZBaseDAOPracticeImpl;

public class AlbumContentHistoryDAOImpl extends SZBaseDAOPracticeImpl<AlbumContentHistory>
        implements AlbumContentHistoryDAO {

    private static AlbumContentHistoryDAOImpl acdi = null;


    public static AlbumContentHistoryDAOImpl getInstance() {
        if (acdi == null) {
            acdi = new AlbumContentHistoryDAOImpl();
        }
        return acdi;
    }

}
