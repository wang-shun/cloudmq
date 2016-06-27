package com.gome.api.open;

/**
 * @author tantexian
 * @since 2016/6/27
 */
public interface LocalTransactionChecker {
    TransactionStatus check(Message var1);
}
