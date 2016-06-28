package com.gome.api.open.transaction;

import com.gome.api.open.base.Message;

/**
 * @author tantexian
 * @since 2016/6/27
 */
public interface LocalTransactionChecker {
    TransactionStatus check(Message var1);
}
