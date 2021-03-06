package com.alibaba.rocketmq.action;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSON;
import com.alibaba.rocketmq.domain.gmq.TopicStats;
import com.alibaba.rocketmq.service.gmq.GMQGroupService;
import com.alibaba.rocketmq.service.gmq.GMQGuestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.rocketmq.domain.gmq.Cluster;
import com.alibaba.rocketmq.service.BrokerService;
import com.alibaba.rocketmq.service.gmq.GMQClusterService;
import com.alibaba.rocketmq.service.gmq.GMQTopicService;
import com.google.common.collect.Maps;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;



/**
 * @author: tianyuliang
 * @since: 2017/2/7
 */
@Controller
@RequestMapping("/guest")
public class GMQGuestAction extends AbstractAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(GMQGuestAction.class);

    @Autowired
    BrokerService brokerService;
    @Autowired
    GMQClusterService clusterService;
    @Autowired
    GMQTopicService gmqTopicService;
    @Autowired
    GMQGuestService gmqGuestService;
    @Autowired
    GMQGroupService gmqGroupService;

    @RequestMapping(value = "/broker.do")
    public String broker(ModelMap map) {
        putPublicAttribute(map, "broker");
        try {
            List<Cluster> brokerList = clusterService.list();
            putTable(map, brokerList);
        }
        catch (Throwable t) {
            putAlertMsg(t, map);
        }
        return TEMPLATE;
    }


    @RequestMapping(value = "/topic.do")
    public String topic(ModelMap map) {
        putPublicAttribute(map, "topic");
        try {
            Map<String, Object> params = getTopicAndCluster();
            putTable(map, params);
        }
        catch (Throwable t) {
            putAlertMsg(t, map);
        }
        return TEMPLATE;
    }

    @RequestMapping(value = "/consumeGroup.do")
    public String consumeGroup(ModelMap map, @RequestParam(required = true) String topic) {
        putPublicAttribute(map, "consumeGroup");
        try {
            Map<String, Object> params = Maps.newHashMap();
            HashSet<String> groups = gmqGuestService.queryTopicConsumeByWho(topic);
            params.put("consumerGroupId", groups);
            params.put("topic", topic);
            putTable(map, params);
        }
        catch (Throwable t) {
            putAlertMsg(t, map);
        }
        return TEMPLATE;
    }

    private Map<String, Object> getTopicAndCluster() throws Throwable {
        Map<String, Object> params = Maps.newHashMap();
        Map<String, Object> topicList = gmqTopicService.getTopicList();
        List<String> topics = (ArrayList)topicList.get("topics");               // 正常topic
        List<String> retryTopics = (ArrayList)topicList.get("retryTopics");      // 重试队列
        List<String> dlqTopics = (ArrayList)topicList.get("dlqTopics");         // 死信队列
        params.put("topics", topics);
        params.put("retryTopics", retryTopics);
        params.put("dlqTopics", dlqTopics);

        List<String> clusterNames = gmqTopicService.getClusterNames();
        params.put("clusterNames", clusterNames);
        return params;
    }


    @RequestMapping(value = "/topicState.do")
    public String topicState(ModelMap map, HttpServletRequest request, @RequestParam(required = true) String topic) {
        putPublicAttribute(map, "topicState");
        try {
            Map<String, Object> params = Maps.newHashMap();
            List<TopicStats> topicState = gmqGroupService.topicStats(topic.trim());
            params.put("topic", topic.trim());
            params.put("topicState", topicState);
            putTable(map, params);
        } catch (Throwable t) {
            putAlertMsg(t, map);
        }
        return TEMPLATE;
    }



    @Override
    protected String getFlag() {
        return "guest_flag";
    }


    @Override
    protected String getName() {
        return "guest";
    }
}
