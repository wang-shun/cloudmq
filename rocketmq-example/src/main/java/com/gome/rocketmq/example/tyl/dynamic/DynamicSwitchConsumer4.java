package com.gome.rocketmq.example.tyl.dynamic;

import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.common.consumer.ConsumeFromWhere;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.gome.rocketmq.common.MyUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author tianyuliang
 * @date 2016/6/27
 */
public class DynamicSwitchConsumer4 {


    public static void main(String[] args) throws InterruptedException, MQClientException {
        try {
            final AtomicLong success = new AtomicLong(0);
            final DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("DynamicSwitchConsumerGroup_2");
            consumer.setNamesrvAddr(MyUtils.getNamesrvAddr());
            consumer.subscribe("dynamicSwitchTopicTest", "*");
            consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
            consumer.registerMessageListener(new MessageListenerConcurrently() {
                @Override
                public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                    for (MessageExt msg : msgs) {
                        sleepTime(50);
                        System.out.println("instanceName=" + consumer.getInstanceName() + ",queueId=" + msg.getQueueId()
                                + ",msgId=" + msg.getMsgId() + ", success=" + success.incrementAndGet()
                                + ", body=" + new String(msg.getBody()) + ",storeHost=" + msg.getStoreHost());
                    }
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }
            });
            consumer.start();
            System.out.println("consumerGroupName=" + consumer.getConsumerGroup() + ",instanceName=" + consumer.getInstanceName() + " consumer started ...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void sleepTime(int second) {
        try {
            Thread.sleep(second * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
