package com.alibaba.rocketmq.action;

import com.alibaba.rocketmq.domain.system.MemoryInfo;
import com.alibaba.rocketmq.util.BaseUtil;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.alibaba.rocketmq.service.GMQSysResourceService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;


/**
 * 系统监控
 * @author tianyuliang
 * @since 2016/7/29
 */
@Controller
@RequestMapping("/sysResource")
public class GMQSysResourceAction extends AbstractAction {

    @Autowired
    private GMQSysResourceService sysResourceService;

    static final Logger logger = LoggerFactory.getLogger(GMQSysResourceAction.class);

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
            logger.error("get memory stats error. ", t);
            putAlertMsg(t, map);
        }
        return TEMPLATE;
    }

    @RequestMapping(value = "/allStats.do", method = {RequestMethod.GET, RequestMethod.POST})
    public String allStats(ModelMap map, HttpServletRequest request, @RequestParam(required = true) String brokerAddr) {
        putPublicAttribute(map, "list");
        try {
            Map<String, Object> params = Maps.newHashMap();
            List<String> brokerAddrs = sysResourceService.getBrokerAddrs();
            params.put("brokerAddrs", brokerAddrs);
            logger.info("broker ip list: {}", StringUtils.join(brokerAddrs, ","));

            Map<String, Object> allStats = sysResourceService.getAllStats(brokerAddr.trim());
            params.put("allStats", allStats);
            params.put("currBrokerAddr", brokerAddr.trim());
            putTable(map, params);
        } catch (Throwable t) {
            logger.error("get all stats error. ", t);
            putAlertMsg(t, map);
        }
        return TEMPLATE;
    }


    @RequestMapping(value = "/main.do", method = {RequestMethod.GET, RequestMethod.POST})
    public String main(ModelMap map, HttpServletRequest request) {
        putPublicAttribute(map, "main");
        try {
            List<String> brokerAddrs = sysResourceService.getBrokerAddrs();
            Map<String, Object> navigation = Maps.newHashMap();
            String firstBroker = CollectionUtils.isNotEmpty(brokerAddrs) ? brokerAddrs.get(0) : "";
            navigation.put("firstBroker", firstBroker);
            putNavigation(map, navigation);
        } catch (Throwable t) {
            logger.error("get main first broker error. ", t);
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
