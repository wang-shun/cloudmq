package com.gome.rocketmq.example.gyl.topic;

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
 * @author: GaoYanLei
 * @since: 2016/6/12
 */

public class CreateTopicNumTest {
    final static int nThreads = 10;
    final static int topicNums = 1000;

    public static void main(String[] args) throws MQClientException {
        final AtomicLong atomicSuccessNums = new AtomicLong(0l);
        final DefaultMQProducer producer = new DefaultMQProducer("CreateTopicNumTest");
        producer.setNamesrvAddr(MyUtils.getNamesrvAddr());
        producer.start();
        ExecutorService exec = Executors.newCachedThreadPool();
        final CyclicBarrier barrier = new CyclicBarrier(nThreads, new Runnable() { // 设置几个线程为一组,当这一组的几个线程都执行完成后,然后执行住线程的
            @Override
            public void run() {
                System.out.printf("All message has send, send topicNums is : %d, " + "Success nums is : %d ",
                    nThreads * topicNums, atomicSuccessNums.get());
                producer.shutdown();
            }
        });
        for (int i = 0; i < nThreads; i++) {
            exec.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        for (int j = 0; j < topicNums; j++) {
                            final Message message =
                                    new Message("createTopic_" + Thread.currentThread().getName() + "_" + j,
                                        "A", ("test1" + j).getBytes());
                            SendResult sendResult = producer.send(message);
                            if (sendResult.getSendStatus() == SendStatus.SEND_OK) {
                                // System.out.println(Thread.currentThread().getName()
                                // + "============"
                                // + atomicSuccessNums.incrementAndGet() +
                                // "========"
                                // + sendResult.getMessageQueue().getQueueId());
//                                Thread.sleep(100);
                                System.out.println("sendResult:" + sendResult + "============"
                                        + atomicSuccessNums.incrementAndGet());
                                // System.out.println(atomicSuccessNums.incrementAndGet());
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
