package com.gome.rocketmq.example.tyl.order.simple;

import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerOrderly;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.common.UtilAll;
import com.alibaba.rocketmq.common.consumer.ConsumeFromWhere;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.gome.rocketmq.common.MyUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author tianyuliang
 * @date 2016/6/28
 */
public class OrderConsumer {

    static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    static AtomicLong success = new AtomicLong(0);

    public static void main(String[] args) throws MQClientException {
        final DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(MyUtils.getDefaultCluster());
        consumer.setNamesrvAddr(MyUtils.getNamesrvAddr());
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
        consumer.subscribe("orderTopicTest", "*");
        final long begin = System.currentTimeMillis();
        consumer.registerMessageListener(new MessageListenerOrderly() {
            @Override
            public ConsumeOrderlyStatus consumeMessage(List<MessageExt> msgs, ConsumeOrderlyContext context) {
                for (MessageExt msg : msgs) {
                    if (msg.getTopic().equals("orderTopicTest")) {
                        System.out.println("success=" + success.incrementAndGet() + ",msgId=" + msg.getMsgId() + ",offset=" + msg.getQueueOffset()
                                + ",body=" + new String(msg.getBody()) + ",storeHost=" + msg.getStoreHost() + ",queueId=" + msg.getQueueId());
                        //  + ",storeTime=" + sdf.format(msg.getStoreTimestamp()) + ",bronTime=" + sdf.format(msg.getBornTimestamp())
                    } else {
                        System.out.println("error: " + msg.toString());
                    }
                }
                return ConsumeOrderlyStatus.SUCCESS;
            }
        });
        consumer.start();
        System.out.println("instanceName=" + consumer.getInstanceName() + ",consumer Started.");
    }

}
