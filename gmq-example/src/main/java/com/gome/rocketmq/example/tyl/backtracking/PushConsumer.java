package com.gome.rocketmq.example.tyl.backtracking;

import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.alibaba.rocketmq.common.protocol.heartbeat.MessageModel;
import com.gome.rocketmq.common.MyUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @author tianyuliang
 * @date 2016/6/30
 */
public class PushConsumer {

    private static final String yyyy_MM_dd_HH_mm_ss_SSS = "yyyy-MM-dd#HH:mm:ss:SSS";
    private static SimpleDateFormat sdf = new SimpleDateFormat(yyyy_MM_dd_HH_mm_ss_SSS);
    private static String topic = "backtrackingTopic";

    public static void main(String[] args) throws InterruptedException, MQClientException {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("push_consumer_group_test");
        consumer.setNamesrvAddr(MyUtils.getNamesrvAddr());
        consumer.subscribe(topic, "*");
        consumer.setMessageModel(MessageModel.CLUSTERING);
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                for (MessageExt msgExt : msgs) {
                    //Date nowDate = new Date(); // sdf.format(nowDate) +
                    System.out.println("msgId=" + msgExt.getMsgId() + ",offset=" + msgExt.getQueueOffset() + ",bornTimestamp=" + msgExt.getStoreTimestamp()
                            + "(" + sdf.format(new Date(msgExt.getStoreTimestamp())) + ")" + ",body=" + new String(msgExt.getBody())
                            + ",queueId=" + msgExt.getQueueId() + ",storeHost=" + msgExt.getStoreHost());
                    sleepTime(1);
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        consumer.start();
        System.out.println("groupName=" + consumer.getConsumerGroup() +
                ",instanceName=" + consumer.getInstanceName() + ",backtrackingTopic=" + topic + " consumer start.");
    }


    private static void sleepTime(int second) {
        try {
            Thread.sleep(second * 1000L);
        } catch (Exception e) {
            System.out.println("error:" + e.getMessage());
            e.printStackTrace();
        }
    }
}
