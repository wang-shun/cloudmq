package com.alibaba.rocketmq.action;

import com.alibaba.rocketmq.common.Table;
import com.alibaba.rocketmq.service.UserService;
import org.apache.commons.cli.Option;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;


/**
 * @author gaoyanlei
 * @since 2016/7/12
 */
@Controller
@RequestMapping("/user")
public class UserAction extends AbstractAction {

    @Autowired
    UserService userService;

    @Override
    protected String getFlag() {
        return "user_flag";
    }

    @Override
    protected String getName() {
        return "User";
    }

    @RequestMapping(value = "/login.do", method = RequestMethod.GET)
    public String list(@RequestParam(required = false) String userName, @RequestParam(required = false) String password) {
        userService.checkUserTest(userName, password);
        return TEMPLATE;
    }

//    @RequestMapping(value = "/add.do", method = RequestMethod.GET)
//    public String add(ModelMap map) {
//        putPublicAttribute(map, "add");
//        Collection<Option> options = topicService.getOptionsForUpdate();
//        putOptions(map, options);
//        map.put(FORM_ACTION, "update.do");// add as update
//        return TEMPLATE;
//    }

    @RequestMapping(value = "/list.do", method = RequestMethod.GET)
    public String list(ModelMap map) {
        putPublicAttribute(map, "list");
        try {
            Table table = userService.findAll();
            putTable(map, table);
        } catch (Throwable t) {
            putAlertMsg(t, map);
        }
        return TEMPLATE;
    }

    @RequestMapping(value = "/delete.do", method = {RequestMethod.GET, RequestMethod.POST})
    public String delete(ModelMap map, HttpServletRequest request, @RequestParam int userId) {
//        Collection<Option> options = topicService.getOptionsForDelete();
//        putPublicAttribute(map, "delete", options, request);
        try {
            if (request.getMethod().equals(GET)) {

            } else if (request.getMethod().equals(POST)) {
                userService.delate(userId);
                putAlertTrue(map);
            } else {
                throwUnknowRequestMethodException(request);
            }
        } catch (Throwable t) {
            putAlertMsg(t, map);
        }
        return TEMPLATE;
    }

//    @RequestMapping(value = "/update.do", method = {RequestMethod.GET, RequestMethod.POST})
//    public String update(ModelMap map, HttpServletRequest request, @RequestParam String topic,
//                         @RequestParam(required = false) String readQueueNums,
//                         @RequestParam(required = false) String writeQueueNums,
//                         @RequestParam(required = false) String perm, @RequestParam(required = false) String brokerAddr,
//                         @RequestParam(required = false) String clusterName) {
//        Collection<Option> options = topicService.getOptionsForUpdate();
//        putPublicAttribute(map, "update", options, request);
//        try {
//            if (request.getMethod().equals(GET)) {
//
//            } else if (request.getMethod().equals(POST)) {
//                checkOptions(options);
//                topicService.update(topic, readQueueNums, writeQueueNums, perm, brokerAddr, clusterName);
//                putAlertTrue(map);
//            } else {
//                throwUnknowRequestMethodException(request);
//            }
//        } catch (Throwable t) {
//            putAlertMsg(t, map);
//        }
//
//        return TEMPLATE;
//    }

}
