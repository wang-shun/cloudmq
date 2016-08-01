package com.alibaba.rocketmq.dao;


import com.alibaba.rocketmq.dao.base.BaseDaoImpl;
import com.alibaba.rocketmq.domain.gmq.Broker;

public class BrokerDao extends BaseDaoImpl<Broker, Integer> {

    @Override
    public String getNameSpace(String statement) {
        return this.getClass().getName() + "." + statement;
    }
}
