package com.gome.rocketmq.example.tyl.retryConsumer;

import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.alibaba.rocketmq.client.consumer.listener.*;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.common.consumer.ConsumeFromWhere;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.gome.rocketmq.common.MyUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author tianyuliang
 * @since 2016/7/11
 */
public class Consumer {

    private static final String groupName = MyUtils.getDefaultCluster();
    private static final String strDateTime = "yyyy-MM-dd#HH:mm:ss.SSS";


    public static void main(String[] args) throws InterruptedException, MQClientException {
        try {

            final String topic = args.length >= 1 ? args[0].trim() : "topicMaxNumTest59";

            final AtomicLong index = new AtomicLong(0L);
            final DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(groupName);
            consumer.setNamesrvAddr(MyUtils.getNamesrvAddr());
            consumer.subscribe(topic, "*");
            consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
            final SimpleDateFormat sdf = new SimpleDateFormat(strDateTime);
            consumer.registerMessageListener(new MessageListenerConcurrently() {

                @Override
                public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                    for (MessageExt msg : msgs) {
                        Date nowDate = new Date();
                        long diff = (System.currentTimeMillis() - msg.getBornTimestamp()) / 1000;
                        System.out.println("index=" + index.incrementAndGet() + ",msgId=" + msg.getMsgId() + ",reTimes=" + msg.getReconsumeTimes()
                                + ",body=" + new String(msg.getBody()) + "," + sdf.format(nowDate) + ",diff=" + (diff)
                                + ",bronTime=" + sdf.format(msg.getBornTimestamp()));
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
