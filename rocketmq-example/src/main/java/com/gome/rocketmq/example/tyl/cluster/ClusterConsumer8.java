package com.gome.rocketmq.example.tyl.cluster;

import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.common.consumer.ConsumeFromWhere;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.alibaba.rocketmq.common.protocol.heartbeat.MessageModel;
import com.gome.rocketmq.common.MyUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * xxx
 *
 * @author tianyuliang
 * @date 2016/6/23
 */
public class ClusterConsumer8 {

    public static void main(String[] args) throws InterruptedException, MQClientException {
        try {
            final AtomicLong success = new AtomicLong(0);
            final DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(MyUtils.getDefaultCluster());
            consumer.setNamesrvAddr(MyUtils.getNamesrvAddr());
            consumer.subscribe("clusterTopicTest", "*");
            consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
            consumer.setMessageModel(MessageModel.CLUSTERING);
            consumer.registerMessageListener(new MessageListenerConcurrently() {
                @Override
                public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                    for (MessageExt msg : msgs) {
                        sleepTime(1);
                        System.out.println("instanceName=" + consumer.getInstanceName() + ",queueId=" + msg.getQueueId()
                                + ",msgId=" + msg.getMsgId() + ", success=" + success.incrementAndGet() + ", body=" + new String(msg.getBody()));
                    }
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }
            });
            consumer.start();
            System.out.println(consumer.getInstanceName() +" consumer started ...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void sleepTime(int second){
        try {
            Thread.sleep(second * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
