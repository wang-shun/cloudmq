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

/**
 * @author: tianyuliang
 * @since: 2016/6/22
 */
public class BroadCastConsumer1 {
    public static void main(String[] args) throws InterruptedException, MQClientException {
        try {
            final DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(MyUtils.getDefaultCluster());
            consumer.setNamesrvAddr(MyUtils.getNamesrvAddr());
            consumer.subscribe("broadcastTopicTest", "*");
            consumer.setMessageModel(MessageModel.BROADCASTING);
            consumer.setInstanceName("instanceName1111");
            consumer.registerMessageListener(new MessageListenerConcurrently() {
                @Override
                public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                    for (MessageExt msg : msgs) {
                        System.out.println("consumer=" + consumer.getConsumerGroup()
                                + ", instanceName=" + consumer.getInstanceName() + ", msgId=" + new String(msg.getMsgId())
                                + ", queueId=" + msg.getQueueId() + ",offset=" + msg.getQueueOffset());
                    }
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }
            });
            consumer.start();
            System.out.println("PullConsumer Started.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
