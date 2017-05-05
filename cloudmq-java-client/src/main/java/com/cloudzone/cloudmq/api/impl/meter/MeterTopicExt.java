package com.cloudzone.cloudmq.api.impl.meter;

import com.cloudzone.cloudlimiter.meter.MeterTopic;
import com.cloudzone.cloudmq.common.ProcessMsgType;
import com.cloudzone.cloudmq.common.StatDataType;

/**
 * @author yintongjiang
 * @params
 * @since 2017/4/13
 */
public class MeterTopicExt extends MeterTopic {
    private String authKey;
    // 消息处理类型1，消费，0，发送
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

    /**
     * @param tag         topic名称
     * @param authKey     authKey
     * @param type        StatDataType枚举
     * @param processType ProcessMsgType枚举
     */
    public MeterTopicExt(String tag, String authKey, StatDataType type, ProcessMsgType processType) {
        this.setTag(tag);
        this.authKey = authKey;
        this.setType(type.getDes());
        this.processType = processType.getCode();
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
