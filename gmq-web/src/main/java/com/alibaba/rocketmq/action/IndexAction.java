package com.alibaba.rocketmq.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


/**
 * @author tianyuliang
 * @since 2016/12/1
 */
@Controller
@RequestMapping("/")
public class IndexAction extends AbstractAction {

    @RequestMapping(value = "/", method = { RequestMethod.GET, RequestMethod.POST })
    public void redirectIndex(HttpServletRequest request, HttpServletResponse response) {
        response.addHeader("location", "/index.html");
        response.setStatus(302);
    }


    @Override
    protected String getFlag() {
        return "index_flag";
    }


    @Override
    protected String getName() {
        return "index";
    }

}
