package com.cloudzone.cloudmq.api.impl.meter;

import com.cloudzone.cloudlimiter.base.IntervalModel;
import com.cloudzone.cloudlimiter.factory.CloudFactory;
import com.cloudzone.cloudlimiter.meter.CloudMeter;
import com.cloudzone.cloudmq.api.impl.transfer.TransferToMQImpl;

/**
 * @author yintongjiang
 * @params 创建CloudMeter工厂
 * @since 2017/4/12
 */
public class MeterFactory {
    private MeterFactory() {
    }

    private static volatile CloudMeter cloudMeter;

    /**
     * 线程安全单例工厂
     *
     * @return
     */
    public static CloudMeter getCloudMeterSingleton() {
        if (null == cloudMeter) {
            synchronized (MeterFactory.class) {
                if (null == cloudMeter) {
                    cloudMeter = CloudFactory.createCloudMeter();
                    cloudMeter.setIntervalModel(IntervalModel.ALL);
                    cloudMeter.registerListener(new MeterListenerImpl(new TransferToMQImpl()));
                }
            }
        }
        return cloudMeter;
    }
}
