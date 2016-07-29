package com.alibaba.rocketmq.action;

import com.alibaba.rocketmq.domain.system.MemoryInfo;
import com.alibaba.rocketmq.util.BaseUtil;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.alibaba.rocketmq.service.GMQSystemResourceService;

import java.util.List;
import java.util.Map;


/**
 * @author gaoyanlei
 * @since 2016/7/25
 */
@Controller
@RequestMapping("/sysResource")
public class GMQSystemResourceAction extends AbstractAction {

    @Autowired
    private GMQSystemResourceService sysResourceService;

    static final Logger logger = LoggerFactory.getLogger(GMQSystemResourceAction.class);

    // http://10.128.31.109:8080/gome-sigar-SNAPSHOT-1.0/systemResource/all

    // http://10.128.31.109:10020/sigar/systemResource/allStats

    @RequestMapping(value = "/memoryStats.do", method = {RequestMethod.GET, RequestMethod.POST})
    public String memory(ModelMap map, @RequestParam(required = true) String brokerAddr) {
        putPublicAttribute(map, "main");
        try {
            Map<String, Object> params = Maps.newHashMap();
            Map<String, Long> currMemory = BaseUtil.getMemory();
            logger.info("heap memory   {}", BaseUtil.readMemoryForJVM());
            List<String> brokerAddrs = sysResourceService.getBrokerAddrs();
            MemoryInfo memory = sysResourceService.memory(brokerAddr.trim());
            params.put("memory", memory);
            params.put("currMemory", currMemory);
            params.put("brokerAddrs", brokerAddrs);
            putTable(map, params);
        } catch (Throwable t) {
            t.printStackTrace();
            putAlertMsg(t, map);
        }
        return TEMPLATE;
    }

    @RequestMapping(value = "/allStats.do", method = {RequestMethod.GET, RequestMethod.POST})
    public String allStats(ModelMap map, @RequestParam(required = true) String brokerAddr) {
        putPublicAttribute(map, "main");
        try {
            Map<String, Object> params = Maps.newHashMap();
            List<String> brokerAddrs = sysResourceService.getBrokerAddrs();
            Map<String, Object> allStats = sysResourceService.getAllStats(brokerAddr.trim());
            params.put("brokerAddrs", brokerAddrs);
            params.put("allStats", allStats);
            putTable(map, params);
        } catch (Throwable t) {
            t.printStackTrace();
            putAlertMsg(t, map);
        }
        return TEMPLATE;
    }


    @Override
    protected String getFlag() {
        return "sysResource_flag";
    }


    @Override
    protected String getName() {
        return "sysResource";
    }
}
