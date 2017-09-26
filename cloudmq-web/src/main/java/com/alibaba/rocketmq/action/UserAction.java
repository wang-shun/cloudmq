package com.alibaba.rocketmq.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.alibaba.rocketmq.service.gmq.GMQUserService;


/**
 * @author gaoyanlei
 * @since 2016/7/12
 */
@Controller
@RequestMapping("/user")
public class UserAction extends AbstractAction {

    @Autowired
    GMQUserService GMQUserService;


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
            putTable(map, GMQUserService.findAll());
        } catch (Throwable t) {
            putAlertMsg(t, map);
        }
        return TEMPLATE;
    }



}
