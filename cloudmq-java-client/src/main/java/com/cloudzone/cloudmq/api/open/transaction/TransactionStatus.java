package com.cloudzone.cloudmq.api.open.transaction;

/**
 * @author tantexian
 * @since 2016/6/27
 */
public enum TransactionStatus {
    CommitTransaction,      // 事务提交
    RollbackTransaction,    // 事务回滚
    Unknow;                 // 未知结果

    private TransactionStatus() {
    }
}
