package com.cloudzone.cloudmq.api.open.transaction;

import com.cloudzone.cloudmq.api.open.base.Msg;


/**
 * @author tantexian
 * @since 2016/6/27
 */
public interface LocalTransactionExecuter {
    TransactionStatus execute(Msg msg1, Object arg);
}
