package com.wish.rocketmq.example.gyl.delayTime;

import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * @author: GaoYanLei
 * @since: 2016/6/6
 */

public class TestCyclicBarrier {
    public static void main(String[] args) {

        ExecutorService exec = Executors.newCachedThreadPool(); // 创建一个线程池
        final Random random = new Random();

        final CyclicBarrier barrier = new CyclicBarrier(4, new Runnable() { // 设置几个线程为一组,当这一组的几个线程都执行完成后,然后执行住线程的
            @Override
            public void run() {
                System.out.println("大家都到齐了，开始happy去"); // 所有线程执行完之后在执行
            }
        });

        for (int i = 0; i < 4; i++) {
            exec.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (Thread.currentThread().getName().equals("pool-1-thread-4")) {
                            Thread.sleep(random.nextInt(10000));
                        }
                        Thread.sleep(random.nextInt(1000));
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println(Thread.currentThread().getName() + "到了，其他哥们呢");
                    try {
                        barrier.await(); // 等待其他线程 如果线程数达到设置一组的线程数就执行主线程一次
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    catch (BrokenBarrierException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        exec.shutdown();
    }
}
