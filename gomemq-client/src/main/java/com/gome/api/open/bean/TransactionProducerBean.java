package com.gome.api.open.bean;

import com.gome.api.open.base.Message;
import com.gome.api.open.base.SendResult;
import com.gome.api.open.exception.GomeClientException;
import com.gome.api.open.factory.MQFactory;
import com.gome.api.open.transaction.LocalTransactionChecker;
import com.gome.api.open.transaction.LocalTransactionExecuter;
import com.gome.api.open.transaction.TransactionProducer;

import java.util.Properties;

/**
 * 当前版本目前不支持事务
 *
 * @author tantexian
 * @since 2016/6/27
 */
@Deprecated
public class TransactionProducerBean implements TransactionProducer {
    private Properties properties;
    private LocalTransactionChecker localTransactionChecker;
    private TransactionProducer transactionProducer;

    public TransactionProducerBean() {
    }

    public void start() {
        if(null == this.properties) {
            throw new GomeClientException("properties not set");
        } else {
            this.transactionProducer = MQFactory.createTransactionProducer(this.properties, this.localTransactionChecker);
            this.transactionProducer.start();
        }
    }

    public void shutdown() {
        if(this.transactionProducer != null) {
            this.transactionProducer.shutdown();
        }

    }

    public SendResult send(Message message, LocalTransactionExecuter executer, Object arg) {
        com.alibaba.rocketmq.client.producer.SendResult sendResultRMQ = this.transactionProducer.send(message, executer, arg);
        SendResult sendResult = new SendResult();
        sendResult.setMsgId(sendResultRMQ.getMsgId());
        sendResult.setSendStatus(sendResultRMQ.getSendStatus());
        sendResult.setMessageQueue(sendResultRMQ.getMessageQueue());
        sendResult.setQueueOffset(sendResultRMQ.getQueueOffset());
        return sendResult;
    }

    public Properties getProperties() {
        return this.properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public LocalTransactionChecker getLocalTransactionChecker() {
        return this.localTransactionChecker;
    }

    public void setLocalTransactionChecker(LocalTransactionChecker localTransactionChecker) {
        this.localTransactionChecker = localTransactionChecker;
    }

    public boolean isStarted() {
        return this.transactionProducer.isStarted();
    }

    public boolean isClosed() {
        return this.transactionProducer.isClosed();
    }
}
