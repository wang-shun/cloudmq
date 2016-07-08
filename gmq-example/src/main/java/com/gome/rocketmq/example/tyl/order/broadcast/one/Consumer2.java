package com.gome.rocketmq.example.tyl.order.broadcast.one;

import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.common.consumer.ConsumeFromWhere;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.alibaba.rocketmq.common.protocol.heartbeat.MessageModel;
import com.gome.rocketmq.common.MyUtils;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author tianyuliang
 * @date 2016/7/6
 */
public class Consumer2 {

    private static final String topic = "orderTopic_broadcast_1";
    private static final String groupName = "OneBroadcastConsumerGroup";
    private static final String strDateTime = "yyyy-MM-dd#HH:mm:ss.SSS";

    public static void main(String[] args) throws InterruptedException, MQClientException {
        final AtomicLong success = new AtomicLong(0L);
        final DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(groupName);
        consumer.setNamesrvAddr(MyUtils.getNamesrvAddr());
        consumer.subscribe(topic, "*");
        consumer.setMessageModel(MessageModel.BROADCASTING);
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
        final SimpleDateFormat sdf = new SimpleDateFormat(strDateTime);
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                for (MessageExt msg : msgs) {
                    if (msg.getTopic().equals(topic)) {
                        System.out.println("success=" + success.incrementAndGet() + ",msgId=" + msg.getMsgId() + ",offset=" + msg.getQueueOffset()
                                + ",body=" + new String(msg.getBody()) + ",storeHost=" + msg.getStoreHost() + ",queueId=" + msg.getQueueId());
                        //  + ",storeTime=" + sdf.format(msg.getStoreTimestamp()) + ",bronTime=" + sdf.format(msg.getBornTimestamp())
                    } else {
                        System.out.println("error: " + msg.toString());
                    }
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        consumer.start();
        System.out.println("consumerGroupName=" + groupName + ",instanceName=" + consumer.getInstanceName() + ",topic=" + topic + " consumer started ...");
    }

}
