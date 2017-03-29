package com.cloudzone.cloudmq.api.open.bean;

import com.cloudzone.cloudmq.api.open.exception.GomeClientException;
import com.cloudzone.cloudmq.api.open.base.Msg;
import com.cloudzone.cloudmq.api.open.base.TransactionSendResult;
import com.cloudzone.cloudmq.api.open.factory.MQFactory;
import com.cloudzone.cloudmq.api.open.transaction.LocalTransactionChecker;
import com.cloudzone.cloudmq.api.open.transaction.LocalTransactionExecuter;
import com.cloudzone.cloudmq.api.open.transaction.TransactionProducer;

import java.util.Properties;

/**
 * 当前版本目前不支持事务，请勿使用
 *
 * @author tantexian
 * @since 2016/6/27
 */
//@Deprecated
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

    public TransactionSendResult send(Msg msg, LocalTransactionExecuter executer, Object arg) {
        TransactionSendResult transactionSendResult = this.transactionProducer.send(msg, executer, arg);
        return transactionSendResult;
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
