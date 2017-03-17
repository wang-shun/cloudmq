package com.gome.rocketmq.example.tyl.backtracking;

import com.alibaba.rocketmq.client.consumer.DefaultMQPullConsumer;
import com.alibaba.rocketmq.client.consumer.PullResult;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.alibaba.rocketmq.common.message.MessageQueue;
import com.gome.rocketmq.common.MyUtils;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 测试消息回溯，consumer端
 *
 * @author tianyuliang
 * @date 2016/6/30
 */
public class PullConsumer {

    private static final String yyyy_MM_dd_HH_mm_ss_SSS = "yyyy-MM-dd#HH:mm:ss:SSS";
    private static final Map<MessageQueue, Long> offseTable = new HashMap<MessageQueue, Long>();
    private static SimpleDateFormat sdf = new SimpleDateFormat(yyyy_MM_dd_HH_mm_ss_SSS);
    private static String topic = "backtrackingTopic";

    public static void main(String[] args) throws MQClientException {
        DefaultMQPullConsumer consumer = new DefaultMQPullConsumer("pull_consumer_group_test");
        consumer.setNamesrvAddr(MyUtils.getNamesrvAddr());
        consumer.start();
        System.out.println("groupName=" + consumer.getConsumerGroup() +
                ",instanceName=" + consumer.getInstanceName() + ",backtrackingTopic=" + topic + " consumer start.");
        int fetchPullNum = 1;
        Set<MessageQueue> mqs = consumer.fetchSubscribeMessageQueues(topic);
        for (MessageQueue mq : mqs) {
            SINGLE_MQ:
            while (true) {
                try {
                    PullResult pullResult = consumer.pullBlockIfNotFound(mq, null, getMessageQueueOffset(mq), fetchPullNum);
                    List<MessageExt> msgExtList = pullResult.getMsgFoundList();
                    if (msgExtList == null) {
                        System.out.println("msgExtList is empty.");
                        continue;
                    }
                    Date nowDate = null;
                    for (MessageExt msgExt : msgExtList) {
                        //nowDate = new Date();
                        System.out.println("msgId=" + msgExt.getMsgId() + ",offset=" + msgExt.getQueueOffset() + ",bornTimestamp=" + msgExt.getStoreTimestamp()
                                + "(" + sdf.format(new Date(msgExt.getStoreTimestamp())) + ")" + ",body=" + new String(msgExt.getBody())
                                + ",queueId=" + msgExt.getQueueId() + ",storeHost=" + msgExt.getStoreHost());
                        sleepTime(1);
                    }

                    putMessageQueueOffset(mq, pullResult.getNextBeginOffset());
                    sleepTime(1);

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
