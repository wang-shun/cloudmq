package com.gome.rocketmq.example.tyl.benchmark;

import com.alibaba.rocketmq.client.exception.MQBrokerException;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.common.message.Message;
import com.alibaba.rocketmq.remoting.exception.RemotingException;
import com.gome.rocketmq.common.MyUtils;

import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 单线程，顺序发送，测试TPS
 *
 * @author tianyuliang
 * @date 2016/6/28
 */
public class SimpleProducer {

    static final String topic = "BenchmarkSimpleTopicTest";

    public static void main(String[] args) throws MQClientException {
        final int threadCount = 1;
        final int messageSize = args.length >= 1 ? Integer.parseInt(args[0]) : 128;
        final boolean keyEnable = args.length >= 2 ? Boolean.parseBoolean(args[1]) : false;

        final Message msg = buildMessage(messageSize);
        final StatsBenchmarkProducer statsBenchmark = new StatsBenchmarkProducer();
        final Timer timer = new Timer("BenchmarkTimerThread", true);
        final LinkedList<Long[]> snapshotList = new LinkedList<Long[]>();
        final DecimalFormat df = new DecimalFormat("####.000");

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                snapshotList.addLast(statsBenchmark.createSnapshot());
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

                    // index含义：0-代表当前时间，1-发送成功数量 2-发送失败数量 3-接收成功数量 4-接收失败数量 5-发送消息成功总耗时
                    final long sendTps = (long) (((end[3] - begin[3]) / (double) (end[0] - begin[0])) * 1000L);
                    final double averageRT = ((end[5] - begin[5]) / (double) (end[3] - begin[3]));

                    System.out.printf(
                            "send simple message success=%d, TPS=%d, max runTime=%d ms, average runTime=%s ms, send failed=%d, response failed=%d\n"
                            , end[1]
                            , sendTps//
                            , statsBenchmark.getSendMessageMaxRT().get()//
                            , df.format(averageRT)//
                            , end[2]//
                            , end[4]//
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

        final DefaultMQProducer producer = new DefaultMQProducer("benchmark_test_producer");  // MyUtils.getDefaultCluster()
        producer.setNamesrvAddr(MyUtils.getNamesrvAddr());
        producer.setCompressMsgBodyOverHowmuch(Integer.MAX_VALUE);
        producer.start();
        System.out.printf("threadCount=%d, messageSize=%d, keyEnable=%s\n", threadCount, messageSize, keyEnable);
        System.out.println("producerGroup=" + producer.getProducerGroup() + ", instanceName=" + producer.getInstanceName() + ", consumer started.");

        for (int i = 0; i < threadCount; i++) {
            while (true) {
                try {
                    final long beginTimestamp = System.currentTimeMillis();
                    if (keyEnable) {
                        msg.setKeys(String.valueOf(beginTimestamp / 1000));
                    }
                    producer.send(msg);
                    statsBenchmark.getSendRequestSuccessCount().incrementAndGet();
                    statsBenchmark.getReceiveResponseSuccessCount().incrementAndGet();

                    final long currentRT = System.currentTimeMillis() - beginTimestamp;
                    statsBenchmark.getSendMessageSuccessTimeTotal().addAndGet(currentRT);

                    long prevMaxRT = statsBenchmark.getSendMessageMaxRT().get();
                    while (currentRT > prevMaxRT) {
                        boolean updated = statsBenchmark.getSendMessageMaxRT().compareAndSet(prevMaxRT, currentRT);
                        if (updated) {
                            break;
                        }
                        prevMaxRT = statsBenchmark.getSendMessageMaxRT().get();
                    }
                } catch (RemotingException e) {
                    statsBenchmark.getSendRequestFailedCount().incrementAndGet();
                    e.printStackTrace();

                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e1) {
                    }
                } catch (InterruptedException e) {
                    statsBenchmark.getSendRequestFailedCount().incrementAndGet();
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e1) {
                    }
                } catch (MQClientException e) {
                    statsBenchmark.getSendRequestFailedCount().incrementAndGet();
                    e.printStackTrace();
                } catch (MQBrokerException e) {
                    statsBenchmark.getReceiveResponseFailedCount().incrementAndGet();
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e1) {
                    }
                }
            }
        }
    }


    private static Message buildMessage(final int messageSize) {
        Message msg = new Message();
        msg.setTopic(topic);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < messageSize; i += 10) {
            sb.append("hello baby");
        }
        msg.setBody(sb.toString().getBytes());
        return msg;
    }


}
