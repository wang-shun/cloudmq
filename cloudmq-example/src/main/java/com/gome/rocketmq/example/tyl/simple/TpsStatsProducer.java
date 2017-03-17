package com.gome.rocketmq.example.tyl.simple;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author tianyuliang
 * @date 2016/7/4
 */
public class TpsStatsProducer {

    // 1 发送成功次数
    private final AtomicLong sendRequestSuccessCount = new AtomicLong(0L);
    // 2 发送失败次数
    private final AtomicLong sendRequestFailedCount = new AtomicLong(0L);
    // 3 接收成功次数
    private final AtomicLong receiveResponseSuccessCount = new AtomicLong(0L);
    // 4 接收失败次数
    private final AtomicLong receiveResponseFailedCount = new AtomicLong(0L);
    // 5 发送消息成功的总耗时
    private final AtomicLong sendMessageSuccessTimeTotal = new AtomicLong(0L);
    // 6 发送消息最大耗时时间
    private final AtomicLong sendMessageMaxRT = new AtomicLong(0L);


    public Long[] createSnapshot() {
        Long[] snap = new Long[]{//
                System.currentTimeMillis(),//
                this.sendRequestSuccessCount.get(),//
                this.sendRequestFailedCount.get(),//
                this.receiveResponseSuccessCount.get(),//
                this.receiveResponseFailedCount.get(),//
                this.sendMessageSuccessTimeTotal.get(), //
        };
        return snap;
    }

    public AtomicLong getSendRequestSuccessCount() {
        return sendRequestSuccessCount;
    }

    public AtomicLong getSendRequestFailedCount() {
        return sendRequestFailedCount;
    }

    public AtomicLong getReceiveResponseSuccessCount() {
        return receiveResponseSuccessCount;
    }

    public AtomicLong getReceiveResponseFailedCount() {
        return receiveResponseFailedCount;
    }

    public AtomicLong getSendMessageSuccessTimeTotal() {
        return sendMessageSuccessTimeTotal;
    }

    public AtomicLong getSendMessageMaxRT() {
        return sendMessageMaxRT;
    }

}
