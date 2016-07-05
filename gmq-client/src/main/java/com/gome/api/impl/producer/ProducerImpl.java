package com.gome.api.impl.producer;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import com.gome.api.open.base.Msg;
import org.slf4j.Logger;

import com.alibaba.rocketmq.client.exception.MQBrokerException;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.log.ClientLogger;
import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.remoting.exception.RemotingConnectException;
import com.alibaba.rocketmq.remoting.exception.RemotingTimeoutException;
import com.gome.api.impl.base.MQClientAbstract;
import com.gome.api.open.base.Producer;
import com.gome.api.open.base.SendResult;
import com.gome.api.open.exception.GomeClientException;
import com.gome.common.FAQ;
import com.gome.common.PropertiesConst;

;


/**
 * @author tantexian
 * @since 2016/6/27
 */
public class ProducerImpl extends MQClientAbstract implements Producer {
    private static final Logger log = ClientLogger.getLog();
    private final DefaultMQProducer defaultMQProducer;
    private final AtomicBoolean started = new AtomicBoolean(false);

    public ProducerImpl(Properties properties) {
        super(properties);
        this.defaultMQProducer = new DefaultMQProducer();
        String producerGroup = properties.getProperty("ProducerGroupId", "__PRODUCER_DEFAULT_GROUP");
        this.defaultMQProducer.setProducerGroup(producerGroup);
        if (properties.containsKey("SendMsgTimeoutMillis")) {
            this.defaultMQProducer.setSendMsgTimeout(
                Integer.valueOf(properties.get("SendMsgTimeoutMillis").toString()).intValue());
        }
        else {
            this.defaultMQProducer.setSendMsgTimeout(5000);
        }

        this.defaultMQProducer.setInstanceName(this.buildIntanceName());
        this.defaultMQProducer.setNamesrvAddr(this.getNameServerAddr());
        this.defaultMQProducer.setMaxMessageSize(4194304);

        try {
            Properties e = new Properties();
            e.put("MaxMsgSize", "128000");
            e.put("AsyncBufferSize", "2048");
            e.put("MaxBatchNum", "1");
            e.put("WakeUpNum", "1");
            e.put(PropertiesConst.Keys.NAMESRV_ADDR, this.getNameServerAddr());
            e.put("InstanceName", this.buildIntanceName());
        }
        catch (Throwable throwable) {
            log.error("system mqtrace hook init failed ,maybe can\'t send msg trace data");
        }

    }


    public void start() {
        try {
            if (this.started.compareAndSet(false, true)) {
                this.defaultMQProducer.start();
            }

        }
        catch (Exception e) {
            throw new GomeClientException(e.getMessage());
        }
    }


    public void shutdown() {
        if (this.started.compareAndSet(true, false)) {
            this.defaultMQProducer.shutdown();
        }

    }


    public SendResult send(Msg msg) {
        //this.checkONSProducerServiceState(this.defaultMQProducer.getDefaultMQProducerImpl());

        try {
            com.alibaba.rocketmq.client.producer.SendResult sendResultRMQ = this.defaultMQProducer.send(msg);
            SendResult sendResult = new SendResult();
            sendResult.setMsgId(sendResultRMQ.getMsgId());
            sendResult.setSendStatus(sendResultRMQ.getSendStatus());
            sendResult.setMessageQueue(sendResultRMQ.getMessageQueue());
            sendResult.setQueueOffset(sendResultRMQ.getQueueOffset());
            return sendResult;
        }
        catch (Exception e) {
            log.error(String.format("Send msg Exception, %s", new Object[] {msg}), e);
            this.checkProducerException(e, msg);
            return null;
        }
    }


    private void checkProducerException(Exception e, Msg msg) {
        if (e instanceof MQClientException) {
            if (e.getCause() != null) {
                if (e.getCause() instanceof RemotingConnectException) {
                    throw new GomeClientException(FAQ.errorMessage(
                        String.format("Connect broker failed, Topic: %s",
                            new Object[] { msg.getTopic() }),
                        "http://docs.aliyun.com/cn#/pub/ons/faq/exceptions&connect_broker_failed"));
                }

                if (e.getCause() instanceof RemotingTimeoutException) {
                    throw new GomeClientException(FAQ.errorMessage(
                        String.format("Send msg to broker timeout, %dms, Topic: %s",
                            new Object[] { Integer.valueOf(this.defaultMQProducer.getSendMsgTimeout()),
                                           msg.getTopic() }),
                        "http://docs.aliyun.com/cn#/pub/ons/faq/exceptions&send_msg_failed"));
                }

                if (e.getCause() instanceof MQBrokerException) {
                    MQBrokerException excep = (MQBrokerException) e.getCause();
                    throw new GomeClientException(FAQ.errorMessage(
                        String.format("Receive a broker exception, Topic: %s, %s",
                            new Object[] { msg.getTopic(), excep.getErrorMessage() }),
                        "http://docs.aliyun.com/cn#/pub/ons/faq/exceptions&broker_response_exception"));
                }
            }
            else {
                MQClientException excep1 = (MQClientException) e;
                if (-1 == excep1.getResponseCode()) {
                    throw new GomeClientException(FAQ.errorMessage(
                        String.format("Topic does not exist, Topic: %s", new Object[] { msg.getTopic() }),
                        "http://docs.aliyun.com/cn#/pub/ons/faq/exceptions&topic_not_exist"));
                }

                if (13 == excep1.getResponseCode()) {
                    throw new GomeClientException(FAQ.errorMessage(
                        String.format("ONS Client check msg exception, Topic: %s",
                            new Object[] { msg.getTopic() }),
                        "http://docs.aliyun.com/cn#/pub/ons/faq/exceptions&msg_check_failed"));
                }
            }
        }

        throw new GomeClientException("defaultMQProducer send exception", e);
    }


    public void sendOneway(Msg msg) {
        this.checkONSProducerServiceState(this.defaultMQProducer.getDefaultMQProducerImpl());

        try {
            this.defaultMQProducer.sendOneway(msg);
        }
        catch (Exception e) {
            log.error(String.format("Send msg oneway Exception, %s", new Object[] {msg}), e);
            this.checkProducerException(e, msg);
        }

    }


    public boolean isStarted() {
        return this.started.get();
    }


    public boolean isClosed() {
        return !this.isStarted();
    }

}
