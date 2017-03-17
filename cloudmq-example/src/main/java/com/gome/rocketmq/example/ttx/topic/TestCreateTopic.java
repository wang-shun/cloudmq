package com.gome.rocketmq.example.ttx.topic;

import java.util.Date;

import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.client.producer.SendCallback;
import com.alibaba.rocketmq.client.producer.SendResult;
import com.alibaba.rocketmq.common.message.Message;
import com.gome.rocketmq.common.MyUtils;


/**
 * producer，发送消息
 */
public class TestCreateTopic {
    public static void main(String[] args) throws MQClientException, InterruptedException {

        DefaultMQProducer producer = new DefaultMQProducer("TestCreateTopicProducer");

        producer.setNamesrvAddr(MyUtils.getNamesrvAddr());

        producer.start();

        try {
            int n = 10;
            for (int i = 0; i < n; i++) {
                // String topic = "TestCreateTopic" + i + System.currentTimeMillis();
                String topic = "TestCreateTopic-ddd";
                Message msg = new Message(topic, // topic
                        "TagB", // tag
                        ("创建topic测试" + topic + " --Time:" + new Date()).getBytes()// body
                );
                SendResult sendResult = producer.send(msg);
                System.out.println(sendResult);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Thread.sleep(1000);
        }

        System.out.println("消息发送完毕！！！");
        producer.shutdown();


    }
}
