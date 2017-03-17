/*
 * Copyright (c) 2015 wish.com All rights reserved.
 * 本软件源代码版权归----所有,未经许可不得任意复制与传播.
 */
package com.alibaba.rocketmq.dao;

import com.alibaba.rocketmq.dao.base.BaseDaoImpl;
import com.alibaba.rocketmq.domain.gmq.User;
import org.springframework.stereotype.Component;


/**
 * UserDemoDao 实现类
 * 
 * @author ttx
 * @since 2015-06-16
 */
@Component
public class UserDao extends BaseDaoImpl<User, Integer> {

    // 返回本DAO命名空间,并添加statement
    public String getNameSpace(String statement) {
        // 此处使用反射获取类名，与对应mybatis的xml的namespace保持一致 2016/6/16 Add by tantexixan
        return this.getClass().getName() + "." + statement;
    }

}