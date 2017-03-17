package com.gome.rocketmq.example.ttx.queueNums;

import com.alibaba.rocketmq.client.exception.MQBrokerException;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.client.producer.SendResult;
import com.alibaba.rocketmq.common.message.Message;
import com.alibaba.rocketmq.remoting.exception.RemotingException;
import com.gome.rocketmq.common.MyUtils;

import java.util.Date;

/**
 * @author tantexian
 * @since 2016/6/21
 */
public class Producer {
    public static void main(String[] args) throws MQClientException, InterruptedException, RemotingException, MQBrokerException {
        int onceTimeNums = 12;
        int queueNums = 12;
        DefaultMQProducer producer = new DefaultMQProducer("ProducerSetQueueNums");
        producer.setNamesrvAddr(MyUtils.getNamesrvAddr());
        producer.setDefaultTopicQueueNums(queueNums);
        producer.start();

        for (int i =0; i < onceTimeNums; i++) {
            try {
                // 订单消息A
                Message msg = new Message("ProducerSetQueueNums-topic-1", // topic
                        "TagA", // tag
                        ("测试设置QueueNums == " + queueNums + " --Time:" + new Date()).getBytes()// body
                );

                SendResult sendResult = producer.send(msg);
                System.out.println(sendResult);
            } catch (Exception e) {
                System.out.println(e);
            }
        }

        System.out.println("消息发送完毕！！！");
        producer.shutdown();
    }
}
