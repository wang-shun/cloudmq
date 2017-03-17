package com.gome.rocketmq.example.tyl.simple;

import com.alibaba.rocketmq.client.consumer.DefaultMQPullConsumer;
import com.alibaba.rocketmq.client.consumer.PullCallback;
import com.alibaba.rocketmq.client.consumer.PullResult;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.common.MixAll;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.alibaba.rocketmq.common.message.MessageQueue;
import com.gome.rocketmq.common.MyUtils;
import org.apache.commons.collections.CollectionUtils;

import java.util.*;

/**
 * test max pullBatchNumbers
 *
 * @author tianyuliang
 * @date 2016/6/24
 */
public class PullConsumer {
    private static final Map<MessageQueue, Long> offseTable = new HashMap<MessageQueue, Long>();

    public static void main(String[] args) throws MQClientException {
        DefaultMQPullConsumer consumer = new DefaultMQPullConsumer("destroyConsumerGroup_1");
        consumer.setNamesrvAddr(MyUtils.getNamesrvAddr());
        consumer.start();
        System.out.println("pull consumer start.");
        int maxNums = 10;
        Set<MessageQueue> mqs = consumer.fetchSubscribeMessageQueues("flow_topic");
        for (MessageQueue mq : mqs) {
            System.out.println("consume from the queue: " + mq);
            SINGLE_MQ: while (true) {
                try {
                    PullResult pullResult = consumer.pullBlockIfNotFound(mq, null, getMessageQueueOffset(mq), maxNums);
                    List<MessageExt> msgExtList = pullResult.getMsgFoundList();
                    if (msgExtList == null){
                        System.out.println("msgExtList is empty.");
                        continue ;
                    }
                   // System.out.println(pullResult);
                    Long maxOffset = pullResult.getMaxOffset();
                    Long msgExtSize = new Long((long) msgExtList.size());
                    Long nextBeginOffset = pullResult.getNextBeginOffset();
                    System.out.println("maxOffset=" + maxOffset + ",msgExtSize=" + msgExtSize + ",nextBeginOffset=" + nextBeginOffset + ",maxNums=" + maxNums);

                    putMessageQueueOffset(mq, nextBeginOffset);
                    //sleepTime(1);
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
                }
                catch (Exception e) {
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
