package com.gome.rocketmq.example.tyl.simple;

import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.gome.rocketmq.common.MyUtils;

import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 单线程comsumer端TPS测试
 *
 * @author tianyuliang
 * @date 2016/7/4
 */
public class TpsConsumer {


    static final String topic = "TopicTestMQ";  // TopicTestMQ  BenchmarkTest  TopicSimpleTpsTest
    static final String producerGroupId = "TopicSimpleTpsGroupId";

    public static void main(String[] args) throws MQClientException {
        final DecimalFormat df = new DecimalFormat("####.###");
        final TpsStatsConsumer tpsStatsConsumer = new TpsStatsConsumer();
        final Timer timer = new Timer("TpsTimerThread", true);
        final LinkedList<Long[]> snapshotList = new LinkedList<Long[]>();

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                snapshotList.addLast(tpsStatsConsumer.createSnapshot());
                if (snapshotList.size() > 10) {
                    snapshotList.removeFirst();
                }
            }
        }, 1000, 1000);

        timer.scheduleAtFixedRate(new TimerTask() {
            private void printStats() {
                if (snapshotList.size() >= 10) {
                    Long[] begin = snapshotList.getFirst();
                    Long[] end = snapshotList.getLast();

                    final long consumeTps = (long) (((end[1] - begin[1]) / (double) (end[0] - begin[0])) * 1000L);
                    final double averageB2CRT = ((end[2] - begin[2]) / (double) (end[1] - begin[1]));
                    final double averageS2CRT = ((end[3] - begin[3]) / (double) (end[1] - begin[1]));

                    System.out.printf(
                            "consumer success=%d, TPS=%d, average(B2C) runTime=%s ms, average(S2C) runTime=%s ms, MAX(B2C) runTime=%d ms, MAX(S2C) runTime=%d ms\n"
                            , end[1]//
                            , consumeTps//
                            , df.format(averageB2CRT)//
                            , df.format(averageS2CRT)//
                            , end[4]//
                            , end[5]//
                    );
                }
            }


            @Override
            public void run() {
                try {
                    this.printStats();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 10000, 10000);


        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(producerGroupId);
        consumer.setNamesrvAddr(MyUtils.getNamesrvAddr());
        consumer.subscribe(topic, "*");
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                MessageExt msg = msgs.get(0);
                long now = System.currentTimeMillis();

                // 1
                tpsStatsConsumer.getReceiveMessageTotalCount().incrementAndGet();

                // 2
                long born2ConsumerRT = now - msg.getBornTimestamp(); // 当前时间 - 消息在客户端创建时间戳
                tpsStatsConsumer.getBorn2ConsumerTotalRT().addAndGet(born2ConsumerRT);

                // 3
                long store2ConsumerRT = now - msg.getStoreTimestamp();  // 当前时间 - 消息在服务器存储时间戳
                tpsStatsConsumer.getStore2ConsumerTotalRT().addAndGet(store2ConsumerRT);

                // 4
                compareAndSetMax(tpsStatsConsumer.getBorn2ConsumerMaxRT(), born2ConsumerRT);

                // 5
                compareAndSetMax(tpsStatsConsumer.getStore2ConsumerMaxRT(), store2ConsumerRT);

                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });

        consumer.start();
        System.out.println("consumerGroup=" + consumer.getConsumerGroup() + ", instanceName=" + consumer.getInstanceName() + ", consumer started.");
    }

    public static void compareAndSetMax(final AtomicLong target, final long value) {
        long prev = target.get();
        while (value > prev) {
            boolean updated = target.compareAndSet(prev, value);
            if (updated) {
                break;
            }
            prev = target.get();
        }
    }

}
