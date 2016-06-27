package com.gome.api.open;

/**
 * @author tantexian
 * @since 2016/6/27
 */
public interface LocalTransactionExecuter {
    TransactionStatus execute(Message var1, Object var2);
}
