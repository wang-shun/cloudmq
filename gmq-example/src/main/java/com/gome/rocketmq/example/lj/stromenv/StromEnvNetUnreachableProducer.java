package com.gome.rocketmq.example.lj.stromenv;

import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.client.producer.SendResult;
import com.alibaba.rocketmq.common.message.Message;
import com.gome.rocketmq.example.lj.common.StromUtils;

import java.util.UUID;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author jerrylou
 * @params
 * @since 2016/7/22 0022
 */
public class StromEnvNetUnreachableProducer {

    private final static int nThreads = 1000;
    private final static int onceMsgNums = 1000;

    // gmq生产者发送消息 2016/7/22 0022 Add by jerrylou
    public static void main(String[] args) {
        try {
            testMain();
        } catch (MQClientException e) {
            e.printStackTrace();
        }
    }

    public static void testMain() throws MQClientException {

        //创建一个生产者
        final DefaultMQProducer producer = new DefaultMQProducer(StromUtils.getProducerGroup());
        producer.setNamesrvAddr(StromUtils.getNamesrvAddr());
        producer.setDefaultTopicQueueNums(StromUtils.getTopicQueueNums());
        producer.start();

        //计数器
        final AtomicLong atomicSuccessNums = new AtomicLong(0L);
        final AtomicLong atomicFailedNums = new AtomicLong(0L);
        final AtomicLong atomicSlaveNotNums = new AtomicLong(0L);
        final AtomicLong atomicMsgIds = new AtomicLong(0L);

        //创建线程池
        final ExecutorService exec = Executors.newFixedThreadPool(nThreads);
        final long startCurrentTimeMillis = System.currentTimeMillis();
        final CyclicBarrier barrier = new CyclicBarrier(nThreads, new Runnable() {
            @Override
            public void run() {
                long endCurrentTimeMillis = System.currentTimeMillis();
                long escapedTimeMillis = endCurrentTimeMillis - startCurrentTimeMillis;

                System.out.printf(
                        "All message has send, send topicNums is : %d, " + "Success nums is : %d, "
                                + "failed nums is : %d, "
                                + "Slave not available nums is : %d, "
                                + "send sucess nums is : %d, "
                                + " msgCount is : %d, "
                                + "TPS : %d !!!",
                        nThreads * onceMsgNums, atomicSuccessNums.get(), atomicFailedNums.get(),
                        atomicSlaveNotNums.get(),atomicSuccessNums.get() + atomicSlaveNotNums.get(),
                        atomicMsgIds.get(),
                        nThreads * onceMsgNums * 1000 / escapedTimeMillis);
                producer.shutdown();
                exec.shutdown();
            }
        });

        //使用线程生产数据
        for (int i = 0; i < nThreads; i++) {
            final int it = i;
            exec.execute(new Runnable() {
                @Override
                public void run() {
                    for (int j = 0; j < onceMsgNums; j++) {
                        atomicMsgIds.incrementAndGet();
                        final Message message = new Message(StromUtils.getTopicName(),
                                StromUtils.getTags(), ("body" + it + "--" + j).getBytes());
                        message.setKeys(UUID.randomUUID().toString());
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
                                    atomicFailedNums.incrementAndGet();
                                    System.out.println("#### ERROR Message :" + sendResult);
                                    break;
                                case FLUSH_SLAVE_TIMEOUT:
                                    atomicFailedNums.incrementAndGet();
                                    System.out.println("#### ERROR Message :" + sendResult);
                                    break;
                            }
                        } catch (Exception e) {
                            atomicFailedNums.incrementAndGet();
                            e.printStackTrace();
                        }
                    }
                    System.out.println(
                            (barrier.getNumberWaiting() + 1) + "位完成：" + Thread.currentThread().getName());
                    try {
                        barrier.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (BrokenBarrierException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

    }
    
}
