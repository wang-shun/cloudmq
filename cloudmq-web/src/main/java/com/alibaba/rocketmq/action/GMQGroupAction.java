package com.alibaba.rocketmq.action;

import com.alibaba.fastjson.JSON;
import com.alibaba.rocketmq.domain.gmq.TopicStats;
import com.alibaba.rocketmq.service.gmq.GMQGroupService;
import com.alibaba.rocketmq.service.gmq.GMQGuestService;
import com.alibaba.rocketmq.service.gmq.GMQTopicService;
import com.alibaba.rocketmq.util.base.BaseUtil;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static com.alibaba.rocketmq.action.ConnectionAction.LOGGER;


/**
 * @author: tianyuliang
 * @since: 2016/7/19
 */
@Controller
@RequestMapping("/group")
public class GMQGroupAction extends AbstractAction {
    static final Logger LOGGER = LoggerFactory.getLogger(GMQGroupAction.class);

    @Autowired
    GMQGroupService gmqGroupService;
    @Autowired
    private GMQGuestService gmqGuestService;
    @Autowired
    GMQTopicService gmqTopicService;

    @RequestMapping(value = "/consumerProgress.do", method = {RequestMethod.GET, RequestMethod.POST})
    public String consumerProgress(ModelMap map,
                                @RequestParam(required = false) String consumerGroupId,
                                @RequestParam(required = false) String topic) {
        putPublicAttribute(map, "consumerProgress");
        try {
            Map<String, Object> params = Maps.newHashMap();
            List<String> topics = (ArrayList)gmqTopicService.getTopicList().get("topics");
            params.put("topics", topics);

            if(!BaseUtil.isBlank(topic)){
                params.put("topic", topic.trim());
            }
            if(!BaseUtil.isBlank(consumerGroupId)){
                params.put("consumerGroupId", consumerGroupId.trim());
            }

            if (!BaseUtil.isBlank(consumerGroupId)) {
                Map<String, Object> consumerProgress = gmqGroupService.consumerProgress(consumerGroupId);
                params.put("consumerProgress", consumerProgress);
            }
            putTable(map, params);
        } catch (Throwable t) {
            putAlertMsg(t, map);
        }
        return TEMPLATE;
    }

    @RequestMapping(value = "/queryConsumerGroupId.do", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String queryConsumerGroupId(@RequestParam(required = true) String topic) {
        Map<String, Object> params = Maps.newHashMap();
        List<String> groups = new ArrayList<>();
        try{
            HashSet<String> hs = gmqGuestService.queryTopicConsumeByWho(topic.trim());
            for (String group : hs) {
                groups.add(group);
            }
        } catch(Throwable e){
            LOGGER.error(("query consumerGroupId error. topic=" + topic + ", msg=" + e.getMessage()), e);
            // ingore e;
        }
        params.put("groups", groups);
        params.put("topic", topic);
        return JSON.toJSONString(params);
    }

    @RequestMapping(value = "/topicStats.do")
    public String topicStats(ModelMap map, HttpServletRequest request,
                             @RequestParam(required = true) String topicName,
                             @RequestParam(required = true) String groupId) {
        putPublicAttribute(map, "topicStats");
        try {
            Map<String, Object> params = new HashMap<String, Object>();
            List<TopicStats> statsList = gmqGroupService.topicStats(topicName.trim());
            params.put("topic", topicName.trim());
            params.put("groupId", groupId.trim());
            params.put("statsList", statsList);
            putTable(map, params);
        } catch (Throwable t) {
            putAlertMsg(t, map);
        }
        return TEMPLATE;
    }


    @RequestMapping(value = "/topicRoute.do")
    public String topicRoute(ModelMap map, HttpServletRequest request,
                             @RequestParam(required = true) String topicName,
                             @RequestParam(required = true) String groupId) {
        putPublicAttribute(map, "topicRoute");
        try {
            Map<String, Object> params = gmqGroupService.topicRoute(topicName.trim());
            params.put("groupId", groupId.trim());
            params.put("topicName", topicName.trim());
            putTable(map, params);
        } catch (Throwable t) {
            putAlertMsg(t, map);
        }
        return TEMPLATE;
    }


    @Override
    protected String getFlag() {
        return "group_flag";
    }

    @Override
    protected String getName() {
        return "group";
    }
}
