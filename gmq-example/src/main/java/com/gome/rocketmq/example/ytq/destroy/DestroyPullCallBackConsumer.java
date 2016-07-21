package com.gome.rocketmq.example.ytq.destroy;

import com.alibaba.rocketmq.client.consumer.DefaultMQPullConsumer;
import com.alibaba.rocketmq.client.consumer.PullCallback;
import com.alibaba.rocketmq.client.consumer.PullResult;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.alibaba.rocketmq.common.message.MessageQueue;
import com.gome.rocketmq.common.MyUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by yintongjiang on 2016/7/18.
 */
public class DestroyPullCallBackConsumer {
    private final static String group = "destroyConsumerGroup";
    private final static String topicName = "destroyTopic_14";
    private final static String tags = "A";
    private final static AtomicLong msgCount = new AtomicLong(0L);
    private static final Map<MessageQueue, Long> offsetTable = new HashMap<MessageQueue, Long>();
    //最大32超过也为32
    private final static int maxNums = 32;

    public static void main(String[] args) throws MQClientException {
        DefaultMQPullConsumer consumer = new DefaultMQPullConsumer(group);
        consumer.setNamesrvAddr(MyUtils.getNamesrvAddr());
        consumer.start();
        Set<MessageQueue> mqs = consumer.fetchSubscribeMessageQueues(topicName);
        for (final MessageQueue mq : mqs) {
            System.out.println("Consume from the queue: " + mq);
            final AtomicBoolean mgNotNewMsg = new AtomicBoolean(false);
            while (!mgNotNewMsg.get()) {
                try {
                    consumer.pull(mq, tags, getMessageQueueOffset(mq), maxNums, new PullCallback() {
                        @Override
                        public void onSuccess(PullResult pullResult) {
                            //// TODO: 2016/7/20 此处nextBeginOffset一直没变导致死循环
                            putMessageQueueOffset(mq, pullResult.getNextBeginOffset());
                            switch (pullResult.getPullStatus()) {
                                case FOUND:
                                    List<MessageExt> msgExtList = pullResult.getMsgFoundList();
                                    if (null != msgExtList) {
                                        for (MessageExt msg : msgExtList) {
                                            msgCount.incrementAndGet();
                                            System.out.println(Thread.currentThread().getName() +
                                                    " topic:" + msg.getTopic() + ",body:" + new String(msg.getBody()) +
                                                    ",tags=" + msg.getTags() + ",msgId=" + msg.getKeys() + ",msgCount=" + msgCount.get());
                                        }
                                    }
                                    break;
                                case NO_MATCHED_MSG:
                                    break;
                                case NO_NEW_MSG:
                                    mgNotNewMsg.compareAndSet(false, true);
                                    break;
                                case OFFSET_ILLEGAL:
                                    break;
                                default:
                                    break;
                            }
                        }

                        @Override
                        public void onException(Throwable e) {
                        }
                    });


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        consumer.shutdown();
    }


    private static void putMessageQueueOffset(MessageQueue mq, long offset) {
        offsetTable.put(mq, offset);
    }


    private static long getMessageQueueOffset(MessageQueue mq) {
        Long offset = offsetTable.get(mq);
        if (offset != null)
            return offset;

        return 0;
    }
}
