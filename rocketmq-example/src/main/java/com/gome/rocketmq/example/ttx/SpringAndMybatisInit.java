package com.gome.rocketmq.example.ttx;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.TimeZone;
import java.util.concurrent.CountDownLatch;

/**
 * @author tantexian
 * @since 2016/6/14
 */
public class SpringAndMybatisInit {
    public static void start() throws InterruptedException {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+8"));
        final ClassPathXmlApplicationContext ac =
                new ClassPathXmlApplicationContext("spring/spring-config.xml");
        ac.start();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                ac.close();
            }
        });
        CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownLatch.await();
    }
}




