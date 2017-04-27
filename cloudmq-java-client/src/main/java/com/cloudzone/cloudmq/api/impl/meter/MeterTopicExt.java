package com.cloudzone.cloudmq.api.impl.meter;

import com.cloudzone.cloudlimiter.meter.MeterTopic;

/**
 * @author yintongjiang
 * @params
 * @since 2017/4/13
 */
public class MeterTopicExt extends MeterTopic {
    private String authKey;
    private int processType;

    public String getAuthKey() {
        return authKey;
    }

    public void setAuthKey(String authKey) {
        this.authKey = authKey;
    }

    public int getProcessType() {
        return processType;
    }

    public void setProcessType(int processType) {
        this.processType = processType;
    }

    public MeterTopicExt(String tag, String authKey, String type, int processType) {
        this.setTag(tag);
        this.authKey = authKey;
        this.setType(type);
        this.processType = processType;
    }

    @Override
    public String toString() {
        return "MeterTopicExt{" +
                "tag='" + this.getTag() + '\'' +
                "authKey='" + this.getAuthKey() + '\'' +
                ", type='" + this.getType() + '\'' +
                ", processType=" + processType +
                '}';
    }
}
