package com.cloudzone.cloudmq.api.impl.synctime;

import com.cloudzone.cloudlimiter.base.IntervalModel;
import com.cloudzone.cloudlimiter.factory.CloudFactory;
import com.cloudzone.cloudlimiter.meter.CloudMeter;
import com.cloudzone.cloudmq.api.impl.base.MQSyncTimeListener;
import com.cloudzone.cloudmq.api.impl.meter.MeterListenerImpl;
import com.cloudzone.cloudmq.api.impl.transfer.TransferToMQImpl;

/**
 * @author yintongjiang
 * @params 同步时间工厂
 * @since 2017/4/25
 */
public class SyncTimeFactory {
    private SyncTimeFactory() {
    }

    private static volatile MQSyncTimeListener mqSyncTimeListener;

    // 判断mqSyncTimeListener 是否已经实例化了 2017/4/25 Add by yintongqiang
    public static boolean isRegister() {
        return null != mqSyncTimeListener;
    }

    // 线程安全创建mqSyncTimeListener实例 2017/5/4 Add by yintongqiang
    public static MQSyncTimeListener getSyncTimeListenerSingleton() {
        if (null == mqSyncTimeListener) {
            synchronized (SyncTimeFactory.class) {
                if (null == mqSyncTimeListener) {
                    mqSyncTimeListener = new MQSyncTimeListenerImpl();
                }
            }
        }
        return mqSyncTimeListener;
    }
}
