package com.gome.rocketmq.example.tyl.breach.one;

import com.alibaba.fastjson.serializer.IntegerCodec;
import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.gome.rocketmq.common.MyUtils;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author: tianyuliang
 * @since: 2016/7/14
 */
public class Consumer {

    private static String topic = "breachTopic_1";
    private static Long total = 5 * 20000L;


    public static void main(String[] args) throws InterruptedException, MQClientException {
        final AtomicLong success = new AtomicLong(0);
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(MyUtils.getDefaultCluster());
        consumer.setNamesrvAddr(MyUtils.getNamesrvAddr());
        consumer.subscribe(topic, "*");
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                for (MessageExt msgExt : msgs) {
                    System.out.println("threadId=" + Thread.currentThread().getName() + ",success=" + success.incrementAndGet()
                            + ",storeHost=" + msgExt.getStoreHost() + ",body=" + new String(msgExt.getBody()));
                    sleepTime();
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        List<Integer> SSS = Lists.newArrayList();
        consumer.start();
        System.out.println("groupName=" + consumer.getConsumerGroup() + ",instanceName=" + consumer.getInstanceName() + " consumer start.");
    }

    private static void sleepTime() {
        try {
            Thread.sleep(100L);
        } catch (Exception e) {
            System.out.println("error:" + e.getMessage());
            e.printStackTrace();
        }
    }

}
