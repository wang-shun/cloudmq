package com.wish.rocketmq.example.gyl.topic;

import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.client.producer.SendResult;
import com.alibaba.rocketmq.client.producer.SendStatus;
import com.alibaba.rocketmq.common.message.Message;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;


/**
 * @author: GaoYanLei
 * @since: 2016/6/12
 */

public class TopicQueueNumTest {
    final static int nThreads = 1;
    final static int sendNumOnceTime = 20;
    final static int topicNums = 10000;


    public static void main(String[] args) throws MQClientException {
        final AtomicLong atomicSuccessNums = new AtomicLong(0l);
        final DefaultMQProducer producer = new DefaultMQProducer("topic444");
        String namesrvAddr = "192.168.146.131:9876";
        producer.setNamesrvAddr(namesrvAddr);
        producer.setDefaultTopicQueueNums(10);// 设置queue数目
//        producer.setCreateTopicKey("TBW102");
        producer.start();
        ExecutorService exec = Executors.newCachedThreadPool();
        final long startCurrentTimeMillis = System.currentTimeMillis();
        final CyclicBarrier barrier = new CyclicBarrier(nThreads, new Runnable() { // 设置几个线程为一组,当这一组的几个线程都执行完成后,然后执行住线程的
            @Override
            public void run() {
                long endCurrentTimeMillis = System.currentTimeMillis();
                long sendNums = nThreads * sendNumOnceTime;
                long escapedTimeMillis = endCurrentTimeMillis - startCurrentTimeMillis;
                System.out.printf(
                    "All message has send, the random topicNums is : %d, " + "the message nums is : %d , "
                            + "Success nums is : %d, " + "TPS : %d !!!",
                    topicNums, sendNums, atomicSuccessNums.get(), sendNums * 1000 / escapedTimeMillis);
            }
        });
        for (int i = 0; i < nThreads; i++) {
            // message.setDelayTimeLevel(3);
            exec.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        for (int j = 0; j < sendNumOnceTime; j++) {
                            final Message message =
                                    new Message("QueueNumTopic38", "A", ("test1" + j).getBytes());
                            SendResult sendResult = producer.send(message);
                            if (sendResult.getSendStatus() == SendStatus.SEND_OK) {
                                System.out.println(Thread.currentThread().getName() + "============"
                                        + atomicSuccessNums.incrementAndGet() + "========"
                                        + sendResult.getMessageQueue().getQueueId());
                            }
                            else {
                                System.out.println("#### ERROR Message :" + sendResult);
                            }
                        }
                        System.out.println(
                            (barrier.getNumberWaiting() + 1) + "位完成：" + Thread.currentThread().getName());
                        barrier.await();
                    }
                    catch (Exception e) {
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
            });
        }
    }
}
