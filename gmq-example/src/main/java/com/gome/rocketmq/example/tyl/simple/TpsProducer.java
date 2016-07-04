package com.gome.rocketmq.example.tyl.simple;

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
 * 测试单线程的TPS
 * @author tianyuliang
 * @date 2016/7/1
 */
public class TpsProducer {
    static final String topic = "simpleTpsTopic";

    public static void main(String[] args) throws MQClientException {
        final int threadCount = args.length >= 1 ? Integer.parseInt(args[1]) : 1;
        final int messageSize = args.length >= 2 ? Integer.parseInt(args[1]) : 128;

        final Message msg = buildMessage(messageSize);
        final ExecutorService sendThreadPool = Executors.newFixedThreadPool(threadCount);
        final TpsStatsProducer tpsStats = new TpsStatsProducer();
        final Timer timer = new Timer("TpsTimerThread", true);
        final LinkedList<Long[]> snapshotList = new LinkedList<Long[]>();
        final DecimalFormat df = new DecimalFormat("####.###");

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                snapshotList.addLast(tpsStats.createSnapshot());
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
                    // index含义：0-代表当前时间，1-发送成功次数 2-发送失败次数 3-接收成功次数 4-接收失败次数  5-发送消息成功总耗时
                    final long sendTps = (long) (((end[3] - begin[3]) / (double) (end[0] - begin[0])) * 1000L);
                    final double averageRT = ((end[5] - begin[5]) / (double) (end[3] - begin[3]));
                    System.out.printf(
                            "send message success=%d, TPS=%d, max runTime=%d ms, average runTime=%s ms, send failed=%d, response failed=%d\n"
                            , end[1], sendTps, tpsStats.getSendMessageMaxRT().get(), df.format(averageRT), end[2], end[4]
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

        final DefaultMQProducer producer = new DefaultMQProducer("DefaultCluster");
        producer.setNamesrvAddr(MyUtils.getNamesrvAddr());
        producer.setCompressMsgBodyOverHowmuch(Integer.MAX_VALUE);
        producer.start();

        System.out.println("threadCount=" + threadCount + ", messageSize=" + messageSize);
        System.out.println("producerGroup=" + producer.getProducerGroup() + ", instanceName=" + producer.getInstanceName() + ", producer started.");

        for (int i = 0; i < threadCount; i++) {
            sendThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            final long beginTimestamp = System.currentTimeMillis();
                            producer.send(msg);
                            tpsStats.getSendRequestSuccessCount().incrementAndGet();
                            tpsStats.getReceiveResponseSuccessCount().incrementAndGet();

                            final long currentRT = System.currentTimeMillis() - beginTimestamp;
                            tpsStats.getSendMessageSuccessTimeTotal().addAndGet(currentRT);

                            long prevMaxRT = tpsStats.getSendMessageMaxRT().get();
                            while (currentRT > prevMaxRT) {
                                boolean updated = tpsStats.getSendMessageMaxRT().compareAndSet(prevMaxRT, currentRT);
                                if (updated) {
                                    break;
                                }
                                prevMaxRT = tpsStats.getSendMessageMaxRT().get();
                            }
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


    private static Message buildMessage(final int messageSize) {
        Message msg = new Message();
        msg.setTopic(topic);
        StringBuilder sb = new StringBuilder(messageSize + 1);
        for (int i = 0; i < messageSize; i += 10) {
            sb.append("hello baby");
        }
        msg.setBody(sb.toString().getBytes());
        return msg;
    }
}
