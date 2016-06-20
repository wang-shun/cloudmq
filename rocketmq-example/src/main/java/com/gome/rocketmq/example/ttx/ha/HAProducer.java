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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class HAProducer {

    private static int threadCount;
    private static int messageSize;
    private static int onceTimeNum;
    private static int sleepTime;
    private static boolean ischeck;

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

    public static void main(String[] args) {

        final int W = 10000;
        final int K = 1000;
        threadCount = args.length >= 1 ? Integer.parseInt(args[0]) : 1*W;
        messageSize = args.length >= 2 ? Integer.parseInt(args[1]) : 1024*2;
        onceTimeNum = args.length >= 3 ? Integer.parseInt(args[2]) : 1*W;
        sleepTime = args.length >= 4 ? Integer.parseInt(args[3]) : 1;

        final Message msg = buildMessage(messageSize);

        final ExecutorService sendThreadPool = Executors.newFixedThreadPool(threadCount);
        for (int i = 0; i < threadCount; i++) {
            sendThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            Thread.sleep(sleepTime);
                            for (int i = 0; i < onceTimeNum; i++) {
                                testMsgHA();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
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
        System.out.println("body==============bodyHashcode" + body + "/" + bodyHashcode);
        saveMsgInfo2DB(msgInfo);

        SendResult sendResult = producer.send(message);
        if (sendResult.getSendStatus() != SendStatus.SEND_OK) {
            // 如果发送消息不成功，则置deleted =444 ，表示发送异常消息
            msgInfo.setDeleted(444);
            saveMsgInfo2DB(msgInfo);
        }
        System.out.println(sendResult);
    }

    private static void saveMsgInfo2DB(MsgInfo msgInfo) {
        msgInfoDao.insertEntry(msgInfo);
    }

    private static Message buildMessage(final int messageSize) {
        Message msg = new Message();
        msg.setTopic("BenchmarkTest");

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < messageSize; i += 10) {
            sb.append("hello baby");
        }

        msg.setBody(sb.toString().getBytes());

        return msg;
    }
}
