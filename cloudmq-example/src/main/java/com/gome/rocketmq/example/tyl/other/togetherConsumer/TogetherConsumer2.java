package com.gome.rocketmq.example.tyl.other.togetherConsumer;

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
 *
 * @author tianyuliang
 * @date 2016/6/24
 */
public class TogetherConsumer2 {
    public static void main(String[] args) throws InterruptedException, MQClientException {
        try {
            final AtomicLong success = new AtomicLong(0);
            final DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(MyUtils.getDefaultCluster());
            consumer.setNamesrvAddr(MyUtils.getNamesrvAddr());
            consumer.subscribe("togetherTopicTest", "*");
            consumer.getOffsetStore().updateOffset(null, 1L, false);
            consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
            consumer.setMessageModel(MessageModel.CLUSTERING);
            consumer.registerMessageListener(new MessageListenerConcurrently() {
                @Override
                public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                    for (MessageExt msg : msgs) {
                        System.out.println("consumerGroupName=" + consumer.getConsumerGroup() +",instanceName=" + consumer.getInstanceName()
                                + ",queueId=" + msg.getQueueId() + ",msgId=" + msg.getMsgId() + ", offset=" + msg.getQueueOffset()
                                + ", body=" + new String(msg.getBody()) + ",storeHost=" + msg.getStoreHost());
                    }
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }
            });
            consumer.start();
            System.out.println(consumer.getInstanceName() + " consumer started ...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
