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

    private Long brokerId;

    private String brokerName;

    private String addr;

    private String version;

    private Long inTps;

    private Long outTps;

    private Long inTotalYest;

    private Long inTotalToday;

    private Long outTotalYest;

    private Long outTotalTodtay;

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public Long getBrokerId() {
        return brokerId;
    }

    public void setBrokerId(Long brokerId) {
        this.brokerId = brokerId;
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

    public Long getInTps() {
        return inTps;
    }

    public void setInTps(Long inTps) {
        this.inTps = inTps;
    }

    public Long getOutTps() {
        return outTps;
    }

    public void setOutTps(Long outTps) {
        this.outTps = outTps;
    }

    public Long getInTotalYest() {
        return inTotalYest;
    }

    public void setInTotalYest(Long inTotalYest) {
        this.inTotalYest = inTotalYest;
    }

    public Long getInTotalToday() {
        return inTotalToday;
    }

    public void setInTotalToday(Long inTotalToday) {
        this.inTotalToday = inTotalToday;
    }

    public Long getOutTotalYest() {
        return outTotalYest;
    }

    public void setOutTotalYest(Long outTotalYest) {
        this.outTotalYest = outTotalYest;
    }

    public Long getOutTotalTodtay() {
        return outTotalTodtay;
    }

    public void setOutTotalTodtay(Long outTotalTodtay) {
        this.outTotalTodtay = outTotalTodtay;
    }
}
