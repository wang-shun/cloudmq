package com.alibaba.rocketmq.action;

import com.alibaba.rocketmq.domain.User;
import com.alibaba.rocketmq.util.MyBeanUtils;
import com.alibaba.rocketmq.common.Table;
import com.alibaba.rocketmq.service.UserService;
import org.apache.commons.cli.Option;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
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

    @RequestMapping(value = "/add.do", method = {RequestMethod.GET, RequestMethod.POST})
    public String add(ModelMap map, HttpServletRequest request, @RequestParam(required = false) String id,
                      @RequestParam(required = false) String userName,
                      @RequestParam(required = false) String email,
                      @RequestParam(required = false) String mobile, @RequestParam(required = false) String realName) {
        if (request.getMethod().equals(GET)) {
            putPublicAttribute(map, "add");
//            putOptions(map, options);
//            map.put(FORM_ACTION, "add.do");// add as update
        } else if (request.getMethod().equals(POST)) {
//            checkOptions(options);
            userService.save(userName, email, mobile, realName);
            putAlertTrue(map);
        } else {
            throwUnknowRequestMethodException(request);
        }
        return TEMPLATE;
    }

    @RequestMapping(value = "/list.do", method = RequestMethod.GET)
    public String list(ModelMap map) {
        putPublicAttribute(map, "list");
        try {
            putTable(map, userService.findAll());
        } catch (Throwable t) {
            putAlertMsg(t, map);
        }
        return TEMPLATE;
    }

    @RequestMapping(value = "/delete.do", method = {RequestMethod.GET, RequestMethod.POST})
    public String delete(ModelMap map, HttpServletRequest request, @RequestParam int id) {
//        Collection<Option> options = topicService.getOptionsForDelete();
//        putPublicAttribute(map, "delete", options, request);
        try {
            if (request.getMethod().equals(GET)) {

            } else if (request.getMethod().equals(POST)) {
                userService.delate(id);
                putAlertTrue(map);
            } else {
                throwUnknowRequestMethodException(request);
            }
        } catch (Throwable t) {
            putAlertMsg(t, map);
        }
        return TEMPLATE;
    }

    @RequestMapping(value = "/update.do", method = {RequestMethod.GET, RequestMethod.POST})
    public String update(ModelMap map, HttpServletRequest request, @RequestBody User user) {
        Collection<Option> options = userService.getOptionsForUpdate();
//        putPublicAttribute(map, "update", options, MyBeanUtils.copyBean2Map(userService.findById(user.getId())));
        try {
            if (request.getMethod().equals(GET)) {

            } else if (request.getMethod().equals(POST)) {
                checkOptions(options);
//                userService.update(id, userName, email, mobile, realName);
                putAlertTrue(map);
            } else {
                throwUnknowRequestMethodException(request);
            }
        } catch (Throwable t) {
            putAlertMsg(t, map);
        }

        return TEMPLATE;
    }

}
