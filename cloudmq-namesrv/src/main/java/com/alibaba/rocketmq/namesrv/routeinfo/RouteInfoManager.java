/**
 * Copyright (C) 2010-2013 Alibaba Group Holding Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.rocketmq.namesrv.routeinfo;

import io.netty.channel.Channel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.rocketmq.common.DataVersion;
import com.alibaba.rocketmq.common.MixAll;
import com.alibaba.rocketmq.common.TopicConfig;
import com.alibaba.rocketmq.common.constant.LoggerName;
import com.alibaba.rocketmq.common.constant.PermName;
import com.alibaba.rocketmq.common.namesrv.RegisterBrokerResult;
import com.alibaba.rocketmq.common.protocol.body.ClusterInfo;
import com.alibaba.rocketmq.common.protocol.body.TopicConfigSerializeWrapper;
import com.alibaba.rocketmq.common.protocol.body.TopicList;
import com.alibaba.rocketmq.common.protocol.route.BrokerData;
import com.alibaba.rocketmq.common.protocol.route.QueueData;
import com.alibaba.rocketmq.common.protocol.route.TopicRouteData;
import com.alibaba.rocketmq.common.sysflag.TopicSysFlag;
import com.alibaba.rocketmq.remoting.common.RemotingUtil;


/**
 * 一、运行过程中的路由信息，数据只在内存，宕机后数据消失，但是Broker会定期推送最新数据
 * (1)维护了所有topic和所有机器的映射信息
 * (2)是topic的路由结构,也是整个RocketMQ最核心的结构
 *
 *
 * 二、topic/queue和Broker的映射关系
 * (1)1个topic多个broker（1个Master + 多个slave)，每个broker上面都有writeQueueNums个queue
 *      // http://img.blog.csdn.net/20170112140729110?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvY2h1bmxvbmd5dQ==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast
 * (2)topic的queue个数，加总就是writeQueueNums * master个数
 * (3)queueId其实是局部的，对于同1个topic，每个Master上面的queueId编号都是从0开始的
 * (4)一个topic有多个QueueData，每个QueueData有一个brokerName变量, 也就是每1个QueueData对象，对应1个Master机器
 *
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 * @since 2013-7-2
 */
public class RouteInfoManager {
    private static final Logger log = LoggerFactory.getLogger(LoggerName.NamesrvLoggerName);

    /**
     * 读写锁
     */
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     *  核心Map之1: topic到QueueData,维护topic.json文件的topicConfigTable的数据
     * (1)一个topic有多个QueueData，每个QueueData有一个brokerName变量, 也就是每1个QueueData对象，对应1个Master机器
     * (2)topicQueueTable、brokerAddrTable 两种数据结构通过brokerName关联
     * (3)静态参数，是在配置整个集群的时候，从配置文件中读取并确定的
     */
    private final HashMap<String/* topic */, List<QueueData>> topicQueueTable;

    /**
     * 核心Map之2: broker名称、broker属性 对应关系hash表(物理机器信息，brokerName到Master/Slave机器列表)
     * (1)一组borker = 1个Master + 多个Slave
     * (2)静态参数，是在配置整个集群的时候，从配置文件中读取并确定的
     */
    private final HashMap<String/* brokerName */, BrokerData> brokerAddrTable;

    /**
     * 核心Map之3: cluster集群、broker集合 对应关系hash表
     * (1)一个cluster = 多组broker。其中cluster缺省就1个，defaultCluster
     * (2)动态参数，调用CreateTopic()的时候，创建的clusterAddrTable
     */
    private final HashMap<String/* clusterName */, Set<String/* brokerName */>> clusterAddrTable;

    /**
     * 核心Map之4: 活动broker基础信息 对应关系hash表
     * (1)NameServer收到RegisterBroker信息，更新自己的brokerLiveTable结构
     */
    private final HashMap<String/* brokerAddr */, BrokerLiveInfo> brokerLiveTable;

    /**
     * 核心Map之5: Broker地址、过滤器 对应关系hash表
     */
    private final HashMap<String/* brokerAddr */, List<String>/* Filter Server */> filterServerTable;


    public RouteInfoManager() {
        this.topicQueueTable = new HashMap<String, List<QueueData>>(1024);
        this.brokerAddrTable = new HashMap<String, BrokerData>(128);
        this.clusterAddrTable = new HashMap<String, Set<String>>(32);
        this.brokerLiveTable = new HashMap<String, BrokerLiveInfo>(256);
        this.filterServerTable = new HashMap<String, List<String>>(256);
    }


    public byte[] getAllClusterInfo() {
        ClusterInfo clusterInfoSerializeWrapper = new ClusterInfo();
        clusterInfoSerializeWrapper.setBrokerAddrTable(this.brokerAddrTable);
        clusterInfoSerializeWrapper.setClusterAddrTable(this.clusterAddrTable);
        return clusterInfoSerializeWrapper.encode();
    }

    /**
     * 删除Topic
     * @param topic
     */
    public void deleteTopic(final String topic) {
        try {
            try {
                this.lock.writeLock().lockInterruptibly();
                this.topicQueueTable.remove(topic);
            }
            finally {
                this.lock.writeLock().unlock();
            }
        }
        catch (Exception e) {
            log.error("deleteTopic Exception", e);
        }
    }

    /**
     * 获取所有Topic列表
     * @return
     */
    public byte[] getAllTopicList() {
        TopicList topicList = new TopicList();
        try {
            try {
                this.lock.readLock().lockInterruptibly();
                topicList.getTopicList().addAll(this.topicQueueTable.keySet());
            }
            finally {
                this.lock.readLock().unlock();
            }
        }
        catch (Exception e) {
            log.error("getAllTopicList Exception", e);
        }

        return topicList.encode();
    }


    /**
     * 如果收到REGISTER_BROKER请求，那么最终会调用到RouteInfoManager.registerBroker()
     * 注册完成后，返回给Broker端主用Broker的地址和主用Broker的HA服务地址
     *
     * @return 如果是slave，则返回master的ha地址
     */
    public RegisterBrokerResult registerBroker(//
            final String clusterName,// 1
            final String brokerAddr,// 2
            final String brokerName,// 3
            final long brokerId,// 4
            final String haServerAddr,// 5
            final TopicConfigSerializeWrapper topicConfigWrapper,// 6
            final List<String> filterServerList, // 7
            final Channel channel// 8
    ) {
        RegisterBrokerResult result = new RegisterBrokerResult();
        try {
            try {
                this.lock.writeLock().lockInterruptibly();

                /**
                 * 更新集群信息，维护RouteInfoManager.clusterAddrTable变量
                 * (1)若Broker集群名字不在该Map变量中，则初始化一个Set集合,并将brokerName存入该Set集合中
                 * (2)然后以clusterName为key 值，该Set集合为values值存入此Map变量中
                 */
                Set<String> brokerNames = this.clusterAddrTable.get(clusterName);
                if (null == brokerNames) {
                    brokerNames = new HashSet<String>();
                    this.clusterAddrTable.put(clusterName, brokerNames);
                }
                brokerNames.add(brokerName);

                boolean registerFirst = false;

                /**
                 *  更新主备信息, 维护RouteInfoManager.brokerAddrTable变量,该变量是维护BrokerAddr、BrokerId、BrokerName等信息
                 * (1)若该brokerName不在该Map变量中，则创建BrokerData对象，该对象包含了brokerName，以及brokerId和brokerAddr为K-V的brokerAddrs变量
                 * (2)然后以 brokerName 为key值将BrokerData对象存入该brokerAddrTable变量中
                 * (3)说明同一个BrokerName下面可以有多个不同BrokerId 的Broker存在，表示一个BrokerName有多个Broker存在，通过BrokerId来区分主备
                 */
                BrokerData brokerData = this.brokerAddrTable.get(brokerName);
                if (null == brokerData) {
                    registerFirst = true;
                    brokerData = new BrokerData();
                    brokerData.setBrokerName(brokerName);
                    HashMap<Long, String> brokerAddrs = new HashMap<Long, String>();
                    brokerData.setBrokerAddrs(brokerAddrs);

                    this.brokerAddrTable.put(brokerName, brokerData);
                }
                String oldAddr = brokerData.getBrokerAddrs().put(brokerId, brokerAddr);
                registerFirst = registerFirst || (null == oldAddr);

                // 更新Topic信息: 若Broker的注册请求消息中topic的配置不为空，并且该Broker是主(即brokerId=0)
                if (null != topicConfigWrapper && MixAll.MASTER_ID == brokerId) {

                    // 则根据NameServer存储的Broker版本信息来判断是否需要更新NameServer端的topic配置信息
                    if (this.isBrokerTopicConfigChanged(brokerAddr, topicConfigWrapper.getDataVersion()) || registerFirst) {
                        ConcurrentHashMap<String, TopicConfig> tcTable = topicConfigWrapper.getTopicConfigTable();
                        if (tcTable != null) {
                            for (String topic : tcTable.keySet()) {
                                TopicConfig topicConfig = tcTable.get(topic);
                                this.createAndUpdateQueueData(brokerName, topicConfig);
                            }
                        }
                    }
                }

                // 更新最后变更时间: 初始化BrokerLiveInfo对象并以broker地址为key值存入brokerLiveTable变量中
                BrokerLiveInfo prevBrokerLiveInfo = this.brokerLiveTable.put(brokerAddr, //
                    new BrokerLiveInfo(//
                        System.currentTimeMillis(), //
                        topicConfigWrapper.getDataVersion(),//
                        channel, //
                        haServerAddr));
                if (null == prevBrokerLiveInfo) {
                    log.info("new broker registerd, {} HAServer: {}", brokerAddr, haServerAddr);
                }

                // 更新Filter Server列表: 对于filterServerList不为空的,以broker地址为key值存入
                if (filterServerList != null) {
                    if (filterServerList.isEmpty()) {
                        this.filterServerTable.remove(brokerAddr);
                    }
                    else {
                        this.filterServerTable.put(brokerAddr, filterServerList);
                    }
                }

                // 返回值：找到该BrokerName下面的主节点
                if (MixAll.MASTER_ID != brokerId) {
                    String masterAddr = brokerData.getBrokerAddrs().get(MixAll.MASTER_ID);
                    if (masterAddr != null) {
                        //Broker主节点地址: 从brokerLiveTable中获取BrokerLiveInfo对象，取该对象的HaServerAddr值
                        BrokerLiveInfo brokerLiveInfo = this.brokerLiveTable.get(masterAddr);
                        if (brokerLiveInfo != null) {
                            result.setHaServerAddr(brokerLiveInfo.getHaServerAddr());
                            result.setMasterAddr(masterAddr);
                        }
                    }
                }
            }
            finally {
                this.lock.writeLock().unlock();
            }
        }
        catch (Exception e) {
            log.error("registerBroker Exception", e);
        }

        return result;
    }


    /**
     * 判断Topic配置信息是否发生变更
     */
    private boolean isBrokerTopicConfigChanged(final String brokerAddr, final DataVersion dataVersion) {
        BrokerLiveInfo prev = this.brokerLiveTable.get(brokerAddr);
        if (null == prev || !prev.getDataVersion().equals(dataVersion)) {
            return true;
        }

        return false;
    }

    /**
     * 加锁处理：优雅更新Broker写操作
     * @param brokerName
     * @return
     */
    public int wipeWritePermOfBrokerByLock(final String brokerName) {
        try {
            try {
                this.lock.writeLock().lockInterruptibly();
                return wipeWritePermOfBroker(brokerName);
            }
            finally {
                this.lock.writeLock().unlock();
            }
        }
        catch (Exception e) {
            log.error("wipeWritePermOfBrokerByLock Exception", e);
        }

        return 0;
    }

    /**
     * 优雅更新Broker写操作
     * @param brokerName broker名称
     * @return 对应Broker上待处理的Topic个数
     */
    private int wipeWritePermOfBroker(final String brokerName) {
        int wipeTopicCnt = 0;
        Iterator<Entry<String, List<QueueData>>> itTopic = this.topicQueueTable.entrySet().iterator();
        while (itTopic.hasNext()) {
            Entry<String, List<QueueData>> entry = itTopic.next();
            List<QueueData> qdList = entry.getValue();

            Iterator<QueueData> it = qdList.iterator();
            while (it.hasNext()) {
                QueueData qd = it.next();
                if (qd.getBrokerName().equals(brokerName)) {
                    int perm = qd.getPerm();
                    perm &= ~PermName.PERM_WRITE;
                    qd.setPerm(perm);
                    wipeTopicCnt++;
                }
            }
        }

        return wipeTopicCnt;
    }


    /**
     * (1)每来一个Master，创建一个QueueData对象
     * (2)如果是新建topic，就是添加QueueData对象
     * (3)如果是修改topic，就是把旧的QueueData删除，加入新的
     *
     * 例如：
     * A. 假设对于1个topic，有3个Master
     * B. NameSrv也就收到3个RegisterBroker请求
     * C. 相应的该topic对应的QueueDataList里面，也就3个QueueData对象
     *
     * @param brokerName
     * @param topicConfig
     */
    private void createAndUpdateQueueData(final String brokerName, final TopicConfig topicConfig) {
        QueueData queueData = new QueueData();
        queueData.setBrokerName(brokerName);
        queueData.setWriteQueueNums(topicConfig.getWriteQueueNums());
        queueData.setReadQueueNums(topicConfig.getReadQueueNums());
        queueData.setPerm(topicConfig.getPerm());
        queueData.setTopicSynFlag(topicConfig.getTopicSysFlag());

        List<QueueData> queueDataList = this.topicQueueTable.get(topicConfig.getTopicName());
        if (null == queueDataList) {
            queueDataList = new LinkedList<QueueData>();
            queueDataList.add(queueData);
            this.topicQueueTable.put(topicConfig.getTopicName(), queueDataList);
            log.info("new topic registerd, {} {}", topicConfig.getTopicName(), queueData);
        }
        else {
            boolean addNewOne = true;

            Iterator<QueueData> it = queueDataList.iterator();
            while (it.hasNext()) {
                QueueData qd = it.next();
                if (qd.getBrokerName().equals(brokerName)) {
                    if (qd.equals(queueData)) {
                        addNewOne = false;
                    }
                    else {
                        log.info("topic changed, {} OLD: {} NEW: {}", topicConfig.getTopicName(), qd,
                            queueData);
                        it.remove();
                    }
                }
            }

            if (addNewOne) {
                queueDataList.add(queueData);
            }
        }
    }


    /**
     * 关闭Broker
     * @param clusterName 集群名称
     * @param brokerAddr broker地址
     * @param brokerName broker名称
     * @param brokerId brokerId
     */
    public void unregisterBroker(final String clusterName, final String brokerAddr, final String brokerName, final long brokerId) {
        try {
            try {
                this.lock.writeLock().lockInterruptibly();
                BrokerLiveInfo brokerLiveInfo = this.brokerLiveTable.remove(brokerAddr);
                if (brokerLiveInfo != null) {
                    String removeOK = brokerLiveInfo != null ? "OK" : "Failed";
                    log.info("unregisterBroker, remove from brokerLiveTable {}, {}", removeOK, brokerAddr
                    );
                }

                this.filterServerTable.remove(brokerAddr);

                boolean removeBrokerName = false;
                BrokerData brokerData = this.brokerAddrTable.get(brokerName);
                if (null != brokerData) {
                    String addr = brokerData.getBrokerAddrs().remove(brokerId);
                    log.info("unregisterBroker, remove addr from brokerAddrTable {}, {}", //
                        (addr != null ? "OK" : "Failed"),//
                        brokerAddr//
                    );

                    if (brokerData.getBrokerAddrs().isEmpty()) {
                        this.brokerAddrTable.remove(brokerName);
                        log.info("unregisterBroker, remove name from brokerAddrTable OK, {}", //
                            brokerName//
                        );

                        removeBrokerName = true;
                    }
                }

                if (removeBrokerName) {
                    Set<String> nameSet = this.clusterAddrTable.get(clusterName);
                    if (nameSet != null) {
                        boolean removed = nameSet.remove(brokerName);
                        log.info("unregisterBroker, remove name from clusterAddrTable {}, {}", //
                            (removed ? "OK" : "Failed"),//
                            brokerName//
                        );

                        if (nameSet.isEmpty()) {
                            this.clusterAddrTable.remove(clusterName);
                            log.info("unregisterBroker, remove cluster from clusterAddrTable {}", //
                                clusterName//
                            );
                        }
                    }

                    // 删除相应的topic
                    this.removeTopicByBrokerName(brokerName);
                }
            }
            finally {
                this.lock.writeLock().unlock();
            }
        }
        catch (Exception e) {
            log.error("unregisterBroker Exception", e);
        }
    }


    /**
     * 根据brokerName移除它对应的Topic数据
     * @param brokerName
     */
    private void removeTopicByBrokerName(final String brokerName) {
        Iterator<Entry<String, List<QueueData>>> itMap = this.topicQueueTable.entrySet().iterator();
        while (itMap.hasNext()) {
            Entry<String, List<QueueData>> entry = itMap.next();

            String topic = entry.getKey();
            List<QueueData> queueDataList = entry.getValue();
            Iterator<QueueData> it = queueDataList.iterator();
            while (it.hasNext()) {
                QueueData qd = it.next();
                if (qd.getBrokerName().equals(brokerName)) {
                    log.info("removeTopicByBrokerName, remove one broker's topic {} {}", topic, qd);
                    it.remove();
                }
            }

            if (queueDataList.isEmpty()) {
                log.info("removeTopicByBrokerName, remove the topic all queue {}", topic);
                itMap.remove();
            }
        }
    }


    /**
     * 根据Topic收集路由数据
     * @param topic
     * @return
     */
    public TopicRouteData pickupTopicRouteData(final String topic) {
        TopicRouteData topicRouteData = new TopicRouteData();
        boolean foundQueueData = false;
        boolean foundBrokerData = false;
        Set<String> brokerNameSet = new HashSet<String>();
        List<BrokerData> brokerDataList = new LinkedList<BrokerData>();
        topicRouteData.setBrokerDatas(brokerDataList);

        HashMap<String, List<String>> filterServerMap = new HashMap<String, List<String>>();
        topicRouteData.setFilterServerTable(filterServerMap);

        try {
            try {
                this.lock.readLock().lockInterruptibly();
                List<QueueData> queueDataList = this.topicQueueTable.get(topic);
                if (queueDataList != null) {
                    topicRouteData.setQueueDatas(queueDataList);
                    foundQueueData = true;

                    // BrokerName去重
                    Iterator<QueueData> it = queueDataList.iterator();
                    while (it.hasNext()) {
                        QueueData qd = it.next();
                        brokerNameSet.add(qd.getBrokerName());
                    }

                    for (String brokerName : brokerNameSet) {
                        BrokerData brokerData = this.brokerAddrTable.get(brokerName);
                        if (null != brokerData) {
                            BrokerData brokerDataClone = new BrokerData();
                            brokerDataClone.setBrokerName(brokerData.getBrokerName());
                            brokerDataClone.setBrokerAddrs((HashMap<Long, String>) brokerData
                                .getBrokerAddrs().clone());
                            brokerDataList.add(brokerDataClone);
                            foundBrokerData = true;

                            // 增加Filter Server
                            for (final String brokerAddr : brokerDataClone.getBrokerAddrs().values()) {
                                List<String> filterServerList = this.filterServerTable.get(brokerAddr);
                                filterServerMap.put(brokerAddr, filterServerList);
                            }
                        }
                    }
                }
            }
            finally {
                this.lock.readLock().unlock();
            }
        }
        catch (Exception e) {
            log.error("pickupTopicRouteData Exception", e);
        }

        if (log.isDebugEnabled()) {
            log.debug("pickupTopicRouteData {} {}", topic, topicRouteData);
        }

        if (foundBrokerData && foundQueueData) {
            return topicRouteData;
        }

        return null;
    }

    // Broker Channel两分钟过期
    private final static long BrokerChannelExpiredTime = 1000 * 60 * 2;

    /**
     * 清除掉2分钟接受不到心跳的broker列表
     * (1)NameServer会每10s，扫描一次这个brokerLiveTable变量，
     * (2)如果发现上次更新时间距离当前时间超过了2分钟，则认为此broker死亡
     */
    public void scanNotActiveBroker() {
        Iterator<Entry<String, BrokerLiveInfo>> it = this.brokerLiveTable.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, BrokerLiveInfo> next = it.next();
            String remoteAddr = next.getKey();
            BrokerLiveInfo brokerLiveInfo = next.getValue();
            long last = brokerLiveInfo.getLastUpdateTimestamp();
            if ((last + BrokerChannelExpiredTime) < System.currentTimeMillis()) {
                RemotingUtil.closeChannel(brokerLiveInfo.getChannel());
                it.remove();
                log.warn("The broker channel expired, {} {}ms", remoteAddr, BrokerChannelExpiredTime);
                this.onChannelDestroy(remoteAddr, brokerLiveInfo.getChannel());
            }
        }
    }


    /**
     * Channel被关闭、Channel出现异常、Channe的Idle时间超时
     */
    public void onChannelDestroy(String remoteAddr, Channel channel) {
        String brokerAddrFound = null;

        // 加读锁，寻找断开连接的Broker
        if (channel != null) {
            try {
                try {
                    this.lock.readLock().lockInterruptibly();
                    Iterator<Entry<String, BrokerLiveInfo>> itBrokerLiveTable = this.brokerLiveTable.entrySet().iterator();
                    while (itBrokerLiveTable.hasNext()) {
                        Entry<String, BrokerLiveInfo> entry = itBrokerLiveTable.next();
                        if (entry.getValue().getChannel() == channel) {
                            brokerAddrFound = entry.getKey();
                            break;
                        }
                    }
                }
                finally {
                    this.lock.readLock().unlock();
                }
            }
            catch (Exception e) {
                log.error("onChannelDestroy Exception", e);
            }
        }

        if (null == brokerAddrFound) {
            brokerAddrFound = remoteAddr;
        }
        else {
            log.info("the broker's channel destroyed, {}, clean it's data structure at once", brokerAddrFound);
        }

        // 加写锁，删除相关数据结构
        if (brokerAddrFound != null && brokerAddrFound.length() > 0) {
            try {
                try {
                    this.lock.writeLock().lockInterruptibly();
                    // 1 清理brokerLiveTable
                    this.brokerLiveTable.remove(brokerAddrFound);

                    // 2 清理Filter Server
                    this.filterServerTable.remove(brokerAddrFound);

                    // 3 清理brokerAddrTable
                    String brokerNameFound = null;
                    boolean removeBrokerName = false;
                    Iterator<Entry<String, BrokerData>> itBrokerAddrTable = this.brokerAddrTable.entrySet().iterator();
                    while (itBrokerAddrTable.hasNext() && (null == brokerNameFound)) {
                        BrokerData brokerData = itBrokerAddrTable.next().getValue();

                        // 3.1 遍历Master/Slave，删除brokerAddr
                        Iterator<Entry<Long, String>> it = brokerData.getBrokerAddrs().entrySet().iterator();
                        while (it.hasNext()) {
                            Entry<Long, String> entry = it.next();
                            Long brokerId = entry.getKey();
                            String brokerAddr = entry.getValue();
                            if (brokerAddr.equals(brokerAddrFound)) {
                                brokerNameFound = brokerData.getBrokerName();
                                it.remove();
                                String removeMsg = "remove brokerAddr[{}, {}] from brokerAddrTable, because channel destroyed";
                                log.info(removeMsg, brokerId, brokerAddr);
                                break;
                            }
                        }

                        // 3.2 BrokerName无关联BrokerAddr
                        if (brokerData.getBrokerAddrs().isEmpty()) {
                            removeBrokerName = true;
                            itBrokerAddrTable.remove();
                            String removeMsg = "remove brokerName[{}] from brokerAddrTable, because channel destroyed";
                            log.info(removeMsg, brokerData.getBrokerName());
                        }
                    }

                    // 4 清理clusterAddrTable
                    if (brokerNameFound != null && removeBrokerName) {
                        Iterator<Entry<String, Set<String>>> it = this.clusterAddrTable.entrySet().iterator();
                        while (it.hasNext()) {
                            Entry<String, Set<String>> entry = it.next();
                            String clusterName = entry.getKey();
                            Set<String> brokerNames = entry.getValue();
                            boolean removed = brokerNames.remove(brokerNameFound);
                            if (removed) {
                                String removeMsg = "remove brokerName[{}], clusterName[{}] from clusterAddrTable, because channel destroyed";
                                log.info(removeMsg, brokerNameFound, clusterName);

                                // 如果集群对应的所有broker都下线了， 则集群也删除掉
                                if (brokerNames.isEmpty()) {
                                    String msgEmpty = "remove the clusterName[{}] from clusterAddrTable, because channel destroyed and no broker in this cluster";
                                    log.info(msgEmpty, clusterName);
                                    it.remove();
                                }

                                break;
                            }
                        }
                    }

                    // 5 清理topicQueueTable
                    if (removeBrokerName) {
                        Iterator<Entry<String, List<QueueData>>> itTopicQueueTable = this.topicQueueTable.entrySet().iterator();
                        while (itTopicQueueTable.hasNext()) {
                            Entry<String, List<QueueData>> entry = itTopicQueueTable.next();
                            String topic = entry.getKey();
                            List<QueueData> queueDataList = entry.getValue();

                            Iterator<QueueData> itQueueData = queueDataList.iterator();
                            while (itQueueData.hasNext()) {
                                QueueData queueData = itQueueData.next();
                                if (queueData.getBrokerName().equals(brokerNameFound)) {
                                    itQueueData.remove();
                                    log.info("remove topic[{} {}], from topicQueueTable, because channel destroyed", topic, queueData);
                                }
                            }

                            if (queueDataList.isEmpty()) {
                                itTopicQueueTable.remove();
                                log.info("remove topic[{}] all queue, from topicQueueTable, because channel destroyed", topic);
                            }
                        }
                    }
                }
                finally {
                    this.lock.writeLock().unlock();
                }
            }
            catch (Exception e) {
                log.error("onChannelDestroy Exception", e);
            }
        }
    }


    /**
     * 定期打印当前类的数据结构
     */
    public void printAllPeriodically() {
        try {
            try {
                this.lock.readLock().lockInterruptibly();
                log.info("--------------------------------------------------------");
                {
                    log.info("topicQueueTable SIZE: {}", this.topicQueueTable.size());
                    Iterator<Entry<String, List<QueueData>>> it = this.topicQueueTable.entrySet().iterator();
                    while (it.hasNext()) {
                        Entry<String, List<QueueData>> next = it.next();
                        log.info("topicQueueTable Topic: {} {}", next.getKey(), next.getValue());
                    }
                }

                {
                    log.info("brokerAddrTable SIZE: {}", this.brokerAddrTable.size());
                    Iterator<Entry<String, BrokerData>> it = this.brokerAddrTable.entrySet().iterator();
                    while (it.hasNext()) {
                        Entry<String, BrokerData> next = it.next();
                        log.info("brokerAddrTable brokerName: {} {}", next.getKey(), next.getValue());
                    }
                }

                {
                    log.info("brokerLiveTable SIZE: {}", this.brokerLiveTable.size());
                    Iterator<Entry<String, BrokerLiveInfo>> it = this.brokerLiveTable.entrySet().iterator();
                    while (it.hasNext()) {
                        Entry<String, BrokerLiveInfo> next = it.next();
                        log.info("brokerLiveTable brokerAddr: {} {}", next.getKey(), next.getValue());
                    }
                }

                {
                    log.info("clusterAddrTable SIZE: {}", this.clusterAddrTable.size());
                    Iterator<Entry<String, Set<String>>> it = this.clusterAddrTable.entrySet().iterator();
                    while (it.hasNext()) {
                        Entry<String, Set<String>> next = it.next();
                        log.info("clusterAddrTable clusterName: {} {}", next.getKey(), next.getValue());
                    }
                }
            }
            finally {
                this.lock.readLock().unlock();
            }
        }
        catch (Exception e) {
            log.error("printAllPeriodically Exception", e);
        }
    }


    /**
     * 获取指定集群下的所有topic列表
     * 
     * @return
     */
    public byte[] getSystemTopicList() {
        TopicList topicList = new TopicList();
        try {
            try {
                this.lock.readLock().lockInterruptibly();
                for (String cluster : clusterAddrTable.keySet()) {
                    topicList.getTopicList().add(cluster);
                    topicList.getTopicList().addAll(this.clusterAddrTable.get(cluster));
                }

                // 随机取一台 broker
                if (brokerAddrTable != null && !brokerAddrTable.isEmpty()) {
                    Iterator<String> it = brokerAddrTable.keySet().iterator();
                    while (it.hasNext()) {
                        BrokerData bd = brokerAddrTable.get(it.next());
                        HashMap<Long, String> brokerAddrs = bd.getBrokerAddrs();
                        if (bd.getBrokerAddrs() != null && !bd.getBrokerAddrs().isEmpty()) {
                            Iterator<Long> it2 = brokerAddrs.keySet().iterator();
                            topicList.setBrokerAddr(brokerAddrs.get(it2.next()));
                            break;
                        }
                    }
                }
            }
            finally {
                this.lock.readLock().unlock();
            }
        }
        catch (Exception e) {
            log.error("getAllTopicList Exception", e);
        }

        return topicList.encode();
    }


    /**
     * 获取指定集群下的所有topic列表
     * 
     * @param cluster
     * @return
     */
    public byte[] getTopicsByCluster(String cluster) {
        TopicList topicList = new TopicList();
        try {
            try {
                this.lock.readLock().lockInterruptibly();
                Set<String> brokerNameSet = this.clusterAddrTable.get(cluster);
                for (String brokerName : brokerNameSet) {
                    Iterator<Entry<String, List<QueueData>>> topicTableIt = this.topicQueueTable.entrySet().iterator();
                    while (topicTableIt.hasNext()) {
                        Entry<String, List<QueueData>> topicEntry = topicTableIt.next();
                        String topic = topicEntry.getKey();
                        List<QueueData> queueDatas = topicEntry.getValue();
                        for (QueueData queueData : queueDatas) {
                            if (brokerName.equals(queueData.getBrokerName())) {
                                topicList.getTopicList().add(topic);
                                break;
                            }
                        }
                    }
                }
            }
            finally {
                this.lock.readLock().unlock();
            }
        }
        catch (Exception e) {
            log.error("getAllTopicList Exception", e);
        }

        return topicList.encode();
    }


    /**
     * 获取单元逻辑下的所有topic列表
     * 
     * @return
     */
    public byte[] getUnitTopics() {
        TopicList topicList = new TopicList();
        try {
            try {
                this.lock.readLock().lockInterruptibly();
                Iterator<Entry<String, List<QueueData>>> topicTableIt = this.topicQueueTable.entrySet().iterator();
                while (topicTableIt.hasNext()) {
                    Entry<String, List<QueueData>> topicEntry = topicTableIt.next();
                    String topic = topicEntry.getKey();
                    List<QueueData> queueDatas = topicEntry.getValue();
                    if (queueDatas != null && queueDatas.size() > 0 && TopicSysFlag.hasUnitFlag(queueDatas.get(0).getTopicSynFlag())) {
                        topicList.getTopicList().add(topic);
                    }
                }
            }
            finally {
                this.lock.readLock().unlock();
            }
        }
        catch (Exception e) {
            log.error("getAllTopicList Exception", e);
        }

        return topicList.encode();
    }


    /**
     * 获取中心向单元同步的所有topic列表
     * 
     * @return
     */
    public byte[] getHasUnitSubTopicList() {
        TopicList topicList = new TopicList();
        try {
            try {
                this.lock.readLock().lockInterruptibly();
                Iterator<Entry<String, List<QueueData>>> topicTableIt = this.topicQueueTable.entrySet().iterator();
                while (topicTableIt.hasNext()) {
                    Entry<String, List<QueueData>> topicEntry = topicTableIt.next();
                    String topic = topicEntry.getKey();
                    List<QueueData> queueDatas = topicEntry.getValue();
                    if (queueDatas != null && queueDatas.size() > 0 && TopicSysFlag.hasUnitSubFlag(queueDatas.get(0).getTopicSynFlag())) {
                        topicList.getTopicList().add(topic);
                    }
                }
            }
            finally {
                this.lock.readLock().unlock();
            }
        }
        catch (Exception e) {
            log.error("getAllTopicList Exception", e);
        }

        return topicList.encode();
    }


    /**
     * 获取含有单元化订阅组的 非单元化Topic列表
     * 
     * @return
     */
    public byte[] getHasUnitSubUnUnitTopicList() {
        TopicList topicList = new TopicList();
        try {
            try {
                this.lock.readLock().lockInterruptibly();
                Iterator<Entry<String, List<QueueData>>> topicTableIt = this.topicQueueTable.entrySet().iterator();
                while (topicTableIt.hasNext()) {
                    Entry<String, List<QueueData>> topicEntry = topicTableIt.next();
                    String topic = topicEntry.getKey();
                    List<QueueData> queueDatas = topicEntry.getValue();
                    if (queueDatas != null && queueDatas.size() > 0
                            && !TopicSysFlag.hasUnitFlag(queueDatas.get(0).getTopicSynFlag())
                            && TopicSysFlag.hasUnitSubFlag(queueDatas.get(0).getTopicSynFlag())) {
                        topicList.getTopicList().add(topic);
                    }
                }
            }
            finally {
                this.lock.readLock().unlock();
            }
        }
        catch (Exception e) {
            log.error("getAllTopicList Exception", e);
        }

        return topicList.encode();
    }
}

 /**
  * 活动Broker信息
  * @author tianyuliang
  * @since 2017/9/1
  */
class BrokerLiveInfo {
    private long lastUpdateTimestamp;
    private DataVersion dataVersion;
    private Channel channel;
    private String haServerAddr;


    public BrokerLiveInfo(long lastUpdateTimestamp, DataVersion dataVersion, Channel channel, String haServerAddr) {
        this.lastUpdateTimestamp = lastUpdateTimestamp;
        this.dataVersion = dataVersion;
        this.channel = channel;
        this.haServerAddr = haServerAddr;
    }


    public long getLastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }


    public void setLastUpdateTimestamp(long lastUpdateTimestamp) {
        this.lastUpdateTimestamp = lastUpdateTimestamp;
    }


    public DataVersion getDataVersion() {
        return dataVersion;
    }


    public void setDataVersion(DataVersion dataVersion) {
        this.dataVersion = dataVersion;
    }


    public Channel getChannel() {
        return channel;
    }


    public void setChannel(Channel channel) {
        this.channel = channel;
    }


    public String getHaServerAddr() {
        return haServerAddr;
    }


    public void setHaServerAddr(String haServerAddr) {
        this.haServerAddr = haServerAddr;
    }


    @Override
    public String toString() {
        String info = "BrokerLiveInfo [lastUpdateTimestamp=%d, dataVersion=%s, channel=%s, haServerAddr=%s]";
        return String.format(info, lastUpdateTimestamp, dataVersion, channel, haServerAddr);

        /*return "BrokerLiveInfo [lastUpdateTimestamp=" + lastUpdateTimestamp + ", dataVersion=" + dataVersion
                + ", channel=" + channel + ", haServerAddr=" + haServerAddr + "]";*/
    }
}
