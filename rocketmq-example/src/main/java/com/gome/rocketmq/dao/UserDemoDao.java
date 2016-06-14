/*
 * Copyright (c) 2015 wish.com All rights reserved.
 * 本软件源代码版权归----所有,未经许可不得任意复制与传播.
 */
package com.gome.rocketmq.dao;

import com.gome.rocketmq.dao.base.BaseDaoImpl;
import com.gome.rocketmq.domain.UserDemo;
import org.springframework.stereotype.Repository;


/**
 * UserDemoDao 实现类
 * 
 * @author ttx
 * @since 2015-06-16
 */
@Repository("userDemoDao")
public class UserDemoDao extends BaseDaoImpl<UserDemo, Integer> {
    private final static String NAMESPACE = "com.gome.rocketmq.dao.userDemoDao.";


    // 返回本DAO命名空间,并添加statement
    public String getNameSpace(String statement) {
        return NAMESPACE + statement;
    }

}