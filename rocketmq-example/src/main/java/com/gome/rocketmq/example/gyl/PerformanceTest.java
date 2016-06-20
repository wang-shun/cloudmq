package com.gome.rocketmq.example.gyl;

import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import com.gome.rocketmq.common.MyUtils;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.client.producer.SendResult;
import com.alibaba.rocketmq.client.producer.SendStatus;
import com.alibaba.rocketmq.common.message.Message;


/**
 * @author xuanyin
 *
 */
public class PerformanceTest extends AbstractJavaSamplerClient {
    static DefaultMQProducer producer = new DefaultMQProducer();
    static {
        producer.setProducerGroup("performance");
        producer.setNamesrvAddr(MyUtils.getNamesrvAddr());
        producer.setInstanceName("test");
        try {
            producer.start();
        }
        catch (MQClientException e) {
            e.printStackTrace();
        }
    }
    /**
     *
     */
    private static long start = 0;
    private static long end = 0;
    final static int nThreads = 10;
    final static int topicNums = 1000;


    /**
     * 执行runTest()方法前会调用此方法,可放一些初始化代码
     */
    public void setupTest(JavaSamplerContext arg0) {
//        try {
//            producer.setProducerGroup("performance");
//            producer.setNamesrvAddr(MyUtils.getNamesrvAddr());
//            producer.setInstanceName("test");
//            producer.start();
//        }
//        catch (MQClientException e) {
//            e.printStackTrace();
//        }
        // 开始时间
        start = System.currentTimeMillis();
    }


    /**
     * 执行runTest()方法后会调用此方法.
     */
    public void teardownTest(JavaSamplerContext arg0) {

        // 结束时间
        end = System.currentTimeMillis();
        // 总体耗时
        System.err.println("cost time:" + (end - start) / 1000);
    }


    /**
     * JMeter界面中可手工输入参数,代码里面通过此方法获取
     */
    public Arguments getDefaultParameters() {

        Arguments args = new Arguments();
        return args;
    }


    /**
     * JMeter测试用例入口
     */
    @Override
    public SampleResult runTest(JavaSamplerContext arg0) {

        SampleResult sr = new SampleResult();

        try {
            sr.sampleStart();
            // final AtomicLong atomicSuccessNums = new AtomicLong(0l);
            final Message message = new Message("PerformanceTopic", "A", ("test1").getBytes());
            // message.setDelayTimeLevel(14);
            SendResult sendResult = producer.send(message);
            if (sendResult.getSendStatus() == SendStatus.SEND_OK) {
                System.out.println("sendResult:" + sendResult + "============");
            }
            // }
            /**
             * Start~End内的代码会被JMeter 纳入计算吞吐量的范围内,为了使 性能结果合理,无关代码不必放此
             */
            // TODO
            /**
             * True/False可按测试逻辑传值 JMeter会对失败次数做出统计
             */
            sr.setSuccessful(true);
            // End
            sr.sampleEnd();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return sr;
    }


    public static void main(String[] args) throws MQClientException {
        final AtomicLong atomicSuccessNums = new AtomicLong(0l);
        final DefaultMQProducer producer = new DefaultMQProducer("performance");
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
        for (int i = 0; i < 1; i++) {
            exec.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        for (int j = 0; j < 10; j++) {
                            final Message message =
                                    new Message("performanceTopic1", "A", ("test1" + j).getBytes());
                            // message.setDelayTimeLevel(14);
                            SendResult sendResult = producer.send(message);
                            if (sendResult.getSendStatus() == SendStatus.SEND_OK) {
                                System.out.println("sendResult:" + sendResult + "============"
                                        + atomicSuccessNums.incrementAndGet());
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
