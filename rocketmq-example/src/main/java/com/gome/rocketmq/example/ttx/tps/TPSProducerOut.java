package com.gome.rocketmq.example.ttx.tps;

import com.alibaba.rocketmq.client.exception.MQBrokerException;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.client.producer.SendResult;
import com.alibaba.rocketmq.client.producer.SendStatus;
import com.alibaba.rocketmq.common.message.Message;
import com.alibaba.rocketmq.remoting.exception.RemotingException;
import com.gome.rocketmq.common.MyUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;


public class TPSProducerOut {

    private static int nThreads;
    private static int sendNumOnceTime;
    private static int topicNums;


    public static void main(String[] args) throws MQClientException {
        final int B = 100;
        final int K = 1000;
        final int W = 10000;
        nThreads = args.length >= 1 ? Integer.parseInt(args[0]) : 1*K;
        sendNumOnceTime = args.length >= 2 ? Integer.parseInt(args[1]) : 1*K;
        topicNums = args.length >= 3 ? Integer.parseInt(args[2]) : 1024*2;

        final AtomicLong atomicSuccessNums = new AtomicLong(0l);

        final DefaultMQProducer producer = new DefaultMQProducer("TPSProducer");
        producer.setNamesrvAddr(MyUtils.getNamesrvAddr());

        // producer.setSendMsgTimeout(5000);

        producer.start();

        ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(nThreads);
        long startCurrentTimeMillis = System.currentTimeMillis();

        for (int i = 0; i < nThreads; i++) {
            //String topicRandom = "TpsTopic" + new Random().nextInt(topicNums);
            String topicRandom = "TpsTopic";
            // System.out.println("TpsTopic name is :" + topicRandom);
            final Message message = new Message(topicRandom, "tagA", ("I,m test tps topic " + i).getBytes());

            for (int j = 0; j < sendNumOnceTime; j++) {
                newFixedThreadPool.execute(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            SendResult sendResult = producer.send(message);
                            if (sendResult.getSendStatus() == SendStatus.SEND_OK) {
                                System.out.println(atomicSuccessNums.incrementAndGet() + "    message == " + message);
                            }
                            else {
                                System.out.println("#### ERROR Message :" + sendResult);
                            }
                        }
                        catch (MQClientException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        catch (RemotingException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        catch (MQBrokerException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                    }
                });
            }

        }
        try {
            newFixedThreadPool.shutdown();
            newFixedThreadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
            while (true) {
                if (newFixedThreadPool.isTerminated()) {
                    long endCurrentTimeMillis = System.currentTimeMillis();
                    long sendNums = nThreads * sendNumOnceTime;
                    long escapedTimeMillis = endCurrentTimeMillis - startCurrentTimeMillis;
                    System.out.printf(
                        "All message has send, the random topicNums is : %d, " + "the message nums is : %d , "
                                + "Success nums is : %d, " + "TPS : %d !!!",
                        topicNums, sendNums, atomicSuccessNums.get(), sendNums * 1000 / escapedTimeMillis);
                    System.exit(0);
                }
            }
        }
        catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
