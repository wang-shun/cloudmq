/**
 * Copyright (C) 2010-2013 Alibaba Group Holding Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gome.rocketmq.example.gyl.transaction;

import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.producer.SendResult;
import com.alibaba.rocketmq.client.producer.SendStatus;
import com.alibaba.rocketmq.client.producer.TransactionCheckListener;
import com.alibaba.rocketmq.client.producer.TransactionMQProducer;
import com.alibaba.rocketmq.common.message.Message;
import com.gome.rocketmq.common.MyUtils;
import com.gome.rocketmq.example.gyl.transaction.*;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;


/**
 * 发送事务消息例子
 * 
 */
public class TransactionProducer {
    final static int nThreads = 1000;
    final static int runNums = 100;


    public static void main(String[] args) throws MQClientException {
        final AtomicLong atomicSuccessNums = new AtomicLong(0l);
        TransactionCheckListener transactionCheckListener = new TransactionCheckListenerImpl();
        final TransactionMQProducer producer = new TransactionMQProducer("delayTime");
        producer.setNamesrvAddr(MyUtils.getNamesrvAddr());
        producer.setTransactionCheckListener(transactionCheckListener);
        producer.start();
        ExecutorService exec = Executors.newCachedThreadPool();
        final TransactionExecuterImpl tranExecuter = new TransactionExecuterImpl();
        final long startCurrentTimeMillis = System.currentTimeMillis();
        final CyclicBarrier barrier = new CyclicBarrier(nThreads, new Runnable() { // 设置几个线程为一组,当这一组的几个线程都执行完成后,然后执行住线程的
            @Override
            public void run() {
                long endCurrentTimeMillis = System.currentTimeMillis();
                long escapedTimeMillis = endCurrentTimeMillis - startCurrentTimeMillis;
                System.out.printf("All message has send, send topicNums is : %d, " + "TPS : %d !!!",
                    nThreads * runNums, nThreads * runNums * 1000 / escapedTimeMillis);
                producer.shutdown();
            }
        });
        for (int i = 0; i < nThreads; i++) {
            exec.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        for (int j = 0; j < runNums; j++) {
                            Message msg = new Message("transaction_topic", "A", "KEY",
                                ("Hello RocketMQ ").getBytes());

                            producer.sendMessageInTransaction(msg, tranExecuter, null);
                        }
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
