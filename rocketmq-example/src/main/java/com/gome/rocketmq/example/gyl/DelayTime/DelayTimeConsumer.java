package com.gome.rocketmq.example.gyl.DelayTime;

import java.util.List;

import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.alibaba.rocketmq.common.protocol.heartbeat.MessageModel;
import com.gome.rocketmq.common.MyUtils;


/**
 *
 *
 * @author: GaoYanLei
 * @since: 2016/6/3
 */
public class DelayTimeConsumer {
    public static void main(String[] args) throws InterruptedException, MQClientException {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("delayTime");
        consumer.setNamesrvAddr(MyUtils.getNamesrvAddr());
        consumer.subscribe("delayTimeTopic1", "*");
        // consumer.setMessageModel(MessageModel.BROADCASTING);
        consumer.setMessageModel(MessageModel.CLUSTERING);
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs,
                    ConsumeConcurrentlyContext context) {
                MessageExt msg = msgs.get(0);
                System.out.println("Consume1:==============" + Thread.currentThread().getName()
                        + "==============" + new String(msg.getMsgId()) + "============== 延迟:"
                        + (System.currentTimeMillis() - msg.getBornTimestamp()) / 1000 + "秒");
                return ConsumeConcurrentlyStatus.RECONSUME_LATER;
            }
        });
        consumer.start();
        System.out.println("Consumer Started.");
    }
}
