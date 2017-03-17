package com.gome.rocketmq.example.tyl.failOver;

import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.alibaba.rocketmq.client.consumer.listener.*;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.common.consumer.ConsumeFromWhere;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.gome.rocketmq.common.MyUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author: tianyuliang
 * @since: 2016/7/26
 */
public class Consumer {

    public static void main(String[] args) throws MQClientException {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("ccccccccccccc");
        consumer.setNamesrvAddr(MyUtils.getNamesrvAddr());
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
        consumer.subscribe("haTopicTest", "*");
        final AtomicLong success = new AtomicLong(0L);
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                //System.out.println(Thread.currentThread().getName() + " Receive New Messages: " + msgs.toString());
                for (MessageExt msg : msgs) {

                    System.out.println(Thread.currentThread().getName() + " topic:" + msg.getTopic() + ",brokerAddr=" + msg.getStoreHost() + ",body:" + new String(msg.getBody()) + ",success=" + success.incrementAndGet());
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        consumer.start();
        System.out.println("Consumer Started.");
    }
}
