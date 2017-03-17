package com.gome.api.open.transaction;

import com.gome.api.open.base.Msg;

/**
 * @author tantexian
 * @since 2016/6/27
 */
public interface LocalTransactionChecker {
    TransactionStatus check(Msg var1);
}
