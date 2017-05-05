package com.cloudzone.cloudmq.api.impl.transfer;

import com.alibaba.fastjson.JSON;
import com.cloudzone.cloudlimiter.meter.Meterinfo;
import com.cloudzone.cloudmq.api.impl.base.TransferAdapter;
import com.cloudzone.cloudmq.api.impl.meter.MeterTopicExt;
import com.cloudzone.cloudmq.api.impl.producer.ProducerFactory;
import com.cloudzone.cloudmq.api.open.base.Msg;
import com.cloudzone.cloudmq.common.StatData;
import com.cloudzone.cloudmq.common.StatDataType;
import com.cloudzone.cloudmq.common.TimeUnitType;
import com.cloudzone.cloudmq.util.UtilAll;

/**
 * @author yintongjiang
 * @params 转换消息到MQ的实现
 * @since 2017/4/13
 */
public class TransferToMQImpl implements TransferAdapter<Meterinfo> {
    @Override
    public boolean transfer(Meterinfo meterinfo) {
        try {
            MeterTopicExt meterTopicExt = (MeterTopicExt) meterinfo.getMeterTopic();
            int dataType = StatDataType.TPS.getCode();
            if (StatDataType.FLOW.getDes().equals(meterinfo.getType())) {
                dataType = StatDataType.FLOW.getCode();
            }
            StatData statData = new StatData(meterTopicExt.getTag(), meterTopicExt.getAuthKey(), UtilAll.getIp() + "#" + UtilAll.getPid(), meterinfo.getRequestNum(),
                    dataType, TimeUnitType.MINUTES.getCode(), meterTopicExt.getProcessType(), meterinfo.getNowDate());
            if (ProducerFactory.getProducerSingleton().isStarted()) {
                ProducerFactory.getProducerSingleton().send(new Msg(ProducerFactory.TOPIC, ProducerFactory.TAG, JSON.toJSONString(statData).getBytes()));
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
