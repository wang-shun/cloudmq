package com.alibaba.rocketmq.service.gmq;

import static com.alibaba.rocketmq.util.restful.handle.ObjectHandle.getForObject;

import java.util.*;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.rocketmq.common.BrokerConst;
import com.alibaba.rocketmq.common.protocol.body.ClusterInfo;
import com.alibaba.rocketmq.common.protocol.body.KVTable;
import com.alibaba.rocketmq.common.protocol.route.BrokerData;
import com.alibaba.rocketmq.dao.BrokerDao;
import com.alibaba.rocketmq.domain.base.BaseTypeEnum;
import com.alibaba.rocketmq.domain.gmq.Broker;
import com.alibaba.rocketmq.domain.gmq.BrokerExt;
import com.alibaba.rocketmq.domain.gmq.Cluster;
import com.alibaba.rocketmq.domain.system.MemoryInfo;
import com.alibaba.rocketmq.service.AbstractService;
import com.alibaba.rocketmq.tools.admin.DefaultMQAdminExt;
import com.alibaba.rocketmq.util.date.DateUtils;
import com.alibaba.rocketmq.util.date.DateStyle;
import com.alibaba.rocketmq.util.restful.domian.AbstractEntity;
import com.alibaba.rocketmq.util.restful.handle.ObjectHandle;
import com.alibaba.rocketmq.util.restful.restTemplate.TenantIdRestOperations;
import com.google.common.collect.Maps;

/**
 * @author gaoyanlei
 * @since 2016/7/25
 */
@Service
@SuppressWarnings("unchecked")
public class GMQSysResourceService extends AbstractService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GMQSysResourceService.class);

    @Value("#{configProperties['sigar.cpu.url']}")
    private String cpuUrl;

    @Value("#{configProperties['sigar.all.url']}")
    private String allUrl;

    @Value("#{configProperties['sigar.memory.url']}")
    private String memoryUrl;

    @Value("#{configProperties['sigar.http.prefix']}")
    private String httpPrefix;

    @Value("#{configProperties['sigar.http.port']}")
    private String sigarPort;

    @Value("#{configProperties['gmq.broker.port']}")
    private String brokerPort;

    @Value("#{configProperties['gmq.broker.tps.query.maxCount']}")
    private String brokerMaxCount;

    @Autowired
    GMQClusterService clusterService;

    @Autowired
    TenantIdRestOperations restOperations;

    @Autowired
    private BrokerDao brokerDao;


    public MemoryInfo memory(String brokerAddr) throws RuntimeException {
        String url = httpPrefix + brokerAddr.trim() + sigarPort + memoryUrl;
        LOGGER.info("http memory stats url {}", url);
        AbstractEntity abstractEntity = restOperations.getForObject(url, AbstractEntity.class);
        MemoryInfo memory = getForObject(abstractEntity, MemoryInfo.class);
        return memory;
    }

    public List<String> getBrokerAddrs() throws Throwable {
        List<String> brokers = new ArrayList<>();
        List<Cluster> clusters = clusterService.list();
        if (CollectionUtils.isNotEmpty(clusters)) {
            for (Cluster cluster : clusters) {
                if (CollectionUtils.isNotEmpty((cluster.getBrokerList()))) {
                    for (Broker broker : cluster.getBrokerList()) {
                        brokers.add(broker.getAddr().substring(0, broker.getAddr().indexOf(":")));
                    }
                }
            }
        }
        return brokers;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getAllStats(String brokerAddr) {
        try {
            String url = httpPrefix + brokerAddr + sigarPort + allUrl;
            LOGGER.info("http all stats url {}", url);
            AbstractEntity abstractEntity = restOperations.getForObject(url, AbstractEntity.class);
            Map<String, Object> params = ObjectHandle.getForObject(abstractEntity, new HashMap<String, Object>().getClass());
            return params;
        } catch (Exception e) {
            LOGGER.error("remoting client all stats error. ", e);
            return Maps.newHashMap();
        }
    }


    public Map<String, Object> queryBrokers(String brokerIp) throws RuntimeException {
        Map<String, Object> tpsParam = Maps.newHashMap();
        BrokerExt brokerExt = buildBrokerWhereClause(brokerIp);
        List<BrokerExt> brokerExts = brokerDao.selectEntryList(brokerExt);

        List<Map<String, Object>> inTpsList = brokerInTps(brokerExts);
        tpsParam.put("inTps", inTpsList);

        List<Map<String, Object>> outTpsList = brokerOutTps(brokerExts);
        tpsParam.put("outTps", outTpsList);

        return tpsParam;
    }

    private List<Map<String, Object>> brokerInTps(List<BrokerExt> brokerExts) {
        List<Map<String, Object>> inTpsList = new ArrayList<>();
        Map<String, Object> params = null;
        for (BrokerExt brokerExt : brokerExts) {
            params = Maps.newHashMap();
            params.put("name", DateFormatUtils.format(brokerExt.getRuntimeDate(), DateStyle.YYYY_MM_DD_HH_MM_SS.getValue()));
            params.put("value", brokerExt.getInTps().intValue());
            inTpsList.add(params);
        }
        return inTpsList;
    }

    private List<Map<String, Object>> brokerOutTps(List<BrokerExt> brokerExts) {
        List<Map<String, Object>> outTpsList = new ArrayList<>();
        Map<String, Object> params = null;
        for (BrokerExt brokerExt : brokerExts) {
            params = Maps.newHashMap();
            params.put("name", DateFormatUtils.format(brokerExt.getRuntimeDate(), DateStyle.YYYY_MM_DD_HH_MM_SS.getValue()));
            params.put("value", brokerExt.getOutTps().intValue());
            outTpsList.add(params);
        }
        return outTpsList;
    }

    private BrokerExt buildBrokerWhereClause(String brokerIp) {
        BrokerExt brokerExt = new BrokerExt();
        brokerExt.setBrokerIp(brokerIp);
        brokerExt.setBrokerPort(brokerPort);
        brokerExt.setOrderField("RuntimeDate");
        brokerExt.setOrderFieldType(BaseTypeEnum.DESC);
        brokerExt.setLimitMin(0);
        brokerExt.setLimitMax(Integer.parseInt(brokerMaxCount));
        return brokerExt;
    }

    /**
     * 获取broker里面的inTps、outTps、msgTotal等参数
     *
     * @return
     * @throws Throwable
     */
    public List<BrokerExt> doBrokerList() throws Throwable {
        List<BrokerExt> brokers = null;
        DefaultMQAdminExt defaultMQAdminExt = getDefaultMQAdminExt();
        try {
            defaultMQAdminExt.start();
            brokers = getBrokerList(defaultMQAdminExt);
            return brokers;
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        } finally {
            shutdownDefaultMQAdminExt(defaultMQAdminExt);
        }
    }

    private List<BrokerExt> getBrokerList(DefaultMQAdminExt defaultMQAdminExt) throws Exception {
        List<BrokerExt> brokers = new ArrayList();
        ClusterInfo clusterInfoSerializeWrapper = defaultMQAdminExt.examineBrokerClusterInfo();
        Iterator<Map.Entry<String, Set<String>>> itCluster = clusterInfoSerializeWrapper.getClusterAddrTable().entrySet().iterator();
        BrokerExt broker = null;
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

    private BrokerExt setBrokerField(KVTable kvTable, Map.Entry<Long, String> brokerEntry) {
        BrokerExt broker = new BrokerExt();
        String strDate = null;
        try {
            Long brokerId = brokerEntry.getKey().longValue();
            String brokerAddr = brokerEntry.getValue();
            String putTps = kvTable.getTable().get(BrokerConst.PUT_TPS);
            String getTransferedTps = kvTable.getTable().get(BrokerConst.TRANSFERED_TPS);
            String version = kvTable.getTable().get(BrokerConst.BROKER_VERSION);

            String[] inTpsValues = putTps.split(" ");
            Double in = inTpsValues != null && inTpsValues.length > 0 ? Double.parseDouble(inTpsValues[0]) : 0D;

            String[] outTpsValues = getTransferedTps.split(" ");
            Double out = outTpsValues != null && outTpsValues.length > 0 ? Double.parseDouble(outTpsValues[0]) : 0D;

            String msgPutTotalYesterdayMorning = kvTable.getTable().get(BrokerConst.PUT_TOTAL_YESTERDAY_MORNING);
            String msgPutTotalTodayMorning = kvTable.getTable().get(BrokerConst.PUT_TOTAL_TODAY_MORNING);
            String msgPutTotalTodayNow = kvTable.getTable().get(BrokerConst.PUT_TOTAL_TODAY_NOW);
            String msgGetTotalYesterdayMorning = kvTable.getTable().get(BrokerConst.TOTAL_YESTERDAY_MORNING);
            String msgGetTotalTodayMorning = kvTable.getTable().get(BrokerConst.TOTAL_TODAY_MORNING);
            String msgGetTotalTodayNow = kvTable.getTable().get(BrokerConst.TOTAL_TODAY_NOW);

            Long InTotalYest = Long.parseLong(msgPutTotalTodayMorning) - Long.parseLong(msgPutTotalYesterdayMorning);
            Long OutTotalYest = Long.parseLong(msgGetTotalTodayMorning) - Long.parseLong(msgGetTotalYesterdayMorning);
            Long InTotalToday = Long.parseLong(msgPutTotalTodayNow) - Long.parseLong(msgPutTotalTodayMorning);
            Long OutTotalToday = Long.parseLong(msgGetTotalTodayNow) - Long.parseLong(msgGetTotalTodayMorning);

            broker.setBrokerId(brokerId);
            broker.setAddr(brokerAddr);
            broker.setVersion(version);
            broker.setBrokerIp(brokerAddr.substring(0, brokerAddr.indexOf(":")));
            broker.setBrokerPort(brokerAddr.substring(brokerAddr.indexOf(":") + 1, brokerAddr.length()));
            broker.setInTps(in);
            broker.setOutTps(out);
            broker.setInTotalYest(InTotalYest);
            broker.setOutTotalYest(OutTotalYest);
            broker.setInTotalToday(InTotalToday);
            broker.setOutTotalTodtay(OutTotalToday);
            broker.setRuntimeDate(System.currentTimeMillis());
            strDate = DateUtils.DateToString(new Date(), DateStyle.YYYY_MM_DD_HH_MM_SS_SSS);
            broker.setCreateDate(DateUtils.StringToDate(strDate, DateStyle.YYYY_MM_DD_HH_MM_SS_SSS));
            return broker;
        } catch (Exception e) {
            LOGGER.error("get broker detail error. msg={}", e.getMessage(), e);
            throw e;
        }
    }


}
