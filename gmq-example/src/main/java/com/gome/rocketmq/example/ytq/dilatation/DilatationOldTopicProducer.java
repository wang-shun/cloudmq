package com.gome.rocketmq.example.ytq.dilatation;

import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.client.producer.SendResult;
import com.alibaba.rocketmq.common.message.Message;
import com.gome.rocketmq.common.MyUtils;

import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by yintongjiang on 2016/7/18.
 */
public class DilatationOldTopicProducer {

    private final static String group = "test";
    private final static String topicName = "Test";
    private final static String tags = "A";
    private final static int nThreads = 100;
    private final static int topicNums = 1000;

    public static void main(String[] args) throws MQClientException {
        final AtomicLong atomicSuccessNums = new AtomicLong(0L);
        final AtomicLong atomicSlaveNotNums = new AtomicLong(0L);
        final AtomicLong atomicMsgIds = new AtomicLong(0L);
        final AtomicLong atomicFail = new AtomicLong(0L);
        final AtomicLong flushDiskTimeOutCount = new AtomicLong(0L);
        final AtomicLong flushSlaveTimeOutCount = new AtomicLong(0L);
        final DefaultMQProducer producer = new DefaultMQProducer(group);
        producer.setNamesrvAddr(MyUtils.getNamesrvAddr());
        producer.start();
        final ExecutorService exec = Executors.newFixedThreadPool(nThreads);
        final long startCurrentTimeMillis = System.currentTimeMillis();
        final CyclicBarrier barrier = new CyclicBarrier(nThreads, new Runnable() {
            @Override
            public void run() {
                long endCurrentTimeMillis = System.currentTimeMillis();
                long escapedTimeMillis = endCurrentTimeMillis - startCurrentTimeMillis;

                System.out.printf(
                        "All message has send, send topicNums is : %d, " + "Success nums is : %d, " +
                                "Slave not available nums is : %d, " + "Flush disk time out nums is : %d, " +
                                "Flush slave time out nums is : %d, " + "\n" + "Send fail nums is : %d, "
                                + " msgCount is : %d, "
                                + "TPS : %d !!!",
                        nThreads * topicNums, atomicSuccessNums.get(), atomicSlaveNotNums.get(), flushDiskTimeOutCount.get(),
                        flushSlaveTimeOutCount.get(), atomicFail.get(), atomicMsgIds.get(),
                        nThreads * topicNums * 1000 / escapedTimeMillis);
                producer.shutdown();
                exec.shutdown();

            }
        });
        for (int i = 0; i < nThreads; i++) {
            final int finalI = i;
            exec.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        for (int j = 0; j < topicNums; j++) {
                            atomicMsgIds.incrementAndGet();
                            final Message message = new Message(topicName, tags, (finalI + "--" + j).getBytes());
                            try {
                                SendResult sendResult = producer.send(message);
                                switch (sendResult.getSendStatus()) {
                                    case SEND_OK:
                                        System.out.println("sendResult:" + sendResult + "============"
                                                + atomicSuccessNums.incrementAndGet() + " msgId=" + message.getKeys());
                                        break;
                                    case SLAVE_NOT_AVAILABLE:
                                        System.out.println("sendResult:" + sendResult + "============"
                                                + atomicSlaveNotNums.incrementAndGet() + " msgId=" + message.getKeys());
                                        break;
                                    case FLUSH_DISK_TIMEOUT:
                                        flushDiskTimeOutCount.incrementAndGet();
                                        System.out.println("#### ERROR Message :" + sendResult);
                                        break;
                                    case FLUSH_SLAVE_TIMEOUT:
                                        flushSlaveTimeOutCount.incrementAndGet();
                                        System.out.println("#### ERROR Message :" + sendResult);
                                        break;
                                }
                            } catch (Exception e) {
                                atomicFail.incrementAndGet();
                                throw e;
                            }
                        }
                        System.out.println(
                                (barrier.getNumberWaiting() + 1) + "位完成：" + Thread.currentThread().getName());
                        barrier.await();

                    } catch (Exception e) {
                        try {
                            barrier.await();
                            System.out.println((Thread.currentThread().getName() + "未完成"));
                        } catch (InterruptedException | BrokenBarrierException e1) {
                            e1.printStackTrace();
                        }
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
