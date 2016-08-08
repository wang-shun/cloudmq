package com.gome.rocketmq.example.tyl.broadcast;

import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.alibaba.rocketmq.common.protocol.heartbeat.MessageModel;
import com.gome.rocketmq.common.MyUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author: tianyuliang
 * @since: 2016/6/22
 */
public class BroadCastConsumer1 {
    public static void main(String[] args) throws InterruptedException, MQClientException {
        try {
            final AtomicLong success = new AtomicLong(0L);
            final DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("destroyConsumerGroup_5");
            consumer.setNamesrvAddr(MyUtils.getNamesrvAddr());
            consumer.subscribe("flow_topic", "*");
            consumer.setMessageModel(MessageModel.BROADCASTING);
            consumer.registerMessageListener(new MessageListenerConcurrently() {
                @Override
                public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                    for (MessageExt msg : msgs) {
                        System.out.println("consumer=" + consumer.getConsumerGroup()
                                + ", success=" + success.incrementAndGet() + ", msgId=" + new String(msg.getMsgId())
                                + ", queueId=" + msg.getQueueId() + ",offset=" + msg.getQueueOffset());
                    }
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }
            });
            consumer.start();
            System.out.println("PushConsumer Started.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
