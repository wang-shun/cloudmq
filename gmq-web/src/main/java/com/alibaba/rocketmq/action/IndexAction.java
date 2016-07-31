package com.alibaba.rocketmq.action;

import com.alibaba.rocketmq.domain.gmq.User;
import com.alibaba.rocketmq.service.gmq.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Created by yintongjiang on 2016/7/29.
 */
@Controller
@RequestMapping("/gmq")
public class IndexAction extends AbstractAction {
    @Autowired
    private UserService userService;
    private final static String INDEX = "index";

    @Override
    protected String getFlag() {
        return "login_flag";
    }

    @Override
    protected String getName() {
        return "login";
    }

    @RequestMapping(value = "/index.do")
    public String index(ModelMap map) {
        putPublicAttribute(map, "login");
        return INDEX;
    }

    @RequestMapping(value = "/login.do")
    public String login(ModelMap map, HttpServletRequest httpServletRequest, @RequestParam(required = false) String userName,
                        @RequestParam(required = false) String password) {
        User user = userService.loginUser(userName, password);
        if (null != user) {
            HttpSession session = httpServletRequest.getSession();
            session.setAttribute("userId", user.getId());
            session.setAttribute("userName", userName);
            session.setAttribute("isLoginSuccess", true);
            return "redirect:/cluster/list.do";
        } else {
            map.put("errorInfo", "Incorrect username or password");
            putPublicAttribute(map, "login");
            return INDEX;
        }
    }

    @RequestMapping(value = "/logout.do")
    public String logout(ModelMap map, HttpServletRequest httpServletRequest) {
        HttpSession session = httpServletRequest.getSession();
        session.removeAttribute("userName");
        session.removeAttribute("isLoginSuccess");
        putPublicAttribute(map, "login");
        return INDEX;
    }
}
