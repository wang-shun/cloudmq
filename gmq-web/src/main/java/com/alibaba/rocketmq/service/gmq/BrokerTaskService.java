package com.alibaba.rocketmq.service.gmq;

import com.alibaba.rocketmq.dao.BrokerDao;
import com.alibaba.rocketmq.domain.gmq.BrokerExt;
import com.alibaba.rocketmq.service.AbstractService;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 定时任务： 处理Broker TPS等参数入库
 *
 * @author: tianyuliang
 * @since: 2016/8/1
 */
@Service
public class BrokerTaskService extends AbstractService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BrokerTaskService.class);

    @Value("#{configProperties['gmq.broker.schedule.enable']}")
    private String brokerScheduleEnable;

    @Autowired
    private BrokerDao brokerDao;

    @Autowired
    private GMQSysResourceService gmqSysResourceService;

    private Integer saveBroker(BrokerExt broker) throws Exception {
        return brokerDao.insertEntry(broker);
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
            /*LOGGER.info("### start broker schedule ###");
            List<BrokerExt> brokers = getBatchBrokers();
            if (CollectionUtils.isEmpty(brokers)) {
                LOGGER.info("no brokers data to save.");
                return;
            }
            for (BrokerExt broker : brokers) {
                this.saveBroker(broker);
            }
            LOGGER.info("### end broker schedule ### batch count={}", brokers.size());*/
        } catch (Exception e) {
            LOGGER.error("### error broker schedule ### msg={}", e.getMessage(), e);
            // throw e; // ignore e;
        }
    }

}
