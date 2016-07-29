package com.gome.rocketmq.example.lj.stromenv;

import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.common.consumer.ConsumeFromWhere;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.gome.rocketmq.example.lj.common.StromUtils;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author jerrylou
 * @params
 * @since 2016/7/22 0022
 */
public class StromEnvNetUnreachableConsumer {

    private final static int nThreads = 1000;

    public static void main(String[] args) {
        try {
            testMain();
        } catch (MQClientException e) {
            e.printStackTrace();
        }
    }

    public static void testMain() throws MQClientException {
        // 创建消费者
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(StromUtils.getConsumerGroup());
        consumer.setNamesrvAddr(StromUtils.getNamesrvAddr());
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
        consumer.setConsumeThreadMin(nThreads);
        consumer.setConsumeThreadMax(nThreads);
        consumer.subscribe(StromUtils.getTopicName(), StromUtils.getTags());
        final AtomicLong msgCount = new AtomicLong(0L);
        final ConcurrentHashMap<String, String> CONCURRENT_HASH_MAP = new ConcurrentHashMap<>();

        // 注册监听
        consumer.registerMessageListener(new MessageListenerConcurrently() {

            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                for (MessageExt msg : msgs) {
                    msgCount.incrementAndGet();
                    CONCURRENT_HASH_MAP.put(msg.getKeys(), msg.getKeys());
                    System.out.println("keySize=" + CONCURRENT_HASH_MAP.size() + " " + " " + Thread.currentThread().getName() +
                            " bornHost=" + msg.getBornHostString() +
                            ",storeHost=" + msg.getStoreHost().toString() +
                            ",topic=" + msg.getTopic() + ",body=" + new String(msg.getBody()) +
                            ",tags=" + msg.getTags() + ",msgId=" + msg.getKeys() + ",msgCount=" + msgCount.get());
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });

        consumer.start();
        System.out.println("Consumer Started.");
    }
}
