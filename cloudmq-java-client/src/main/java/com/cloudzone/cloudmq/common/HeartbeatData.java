package com.cloudzone.cloudmq.common;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * @author yintongjiang
 * @params
 * @since 2017/4/12
 */
public class HeartbeatData {
    //峰值tps
    @JSONField(name = "tps")
    private long tps;
    //峰值流量tps单位字节
    @JSONField(name = "f_tps")
    private long flowTps;
    //剩余流量(0:无流量，1:有流量)
    @JSONField(name = "s_flow")
    private int surplusFlow;
    //剩余时间(0:无时间，1:有时间)
    @JSONField(name = "s_time")
    private int surplusTime;

    public HeartbeatData(long tps, long flowTps, int surplusFlow, int surplusTime) {
        this.tps = tps;
        this.flowTps = flowTps;
        this.surplusFlow = surplusFlow;
        this.surplusTime = surplusTime;
    }

    public HeartbeatData() {

    }

    public long getTps() {
        return tps;
    }

    public void setTps(long tps) {
        this.tps = tps;
    }

    public long getFlowTps() {
        return flowTps;
    }

    public void setFlowTps(long flowTps) {
        this.flowTps = flowTps;
    }

    public int getSurplusFlow() {
        return surplusFlow;
    }

    public void setSurplusFlow(int surplusFlow) {
        this.surplusFlow = surplusFlow;
    }

    public int getSurplusTime() {
        return surplusTime;
    }

    public void setSurplusTime(int surplusTime) {
        this.surplusTime = surplusTime;
    }
}
