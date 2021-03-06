package com.gome.rocketmq.example.tyl.queueOffset;

import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.client.producer.SendResult;
import com.alibaba.rocketmq.client.producer.SendStatus;
import com.alibaba.rocketmq.common.message.Message;
import com.gome.rocketmq.common.MyUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * topic queue offset balance test
 *
 * @author tianyuliang
 * @since 2016/6/16
 */
public class TopicQueueOffsetTest {

    final static int nThread = 6;
    final static int sendOneTime = 4000;
    final static String topicName = "topicQueueOffsetCheck";
    final static int topicNumbers = 12;

    public static void main(String[] args) throws MQClientException {
        final AtomicLong success = new AtomicLong(0);
        final Map<Integer, Long> offsetMap = new HashMap<Integer, Long>();
        final DefaultMQProducer producer = new DefaultMQProducer("DefaultCluster");
        producer.setNamesrvAddr(MyUtils.getNamesrvAddr());
        producer.setDefaultTopicQueueNums(topicNumbers);

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
                //String msg = String.format("发送%s个queue的数据完毕. 总次数:%s，已发次数:%s，总耗时:%s ms，QPS:%s", topicNums, totalNums, realCount, escaped, (realCount * 1000 / escaped));

                String msg = String.format("send message end. queueNum=%s，success=%s", producer.getDefaultTopicQueueNums(), totalNums, realCount);
                System.out.println(msg);
                for (int i = 0; i < offsetMap.size(); i++) {
                    System.out.println("topic=" + topicName + ", queueId=" + i + ", offset=" + offsetMap.get(i));
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
                Message message = new Message(topicName, "tagOffsetA", ("test bodyData " + j).getBytes());
                SendResult result = producer.send(message);
                if (result.getSendStatus() == SendStatus.SEND_OK) {
                    offsetMap.put(result.getMessageQueue().getQueueId(), result.getQueueOffset());
                    String data = String.format("threadID=%s, success=%s, offset=%s, result=%s", Thread.currentThread().getName(), success.incrementAndGet(), result.getQueueOffset(), result.getMessageQueue().toString());
                    System.out.println(data);
                } else {
                    System.out.println("error: " + Thread.currentThread().getName() + "===" + success.get() + "===" + result.toString());
                }
            }

            // System.out.println(Thread.currentThread().getName() + " send all data. success===" + success.get());
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
