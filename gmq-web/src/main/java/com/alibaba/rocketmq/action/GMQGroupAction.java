package com.alibaba.rocketmq.action;

import com.alibaba.rocketmq.domain.gmq.TopicStats;
import com.alibaba.rocketmq.service.gmq.GMQGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author: tianyuliang
 * @since: 2016/7/19
 */
@Controller
@RequestMapping("/group")
public class GMQGroupAction extends AbstractAction {

    @Autowired
    GMQGroupService gmqGroupService;


    @RequestMapping(value = "/consumerGroup.do", method = {RequestMethod.GET, RequestMethod.POST})
    public String consumerGroup(ModelMap map, HttpServletRequest request, @RequestParam(required = false) String groupId) {
        putPublicAttribute(map, "consumerGroup");
        try {
            Map<String, Object> params = gmqGroupService.consumerProgress(groupId);
            putTable(map, params);
        } catch (Throwable t) {
            putAlertMsg(t, map);
        }
        return TEMPLATE;
    }


    @RequestMapping(value = "/topicStats.do", method = RequestMethod.GET)
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


    @RequestMapping(value = "/topicRoute.do", method = RequestMethod.GET)
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
