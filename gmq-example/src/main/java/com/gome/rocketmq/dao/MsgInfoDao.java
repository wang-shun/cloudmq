/*
 * Copyright (c) 2015 wish.com All rights reserved.
 * 本软件源代码版权归----所有,未经许可不得任意复制与传播.
 */
package com.gome.rocketmq.dao;

import com.gome.rocketmq.dao.base.BaseDaoImpl;
import com.gome.rocketmq.domain.MsgInfo;
import org.springframework.stereotype.Component;


/**
 * MsgInfoDao 实现类
 * 
 * @author ttx
 * @since 2016-6-16
 */
@Component
public class MsgInfoDao extends BaseDaoImpl<MsgInfo, Integer> {

    // 返回本DAO命名空间,并添加statement
    public String getNameSpace(String statement) {
        return this.getClass().getName() + "." + statement;
    }

}