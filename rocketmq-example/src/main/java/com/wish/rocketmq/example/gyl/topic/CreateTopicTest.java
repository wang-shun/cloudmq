package com.wish.rocketmq.example.gyl.topic;

import com.alibaba.rocketmq.broker.BrokerController;
import com.alibaba.rocketmq.broker.topic.TopicConfigManager;
import com.alibaba.rocketmq.common.BrokerConfig;
import com.alibaba.rocketmq.common.MixAll;
import com.alibaba.rocketmq.common.TopicConfig;
import com.alibaba.rocketmq.remoting.netty.NettyClientConfig;
import com.alibaba.rocketmq.remoting.netty.NettyServerConfig;
import com.alibaba.rocketmq.store.config.MessageStoreConfig;


/**
 * 测试创建topic queue数目
 *
 * @author: GaoYanLei
 * @since: 2016/6/12
 */

public class CreateTopicTest {
    public static void main(String[] args) throws Exception {
        BrokerController brokerController = new BrokerController(//
            new BrokerConfig(), //
            new NettyServerConfig(), //
            new NettyClientConfig(), //
            new MessageStoreConfig());
        boolean initResult = brokerController.initialize();
        System.out.println("initialize " + initResult);
        brokerController.start();

        TopicConfigManager topicConfigManager = new TopicConfigManager(brokerController);

        TopicConfig topicConfig1 = topicConfigManager.createTopicInSendMessageMethod("TestTopic_16",
            MixAll.DEFAULT_TOPIC, null, 16, 0);
        TopicConfig topicConfig2 = topicConfigManager.createTopicInSendMessageMethod("TestTopic_4",
            MixAll.DEFAULT_TOPIC, null, 4, 0);

        System.out.println(topicConfig1);
        System.out.println(topicConfig2);
        topicConfigManager.persist();
        brokerController.shutdown();
    }
}
