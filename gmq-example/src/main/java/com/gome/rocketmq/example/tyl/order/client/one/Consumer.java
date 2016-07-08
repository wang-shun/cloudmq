package com.gome.rocketmq.example.tyl.order.client.one;

import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.common.consumer.ConsumeFromWhere;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.gome.rocketmq.common.MyUtils;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author tianyuliang
 * @date 2016/7/1
 */
public class Consumer {

    private static final String topic = "orderTopic_1";
    private static final String groupName = "OneConsumerGroup";
    private static final String strDateTime = "yyyy-MM-dd#HH:mm:ss.SSS";

    public static void main(String[] args) throws InterruptedException, MQClientException {
        try {
            final AtomicLong success = new AtomicLong(0L);
            final DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(groupName);
            consumer.setNamesrvAddr(MyUtils.getNamesrvAddr());
            consumer.subscribe(topic, "*");
            consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
            final SimpleDateFormat sdf = new SimpleDateFormat(strDateTime);
            consumer.registerMessageListener(new MessageListenerConcurrently() {
                @Override
                public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                    for (MessageExt msg : msgs) {
                        if (msg.getTopic().equals(topic) && msg.getTags().equals("A")) {
                            System.out.println("success=" + success.incrementAndGet() + ",msgId=" + msg.getMsgId() + ",offset=" + msg.getQueueOffset()
                                    + ",body=" + new String(msg.getBody()) + ",storeHost=" + msg.getStoreHost() + ",queueId=" + msg.getQueueId());
                            //  + ",storeTime=" + sdf.format(msg.getStoreTimestamp()) + ",bronTime=" + sdf.format(msg.getBornTimestamp())
                        }
                    }
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }
            });
            consumer.start();
            System.out.println("consumerGroupName=" + groupName + ",instanceName=" + consumer.getInstanceName() + ",topic=" + topic + " consumer started ...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
