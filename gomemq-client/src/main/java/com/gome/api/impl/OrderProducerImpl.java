package com.gome.api.impl;

import com.alibaba.rocketmq.client.log.ClientLogger;
import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.client.producer.MessageQueueSelector;
import com.alibaba.rocketmq.common.UtilAll;
import com.alibaba.rocketmq.common.message.MessageQueue;
import com.gome.api.open.GomeClientException;
import com.gome.api.open.Message;
import com.gome.api.open.OrderProducer;
import com.gome.api.open.SendResult;
import com.gome.common.MQTraceConstants;
import com.gome.util.MyUtils;
import org.slf4j.Logger;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author tantexian
 * @since 2016/6/27
 */
public class OrderProducerImpl extends MQClientAbstract implements OrderProducer {
    private static final Logger log = ClientLogger.getLog();
    private final DefaultMQProducer defaultMQProducer;
    private final AtomicBoolean started = new AtomicBoolean(false);

    public OrderProducerImpl(Properties properties) {
        super(properties);
        this.defaultMQProducer = new DefaultMQProducer();
        String producerGroup = properties.getProperty("ProducerId", "__ONS_PRODUCER_DEFAULT_GROUP");
        this.defaultMQProducer.setProducerGroup(producerGroup);
        boolean isVipChannelEnabled = Boolean.parseBoolean(properties.getProperty("isVipChannelEnabled", "false"));
        String sendMsgTimeoutMillis = properties.getProperty("SendMsgTimeoutMillis", "3000");
        this.defaultMQProducer.setSendMsgTimeout(Integer.parseInt(sendMsgTimeoutMillis));
        this.defaultMQProducer.setInstanceName(this.buildIntanceName());
        this.defaultMQProducer.setNamesrvAddr(this.getNameServerAddr());

        try {
            Properties e = new Properties();
            e.put("MaxMsgSize", "128000");
            e.put("AsyncBufferSize", "2048");
            e.put("MaxBatchNum", "1");
            e.put("WakeUpNum", "1");
            e.put(MQTraceConstants.NAMESRV_ADDR, this.getNameServerAddr());
            e.put("InstanceName", this.buildIntanceName());
        } catch (Throwable var7) {
            log.error("system mqtrace hook init failed ,maybe can\'t send msg trace data");
        }

    }

    public void start() {
        try {
            if(this.started.compareAndSet(false, true)) {
                this.defaultMQProducer.start();
            }

        } catch (Exception var2) {
            throw new GomeClientException(var2.getMessage());
        }
    }

    public void shutdown() {
        if(this.started.compareAndSet(true, false)) {
            this.defaultMQProducer.shutdown();
        }

    }

    public SendResult send(Message message, String shardingKey) {
        if(UtilAll.isBlank(shardingKey)) {
            throw new GomeClientException("\'shardingKey\' is blank.");
        } else {
            this.checkONSProducerServiceState(this.defaultMQProducer.getDefaultMQProducerImpl());
            com.alibaba.rocketmq.common.message.Message msgRMQ = MyUtils.msgConvert(message);

            try {
                com.alibaba.rocketmq.client.producer.SendResult e = this.defaultMQProducer.send(msgRMQ, new MessageQueueSelector() {
                    public MessageQueue select(List<MessageQueue> mqs, com.alibaba.rocketmq.common.message.Message msg, Object shardingKey) {
                        int select = Math.abs(shardingKey.hashCode());
                        if(select < 0) {
                            select = 0;
                        }

                        return (MessageQueue)mqs.get(select % mqs.size());
                    }
                }, shardingKey);
                SendResult sendResult = new SendResult();
                return sendResult;
            } catch (Exception var6) {
                throw new GomeClientException("defaultMQProducer send order exception", var6);
            }
        }
    }

    public boolean isStarted() {
        return this.started.get();
    }

    public boolean isClosed() {
        return !this.isStarted();
    }
}
