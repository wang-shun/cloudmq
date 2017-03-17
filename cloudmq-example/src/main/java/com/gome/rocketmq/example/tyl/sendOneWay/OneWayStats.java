package com.gome.rocketmq.example.tyl.sendOneWay;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author tianyuliang
 * @date 2016/6/29
 */
public class OneWayStats {
    // 1 发送成功次数
    private final AtomicLong sendRequestSuccessCount = new AtomicLong(0L);
    // 2 发送失败次数
    private final AtomicLong sendRequestFailedCount = new AtomicLong(0L);
    // 3 发送成功总耗时
    private final AtomicLong sendMessageSuccessTimeTotal = new AtomicLong(0L);

    public Long[] createSnapshot() {
        Long[] snap = new Long[]{//
                System.currentTimeMillis(),//
                this.sendRequestSuccessCount.get(),//
                this.sendRequestFailedCount.get(),//
                this.sendMessageSuccessTimeTotal.get()
        };
        return snap;
    }

    public AtomicLong getSendRequestSuccessCount() {
        return sendRequestSuccessCount;
    }

    public AtomicLong getSendRequestFailedCount() {
        return sendRequestFailedCount;
    }

    public AtomicLong getSendMessageSuccessTimeTotal() {
        return sendMessageSuccessTimeTotal;
    }
}
