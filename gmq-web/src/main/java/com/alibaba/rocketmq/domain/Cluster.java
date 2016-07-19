package com.alibaba.rocketmq.domain;

import java.util.List;

/**
 * Created by dell on 2016/7/18.
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
