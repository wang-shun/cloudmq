package com.gome.rocketmq.example.ytq.destroy;

import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.alibaba.rocketmq.client.consumer.listener.*;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.common.consumer.ConsumeFromWhere;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.gome.rocketmq.common.MyUtils;
import com.sun.org.apache.xpath.internal.SourceTree;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.Thread.currentThread;

/**
 * Created by yintongjiang on 2016/7/18.
 */
public class DestroyPushConcurrentlyConsumer {
    private final static String group = "destroyConsumerConGroup";
    private final static String topicName = "flow_topic_9";
    private final static String tags = "A";
    private final static AtomicLong msgCount = new AtomicLong(0L);

    public static void main(String[] args) throws MQClientException {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(group);
        consumer.setNamesrvAddr(MyUtils.getNamesrvAddr());
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
        consumer.subscribe(topicName, tags);
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                for (MessageExt msg : msgs) {
                    msgCount.incrementAndGet();
                    System.out.println(System.currentTimeMillis() / 1000 + " " + Thread.currentThread().getName() + " bornHost=" + msg.getBornHostString() +
                            ",storeHost=" + msg.getStoreHost().toString() +
                            ",topic=" + msg.getTopic() + ",body=" + new String(msg.getBody()) +
                            ",tags=" + msg.getTags() + ",msgId=" + msg.getKeys() + ",msgCount=" + msgCount.get());
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }

        });
        consumer.start();
        System.out.println("Consumer Started.");
    }
}
