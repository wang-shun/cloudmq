package com.alibaba.rocketmq.action;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;

/**
 * @author: tianyuliang
 * @since: 2016/7/25
 */
@Controller
@RequestMapping("/gmq/other")
public class GMQFAQAction extends AbstractAction {


    @RequestMapping(value = "/faq.do", method = RequestMethod.GET)
    public String faq(ModelMap map, HttpServletRequest request) {
        putPublicAttribute(map, "faq");
        putTable(map, null);
        return TEMPLATE;
    }


    @Override
    protected String getFlag() {
        return "faq_flag";
    }

    @Override
    protected String getName() {
        return "other";
    }
}
