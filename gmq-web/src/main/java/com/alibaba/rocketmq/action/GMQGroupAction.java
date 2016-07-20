package com.alibaba.rocketmq.action;

import com.alibaba.rocketmq.service.GMQGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;


/**
 * @author: tianyuliang
 * @since: 2016/7/19
 */
@Controller
@RequestMapping("/group")
public class GMQGroupAction extends AbstractAction {

    @Autowired
    GMQGroupService gmqGroupService;


    /*@RequestMapping(value = "/consumerGroup.do", method = {RequestMethod.GET, RequestMethod.POST})
    public String consumerGroup(ModelMap map, HttpServletRequest request, @RequestParam(required = false) String groupId) {
        putPublicAttribute(map, "consumerGroup");
        try {
            if (request.getMethod().equals(GET)) {

            } else if (request.getMethod().equals(POST)) {
                Map<String, Object> params = gmqGroupService.consumerProgress(groupId);
                params.put("consumerGroupId", groupId);
                putTable(map, params);
            } else {
                throwUnknowRequestMethodException(request);
            }

        } catch (Throwable t) {
            putAlertMsg(t, map);
        }
        return TEMPLATE;
    }


    @RequestMapping(value = "/topicStats.do", method = RequestMethod.GET)
    public String topicStats(ModelMap map, HttpServletRequest request, @RequestParam(required = true) String topic, @RequestParam String groupId) {
        putPublicAttribute(map, "topicStats");
        try {
            Map<String, Object> params = new HashMap<String, Object>();
            List<TopicStatsVo> statsList = gmqGroupService.topicStats(topic);
            params.put("topic", topic);
            params.put("groupId", groupId);
            params.put("statsList", statsList);
            putTable(map, statsList);
        } catch (Throwable t) {
            putAlertMsg(t, map);
        }
        return TEMPLATE;
    }*/


    @Override
    protected String getFlag() {
        return "group_flag";
    }

    @Override
    protected String getName() {
        return "group";
    }
}
