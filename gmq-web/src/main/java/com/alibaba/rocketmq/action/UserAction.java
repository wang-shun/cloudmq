package com.alibaba.rocketmq.action;

import com.alibaba.rocketmq.domain.User;
import com.alibaba.rocketmq.domain.UserVo;
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

    @RequestMapping(value = "/saveOrUpdate.do", method = {RequestMethod.GET})
    public String AddView(ModelMap map, @RequestParam(required = false) Integer id) {
        putPublicAttribute(map, "add");
        if (id != null) {
            putTable(map, userService.findById(id));
        }
        return TEMPLATE;
    }

    @RequestMapping(value = "/saveOrUpdate1.do", method = {RequestMethod.POST})
    public String save(ModelMap map, @RequestBody UserVo user) {
        try {
            putPublicAttribute(map, "list");
//            if (user.getId() == null) {
//                userService.saveOrUpdate(UserVo);
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return TEMPLATE;
    }

    @RequestMapping(value = "/login.do", method = RequestMethod.GET)
    public String list(@RequestParam(required = false) String userName, @RequestParam(required = false) String password) {
        userService.checkUserTest(userName, password);
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
//        Collection<Option> options = userService.getOptionsForUpdate();
//        putPublicAttribute(map, "update", options, MyBeanUtils.copyBean2Map(userService.findById(user.getId())));
        try {
            if (request.getMethod().equals(GET)) {

            } else if (request.getMethod().equals(POST)) {
//                checkOptions(options);
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
