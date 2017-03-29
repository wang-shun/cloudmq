package com.cloudzone.cloudmq.demo.transaction;

import com.cloudzone.cloudmq.api.open.base.Msg;
import com.cloudzone.cloudmq.api.open.transaction.LocalTransactionChecker;
import com.cloudzone.cloudmq.api.open.transaction.TransactionStatus;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 服务器回查客户端
 * 以下只是一个业务场景的模拟，具体实现需由任务团队自行实现
 * @author leiyuanjie
 * @since 2017-02-17.
 */
public class LocalTransactionCheckerImpl implements LocalTransactionChecker {
    //创建一个初始值为0的AtomicInteger对象，不带参数默认创建初始值为0的对象
    private AtomicInteger transactionIndex = new AtomicInteger(0);

    @Override
    public TransactionStatus check(Msg msg) {
        //产生一个随机数，，模拟业务场景
        int value = transactionIndex.getAndIncrement();
        //模拟当 value 为6 的倍数 时，抛出异常!
        if ((value % 6) == 0) {
            throw new RuntimeException("Could not find db");
        }
        //模拟当 value 为5的倍数时，回滚事务。
        else if ((value % 5) == 0) {
            return TransactionStatus.RollbackTransaction;
        }
        //其他情况则默认为事务的 CommitTransaction
        else if ((value % 4) == 0) {
            return TransactionStatus.CommitTransaction;
        }
        return TransactionStatus.CommitTransaction;
    }
}
