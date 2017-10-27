package com.cloudzone.cloudmq.common;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;


/**
 * @author yintongjiang
 * @params 统计单位
 * @since 2017/3/30
 */
public class ThreadFactoryImpl implements ThreadFactory {

    private final AtomicLong threadIndex = new AtomicLong(0);

    private final String threadNamePrefix;


    public ThreadFactoryImpl(final String threadNamePrefix) {
        this.threadNamePrefix = threadNamePrefix;
    }


    @Override
    public Thread newThread(Runnable r) {
        String threadName = threadNamePrefix + this.threadIndex.incrementAndGet();
        Thread thread = new Thread(r, threadName);
        // thread.setDaemon(true);
        return thread;
    }
}
