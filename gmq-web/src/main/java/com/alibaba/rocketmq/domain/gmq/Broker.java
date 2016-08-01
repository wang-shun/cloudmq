package com.alibaba.rocketmq.domain.gmq;

import com.alibaba.rocketmq.domain.base.BaseDomain;

/**
 * 从rocketmq获取的broker属性
 *
 * @author: tianyuliang
 * @since: 2016/7/19
 */
public class Broker extends BaseDomain {

    private static final long serialVersionUID = 1L;

    private String clusterName;

    private long brokerID;

    private String brokerName;

    private String addr;

    private String version;

    private double inTps;

    private double outTps;

    private double inTotalYest;

    private double inTotalToday;

    private double outTotalYest;

    private double outTotalTodtay;

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public long getBrokerID() {
        return brokerID;
    }

    public void setBrokerID(long brokerID) {
        this.brokerID = brokerID;
    }

    public String getBrokerName() {
        return brokerName;
    }

    public void setBrokerName(String brokerName) {
        this.brokerName = brokerName;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public double getInTps() {
        return inTps;
    }

    public void setInTps(double inTps) {
        this.inTps = inTps;
    }

    public double getOutTps() {
        return outTps;
    }

    public void setOutTps(double outTps) {
        this.outTps = outTps;
    }

    public double getInTotalYest() {
        return inTotalYest;
    }

    public void setInTotalYest(double inTotalYest) {
        this.inTotalYest = inTotalYest;
    }

    public double getInTotalToday() {
        return inTotalToday;
    }

    public void setInTotalToday(double inTotalToday) {
        this.inTotalToday = inTotalToday;
    }

    public double getOutTotalYest() {
        return outTotalYest;
    }

    public void setOutTotalYest(double outTotalYest) {
        this.outTotalYest = outTotalYest;
    }

    public double getOutTotalTodtay() {
        return outTotalTodtay;
    }

    public void setOutTotalTodtay(double outTotalTodtay) {
        this.outTotalTodtay = outTotalTodtay;
    }
}
