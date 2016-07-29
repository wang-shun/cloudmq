package com.alibaba.rocketmq.domain.gmq;

import java.util.List;

/**
 * @author: tianyuliang
 * @since: 2016/7/19
 */
public class Cluster {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Broker> getBrokerList() {
        return brokerList;
    }

    public void setBrokerList(List<Broker> brokerList) {
        this.brokerList = brokerList;
    }

    private List<Broker> brokerList;
}
