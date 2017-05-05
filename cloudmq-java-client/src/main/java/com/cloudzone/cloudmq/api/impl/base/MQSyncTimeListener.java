package com.cloudzone.cloudmq.api.impl.base;

/**
 * @author yintongjiang
 * @params 定时拉去服务器时间与本地时间进行对比
 * @since 2017/4/25
 */
public interface MQSyncTimeListener {
    // syncTime 用于服务器时间与本地时间 2017/4/25 Add by yintongqiang
    void syncTime(long timestamp);
}
