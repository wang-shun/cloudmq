package com.alibaba.rocketmq.service;

import java.util.*;

import com.alibaba.rocketmq.domain.gmq.Broker;
import com.alibaba.rocketmq.domain.gmq.BrokerExt;
import com.alibaba.rocketmq.domain.gmq.Cluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.rocketmq.common.protocol.body.ClusterInfo;
import com.alibaba.rocketmq.common.protocol.body.KVTable;
import com.alibaba.rocketmq.common.protocol.route.BrokerData;
import com.alibaba.rocketmq.tools.admin.DefaultMQAdminExt;


/**
 * 重构
 *
 * @author gaoyanlei
 * @since 2016/7/18
 */
@Service
public class GMQClusterService extends AbstractService {

    static final Logger logger = LoggerFactory.getLogger(ClusterService.class);


    // @CmdTrace(cmdClazz = ClusterListSubCommand.class)
    public List<Cluster> list() throws Throwable {
        Throwable t = null;
        DefaultMQAdminExt defaultMQAdminExt = getDefaultMQAdminExt();
        try {
            defaultMQAdminExt.start();
            return clusterList(defaultMQAdminExt);
        }
        catch (Throwable e) {
            logger.error(e.getMessage(), e);
            t = e;
        }
        finally {
            shutdownDefaultMQAdminExt(defaultMQAdminExt);
        }
        throw t;
    }


    /**
     * 获取clusterList
     *
     * @author gaoyanlei
     * @since 2016/7/18
     */
    public List<Cluster> clusterList(DefaultMQAdminExt defaultMQAdminExt) throws Throwable {
        Throwable t = null;
        try {
            List<Cluster> clusterList = new ArrayList<Cluster>();
            ClusterInfo clusterInfoSerializeWrapper = defaultMQAdminExt.examineBrokerClusterInfo();
            Set<Map.Entry<String, Set<String>>> clusterSet =
                    clusterInfoSerializeWrapper.getClusterAddrTable().entrySet();
            Iterator<Map.Entry<String, Set<String>>> itCluster = clusterSet.iterator();
            while (itCluster.hasNext()) {
                Cluster cluster = new Cluster();
                Map.Entry<String, Set<String>> next = itCluster.next();
                cluster.setName(next.getKey());
                Set<String> brokerNameSet = new HashSet<String>();
                brokerNameSet.addAll(next.getValue());
                cluster
                    .setBrokerList(brokerList(brokerNameSet, clusterInfoSerializeWrapper, defaultMQAdminExt));
                clusterList.add(cluster);
            }
            return clusterList;
        }
        catch (Throwable e) {
            logger.error(e.getMessage(), e);
            t = e;
        }
        finally {
            shutdownDefaultMQAdminExt(defaultMQAdminExt);
        }
        throw t;
    }


    /**
     * 获取brokerList
     *
     * @author gaoyanlei
     * @since 2016/7/19
     */
    public List<Broker> brokerList(Set<String> brokerNameSet, ClusterInfo clusterInfoSerializeWrapper,
            DefaultMQAdminExt defaultMQAdminExt) throws Throwable {
        List<Broker> brokerList = new ArrayList<Broker>();
        for (String brokerName : brokerNameSet) {
            BrokerData brokerData = clusterInfoSerializeWrapper.getBrokerAddrTable().get(brokerName);
                if (brokerData != null) {
                Set<Map.Entry<Long, String>> brokerAddrSet = brokerData.getBrokerAddrs().entrySet();
                Iterator<Map.Entry<Long, String>> itAddr = brokerAddrSet.iterator();
                while (itAddr.hasNext()) {
                    Broker broker = new Broker();
                    broker.setBrokerName(brokerName);
                    Map.Entry<Long, String> addr = itAddr.next();
                    broker.setAddr(addr.getValue());
                    broker.setBrokerID(addr.getKey().longValue());
                    brokerList.add(broker(broker, defaultMQAdminExt.fetchBrokerRuntimeStats(addr.getValue())));
                }
            }
        }
        return brokerList;
    }


    /**
     * 获取每个broker
     * 
     * @author gaoyanlei
     * @since 2016/7/19
     */
    public Broker broker(Broker broker, KVTable kvTable) {
        broker.setVersion(kvTable.getTable().get("brokerVersionDesc"));
        broker.setInTps(split(kvTable.getTable().get("putTps")));
        broker.setOutTps(split(kvTable.getTable().get("getTransferedTps")));
        String msgPutTotalYesterdayMorning = kvTable.getTable().get("msgPutTotalYesterdayMorning");
        String msgPutTotalTodayMorning = kvTable.getTable().get("msgPutTotalTodayMorning");
        String msgPutTotalTodayNow = kvTable.getTable().get("msgPutTotalTodayNow");
        String msgGetTotalYesterdayMorning = kvTable.getTable().get("msgGetTotalYesterdayMorning");
        String msgGetTotalTodayMorning = kvTable.getTable().get("msgGetTotalTodayMorning");
        String msgGetTotalTodayNow = kvTable.getTable().get("msgGetTotalTodayNow");
        broker.setInTotalYest(Long.parseLong(msgPutTotalTodayMorning) - Long.parseLong(msgPutTotalYesterdayMorning));
        broker.setOutTotalYest(Long.parseLong(msgGetTotalTodayMorning) - Long.parseLong(msgGetTotalYesterdayMorning));
        broker.setInTotalToday(Long.parseLong(msgPutTotalTodayNow) - Long.parseLong(msgPutTotalTodayMorning));
        broker.setOutTotalTodtay(Long.parseLong(msgGetTotalTodayNow) - Long.parseLong(msgGetTotalTodayMorning));
        return broker;
    }


    private double split(String string) {
        String[] tpss = string.split(" ");
        if (tpss != null && tpss.length > 0) {
            return Double.parseDouble(tpss[0]);
        }
        return 0;
    }
}
