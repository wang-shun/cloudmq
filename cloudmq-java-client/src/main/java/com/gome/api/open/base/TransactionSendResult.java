package com.gome.api.open.base;

/**
 * @author leiyuanjie
 * @since 2017-02-20.
 */
public class TransactionSendResult extends com.alibaba.rocketmq.client.producer.TransactionSendResult{
    @Override
    public String toString() {
        return super.toString() + ", localTransactionState=" + this.getLocalTransactionState();
    }
}
