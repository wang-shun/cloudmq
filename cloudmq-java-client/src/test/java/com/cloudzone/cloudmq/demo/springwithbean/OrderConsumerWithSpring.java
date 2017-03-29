package com.cloudzone.cloudmq.demo.springwithbean;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author leiyuanjie
 * @since 2017-02-22.
 */
public class OrderConsumerWithSpring {
    public static void main(String[] args) {
        /**
         * 消费者Bean配置在orderConsumer.xml中,可通过ApplicationContext获取或者直接注入到其他类(比如具体的Controller)中.
         * 此处为启动消费者，具体消息消费，在orderConsumer.xml中配置的对应Listener
         */
        ApplicationContext context = new ClassPathXmlApplicationContext("orderConsumer.xml");
        assert context != null;
        System.out.println("orderConsumerWithSpring consumer Started");
    }

}
