package com.cloudzone.cloudmq.api.impl.producer;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import com.cloudzone.cloudmq.api.open.base.Msg;
import com.cloudzone.cloudmq.api.open.exception.GomeClientException;
import com.cloudzone.cloudmq.api.open.order.OrderProducer;
import com.cloudzone.cloudmq.common.PropertiesConst;
import org.slf4j.Logger;

import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.client.producer.MessageQueueSelector;
import com.alibaba.rocketmq.common.UtilAll;
import com.alibaba.rocketmq.common.message.MessageQueue;
import com.cloudzone.cloudmq.api.impl.base.MQClientAbstract;
import com.cloudzone.cloudmq.api.open.base.SendResult;
import com.cloudzone.cloudmq.common.MQTraceConstants;
import com.cloudzone.cloudmq.common.MyUtils;
import com.cloudzone.cloudmq.log.GClientLogger;

/**
 * @author tantexian
 * @since 2016/6/27
 */
public class OrderProducerImpl extends MQClientAbstract implements OrderProducer {
    private static final Logger log = GClientLogger.getLog();
    private final DefaultMQProducer defaultMQProducer;
    private final AtomicBoolean started = new AtomicBoolean(false);

    public OrderProducerImpl(Properties properties) {
        super(properties);
        this.defaultMQProducer = new DefaultMQProducer();
        String producerGroup = properties.getProperty(PropertiesConst.Keys.ProducerGroupId);
        if (UtilAll.isBlank(producerGroup)) {
            throw new GomeClientException(String.format("'%s' property can't be empty", PropertiesConst.Keys.ProducerGroupId));
        }

        this.defaultMQProducer.setProducerGroup(producerGroup.trim());
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
        } catch (Throwable throwable) {
            log.error("system mqtrace hook init failed ,maybe can\'t send msg trace data");
        }

    }

    public void start() {
        try {
            if (this.started.compareAndSet(false, true)) {
                this.defaultMQProducer.start();
                this.startSchedule();
            }

        } catch (Exception exception) {
            throw new GomeClientException(exception.getMessage());
        }
    }

    public void shutdown() {
        if (this.started.compareAndSet(true, false)) {
            this.defaultMQProducer.shutdown();
            this.shutdownSchedule();
        }

    }

    public SendResult send(Msg msg, String shardingKey) {
        // 发送消息时候对topic进行校验和统计限流 2017/3/29 Add by yintongqiang
        this.checkTopic(this.properties, msg.getTopic(), msg);
        if (UtilAll.isBlank(shardingKey)) {
            throw new GomeClientException("\'shardingKey\' is blank.");
        } else {
            //this.checkONSProducerServiceState(this.defaultMQProducer.getDefaultMQProducerImpl());
            com.alibaba.rocketmq.common.message.Message msgRMQ = MyUtils.msgConvert(msg);

            try {
                com.alibaba.rocketmq.client.producer.SendResult sendResultRMQ = this.defaultMQProducer.send(msgRMQ, new MessageQueueSelector() {
                    public MessageQueue select(List<MessageQueue> mqs, com.alibaba.rocketmq.common.message.Message msg, Object shardingKey) {
                        int select = Math.abs(shardingKey.hashCode());
                        if (select < 0) {
                            select = 0;
                        }

                        return (MessageQueue) mqs.get(select % mqs.size());
                    }
                }, shardingKey);
                SendResult sendResult = new SendResult();
                sendResult.setMsgId(sendResultRMQ.getMsgId());
                sendResult.setSendStatus(sendResultRMQ.getSendStatus());
                sendResult.setMessageQueue(sendResultRMQ.getMessageQueue());
                sendResult.setQueueOffset(sendResultRMQ.getQueueOffset());
                return sendResult;
            } catch (Exception exception) {
                throw new GomeClientException("defaultMQProducer send order exception", exception);
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
