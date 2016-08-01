package com.alibaba.rocketmq.service.gmq;


import com.alibaba.rocketmq.common.ThreadFactoryImpl;
import com.alibaba.rocketmq.dao.BrokerDao;
import com.alibaba.rocketmq.domain.gmq.Broker;
import com.alibaba.rocketmq.service.AbstractService;
import com.alibaba.rocketmq.service.GMQSysResourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GMQBrokerService extends AbstractService {

    private static final Logger logger = LoggerFactory.getLogger(GMQBrokerService.class);
    @Autowired
    private BrokerDao brokerDao;
    @Autowired
    private GMQSysResourceService gmqSysResourceService;
    private final ScheduledExecutorService scheduledExecutorService =
            Executors.newSingleThreadScheduledExecutor(new ThreadFactoryImpl(
                    "BrokerServiceScheduledThread_"));
    @Value("#{configProperties['broker.schedule.initialDelay']}")
    private Long initialDelay;
    @Value("#{configProperties['broker.schedule.period']}")
    private Long period;

    public void init() {
        try {
            this.scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    try {
                        logger.info("### start broker schedule ###");
                        batchSave(gmqSysResourceService.doBrokerList());
                        logger.info("### end broker schedule ###");
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                        logger.error("### error broker schedule,error{} ###", throwable);
                    }
                }
            }, initialDelay, period, TimeUnit.MILLISECONDS);

        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public void shutdown() {
        this.scheduledExecutorService.shutdown();
    }

    public boolean save(Broker broker) {
        return brokerDao.insertEntry(broker) > 0;
    }

    public void batchSave(List<Broker> brokerList) {
        for (Broker broker : brokerList) {
            this.save(broker);
        }
    }

}
