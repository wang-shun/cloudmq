package com.alibaba.rocketmq.domain.gmq;

/**
 * gmq-web站点页面需要的broker属性, 需要继承BaseDomain基础类
 *
 * @author: tianyuliang
 * @since: 2016/8/1
 */
public class BrokerExt extends Broker {

    private String brokerIp;

    private String brokerPort;

    private long runtimeDate;

    public String getBrokerIp() {
        return brokerIp;
    }

    public void setBrokerIp(String brokerIp) {
        this.brokerIp = brokerIp;
    }

    public String getBrokerPort() {
        return brokerPort;
    }

    public void setBrokerPort(String brokerPort) {
        this.brokerPort = brokerPort;
    }

    public long getRuntimeDate() {
        return runtimeDate;
    }

    public void setRuntimeDate(long runtimeDate) {
        this.runtimeDate = runtimeDate;
    }
}
