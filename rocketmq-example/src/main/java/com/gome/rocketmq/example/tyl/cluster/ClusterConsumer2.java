package com.gome.rocketmq.example.tyl.cluster;

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
 * @author: GaoYanLei
 * @since: 2016/6/3
 */
public class ClusterConsumer2 {
    public static void main(String[] args) throws InterruptedException, MQClientException {
        try {
            final DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(MyUtils.getDefaultCluster());
            consumer.setNamesrvAddr(MyUtils.getNamesrvAddr());
            consumer.subscribe("orderTopicTest", "*");
            consumer.setMessageModel(MessageModel.BROADCASTING);
            consumer.setInstanceName("instanceName2222");
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
            System.out.println("Consumer Started.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
