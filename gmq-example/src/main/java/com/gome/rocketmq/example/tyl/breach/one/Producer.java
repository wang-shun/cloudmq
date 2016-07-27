package com.gome.rocketmq.example.tyl.breach.one;

import com.alibaba.fastjson.serializer.IntegerCodec;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.client.producer.SendResult;
import com.alibaba.rocketmq.client.producer.SendStatus;
import com.alibaba.rocketmq.common.message.Message;
import com.gome.rocketmq.common.MyUtils;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 破坏性测试 第一组 优雅关闭broker-master3节点
 *
 * @author: tianyuliang
 * @since: 2016/7/14
 */
public class Producer {

    final static int nThread = 5;
    final static int sendOneTime = 6000;
    final static String topicName = "breachTopic_1";

    public static void main(String[] args) throws MQClientException {
        final AtomicLong success = new AtomicLong(0);
        final AtomicLong fail = new AtomicLong(0);
        final DefaultMQProducer producer = new DefaultMQProducer(MyUtils.getDefaultCluster());
        producer.setNamesrvAddr(MyUtils.getNamesrvAddr());
        producer.start();

        ExecutorService excutor = Executors.newFixedThreadPool(nThread);
        final Long begin = System.currentTimeMillis();
        final CyclicBarrier barrier = new CyclicBarrier(nThread, new Runnable() {
            @Override
            public void run() {
                long end = System.currentTimeMillis();
                long totalNums = nThread * sendOneTime;
                long escaped = end - begin;
                long realCount = success.get();
                String msg = String.format("send message end. success=%s", totalNums, realCount);
                System.out.println(msg);
                producer.shutdown();
            }
        });

        for (int i = 0; i < nThread; i++) {
            final int index = i;
            excutor.execute(new Runnable() {
                @Override
                public void run() {
                    sendMessage(producer, barrier, success, fail, index);
                }
            });
        }
    }

    private static void sendMessage(DefaultMQProducer producer, CyclicBarrier barrier, AtomicLong success, AtomicLong fail, Integer inddex) {
        try {
            Message message = null;
            SendResult result = null;
            for (Integer j = 0; j < sendOneTime; j++) {
                message = new Message(topicName, "tagA", (String.format("bodyData-%s-%s", inddex, j)).getBytes());
                result = producer.send(message);
                if (result.getSendStatus() == SendStatus.SEND_OK || result.getSendStatus() == SendStatus.SLAVE_NOT_AVAILABLE) {
                    String data = String.format("threadId=%s, success=%s, fail=%s, brokerName=%s, body=%s",
                            Thread.currentThread().getName(), success.incrementAndGet(), fail.get(), result.getMessageQueue().getBrokerName(), new String(message.getBody()));
                    System.out.println(data);
                } else {
                    fail.incrementAndGet();
                }
            }

            // System.out.println(Thread.currentThread().getName() + " send all data. success===" + success.get());
            barrier.await();
        } catch (Exception e) {
            try {
                barrier.await();
                fail.incrementAndGet();
                System.out.println(Thread.currentThread().getName() + " send not ok, success=" + success.get() + ", fail=" + fail.get());
                e.printStackTrace();
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            } catch (BrokenBarrierException e1) {
                e1.printStackTrace();
            }
        }
    }

}
