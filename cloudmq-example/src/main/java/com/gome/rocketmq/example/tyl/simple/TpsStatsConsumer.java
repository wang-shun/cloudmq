package com.gome.rocketmq.example.tyl.simple;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author tianyuliang
 * @date 2016/7/4
 */
public class TpsStatsConsumer {
    // 1 接收消息总数
    private final AtomicLong receiveMessageTotalCount = new AtomicLong(0L);
    // 2 消费总耗时
    private final AtomicLong born2ConsumerTotalRT = new AtomicLong(0L);
    // 3 存储总耗时
    private final AtomicLong store2ConsumerTotalRT = new AtomicLong(0L);
    // 4 消费最大耗时
    private final AtomicLong born2ConsumerMaxRT = new AtomicLong(0L);
    // 5 存储最大耗时
    private final AtomicLong store2ConsumerMaxRT = new AtomicLong(0L);


    public Long[] createSnapshot() {
        Long[] snap = new Long[]{//
                System.currentTimeMillis(),//
                this.receiveMessageTotalCount.get(),//
                this.born2ConsumerTotalRT.get(),//
                this.store2ConsumerTotalRT.get(),//
                this.born2ConsumerMaxRT.get(),//
                this.store2ConsumerMaxRT.get(), //
        };
        return snap;
    }


    public AtomicLong getReceiveMessageTotalCount() {
        return receiveMessageTotalCount;
    }

    public AtomicLong getBorn2ConsumerTotalRT() {
        return born2ConsumerTotalRT;
    }

    public AtomicLong getStore2ConsumerTotalRT() {
        return store2ConsumerTotalRT;
    }

    public AtomicLong getBorn2ConsumerMaxRT() {
        return born2ConsumerMaxRT;
    }

    public AtomicLong getStore2ConsumerMaxRT() {
        return store2ConsumerMaxRT;
    }



}
