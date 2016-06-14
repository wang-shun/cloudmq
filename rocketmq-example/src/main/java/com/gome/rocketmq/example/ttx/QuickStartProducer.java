package com.gome.rocketmq.example.ttx;

import java.util.Date;

import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.client.producer.SendResult;
import com.alibaba.rocketmq.common.message.Message;


/**
 * Producer，发送消息
 * 
 */
public class QuickStartProducer {
    public static void main(String[] args) throws MQClientException, InterruptedException {
        DefaultMQProducer producer = new DefaultMQProducer("QuickStartProducer");
        String namesrvAddr = "192.168.15.11:9876;192.168.15.12:9876";
        producer.setNamesrvAddr(namesrvAddr);

        producer.start();

        for (int i = 0; i < 1; i++) {
            try {
                Message msg = new Message("QuickStartTopic",// topic
                    "TagA",// tag
                    ("QuickStartTopic, Hello RocketMQ " + i + new Date()).getBytes()// body
                        );
                SendResult sendResult = producer.send(msg);
                producer.sendOneway(msg);
                System.out.println(sendResult);
            }
            catch (Exception e) {
                e.printStackTrace();
                Thread.sleep(1000);
            }
        }
        System.out.println("消息发送完毕！！！");
        producer.shutdown();
    }
}
