package com.gome.rocketmq.example.ttx.ha;

import com.alibaba.rocketmq.client.exception.MQBrokerException;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.client.producer.SendResult;
import com.alibaba.rocketmq.client.producer.SendStatus;
import com.alibaba.rocketmq.common.message.Message;
import com.alibaba.rocketmq.remoting.exception.RemotingException;
import com.gome.rocketmq.common.MyUtils;
import com.gome.rocketmq.dao.MsgInfoDao;
import com.gome.rocketmq.domain.MsgInfo;

import java.util.Date;
import java.util.Random;


public class HAProducer {

    private static MsgInfoDao msgInfoDao = (MsgInfoDao) MyUtils.getSpringBean("msgInfoDao");
    private static DefaultMQProducer producer = new DefaultMQProducer("HAProducer");
    static {
        producer.setNamesrvAddr(MyUtils.getNamesrvAddr());

        try {
            producer.start();
        } catch (MQClientException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws MQClientException, RemotingException, InterruptedException, MQBrokerException {
        for (int i = 0; i < 100000; i++) {
            testMsgHA();
        }
        System.exit(0);
    }

    private static void testMsgHA() throws MQClientException, RemotingException, InterruptedException, MQBrokerException {
        String body = "测试HAbody --Time:" + new Date() + new Random().nextInt();
        String topic = "HAProducerTopic";
        String tag = "tagA";
        String key = ((body + topic).hashCode() + new Random().nextInt(10000)) + "";
        Message message = new Message(topic, // topic
                tag, // tag
                key, // key
                (body.getBytes()) // body
        );

        MsgInfo msgInfo = new MsgInfo();
        msgInfo.setCreatedTime(new Date());
        msgInfo.setBody(body);
        msgInfo.setTopic(topic);
        int bodyHashcode = body.hashCode();
        msgInfo.setBodyHashcode(bodyHashcode);
        msgInfo.setKey(key);
        System.out.println("body==============bodyHashcode" +  body + "/" + bodyHashcode);
        saveMsgInfo2DB(msgInfo);

        SendResult sendResult = producer.send(message);
        if (sendResult.getSendStatus() == SendStatus.SEND_OK) {

        }
        System.out.println(sendResult);
        System.out.println("I,m the last!!!");

    }

    private static void saveMsgInfo2DB(MsgInfo msgInfo) {
        msgInfoDao.insertEntry(msgInfo);
    }
}
