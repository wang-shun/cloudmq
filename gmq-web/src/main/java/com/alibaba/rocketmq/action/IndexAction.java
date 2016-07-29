package com.alibaba.rocketmq.action;

import com.alibaba.rocketmq.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Created by yintongjiang on 2016/7/29.
 */
@Controller
@RequestMapping("/index")
public class IndexAction extends AbstractAction {
    @Autowired
    UserService userService;

    @Override
    protected String getFlag() {
        return "login_flag";
    }

    @Override
    protected String getName() {
        return "login";
    }

    @RequestMapping(value = "/login.do")
    public String login(ModelMap map, @RequestParam(required = false) String userName,
                        @RequestParam(required = false) String password) {
        putPublicAttribute(map, "login");
        userService.checkUserTest(userName, password);
        return TEMPLATE;
    }
}
