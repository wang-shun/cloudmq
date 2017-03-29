package com.cloudzone.cloudmq.api.open.transaction;

/**
 * @author tantexian
 * @since 2016/6/27
 */
public enum TransactionStatus {
    CommitTransaction,
    RollbackTransaction,
    Unknow;

    private TransactionStatus() {
    }
}
