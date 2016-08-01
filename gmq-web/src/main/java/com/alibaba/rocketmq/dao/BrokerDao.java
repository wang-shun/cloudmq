package com.alibaba.rocketmq.dao;


import com.alibaba.rocketmq.dao.base.BaseDaoImpl;
import com.alibaba.rocketmq.domain.gmq.BrokerExt;
import org.springframework.stereotype.Component;


/**
 *
 * @author tianyuliang
 * @since 2016/8/1
 */
@Component
public class BrokerDao extends BaseDaoImpl<BrokerExt, Integer> {

    @Override
    public String getNameSpace(String statement) {
        return this.getClass().getName() + "." + statement;
    }
}
