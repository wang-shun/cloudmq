/*
 * Copyright (c) 2015 wish.com All rights reserved.
 * 本软件源代码版权归----所有,未经许可不得任意复制与传播.
 */
package com.alibaba.rocketmq.dao.base;

import com.alibaba.rocketmq.domain.base.BaseDomain;
import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;


/**
 * 对mybatis的支持<br/>
 * spring配置文件需定义sqlTemplate与batchSqlTemplate
 *
 * @author ttx
 * @since 2015-06-16
 */
abstract class MyBatisSupport {

    protected static final Logger LOGGER = LoggerFactory.getLogger(MyBatisSupport.class);

    @Resource
    private SqlSessionTemplate sqlTemplate;

    @Resource
    private SqlSessionTemplate batchSqlTemplate;

    /**
     * SqlSessionTemplate
     *
     * @param batch    是否批处理
     * @param readonly 是否只读
     * @return
     */
    protected SqlSessionTemplate getSqlTemplate(boolean batch, boolean readonly) throws RuntimeException {
        if (readonly) {
        }

        if (batch) {
            return batchSqlTemplate;
        }
        return sqlTemplate;
    }

    /**
     * 新增对象,并返回对象的自增主键，要求主键名称字段为id
     *
     * @param statement
     * @param parameter
     * @return
     */
    protected Integer insert(String statement, Object parameter) throws RuntimeException {
        Integer res = 0;
        try {
            if (parameter != null) {
                res = getSqlTemplate(false, false).insert(statement, parameter);
            }
            if (parameter instanceof BaseDomain && res > 0) {
                res = ((BaseDomain) parameter).getId();
            } else {
                Field[] fields = parameter.getClass().getDeclaredFields();
                if (fields != null && fields.length > 0) {
                    for (Field field : fields) {
                        if ("id".equals(field.getName())) {
                            field.setAccessible(true);
                            res = (Integer) field.get(parameter);
                        }
                    }
                }
            }
            return res;
        } catch (Exception ex) {
            LOGGER.error("Mybatis execute add operation error.", ex);
            throw new RuntimeException("Mybatis execute add error.", ex);
        }
    }


    /**
     * 新增对象,并返回对象的自增主键，要求主键名称字段为id
     *
     * @param statement
     * @param parameter
     * @return
     */
    protected Integer insertByMap(String statement, Map<String, Object> parameter) throws RuntimeException {
        Integer res = 0;
        try {
            if (parameter != null) {
                res = getSqlTemplate(false, false).insert(statement, parameter);
            }
            return res > 0 ? (Integer) parameter.get("id") : res;
        } catch (Exception ex) {
            LOGGER.error("Mybatis execute add operation error.", ex);
            throw new RuntimeException("Mybatis execute add error.", ex);
        }
    }

    /**
     * 删除对象
     *
     * @param statement
     * @param parameter
     * @return
     */
    protected int delete(String statement, Object parameter) throws RuntimeException {
        int res = 0;
        try {
            res = getSqlTemplate(false, false).delete(statement, parameter);
        } catch (Exception ex) {
            throw new RuntimeException("Mybatis execute delete error.", ex);
        }
        return res;
    }

    /**
     * 更新对象
     *
     * @param statement
     * @param parameter
     * @return
     */
    protected int update(String statement, Object parameter) throws RuntimeException {
        int res = 0;
        try {
            if (parameter != null) {
                res = getSqlTemplate(false, false).update(statement, parameter);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Mybatis execute update error.", ex);
        }
        return res;
    }

    /**
     * 查询一条记录
     *
     * @param statement
     * @param parameter
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    protected <T> T select(String statement, Object parameter) throws RuntimeException {
        T obj = null;
        try {
            obj = (T) getSqlTemplate(false, true).selectOne(statement, parameter);
        } catch (Exception ex) {
            throw new RuntimeException("Mybatis execute select error.", ex);
        }
        return obj;
    }

    /**
     * 查询列表
     *
     * @param <T>
     * @param statement
     * @param parameter
     * @param
     * @return
     */
    protected <T> List<T> selectList(String statement, Object parameter) throws RuntimeException {
        List<T> list = null;
        try {
            list = getSqlTemplate(false, true).selectList(statement, parameter);
        } catch (Exception ex) {
            throw new RuntimeException("Mybatis execute select for list error", ex);
        }
        return list;
    }

    /**
     * 查询Map
     *
     * @param <K>
     * @param <V>
     * @param statement
     * @param parameter
     * @param mapKey
     * @return
     */
    protected <K, V> Map<K, V> selectMap(String statement, Object parameter, String mapKey) throws RuntimeException {
        Map<K, V> map = null;
        try {
            map = getSqlTemplate(false, true).selectMap(statement, parameter, mapKey);
        } catch (Exception ex) {
            throw new RuntimeException("Mybatis execute select for map error", ex);
        }
        return map;
    }

}
