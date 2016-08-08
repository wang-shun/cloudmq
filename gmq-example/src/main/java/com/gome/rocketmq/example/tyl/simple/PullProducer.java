package com.gome.rocketmq.example.tyl.simple;

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
 * send default message for pull/push consumer
 *
 * @author tianyuliang
 * @date 2016/6/24
 */
public class PullProducer {
    final static int nThread = 2;
    final static int sendOneTime = 20;
    final static String topicName = "pullTopicTest";  //  BenchmarkTest    pullTopicTest ? why not work in dev ??

    public static void main(String[] args) throws MQClientException {
        final AtomicLong success = new AtomicLong(0);
        final DefaultMQProducer producer = new DefaultMQProducer("PROJECT_test123");
        System.out.println("namesrv=" + MyUtils.getNamesrvAddr());
        producer.setNamesrvAddr(MyUtils.getNamesrvAddr());
        producer.start();

        final CyclicBarrier barrier = new CyclicBarrier(nThread, new Runnable() {
            @Override
            public void run() {
                System.out.println(Thread.currentThread().getName() + " send message end. " + success.get());
                producer.shutdown();
            }
        });

        ExecutorService excutor = Executors.newFixedThreadPool(nThread);
        for (int i = 0; i < nThread; i++) {
            excutor.execute(new Runnable() {
                @Override
                public void run() {
                    sendMessage(producer, success, barrier);
                }
            });
        }
    }

    private static void sendMessage(DefaultMQProducer producer, AtomicLong success, CyclicBarrier barrier ) {
        try {
            Message message = null;
            for (int j = 0; j < sendOneTime; j++) {
                message = new Message(topicName, "A", "data".getBytes());
                SendResult result = producer.send(message);
                if (result.getSendStatus() == SendStatus.SEND_OK) {
                    System.out.println("send msg success. success=" + success.incrementAndGet() + ",topic=" + message.getTopic()
                            + ",offset=" + result.getQueueOffset() + ",body=" + new String(message.getBody()) + ",msgId=" + result.getMsgId());
                } else {
                    System.out.println("error: " + Thread.currentThread().getName() + "===" + success.get() + "===" + result.toString());
                }
            }
            System.out.println("send message successful. " + success.get());
            barrier.await();
        } catch (Exception e) {
            System.out.println("error: " + Thread.currentThread().getName() + " === " + e.getMessage());
            try {
                barrier.await();
                System.out.println((Thread.currentThread().getName() + "未完成"));
            }
            catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            catch (BrokenBarrierException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
    }
}
