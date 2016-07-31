package com.alibaba.rocketmq.service;

import com.alibaba.rocketmq.common.BrokerConst;
import com.alibaba.rocketmq.common.protocol.body.ClusterInfo;
import com.alibaba.rocketmq.common.protocol.body.KVTable;
import com.alibaba.rocketmq.common.protocol.route.BrokerData;
import com.alibaba.rocketmq.domain.gmq.Broker;
import com.alibaba.rocketmq.domain.gmq.Cluster;
import com.alibaba.rocketmq.domain.system.MemoryInfo;
import com.alibaba.rocketmq.tools.admin.DefaultMQAdminExt;
import com.alibaba.rocketmq.util.restful.domian.AbstractEntity;
import com.alibaba.rocketmq.util.restful.handle.ObjectHandle;
import com.alibaba.rocketmq.util.restful.restTemplate.TenantIdRestOperations;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.alibaba.rocketmq.util.restful.handle.ObjectHandle.getForObject;

/**
 * @author gaoyanlei
 * @since 2016/7/25
 */
@Service
public class GMQSysResourceService extends AbstractService {

    private static final Logger logger = LoggerFactory.getLogger(GMQSysResourceService.class);

    @Value("#{configProperties['sigar.cpu.url']}")
    private String cpuUrl;

    @Value("#{configProperties['sigar.all.url']}")
    private String allUrl;

    @Value("#{configProperties['sigar.memory.url']}")
    private String memoryUrl;

    @Value("#{configProperties['sigar.http.prefix']}")
    private String httpPrefix;

    @Value("#{configProperties['sigar.http.port']}")
    private String port;

    @Autowired
    GMQClusterService clusterService;

    @Autowired
    TenantIdRestOperations restOperations;


    public MemoryInfo memory(String brokerAddr) throws RuntimeException {
        String url = httpPrefix + brokerAddr.trim() + port + memoryUrl;
        logger.info("http memory stats url {}", url);
        AbstractEntity abstractEntity = restOperations.getForObject(url, AbstractEntity.class);
        MemoryInfo memory = getForObject(abstractEntity, MemoryInfo.class);
        return memory;
    }

    public List<String> getBrokerAddrs() throws Throwable {
        List<String> brokers = new ArrayList<>();
        List<Cluster> clusters = clusterService.list();
        if (clusters != null && clusters.size() > 0) {
            for (Cluster cluster : clusters) {
                if (CollectionUtils.isNotEmpty((cluster.getBrokerList())) && cluster.getBrokerList().size() > 0) {
                    for (Broker broker : cluster.getBrokerList()) {
                        brokers.add(broker.getAddr().substring(0, broker.getAddr().indexOf(":")));
                    }
                }
            }
        }
        Collections.sort(brokers);
        return brokers;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getAllStats(String brokerAddr) {
        try {
            String url = httpPrefix + brokerAddr + port + allUrl;
            logger.info("http all stats url {}", url);
            AbstractEntity abstractEntity = restOperations.getForObject(url, AbstractEntity.class);
            Map<String, Object> params = ObjectHandle.getForObject(abstractEntity, new HashMap<String, Object>().getClass());
            return params;
        } catch (Exception e) {
            logger.error("remoting client all stats error. ", e);
            return Maps.newHashMap();
        }
    }


    /**
     * 获取brokerList
     *
     * @return
     * @throws Throwable
     */
    public List<Broker> doBrokerList() throws Throwable {
        List<Broker> brokers = null;
        DefaultMQAdminExt defaultMQAdminExt = getDefaultMQAdminExt();
        try {
            defaultMQAdminExt.start();
            brokers = getBrokerList(defaultMQAdminExt);
            return brokers;
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            throw e;
        } finally {
            shutdownDefaultMQAdminExt(defaultMQAdminExt);
        }
    }

    public List<Broker> getBrokerList(DefaultMQAdminExt defaultMQAdminExt) throws Exception {
        List<Broker> brokers = new ArrayList();
        ClusterInfo clusterInfoSerializeWrapper = defaultMQAdminExt.examineBrokerClusterInfo();
        Iterator<Map.Entry<String, Set<String>>> itCluster = clusterInfoSerializeWrapper.getClusterAddrTable().entrySet().iterator();
        Broker broker = null;
        KVTable kvTable = null;
        while (itCluster.hasNext()) {
            Map.Entry<String, Set<String>> clusterEntry = itCluster.next();
            String clusterName = clusterEntry.getKey();
            Set<String> brokerNameSet = new HashSet<String>();
            brokerNameSet.addAll(clusterEntry.getValue());
            for (String brokerName : brokerNameSet) {
                BrokerData brokerData = clusterInfoSerializeWrapper.getBrokerAddrTable().get(brokerName);
                if (brokerData == null) {
                    continue;
                }
                Iterator<Map.Entry<Long, String>> itAddr = brokerData.getBrokerAddrs().entrySet().iterator();
                while (itAddr.hasNext()) {
                    Map.Entry<Long, String> brokerEntry = itAddr.next();
                    kvTable = defaultMQAdminExt.fetchBrokerRuntimeStats(brokerEntry.getValue());
                    broker = setBrokerField(kvTable, brokerEntry);
                    broker.setClusterName(clusterName);
                    broker.setBrokerName(brokerName);
                    brokers.add(broker);
                }
            }
        }
        return brokers;
    }

    private Broker setBrokerField(KVTable kvTable, Map.Entry<Long, String> brokerEntry) {
        Broker broker = new Broker();
        try {
            long brokerId = brokerEntry.getKey().longValue();
            String brokerAddr = brokerEntry.getValue();
            String putTps = kvTable.getTable().get(BrokerConst.PUT_TPS);
            String getTransferedTps = kvTable.getTable().get(BrokerConst.TRANSFERED_TPS);
            String version = kvTable.getTable().get(BrokerConst.BROKER_VERSION);

            String[] inTpsValues = putTps.split(" ");
            double in = inTpsValues != null && inTpsValues.length > 0 ? Double.parseDouble(inTpsValues[0]) : 0D;

            String[] outTpsValues = getTransferedTps.split(" ");
            double out = outTpsValues != null && outTpsValues.length > 0 ? Double.parseDouble(outTpsValues[0]) : 0D;

            String msgPutTotalYesterdayMorning = kvTable.getTable().get(BrokerConst.PUT_TOTAL_YESTERDAY_MORNING);
            String msgPutTotalTodayMorning = kvTable.getTable().get(BrokerConst.PUT_TOTAL_TODAY_MORNING);
            String msgPutTotalTodayNow = kvTable.getTable().get(BrokerConst.PUT_TOTAL_TODAY_NOW);
            String msgGetTotalYesterdayMorning = kvTable.getTable().get(BrokerConst.TOTAL_YESTERDAY_MORNING);
            String msgGetTotalTodayMorning = kvTable.getTable().get(BrokerConst.TOTAL_TODAY_MORNING);
            String msgGetTotalTodayNow = kvTable.getTable().get(BrokerConst.TOTAL_TODAY_NOW);

            long InTotalYest = Long.parseLong(msgPutTotalTodayMorning) - Long.parseLong(msgPutTotalYesterdayMorning);
            long OutTotalYest = Long.parseLong(msgGetTotalTodayMorning) - Long.parseLong(msgGetTotalYesterdayMorning);
            long InTotalToday = Long.parseLong(msgPutTotalTodayNow) - Long.parseLong(msgPutTotalTodayMorning);
            long OutTotalToday = Long.parseLong(msgGetTotalTodayNow) - Long.parseLong(msgGetTotalTodayMorning);

            broker.setBrokerID(brokerId);
            broker.setAddr(brokerAddr);
            broker.setVersion(version);
            broker.setInTPS(in);
            broker.setOutTPS(out);
            broker.setInTotalYest(InTotalYest);
            broker.setOutTotalYest(OutTotalYest);
            broker.setInTotalToday(InTotalToday);
            broker.setOutTotalTodtay(OutTotalToday);
            return broker;
        } catch (Exception e) {
            logger.error("get broker detail error. msg={}", e.getMessage(), e);
            throw e;
        }
    }

}
