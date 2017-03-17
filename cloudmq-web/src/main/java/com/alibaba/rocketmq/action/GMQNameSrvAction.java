package com.alibaba.rocketmq.action;

import com.alibaba.rocketmq.service.gmq.GMQNamesrvService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 *
 * @author: tianyuliang
 * @since: 2016/7/19
 */
@Controller
@RequestMapping("/gmq/namesrv")
public class GMQNameSrvAction extends AbstractAction {


    @Autowired
    GMQNamesrvService gmqNamesrvService;


    @RequestMapping(value = "/getNameSrvAddr.do", method = RequestMethod.GET)
    public String getNameSrvAddr(ModelMap map) {
        putPublicAttribute(map, "getNameSrvAddr");
        try {
            String addr = gmqNamesrvService.getGMQNamesrvAddr();
            putTable(map, addr);
        }
        catch (Throwable t) {
            putAlertMsg(t, map);
        }
        return TEMPLATE;
    }


    @Override
    protected String getFlag() {
        return "namesrv_flag";
    }


    @Override
    protected String getName() {
        return "Namesrv";
    }

}
