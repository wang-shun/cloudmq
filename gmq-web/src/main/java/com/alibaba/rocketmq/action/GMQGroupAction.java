package com.alibaba.rocketmq.action;

import com.alibaba.rocketmq.common.Table;
import com.alibaba.rocketmq.service.GMQGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

import java.util.Map;

import static com.sun.xml.internal.fastinfoset.alphabet.BuiltInRestrictedAlphabets.table;

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


    @Override
    protected String getFlag() {
        return "group_flag";
    }

    @Override
    protected String getName() {
        return "group";
    }
}
