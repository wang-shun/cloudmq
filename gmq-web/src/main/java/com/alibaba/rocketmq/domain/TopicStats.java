package com.alibaba.rocketmq.domain;

import com.alibaba.rocketmq.common.message.MessageQueue;

/**
 * @author: tianyuliang
 * @since: 2016/7/20
 */
public class TopicStats extends MessageQueue {

    private Long minOffset;

    private Long maxOffset;

    private String lastUpdated;

    public Long getMinOffset() {
        return minOffset;
    }

    public void setMinOffset(Long minOffset) {
        this.minOffset = minOffset;
    }

    public Long getMaxOffset() {
        return maxOffset;
    }

    public void setMaxOffset(Long maxOffset) {
        this.maxOffset = maxOffset;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
