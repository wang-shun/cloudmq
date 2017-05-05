package com.cloudzone.cloudmq.api.impl.base;

import com.cloudzone.cloudmq.common.HeartbeatData;

/**
 * @author yintongqiang
 * @params 从cloudzone定时获取信息
 * @since 2017/4/12
 */
public interface MQHeartbeatListener {
    // doHeartbeat 根据topic和authKey请求远程接口返回心跳数据 2017/4/12 Add by yintongqiang
    HeartbeatData doHeartbeat(String topic, String authKey);
}
