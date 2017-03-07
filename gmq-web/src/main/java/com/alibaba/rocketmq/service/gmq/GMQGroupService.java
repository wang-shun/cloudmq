package com.alibaba.rocketmq.service.gmq;

import com.alibaba.rocketmq.common.UtilAll;
import com.alibaba.rocketmq.common.admin.ConsumeStats;
import com.alibaba.rocketmq.common.admin.OffsetWrapper;
import com.alibaba.rocketmq.common.admin.TopicOffset;
import com.alibaba.rocketmq.common.admin.TopicStatsTable;
import com.alibaba.rocketmq.common.message.MessageQueue;
import com.alibaba.rocketmq.common.protocol.route.TopicRouteData;
import com.alibaba.rocketmq.domain.gmq.ConsumerGroup;
import com.alibaba.rocketmq.domain.gmq.TopicStats;
import com.alibaba.rocketmq.service.AbstractService;
import com.alibaba.rocketmq.tools.admin.DefaultMQAdminExt;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;


/**
 * @author: tianyuliang
 * @since: 2016/7/19
 */
@Service
@SuppressWarnings("unchecked")
public class GMQGroupService extends AbstractService {

    static final Logger logger = LoggerFactory.getLogger(GMQGroupService.class);

    public Map<String, Object> consumerProgress(String consumerGroupId) throws Throwable {
        Map<String, Object> params = new HashedMap();
        List<ConsumerGroup> list = new ArrayList<>();
        if (StringUtils.isBlank(consumerGroupId)) {
            params.put("list", list);
            return params;
        }

        Throwable t = null;
        DefaultMQAdminExt defaultMQAdminExt = getDefaultMQAdminExt();
        try {
            defaultMQAdminExt.start();
            if (StringUtils.isNotBlank(consumerGroupId)) {
                ConsumeStats consumeStats = defaultMQAdminExt.examineConsumeStats(consumerGroupId);

                List<MessageQueue> mqList = new LinkedList<MessageQueue>();
                mqList.addAll(consumeStats.getOffsetTable().keySet());
                Collections.sort(mqList);

                // "#Topic", "#Broker Name", "#QID", "#BrokerOffset", "#ConsumerOffset", "#DiffOffset"
                ConsumerGroup groupVo = null;
                long diffTotal = 0L;
                for (MessageQueue mq : mqList) {
                    groupVo = new ConsumerGroup();
                    groupVo.setBrokerName(mq.getBrokerName());
                    groupVo.setQueueId(mq.getQueueId());
                    groupVo.setTopic(mq.getTopic());

                    OffsetWrapper offsetWrapper = consumeStats.getOffsetTable().get(mq);
                    groupVo.setBrokerOffset(offsetWrapper.getBrokerOffset());
                    groupVo.setConsumerOffset(offsetWrapper.getConsumerOffset());

                    long diff = offsetWrapper.getBrokerOffset() - offsetWrapper.getConsumerOffset();
                    groupVo.setDiffOffset(diff);
                    list.add(groupVo);

                    diffTotal += diff;
                }
                params.put("groupId", consumerGroupId);
                params.put("list", list);
                params.put("tps", consumeStats.getConsumeTps());
                params.put("diffTotal", diffTotal);
                return params;
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            t = e;
        } finally {
            shutdownDefaultMQAdminExt(defaultMQAdminExt);
        }
        throw t;
    }

    public List<TopicStats> topicStats(String topicName) throws Throwable {
        List<TopicStats> topicStatsList = new ArrayList<>();
        Throwable t = null;
        DefaultMQAdminExt defaultMQAdminExt = getDefaultMQAdminExt();
        try {
            defaultMQAdminExt.start();
            TopicStatsTable topicStatsTable = defaultMQAdminExt.examineTopicStats(topicName);

            List<MessageQueue> mqList = new LinkedList<MessageQueue>();
            mqList.addAll(topicStatsTable.getOffsetTable().keySet());
            Collections.sort(mqList);
            // "#Broker Name", "#QueueID", "#Min Offset", "#Max Offset", "#Last Updated"
            TopicStats statsVo = null;
            TopicOffset topicOffset = null;
            for (MessageQueue mq : mqList) {
                topicOffset = topicStatsTable.getOffsetTable().get(mq);
                statsVo = new TopicStats();
                statsVo.setBrokerName(mq.getBrokerName());
                statsVo.setTopic(mq.getTopic());
                statsVo.setQueueId(mq.getQueueId());
                statsVo.setLastUpdated(topicOffset.getLastUpdateTimestamp() <= 0 ? "" : UtilAll.timeMillisToHumanString2(topicOffset.getLastUpdateTimestamp()));
                statsVo.setMaxOffset(topicOffset.getMaxOffset());
                statsVo.setMinOffset(topicOffset.getMinOffset());
                topicStatsList.add(statsVo);
            }
            return topicStatsList;
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            t = e;
        } finally {
            shutdownDefaultMQAdminExt(defaultMQAdminExt);
        }
        throw t;
    }

    public Map<String, Object> topicRoute(String topicName) throws Throwable {
        Map<String, Object> params = new HashMap<>();
        Throwable t = null;
        DefaultMQAdminExt adminExt = getDefaultMQAdminExt();
        try {
            adminExt.start();
            TopicRouteData routeData = adminExt.examineTopicRouteInfo(topicName);
            params.put("brokerDatas", routeData.getBrokerDatas());
            params.put("queueDatas", routeData.getQueueDatas());
            return params;
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            t = e;
        } finally {
            shutdownDefaultMQAdminExt(adminExt);
        }
        throw t;
    }

}
