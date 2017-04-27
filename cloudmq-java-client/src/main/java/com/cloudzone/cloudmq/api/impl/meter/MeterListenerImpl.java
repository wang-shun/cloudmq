package com.cloudzone.cloudmq.api.impl.meter;

import com.cloudzone.cloudlimiter.base.AcquireStatus;
import com.cloudzone.cloudlimiter.base.MeterListener;
import com.cloudzone.cloudlimiter.meter.Meterinfo;
import com.cloudzone.cloudmq.api.impl.base.TransferAdapter;

import java.util.List;

/**
 * @author yintongjiang
 * @params
 * @since 2017/4/12
 */
public class MeterListenerImpl implements MeterListener {
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
                        if (info.getTag().equals("jcpt-client-to-cloudzone-800")) {
                            System.out.println(info);
                        }
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
