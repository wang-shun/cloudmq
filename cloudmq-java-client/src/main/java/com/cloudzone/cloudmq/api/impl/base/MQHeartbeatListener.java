package com.cloudzone.cloudmq.api.impl.base;

import com.cloudzone.cloudmq.common.HeartbeatData;

/**
 * @author yintongqiang
 * @params 从cloudzone定时获取信息
 * @since 2017/4/12
 */
public interface MQHeartbeatListener {

    HeartbeatData doHeartbeat(String topic, String authKey);
}
