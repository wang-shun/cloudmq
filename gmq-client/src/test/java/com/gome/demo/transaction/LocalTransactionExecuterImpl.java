package com.gome.demo.transaction;

import com.gome.api.open.base.Msg;
import com.gome.api.open.transaction.LocalTransactionExecuter;
import com.gome.api.open.transaction.TransactionStatus;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 执行本地事务
 * 以下只是一个业务场景的模拟，具体实现需由任务团队自行实现
 * @author leiyuanjie
 * @since 2017-02-17.
 */
public class LocalTransactionExecuterImpl implements LocalTransactionExecuter {
    //创建一个初始值为1的AtomicInteger对象，不带参数默认创建初始值为0的对象
    private AtomicInteger transactionIndex = new AtomicInteger(1);

    @Override
    public TransactionStatus execute(Msg msg, Object arg) {
        //产生一个随机数 value ，模拟业务场景
        int value = transactionIndex.getAndIncrement();
        //当 value 为0 时，抛出异常!
        if (value == 0) {
            throw new RuntimeException("Can not find db");
            //模拟当 value 为5的倍数时，回滚事务。
        } else if (value % 5 == 0) {
            return TransactionStatus.RollbackTransaction;
        }
        //其他情况则默认为事务的 CommitTransaction
        return TransactionStatus.CommitTransaction;

    }
}
