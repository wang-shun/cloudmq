package com.gome.rocketmq.example.tyl.order;

import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.client.producer.SendResult;
import com.alibaba.rocketmq.client.producer.SendStatus;
import com.alibaba.rocketmq.common.UtilAll;
import com.alibaba.rocketmq.common.message.Message;
import com.gome.rocketmq.common.MyUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 测试多线程发送数据的QPS
 *
 * @author tianyuliang
 * @date 2016/6/28
 */
public class MultiThreadingProducer {

    final static int nThread = 24;
    final static int sendOneTime = 800000;
    final static String topic = "multiThreadingTopicTest";
    final static int topicNumbers = 8;

    public static void main(String[] args) throws MQClientException {
        final AtomicLong success = new AtomicLong(0);
        final Map<Integer, Long> offsetMap = new HashMap<Integer, Long>();
        final DefaultMQProducer producer = new DefaultMQProducer(MyUtils.getDefaultCluster());
        producer.setNamesrvAddr(MyUtils.getNamesrvAddr());
        producer.start();

        ExecutorService excutor = Executors.newFixedThreadPool(nThread);
        final Long begin = System.currentTimeMillis();
        final CyclicBarrier barrier = new CyclicBarrier(nThread, new Runnable() {
            @Override
            public void run() {
                long totalNums = nThread * sendOneTime;
                long escaped = UtilAll.computeEclipseTimeMilliseconds(begin);
                long realCount = success.get();
                String msg = String.format("发送%s个queue的数据完毕. 总次数:%s，已发次数:%s，总耗时:%s ms，QPS:%s", topicNumbers, totalNums, realCount, escaped, (realCount * 1000 / escaped));

                //String msg = String.format("send message end. queueNum=%s，success=%s", producer.getDefaultTopicQueueNums(), totalNums, realCount);
                System.out.println(msg);
                for (int i = 0; i < offsetMap.size(); i++) {
                    System.out.println("topic=" + topic + ", queueId=" + i + ", offset=" + offsetMap.get(i));
                }
                producer.shutdown();
            }
        });

        for (int i = 0; i < nThread; i++) {
            excutor.execute(new Runnable() {
                @Override
                public void run() {
                    sendMessage(producer, barrier, success, offsetMap);
                }
            });
        }
    }

    private static void sendMessage(DefaultMQProducer producer, CyclicBarrier barrier, AtomicLong success, Map<Integer, Long> offsetMap) {
        try {
            for (int j = 0; j < sendOneTime; j++) {
                Message message = new Message(topic, "tagA", ("test bodyData - " + j).getBytes());
                SendResult result = producer.send(message);
                if (result.getSendStatus() == SendStatus.SEND_OK || result.getSendStatus() == SendStatus.SLAVE_NOT_AVAILABLE) {
                    offsetMap.put(result.getMessageQueue().getQueueId(), result.getQueueOffset());
                    String data = String.format("threadID=%s, success=%s, offset=%s, queueId=%s, brokerName=%s",
                            Thread.currentThread().getName(), success.incrementAndGet(), result.getQueueOffset(),
                            result.getMessageQueue().getQueueId(), result.getMessageQueue().getBrokerName());
                    System.out.println(data);
                } else {
                    System.out.println("error: " + Thread.currentThread().getName() + "===" + success.get() + "===" + result.toString());
                }
            }

            System.out.println(Thread.currentThread().getName() + " send all data. success===" + success.get());
            barrier.await();
        } catch (Exception e) {
            try {
                barrier.await();
                System.out.println(Thread.currentThread().getName() + " send not ok, success===" + success.get());
                e.printStackTrace();
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            } catch (BrokenBarrierException e1) {
                e1.printStackTrace();
            }
        }
    }


}
