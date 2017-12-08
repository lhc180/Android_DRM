package cn.com.pyc.szpbb.sdk.database.dbBase;

import java.util.Collection;
import java.util.List;

public interface BaseDAOPractice<T> {
    /* 数据表创建 */

    void create(Class<T> entityClass);

    /* 数据实体保存 */
    boolean save(T entity);

    long[] batchSave(Collection<T> entityCollection);

    boolean cascadedSave(T entity);

    boolean cascadedBatchSave(Collection<T> entityCollection);

    /* 数据实体删除 */
    int delete(T entity);

    int[] batchDelete(Collection<T> entityCollection);

    boolean cascadedDeleteById(String id);

    boolean cascadedBatchDelete(Collection<T> entityCollection);

    /* 数据实体更新 */
    int update(T entity);

    int[] batchUpdate(Collection<T> entityCollection);

    boolean cascadedUpdate(T entity);

    boolean cascadedBatchUpdate(Collection<T> entityCollection);

    /* 数据实体查询 */
    T findById(String id);

    List<T> findAll(Class<T> entityClass, String order);

    List<?> findByQuery(String sql, Class<?> entityClass);

    List<?> findByQuery(String[] elements, String[] args, Class<?> entityClass);

    T findObjectByQuery(String[] elements, String[] args, Class<?> entityClass);

	/* 自定义SQL操作 */
    //void executeBySql(String sql);
}
