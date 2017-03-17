package com.gome.rocketmq.example.tyl.order.simple;

import org.apache.derby.impl.jdbc.ReaderToAscii;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author tianyuliang
 * @date 2016/6/30
 */
public class TpsStats {

    private  Long sendCurrentTimeMillis = 0L;
    // 1 发送成功次数
    private final AtomicLong sendRequestSuccessCount = new AtomicLong(0L);
    // 2 发送失败次数
    private final AtomicLong sendRequestFailedCount = new AtomicLong(0L);
    // 3 发送成功总耗时
    private final AtomicLong sendMessageSuccessTimeTotal = new AtomicLong(0L);
    // 4 topic名称索引
    private final AtomicLong topicNameIndex = new AtomicLong(0L);

    private Long[] snap = null;

    public Long[] buildSnapList(){
        snap = new Long[]{//
                System.currentTimeMillis(),         //
                this.sendRequestSuccessCount.get(),//
                this.sendRequestFailedCount.get(),//
                this.sendMessageSuccessTimeTotal.get(),//
                this.topicNameIndex.get()
        };
        sendCurrentTimeMillis = snap[0];
        return snap;
    }

    public Long getSendCurrentTimeMillis() {
        return sendCurrentTimeMillis;
    }

    private String buildSnap2String(Long[] snap) {
        return String.format("success=%s,timeTotal=%s,topicIndex=%s,fail=%s,currMillis=%s",
                snap[1], snap[3], snap[4], snap[2], snap[0]);
    }

    private String buildSnap2String(TpsStats tpsStats) {
        return String.format("success=%s,timeTotal=%s,topicIndex=%s,fail=%s,currMillis=%s",
                tpsStats.getSendRequestSuccessCount().get(), tpsStats.getSendMessageSuccessTimeTotal().get(),
                tpsStats.getTopicNameIndex(), tpsStats.getSendRequestFailedCount().get(), tpsStats.getSendCurrentTimeMillis());
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

    public AtomicLong getTopicNameIndex() {
        return topicNameIndex;
    }

    public void setSendCurrentTimeMillis(Long sendCurrentTimeMillis) {
        this.sendCurrentTimeMillis = sendCurrentTimeMillis;
    }

    public Long[] getSnap() {
        return snap;
    }

    public void setSnap(Long[] snap) {
        this.snap = snap;
    }
}
