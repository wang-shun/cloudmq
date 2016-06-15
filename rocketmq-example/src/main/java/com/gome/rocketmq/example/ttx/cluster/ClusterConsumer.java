package com.gome.rocketmq.example.ttx.cluster;

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


/**
 * @author tantexian
 * @since 2016/6/8
 */
public class ClusterConsumer {

    static DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("ClusterConsumerGroup");

    public static void main(String[] args) throws InterruptedException, MQClientException {
        clusterPush(1);
        /*clusterPush(2);
        clusterPush(3);*/
        //consumer.start();
    }


    public static void clusterPush(int flag) throws InterruptedException, MQClientException {

        consumer.setNamesrvAddr(MyUtils.getNamesrvAddr());

        final int nums = flag;

        /**
         * 设置Consumer第一次启动是从队列头部开始消费还是队列尾部开始消费<br>
         * 如果非第一次启动，那么按照上次消费的位置继续消费
         */
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);

        consumer.setMessageModel(MessageModel.CLUSTERING);

        consumer.subscribe("clusterTopic", "*");

        consumer.registerMessageListener(new MessageListenerConcurrently() {

            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs,
                    ConsumeConcurrentlyContext context) {
                for (MessageExt msg : msgs) {
                    System.out.println(nums + "--" + Thread.currentThread().getName() + " Receive New Messages: " + new String(msg.getBody()));
                }
               // System.out.println(Thread.currentThread().getName() + " Receive New Messages: " );

                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });

        consumer.start();

        System.out.println("Cluster Consumer Started.");
    }
}
