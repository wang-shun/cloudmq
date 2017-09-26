package com.alibaba.rocketmq.service.gmq;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.rocketmq.domain.gmq.BrokerExt;
import com.alibaba.rocketmq.service.AbstractService;

/**
 * 定时任务： 处理Broker TPS等参数入库
 *
 * @author: tianyuliang
 * @since: 2016/8/1
 */
@Service
public class BrokerTaskService extends AbstractService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BrokerTaskService.class);

    /**
     * 是否开启Broker定时查询集群TPS任务 (从配置文件获取开关)
     */
    private Boolean brokerScheduleEnable;


    @Autowired
    private GMQSysResourceService gmqSysResourceService;

    private Integer saveBroker(BrokerExt broker) throws Exception {
       // return brokerDao.insertEntry(broker);
        return 0;
    }

    private List<BrokerExt> getBatchBrokers() throws RuntimeException {
        try {
            return gmqSysResourceService.doBrokerList();
        } catch (Throwable e) {
            LOGGER.error("save simple broker data error.", e);
            throw new RuntimeException(e);
        }
    }

    public void batchSaveBroker() {
        try {
            if(brokerScheduleEnable == null || !brokerScheduleEnable){
                return;  //屏蔽此处的log日志
            }
            LOGGER.info("### start broker schedule ###");
            List<BrokerExt> brokers = getBatchBrokers();
            if (CollectionUtils.isEmpty(brokers)) {
                LOGGER.info("no brokers data to save.");
                return;
            }
            for (BrokerExt broker : brokers) {
                this.saveBroker(broker);
            }
            LOGGER.info("### end broker schedule ### batch count={}", brokers.size());
        } catch (Exception e) {
            LOGGER.error("### error broker schedule ### msg={}", e.getMessage(), e);
            // throw e; // ignore e;
        }
    }

    public Boolean getBrokerScheduleEnable() {
        return brokerScheduleEnable;
    }

    public void setBrokerScheduleEnable(Boolean brokerScheduleEnable) {
        this.brokerScheduleEnable = brokerScheduleEnable;
    }

}
