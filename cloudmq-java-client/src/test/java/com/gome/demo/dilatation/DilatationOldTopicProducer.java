package com.gome.demo.dilatation;

import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.producer.SendResult;
import com.gome.api.open.base.Msg;
import com.gome.api.open.base.Producer;
import com.gome.api.open.factory.MQFactory;
import com.gome.common.PropertiesConst;

import java.util.Properties;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by yintongjiang on 2016/7/18.
 */
public class DilatationOldTopicProducer {

    public static void main(String[] args) throws MQClientException {
        final int nThreads = args.length >= 1 ? Integer.parseInt(args[0]) : 100;
        final int topicNums = args.length >= 2 ? Integer.parseInt(args[1]) : 100;
        final String topicName = args.length >= 3 ? args[2] : "Test";
        final String tag = args.length >= 4 ? args[3] : "A";
        final String group = args.length >= 5 ? args[4] : "testProducer";

        final AtomicLong atomicSuccessNums = new AtomicLong(0L);
        final AtomicLong atomicSlaveNotNums = new AtomicLong(0L);
        final AtomicLong atomicMsgIds = new AtomicLong(0L);
        final AtomicLong atomicFail = new AtomicLong(0L);
        final AtomicLong flushDiskTimeOutCount = new AtomicLong(0L);
        final AtomicLong flushSlaveTimeOutCount = new AtomicLong(0L);
        Properties properties = new Properties();
        properties.put(PropertiesConst.Keys.ProducerGroupId, group);
        properties.put(PropertiesConst.Keys.NAMESRV_ADDR, "10.128.31.104:9876;10.128.31.105:9876");
        final Producer producer = MQFactory.createProducer(properties);
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
                            final Msg msg = new Msg(topicName, tag, (finalI + "--" + j).getBytes());
                            msg.setKey(atomicMsgIds.incrementAndGet() + "");
                            try {
                                SendResult sendResult = producer.send(msg);
                                switch (sendResult.getSendStatus()) {
                                    case SEND_OK:
                                        atomicSuccessNums.incrementAndGet();
                                        break;
                                    case SLAVE_NOT_AVAILABLE:
                                        atomicSlaveNotNums.incrementAndGet();
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
