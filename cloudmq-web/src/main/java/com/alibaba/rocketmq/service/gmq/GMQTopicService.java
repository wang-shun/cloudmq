package com.alibaba.rocketmq.service.gmq;

import com.alibaba.rocketmq.common.TopicConfig;
import com.alibaba.rocketmq.common.protocol.body.TopicList;
import com.alibaba.rocketmq.common.protocol.route.QueueData;
import com.alibaba.rocketmq.config.ConfigureInitializer;
import com.alibaba.rocketmq.domain.gmq.Cluster;
import com.alibaba.rocketmq.service.AbstractService;
import com.alibaba.rocketmq.tools.admin.DefaultMQAdminExt;
import com.alibaba.rocketmq.tools.command.CommandUtil;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;


/**
 * @author: tianyuliang
 * @since: 2016/7/21
 */
@Service
public class GMQTopicService extends AbstractService {

    static final Logger LOGGER = LoggerFactory.getLogger(GMQTopicService.class);

    @Autowired
    ConfigureInitializer configureInitializer;

    @Autowired
    GMQClusterService clusterService;

    public boolean update(String topicName, String clusterName, String readQueueNums, String writeQueueNums) throws Throwable {
        Throwable t = null;
        DefaultMQAdminExt defaultMQAdminExt = getDefaultMQAdminExt();
        try {
            if (StringUtils.isBlank(topicName)) {
                throw new IllegalStateException("topicName can not be blank.");
            }
            if (StringUtils.isBlank(clusterName)) {
                throw new IllegalStateException("clusterName can not be blank.");
            }

            TopicConfig topicConfig = new TopicConfig();
            topicConfig.setReadQueueNums(8);
            topicConfig.setWriteQueueNums(8);
            topicConfig.setTopicName(topicName);

            if (StringUtils.isNotBlank(readQueueNums)) {
                topicConfig.setReadQueueNums(Integer.parseInt(readQueueNums));
            }

            if (StringUtils.isNotBlank(writeQueueNums)) {
                topicConfig.setWriteQueueNums(Integer.parseInt(writeQueueNums));
            }

            String operation = StringUtils.isBlank(readQueueNums) && StringUtils.isBlank(writeQueueNums) ? "create" : "update";

            defaultMQAdminExt.start();
            Set<String> masterSet = CommandUtil.fetchMasterAddrByClusterName(defaultMQAdminExt, clusterName);
            for (String addr : masterSet) {
                LOGGER.info(operation + " topic. topic=" + topicName + ",masterAddr=" + addr);
                defaultMQAdminExt.createAndUpdateTopicConfig(addr, topicConfig);
            }
            return true;
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
            t = e;
        } finally {
            shutdownDefaultMQAdminExt(defaultMQAdminExt);
        }
        throw t;
    }

    private List<String> list() throws Throwable {
        List<String> topics = new ArrayList<String>();
        Throwable t = null;
        DefaultMQAdminExt defaultMQAdminExt = getDefaultMQAdminExt();
        try {
            defaultMQAdminExt.start();

            TopicList topicList = defaultMQAdminExt.fetchAllTopicList();
            if (topicList.getTopicList().size() == 0) {
                throw new IllegalStateException("defaultMQAdminExt.fetchAllTopicList() is blank.");
            }
            topics.addAll(topicList.getTopicList());
            LOGGER.info("all topic size=" + topics.size());
            return topics;
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
            t = e;
        } finally {
            shutdownDefaultMQAdminExt(defaultMQAdminExt);
        }
        throw t;
    }

    public boolean delete(String topicName, String clusterName) throws Throwable {
        Throwable t = null;
        DefaultMQAdminExt adminExt = getDefaultMQAdminExt();
        try {
            if (StringUtils.isBlank(topicName)) {
                throw new IllegalStateException("topicName is blank.");
            }
            if (StringUtils.isBlank(clusterName)) {
                throw new IllegalStateException("clusterName is blank.");
            }

            adminExt.start();
            Set<String> masterSet = CommandUtil.fetchMasterAddrByClusterName(adminExt, clusterName);
            LOGGER.info("delete topic in broker. topic=" + topicName + ",clusterName=" + clusterName);
            adminExt.deleteTopicInBroker(masterSet, topicName);

            Set<String> nameServerSet = null;
            if (StringUtils.isNotBlank(configureInitializer.getNamesrvAddr())) {
                String[] ns = configureInitializer.getNamesrvAddr().split(";");
                nameServerSet = new HashSet<String>(Arrays.asList(ns));
            }
            LOGGER.info("delete topic in nameServer. topic=" + topicName + ",clusterName=" + clusterName);
            adminExt.deleteTopicInNameServer(nameServerSet, topicName);
            return true;
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
            t = e;
        } finally {
            shutdownDefaultMQAdminExt(adminExt);
        }
        throw t;
    }

    /***
     * 获取集群Topic列表，分为topics、retryTopics、dlqTopics
     * @return
     * @throws Throwable
     */
    public Map<String, Object> getTopicList() throws Throwable {
        Map<String, Object> params = Maps.newHashMap();
        List<String> list = this.list();
        List<String> topics = new ArrayList<>();        // 正常topic
        List<String> retryTopics = new ArrayList<>();   // 重试队列
        List<String> dlqTopics = new ArrayList<>();     // 死信队列
        for (String topic : list) {
            if(topic.startsWith("%RETRY%")){
                retryTopics.add(topic);
            } else if(topic.startsWith("%DLQ%")) {
                dlqTopics.add(topic);
            } else {
                topics.add(topic);
            }
        }
        Collections.sort(topics);
        params.put("topics", topics);
        params.put("retryTopics", retryTopics);
        params.put("dlqTopics", dlqTopics);
        return params;
    }

    public List<String> getClusterNames() throws Throwable {
        List<String> clusterNames = new ArrayList<String>();
        List<Cluster> clusters = clusterService.list();
        if (clusters != null && clusters.size() > 0) {
            for (Cluster cluster : clusters) {
                clusterNames.add(cluster.getName());
            }
        }
        return clusterNames;
    }

    public QueueData getTopicRouteData(String topicName) throws Throwable {
        Throwable t = null;
        DefaultMQAdminExt adminExt = getDefaultMQAdminExt();
        try {
            adminExt.start();
            List<QueueData> queueDatas = adminExt.examineTopicRouteInfo(topicName).getQueueDatas();
            return CollectionUtils.isEmpty(queueDatas) ? null : queueDatas.get(0);
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
            t = e;
        } finally {
            shutdownDefaultMQAdminExt(adminExt);
        }
        throw t;
    }

    public void updateAllTopic(String clusterName, String[] excludeTopic) throws Throwable {
        DefaultMQAdminExt defaultMQAdminExt = getDefaultMQAdminExt();
        Map<String, List<String>> mapError = new HashMap<>();
        try {
            defaultMQAdminExt.start();
            TopicList topicList = defaultMQAdminExt.fetchAllTopicList();
            for (final String topic : topicList.getTopicList()) {
                if (!topic.contains("%RETRY%") && !topic.contains("%DLQ%") && !containsTopic(topic, excludeTopic)) {
                    List<QueueData> queueData = defaultMQAdminExt.examineTopicRouteInfo(topic).getQueueDatas();
                    if (null != queueData && !queueData.isEmpty()) {
                        QueueData qData = queueData.get(0);
                        TopicConfig topicConfig = new TopicConfig();
                        topicConfig.setTopicName(topic);
                        topicConfig.setReadQueueNums(qData.getReadQueueNums());
                        topicConfig.setWriteQueueNums(qData.getWriteQueueNums());
                        Set<String> masterSet = CommandUtil.fetchMasterAddrByClusterName(defaultMQAdminExt, clusterName);
                        for (String addr : masterSet) {
                            try {
                                LOGGER.info("updateAllTopic topic. topic=" + topic + ",masterAddr=" + addr);
                                defaultMQAdminExt.createAndUpdateTopicConfig(addr, topicConfig);
                            } catch (Exception e) {
                                if (mapError.containsKey(e.getMessage())) {
                                    mapError.get(e.getMessage()).add(topic);
                                }else{
                                    mapError.put(e.getMessage(),new ArrayList<String>(){{
                                        add(topic);
                                    }});
                                }

                            }
                        }
                    }
                }
            }
        } finally {
            shutdownDefaultMQAdminExt(defaultMQAdminExt);
        }
        StringBuilder sb=new StringBuilder();
        if(!mapError.isEmpty()){
            for(Map.Entry<String,List<String>> entry:mapError.entrySet()){
                sb.append(entry.getKey());
                sb.append(":");
                sb.append(org.apache.commons.lang.StringUtils.join(entry.getValue().toArray(),","));
                sb.append(";");
            }
           throw  new Exception(sb.toString());
        }
    }

    private boolean containsTopic(String topic, String[] excludeTopic) {
        for (String exTopic : excludeTopic) {
            if (topic.equals(exTopic.trim())) {
                return true;
            }
        }
        return false;
    }

}
