package com.cloudzone.cloudmq.api.impl.meter;

import com.cloudzone.cloudlimiter.base.AcquireStatus;
import com.cloudzone.cloudlimiter.base.MeterListener;
import com.cloudzone.cloudlimiter.meter.Meterinfo;
import com.cloudzone.cloudmq.api.impl.base.TransferAdapter;
import com.cloudzone.cloudmq.api.impl.producer.ProducerFactory;
import com.cloudzone.cloudmq.log.GClientLogger;
import org.slf4j.Logger;

import java.util.List;

/**
 * @author yintongjiang
 * @params MeterListener监听实现
 * @since 2017/4/12
 */
public class MeterListenerImpl implements MeterListener {
    private static final Logger log = GClientLogger.getLog();
    private TransferAdapter<Meterinfo> transferAdapter;

    public MeterListenerImpl(TransferAdapter<Meterinfo> transferAdapter) {
        this.transferAdapter = transferAdapter;
    }

    @Override
    public AcquireStatus acquireStats(List<Meterinfo> list) {
        for (Meterinfo info : list) {
            try {
                switch (info.getTimeUnitType()) {
                    case SECONDS:
                        break;
                    case MINUTES:
//                        if (!ProducerFactory.TOPIC.equals(info.getTag())) {
//                            log.info(info.toString());
//                        }
                        transferAdapter.transfer(info);
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return AcquireStatus.ACQUIRE_SUCCESS;
    }

}
