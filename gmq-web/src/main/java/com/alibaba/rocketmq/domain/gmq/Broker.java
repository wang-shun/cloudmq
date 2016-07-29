package com.alibaba.rocketmq.domain.gmq;

/**
 * @author: tianyuliang
 * @since: 2016/7/19
 */
public class Broker {
    private long brokerID;
    private String addr;
    private String version;
    private double inTPS;
    private double outTPS;
    private double inTotalYest;
    private double inTotalToday;
    private double outTotalYest;
    private double outTotalTodtay;
    private String brokerName;

    public long getBrokerID() {
        return brokerID;
    }

    public void setBrokerID(long brokerID) {
        this.brokerID = brokerID;
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

    public double getInTPS() {
        return inTPS;
    }

    public void setInTPS(double inTPS) {
        this.inTPS = inTPS;
    }

    public double getOutTPS() {
        return outTPS;
    }

    public void setOutTPS(double outTPS) {
        this.outTPS = outTPS;
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

    public String getBrokerName() {
        return brokerName;
    }

    public void setBrokerName(String brokerName) {
        this.brokerName = brokerName;
    }
}
