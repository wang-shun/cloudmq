package com.wish.rocketmq.example.ttx;

import java.util.List;

import com.alibaba.rocketmq.client.consumer.AllocateMessageQueueStrategy;
import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import com.alibaba.rocketmq.client.consumer.rebalance.AllocateMessageQueueAveragely;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.common.consumer.ConsumeFromWhere;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.alibaba.rocketmq.remoting.RPCHook;
import com.alibaba.rocketmq.remoting.protocol.RemotingCommand;


public class SimplePushConsumerRPCHook {

    /**
     * 当前例子是PushConsumer用法，使用方式给用户感觉是消息从RocketMQ服务器推到了应用客户端。<br>
     * 但是实际PushConsumer内部是使用长轮询Pull方式从Broker拉消息，然后再回调用户Listener方法<br>
     */
    public static void main(String[] args) throws InterruptedException, MQClientException {

        RPCHook rpcHook = new RPCHook() {

            @Override
            public void doBeforeRequest(String remoteAddr, RemotingCommand request) {
                System.out.println("doBeforeRequest");
            }


            @Override
            public void doAfterResponse(String remoteAddr, RemotingCommand request,
                    RemotingCommand response) {
                System.out.println("doAfterResponse");
            }
        };

        AllocateMessageQueueStrategy allocateMessageQueueStrategy = new AllocateMessageQueueAveragely();

        /**
         * 一个应用创建一个Consumer，由应用来维护此对象，可以设置为全局对象或者单例<br>
         * 注意：ConsumerGroupName需要由应用来保证唯一
         */
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("SimpleRPCHookPushConsumer_001", rpcHook,
            allocateMessageQueueStrategy);
        String namesrvAddr = "192.168.15.11:9876;192.168.15.12:9876";
        consumer.setNamesrvAddr(namesrvAddr);

        /**
         * 订阅指定topic下tags分别等于TagA或TagB或TagD
         */
        consumer.subscribe("RPCHookTopic", "*");

        /**
         * 设置Consumer第一次启动是从队列头部开始消费还是队列尾部开始消费<br>
         * 如果非第一次启动，那么按照上次消费的位置继续消费
         */
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);

        consumer.registerMessageListener(new MessageListenerConcurrently() {

            /**
             * 默认msgs里只有一条消息，可以通过设置consumeMessageBatchMaxSize参数来批量接收消息
             */
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs,
                    ConsumeConcurrentlyContext context) {
                System.out.println(Thread.currentThread().getName() + " Receive New Messages: " + msgs);

                MessageExt msg = msgs.get(0);
                if (msg.getTopic().equals("SimpleOrderTopic")) {
                    // 执行TopicTest1的消费逻辑
                    if (msg.getTags() != null && msg.getTags().equals("TagA")) {
                        // 执行TagA的消费
                        System.out
                            .println("####----获取订单消息TagA：" + new String(msg.getBody()) + ", 业务逻辑处理ing...\n");
                    }
                    else if (msg.getTags() != null && msg.getTags().equals("TagC")) {
                        // 执行TagC的消费
                        System.out
                            .println("####----获取订单消息TagC：" + new String(msg.getBody()) + ", 业务逻辑处理ing...\n");
                    }
                    else if (msg.getTags() != null && msg.getTags().equals("TagD")) {
                        // 执行TagD的消费
                        System.out
                            .println("####----获取订单消息TagD：" + new String(msg.getBody()) + ", 业务逻辑处理ing...\n");
                    }
                }
                else if (msg.getTopic().equals("SimplePayTopic")) {
                    // 执行TopicTest2的消费逻辑
                    System.out.println("####----获取付款消息：" + new String(msg.getBody()) + ", 业务逻辑处理ing...\n");
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });

        /**
         * Consumer对象在使用之前必须要调用start初始化，初始化一次即可<br>
         */
        consumer.start();

        System.out.println("Consumer Started.");
    }
}
