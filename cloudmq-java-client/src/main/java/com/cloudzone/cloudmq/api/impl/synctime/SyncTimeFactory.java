package com.cloudzone.cloudmq.api.impl.synctime;

import com.cloudzone.cloudlimiter.base.IntervalModel;
import com.cloudzone.cloudlimiter.factory.CloudFactory;
import com.cloudzone.cloudlimiter.meter.CloudMeter;
import com.cloudzone.cloudmq.api.impl.base.MQSyncTimeListener;
import com.cloudzone.cloudmq.api.impl.meter.MeterListenerImpl;
import com.cloudzone.cloudmq.api.impl.transfer.TransferToMQImpl;

/**
 * @author yintongjiang
 * @params
 * @since 2017/4/12
 */
public class SyncTimeFactory {
    private SyncTimeFactory() {
    }

    private static volatile MQSyncTimeListener mqSyncTimeListener;

    public static boolean isRegister() {
        return null != mqSyncTimeListener;
    }

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
