package com.gome.rocketmq.example.lj.simlpe;

import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.common.consumer.ConsumeFromWhere;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.gome.rocketmq.example.lj.common.MyUtils;

import java.util.List;

/**
 * @author jerrylou
 * @params
 * @since 2016/7/21 0021
 */
public class BasicConsumerTest {

    public static void main(String[] args) throws MQClientException {

        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(MyUtils.getConsumerGroup());
        consumer.setNamesrvAddr(MyUtils.getNamesrvAddr());

        consumer.subscribe(MyUtils.getTopicName(), MyUtils.getTags());
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);

        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                System.out.println(Thread.currentThread().getName() + " Receive New Messages: " + msgs);

                int length = msgs.size();
                for (int i = 0; i < length; i++) {
                    MessageExt msg = msgs.get(i);
                    if (msg.getTopic().equals(MyUtils.getTopicName())) {
                        if (msg.getTags().equals(MyUtils.getTags())) {
                            System.out.println("success - " + new String(msg.getBody()));
                        } else {
                            System.out.println("tags not equal." + msg.getTags());
                        }
                    } else {
                        System.out.println("topic not equal." + msg.getTopic());
                    }
                }

                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });

        consumer.start();

        System.out.println("Consumer Started.");
    }
}
