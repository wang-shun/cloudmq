package com.alibaba.rocketmq.action;

import com.alibaba.rocketmq.common.protocol.route.QueueData;
import com.alibaba.rocketmq.domain.gmq.TopicStats;
import com.alibaba.rocketmq.service.GMQGroupService;
import com.alibaba.rocketmq.service.GMQTopicService;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * @author: tianyuliang
 * @since: 2016/7/21
 */
@Controller
@RequestMapping("/gmq/topic")
public class GMQTopicAction extends AbstractAction {


    @Autowired
    GMQTopicService gmqTopicService;

    @Autowired
    GMQGroupService gmqGroupService;

    @RequestMapping(value = "/list.do", method = RequestMethod.GET)
    public String list(ModelMap map) {
        putPublicAttribute(map, "list");
        try {
            Map<String, Object> params = getTopicAndCluster();
            putTable(map, params);
        } catch (Throwable t) {
            putAlertMsg(t, map);
        }
        return TEMPLATE;
    }


    /**
     * add topic to save
     *
     * @author tianyuliang
     * @params topicName
     * @params clusterName
     * @since 2016/7/19
     */
    @RequestMapping(value = "/save.do", method = {RequestMethod.GET, RequestMethod.POST})
    public String save(ModelMap map,
                       @RequestParam(required = true) String topicName,
                       @RequestParam(required = true) String clusterName) {
        try {
            putPublicAttribute(map, "list");
            gmqTopicService.update(topicName.trim(), clusterName.trim(), null, null); // 只要不抛异常，那就是创建更新成功
            Map<String, Object> params = getTopicAndCluster();
            putTable(map, params);
        } catch (Throwable t) {
            putPublicAttribute(map, "add");
            putAlertMsg(t, map);
        }
        return TEMPLATE;
    }

    /**
     * update topic info for full param
     *
     * @author tianyuliang
     * @params
     * @since 2016/7/21
     */
    @RequestMapping(value = "/update.do", method = {RequestMethod.GET, RequestMethod.POST})
    public String update(ModelMap map,
                         @RequestParam(required = true) String topicName,
                         @RequestParam(required = true) String clusterName,
                         @RequestParam(required = true) String readQueueNums,
                         @RequestParam(required = true) String writeQueueNums) {
        try {
            putPublicAttribute(map, "list");
            gmqTopicService.update(topicName.trim(), clusterName.trim(), readQueueNums.trim(), writeQueueNums.trim());
            Map<String, Object> params = getTopicAndCluster();
            putTable(map, params);
        } catch (Throwable t) {
            putPublicAttribute(map, "update");
            putAlertMsg(t, map);
        }
        return TEMPLATE;
    }

    @RequestMapping(value = "/queryTopicQueueData.do", method = {RequestMethod.GET, RequestMethod.POST})
    public String queryTopic(ModelMap map,
                             @RequestParam(required = true) String topicName,
                             @RequestParam(required = true) String clusterName) {
        try {
            putPublicAttribute(map, "update");
            Map<String, Object> params = Maps.newHashMap();
            List<String> clusterNames = gmqTopicService.getClusterNames();
            QueueData queueData = gmqTopicService.getTopicRouteData(topicName.trim());
            params.put("topicName", topicName.trim());
            params.put("clusterName", clusterName.trim());
            params.put("clusterNames", clusterNames);
            params.put("queueData", queueData);
            putTable(map, params);
        } catch (Throwable t) {
            putAlertMsg(t, map);
        }
        return TEMPLATE;
    }

    @RequestMapping(value = "/delete.do", method = RequestMethod.GET)
    public String delete(ModelMap map,
                         @RequestParam(required = true) String topicName,
                         @RequestParam(required = true) String clusterName) {
        putPublicAttribute(map, "list");
        try {
            gmqTopicService.delete(topicName.trim(), clusterName.trim()); // 只要不抛异常，那就是删除成功
            Map<String, Object> params = getTopicAndCluster();
            putTable(map, params);
        } catch (Throwable t) {
            putAlertMsg(t, map);
        }
        return TEMPLATE;
    }

    @RequestMapping(value = "/add.do", method = RequestMethod.GET)
    public String add(ModelMap map) {
        putPublicAttribute(map, "add");
        try {
            List<String> clusterNames = gmqTopicService.getClusterNames();
            putTable(map, clusterNames);
        } catch (Throwable t) {
            putAlertMsg(t, map);
        }
        return TEMPLATE;
    }


    public Map<String, Object> getTopicAndCluster() throws Throwable {
        Map<String, Object> params = Maps.newHashMap();
        List<String> topics = gmqTopicService.list();
        List<String> clusterNames = gmqTopicService.getClusterNames();
        params.put("topics", topics);
        params.put("clusterNames", clusterNames);
        return params;
    }


    @RequestMapping(value = "/stats.do", method = RequestMethod.GET)
    public String topicStats(ModelMap map, HttpServletRequest request, @RequestParam(required = true) String topicName) {
        putPublicAttribute(map, "stats");
        try {
            Map<String, Object> params = Maps.newHashMap();
            List<TopicStats> statsList = gmqGroupService.topicStats(topicName.trim());
            params.put("topic", topicName.trim());
            params.put("statsList", statsList);
            putTable(map, params);
        } catch (Throwable t) {
            putAlertMsg(t, map);
        }
        return TEMPLATE;
    }


    @Override
    protected String getFlag() {
        return "gmq_topic_flag";
    }

    @Override
    protected String getName() {
        return "topic";
    }
}
