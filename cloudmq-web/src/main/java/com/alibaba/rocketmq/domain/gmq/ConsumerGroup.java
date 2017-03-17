package com.alibaba.rocketmq.domain.gmq;

import com.alibaba.rocketmq.common.message.MessageQueue;

/**
 * @author: tianyuliang
 * @since: 2016/7/19
 */
public class ConsumerGroup extends MessageQueue {

    private Long brokerOffset;

    private Long consumerOffset;

    private Long diffOffset;

    public Long getBrokerOffset() {
        return brokerOffset;
    }

    public void setBrokerOffset(Long brokerOffset) {
        this.brokerOffset = brokerOffset;
    }

    public Long getConsumerOffset() {
        return consumerOffset;
    }

    public void setConsumerOffset(Long consumerOffset) {
        this.consumerOffset = consumerOffset;
    }

    public Long getDiffOffset() {
        return diffOffset;
    }

    public void setDiffOffset(Long diffOffset) {
        this.diffOffset = diffOffset;
    }
}
