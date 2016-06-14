package com.gome.rocketmq.example.gyl.cluster;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.alibaba.rocketmq.common.protocol.heartbeat.MessageModel;
import com.gome.rocketmq.common.MyUtil;


/**
 * @author: GaoYanLei
 * @since: 2016/6/3
 */
public class ClusterConsumer4 {
    public static void main(String[] args) throws InterruptedException, MQClientException {
        final AtomicLong atomicSuccessNums = new AtomicLong(0l);
        try {
            DefaultMQPushConsumer consumer = new DefaultMQPushConsumer();
            consumer.setNamesrvAddr(MyUtil.getNamesrvAddr());
            consumer.subscribe("clusterTopic", "*");
            consumer.setMessageModel(MessageModel.CLUSTERING);
            consumer.setConsumerGroup("cluster");
            consumer.registerMessageListener(new MessageListenerConcurrently() {
                @Override
                public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs,
                        ConsumeConcurrentlyContext context) {
                    System.out
                        .println(Thread.currentThread().getName() + " Receive New Messages: " + msgs.size());
                    MessageExt msg = msgs.get(0);
                    System.out.println(Thread.currentThread().getName() + "=============="
                            + new String(msg.getMsgId()) + "==========" + msg.getQueueId());
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }
            });
            consumer.start();
            System.out.println("Consumer Started.");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
