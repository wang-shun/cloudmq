package com.gome.rocketmq.example.tyl.dynamic;

import com.alibaba.rocketmq.client.consumer.DefaultMQPullConsumer;
import com.alibaba.rocketmq.client.consumer.PullResult;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.alibaba.rocketmq.common.message.MessageQueue;
import com.gome.rocketmq.common.MyUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author tianyuliang
 * @date 2016/6/27
 */
public class DynamicSwitchConsumer3 {


    private static final Map<MessageQueue, Long> offseTable = new HashMap<MessageQueue, Long>();

    public static void main(String[] args) throws MQClientException {
        DefaultMQPullConsumer consumer = new DefaultMQPullConsumer(MyUtils.getDefaultCluster());
        consumer.setNamesrvAddr(MyUtils.getNamesrvAddr());
        consumer.start();
        System.out.println("pull consumer start.");
        int maxNums = 1;
        AtomicLong success = new AtomicLong(0);
        Set<MessageQueue> mqs = consumer.fetchSubscribeMessageQueues("dynamicSwitchTopicTest");
        for (MessageQueue mq : mqs) {
            SINGLE_MQ:
            while (true) {
                try {
                    PullResult pullResult = consumer.pullBlockIfNotFound(mq, null, getMessageQueueOffset(mq), maxNums);
                    List<MessageExt> msgExtList = pullResult.getMsgFoundList();
                    if (msgExtList == null) {
                        System.out.println("msgExtList is empty.");
                        continue;
                    }
                    Long nextBeginOffset = pullResult.getNextBeginOffset();
                    for (MessageExt msg : msgExtList) {
                        System.out.println("instanceName=" + consumer.getInstanceName()
                                + ",queueId=" + msg.getQueueId() + ",offset=" + msg.getQueueOffset()
                                + ",msgId=" + msg.getMsgId() + ", success=" + success.incrementAndGet()
                                + ", body=" + new String(msg.getBody()) + ",storeHost=" + msg.getStoreHost());
                    //    sleepTime(10);
                    }

                    putMessageQueueOffset(mq, nextBeginOffset);

                    switch (pullResult.getPullStatus()) {
                        case FOUND:
                            // TODO
                            break;
                        case NO_MATCHED_MSG:
                            break;
                        case NO_NEW_MSG:
                            break SINGLE_MQ;
                        case OFFSET_ILLEGAL:
                            break;
                        default:
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        consumer.shutdown();
    }


    private static void putMessageQueueOffset(MessageQueue mq, long offset) {
        offseTable.put(mq, offset);
    }


    private static long getMessageQueueOffset(MessageQueue mq) {
        Long offset = offseTable.get(mq);
        return offset != null ? offset : 0L;
    }

    private static void sleepTime(int second) {
        try {
            Thread.sleep(second * 1000L);
        } catch (Exception e) {
            System.out.println("error:" + e.getMessage());
            e.printStackTrace();
        }
    }


}
