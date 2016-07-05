package com.gome.demo.springwithbean;

import com.gome.api.open.base.Consumer;
import com.gome.api.open.base.Producer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author tantexian
 * @since 2016/6/27
 */
public class ConsumeWithSpring {
    public static void main(String[] args) {
        /**
         * 消费者Bean配置在consumer.xml中,可通过ApplicationContext获取或者直接注入到其他类(比如具体的Controller)中.
         * 此处为启动消费者，具体消息消费，在consumer.xml中配置的对应Listener
         */
        ApplicationContext context = new ClassPathXmlApplicationContext("consumer.xml");
        Consumer consumer = (Consumer) context.getBean("consumer");
        assert consumer != null;
        ConsumerMessageListener msgListener = (ConsumerMessageListener) context.getBean("msgListener");
        assert msgListener != null;

        consumer.subscribe("TopicTestMQ", "*", msgListener);

        System.out.println("ConsumeWithSpring consumer Started");
    }
}
