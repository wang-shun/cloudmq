package com.wish.rocketmq.example.gyl.cluster;

import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.common.message.MessageConst;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.alibaba.rocketmq.common.protocol.heartbeat.MessageModel;


/**
 * @author: GaoYanLei
 * @since: 2016/6/3
 */
public class ClusterConsumer2 {
    public static void main(String[] args) throws InterruptedException, MQClientException {
        final AtomicLong atomicSuccessNums = new AtomicLong(0l);
        try {
            DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("cluster");
            consumer.setNamesrvAddr("192.168.146.131:9876");
            consumer.subscribe("clusterTopic", "*");
            consumer.setMessageModel(MessageModel.CLUSTERING);
//            consumer.setMessageModel(MessageModel.BROADCASTING);
            consumer.registerMessageListener(new MessageListenerConcurrently() {
                @Override
                public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs,
                        ConsumeConcurrentlyContext context) {
                    MessageExt msg = msgs.get(0);
                    System.out.println(
                            Thread.currentThread().getName() + "==============" + new String(msg.getMsgId())
                                    + "===========" + new String(msg.getBody()) + "=======" + msg.getQueueId());
                    // long offset = msgs.get(0).getQueueOffset();
                    // String maxOffset =
                    // msgs.get(0).getProperty(MessageConst.PROPERTY_MAX_OFFSET);
                    // System.out.println();
                    // long diff = Long.parseLong(maxOffset) - offset;
                    // if (diff > 100) {
                    // // 处理消息堆积情况
                    // System.out.println("maxOffset:" + maxOffset + ",diff:" +
                    // offset + ",消息堆积个数:" + diff);
                    // return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                    // }
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
