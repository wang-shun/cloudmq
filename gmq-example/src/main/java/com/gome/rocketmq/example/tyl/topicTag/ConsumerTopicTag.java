package com.gome.rocketmq.example.tyl.topicTag;

import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerOrderly;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.common.consumer.ConsumeFromWhere;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.gome.rocketmq.common.MyUtils;

import java.util.List;

/**
 * consumer topic with topicTag
 *
 * @author tianyuliang
 * @date 2016/6/22
 */
public class ConsumerTopicTag {

    public static void main(String[] args) throws MQClientException {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(MyUtils.getDefaultCluster());
        consumer.setNamesrvAddr(MyUtils.getNamesrvAddr());
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
        consumer.subscribe("orderTopic001", "tagA || tagC");
        consumer.subscribe("orderTopicTest", "*");
        consumer.subscribe("topicMaxNumTest24", "orderId-900");

        consumer.registerMessageListener(new MessageListenerOrderly() {
            @Override
            public ConsumeOrderlyStatus consumeMessage(List<MessageExt> msgs, ConsumeOrderlyContext context) {
                //System.out.println(Thread.currentThread().getName() + " Receive New Messages: " + msgs.toString());
                for (MessageExt msg : msgs) {
                    System.out.println(Thread.currentThread().getName()+" topic:"+msg.getTopic()+",body:" + new String(msg.getBody()) + ",tags=" + msg.getTags());
                }
                return ConsumeOrderlyStatus.SUCCESS;
            }
        });
        consumer.start();
        System.out.println("Consumer Started.");
    }
}
