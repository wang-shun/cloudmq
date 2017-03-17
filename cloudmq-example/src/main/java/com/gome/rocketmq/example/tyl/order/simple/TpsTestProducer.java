package com.gome.rocketmq.example.tyl.order.simple;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author tianyuliang
 * @date 2016/6/30
 */
public class TpsTestProducer {

    static final String topic = "simpleTpsTopic_";

    public static void main(String[] args) throws MQClientException {
        final int threadCount = args.length >= 1 ? Integer.parseInt(args[0]) : 3;
        final int messageSize = args.length >= 2 ? Integer.parseInt(args[1]) : 50;

        final ExecutorService sendThreadPool = Executors.newFixedThreadPool(threadCount);
        final DecimalFormat df = new DecimalFormat("####.###");
        final TpsStats tpsStats0 = new TpsStats();
        final TpsStats tpsStats1 = new TpsStats();
        final TpsStats tpsStats2 = new TpsStats();

        final LinkedList<Long[]> snapshotList0 = new LinkedList<Long[]>();
        final LinkedList<Long[]> snapshotList1 = new LinkedList<Long[]>();
        final LinkedList<Long[]> snapshotList2 = new LinkedList<Long[]>();

        final Timer timer = new Timer("TpsTimerThread", true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                snapshotList0.addLast(tpsStats0.buildSnapList());
                snapshotList1.addLast(tpsStats1.buildSnapList());
                snapshotList2.addLast(tpsStats2.buildSnapList());
                if (snapshotList0.size() > 5) {
                    snapshotList0.removeFirst();
                }
                if (snapshotList1.size() > 5) {
                    snapshotList1.removeFirst();
                }
                if (snapshotList2.size() > 5) {
                    snapshotList2.removeFirst();
                }
            }
        }, 1000, 1000);


        timer.scheduleAtFixedRate(new TimerTask() {

            private String buildSnap2String(Long[] snap) {
                return String.format("success=%s,timeTotal=%s,topicIndex=%s,fail=%s,currMillis=%s",
                        snap[1], snap[3], snap[4], snap[2], snap[0]);
            }

            private void printStats() {
                if (snapshotList0.size() >= 10 && snapshotList1.size() >= 10 & snapshotList2.size() >= 10) {
                    // index 0-当前时间  1-发送成功次数  2-发送失败次数  3-发送成功总耗时 4-topicIndex
                    Long[] begin0 = snapshotList0.getFirst();
                    Long[] end0 = snapshotList0.getLast();
                    final long sendTps0 = (long) (((end0[3] - begin0[3]) / (double) (end0[0] - begin0[0])) * 1000L);

                    Long[] begin1 = snapshotList0.getFirst();
                    Long[] end1 = snapshotList0.getLast();
                    final long sendTps1 = (long) (((end1[3] - begin1[3]) / (double) (end1[0] - begin1[0])) * 1000L);

                    Long[] begin2 = snapshotList0.getFirst();
                    Long[] end2 = snapshotList0.getLast();
                    final long sendTps2 = (long) (((end2[3] - begin2[3]) / (double) (end2[0] - begin2[0])) * 1000L);

                    System.out.println(buildSnap2String(begin0));
                    System.out.println(buildSnap2String(end0));
                    System.out.printf("send message topic=%s, success=%d, TPS=%d, send failed=%d\n", (topic + end0[4]), end0[1], sendTps0, end0[2]);

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

        final DefaultMQProducer producer = new DefaultMQProducer(MyUtils.getDefaultCluster());  // sendOneWay_test_producer
        producer.setNamesrvAddr(MyUtils.getNamesrvAddr());
        producer.setCompressMsgBodyOverHowmuch(Integer.MAX_VALUE);
        producer.start();

        System.out.println("threadCount=" + threadCount + ", messageSize=" + messageSize);
        System.out.println("producerGroup=" + producer.getProducerGroup() + ", instanceName=" + producer.getInstanceName() + ", consumer started.");
        for (int i = 0; i < threadCount; i++) {
            final Message msg = buildMessage(messageSize, i);
            int index = i % 3;
            final TpsStats tpsStats = index == 0 ? tpsStats0 : (index == 1 ? tpsStats1 : tpsStats2);
            sendThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            final long beginTimestamp = System.currentTimeMillis();
                            int topicIndex = Integer.parseInt(msg.getKeys());

                            producer.send(msg);
                            tpsStats.getTopicNameIndex().set(topicIndex);
                            tpsStats.getSendRequestSuccessCount().incrementAndGet();
                            final long currentRT = System.currentTimeMillis() - beginTimestamp;
                            tpsStats.getSendMessageSuccessTimeTotal().addAndGet(currentRT);

                            buildSnap2String(tpsStats);

                        } catch (RemotingException e) {
                            tpsStats.getSendRequestFailedCount().incrementAndGet();
                            e.printStackTrace();
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e1) {
                            }
                        } catch (InterruptedException e) {
                            tpsStats.getSendRequestFailedCount().incrementAndGet();
                            try {
                                Thread.sleep(3000);
                            } catch (InterruptedException e1) {
                            }
                        } catch (MQClientException e) {
                            tpsStats.getSendRequestFailedCount().incrementAndGet();
                            e.printStackTrace();
                        } catch (MQBrokerException e) {
                            tpsStats.getSendRequestFailedCount().incrementAndGet();
                            try {
                                Thread.sleep(3000);
                            } catch (InterruptedException e1) {
                            }
                        }
                    }
                }
            });
        }
    }

    private static String buildSnap2String(final TpsStats tpsStats) {
        return String.format("main success=%s,timeTotal=%s,topicIndex=%s,fail=%s,currMillis=%s",
                tpsStats.getSendRequestSuccessCount().get(), tpsStats.getSendMessageSuccessTimeTotal().get(),
                tpsStats.getTopicNameIndex(), tpsStats.getSendRequestFailedCount().get(), tpsStats.getSendCurrentTimeMillis());
    }

    private static Message buildMessage(Integer messageSize, Integer topicIndex) {
        Message msg = new Message();
        msg.setTags("A");
        msg.setKeys(topicIndex.toString());
        msg.setTopic(topic + topicIndex);
        msg.setBody(buildBodyData(messageSize).getBytes());
        return msg;
    }

    private static String buildBodyData(int messageSize) {
        StringBuilder sb = new StringBuilder(messageSize + 1);
        for (int i = 0; i < messageSize; i += 10) {
            sb.append("hello baby");
        }
        return sb.toString();
    }

}
