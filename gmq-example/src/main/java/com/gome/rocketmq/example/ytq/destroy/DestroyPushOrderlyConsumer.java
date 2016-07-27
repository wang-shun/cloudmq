package com.gome.rocketmq.example.ytq.destroy;

import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.alibaba.rocketmq.client.consumer.listener.*;
import com.alibaba.rocketmq.client.consumer.rebalance.AllocateMessageQueueAveragelyByCircle;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.common.consumer.ConsumeFromWhere;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.gome.rocketmq.common.MyUtils;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by yintongjiang on 2016/7/18.
 */
public class DestroyPushOrderlyConsumer {
    private final static String group = "destroyConsumerGroup";
    private final static String topicName = "flow_topic_3";
    private final static String tags = "A";
    private final static AtomicLong msgCount = new AtomicLong(0L);
    private final static ConcurrentHashMap<String, String> CONCURRENT_HASH_MAP = new ConcurrentHashMap<>();

    public static void main(String[] args) throws MQClientException {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(group);
//        consumer.setAllocateMessageQueueStrategy(new AllocateMessageQueueAveragelyByCircle());
        consumer.setNamesrvAddr(MyUtils.getNamesrvAddr());
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
        consumer.setConsumeThreadMin(1000);
        consumer.setConsumeThreadMax(1000);
        consumer.subscribe(topicName, tags);
        consumer.registerMessageListener(new MessageListenerOrderly() {

            @Override
            public ConsumeOrderlyStatus consumeMessage(List<MessageExt> msgs, ConsumeOrderlyContext context) {
                for (MessageExt msg : msgs) {
                    CONCURRENT_HASH_MAP.put(msg.getKeys(), msg.getKeys());
                    msgCount.incrementAndGet();
                    System.out.println("keySize=" + CONCURRENT_HASH_MAP.size() + " " + Thread.currentThread().getName() + " bornHost=" + msg.getBornHostString() +
                            ",storeHost=" + msg.getStoreHost().toString() +
                            ",topic=" + msg.getTopic() + ",body=" + new String(msg.getBody()) +
                            ",tags=" + msg.getTags() + ",msgId=" + msg.getKeys() + ",msgCount=" + msgCount.get());
                }
                return ConsumeOrderlyStatus.SUCCESS;
            }
        });
        consumer.start();
        System.out.println("Consumer Started.");
    }
}
