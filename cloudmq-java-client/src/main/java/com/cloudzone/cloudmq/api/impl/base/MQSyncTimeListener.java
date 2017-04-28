package com.cloudzone.cloudmq.api.impl.base;

/**
 * @author yintongjiang
 * @params
 * @since 2017/4/25
 */
public interface MQSyncTimeListener {
    void syncTime(long timestamp);
}
