package com.alibaba.rocketmq.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.alibaba.rocketmq.service.GMQSystemResourceService;


/**
 * @author gaoyanlei
 * @since 2016/7/25
 */
@Controller
@RequestMapping("/SystemResource")
public class GMQSystemResourceAction extends AbstractAction {

    @Autowired
    private GMQSystemResourceService systemResourceService;


    @RequestMapping(value = "/memoryStats.do", method = { RequestMethod.GET, RequestMethod.POST })
    public String memory(ModelMap map,@RequestParam(required = false) String ipAndPort) {
        putPublicAttribute(map, "main");
        try {
            putTable(map, systemResourceService.memory(ipAndPort));
        } catch (Throwable t) {
            t.printStackTrace();
         putAlertMsg(t, map);
        }
        return TEMPLATE;
    }


    @Override
    protected String getFlag() {
        return "systemResource_flag";
    }


    @Override
    protected String getName() {
        return "SystemResource";
    }
}
