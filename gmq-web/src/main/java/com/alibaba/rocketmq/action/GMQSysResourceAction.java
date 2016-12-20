package com.alibaba.rocketmq.action;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.rocketmq.domain.system.MemoryInfo;
import com.alibaba.rocketmq.util.base.BaseUtil;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.alibaba.rocketmq.service.gmq.GMQSysResourceService;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;


/**
 * 系统监控
 *
 * @author tianyuliang
 * @since 2016/7/29
 */
@Controller
@RequestMapping("/sysResource")
@SuppressWarnings("unchecked")
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
            List<String> brokerAddrs = getBrokerAddrList("allStats");
            params.put("brokerAddrs", brokerAddrs);

            Map<String, Object> allStats = sysResourceService.getAllStats(brokerAddr.trim());
            params.put("allStats", allStats);
            params.put("currBrokerAddr", brokerAddr.trim());

            Map<String, Object> tps = sysResourceService.queryBrokers(brokerAddr.trim());
            List<Map<String, Object>> inTps = (List<Map<String, Object>>) tps.get("inTps");
            JSONObject inTpsObj = new JSONObject();
            inTpsObj.put("inTps", inTps);

            List<Map<String, Object>> outTps = (List<Map<String, Object>>) tps.get("outTps");
            JSONObject outTpsObj = new JSONObject();
            outTpsObj.put("outTpsObj", outTps);

            params.put("inTps", inTpsObj.toJSONString());
            params.put("outTps", outTpsObj.toJSONString());

            putTable(map, params);
        } catch (Throwable t) {
            logger.error("get all stats error. ", t);
            putAlertMsg(t, map);
        }
        return TEMPLATE;
    }


    @RequestMapping(value = "/main.do", method = {RequestMethod.GET, RequestMethod.POST})
    public String main(ModelMap map) {
        putPublicAttribute(map, "main");
        try {
            List<String> brokerAddrs = getBrokerAddrList("main");
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

    @RequestMapping(value = "queryBrokerTps", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String brokerList(ModelMap map, HttpServletRequest request,
                             @RequestParam(required = true) String brokerAddr,
                             @RequestParam(required = false) String random) {
        try {
            Map<String, Object> params = Maps.newHashMap();
            Map<String, Object> tps = sysResourceService.queryBrokers(brokerAddr.trim());
            List<Map<String, Object>> inTps = (List<Map<String, Object>>) tps.get("inTps");
            JSONObject inTpsObj = new JSONObject();
            inTpsObj.put("inTps", inTps);

            List<Map<String, Object>> outTps = (List<Map<String, Object>>) tps.get("outTps");
            JSONObject outTpsObj = new JSONObject();
            outTpsObj.put("outTpsObj", outTps);

            params.put("inTps", inTpsObj.toJSONString());
            params.put("outTps", outTpsObj.toJSONString());

            JSONObject tpsObj = new JSONObject();
            tpsObj.put("tps", params);
            //logger.info("tps=" + tpsObj.toJSONString());
            return tpsObj.toJSONString();
        } catch (Throwable t) {
            logger.error("query broker tps error. msg={}", t.getMessage(), t);
            return "{\"result\":\"" + t.getMessage() + "\"}";
        }
    }


    /**
     * 最多重复10次获取brokerAddrs列表，以防止UI页面无数据
     *
     * @author tianyuliang
     * @since 2016/8/4
     */
    private List<String> getBrokerAddrList(String methodName) throws Throwable {
        List<String> brokerAddrs = sysResourceService.getBrokerAddrs();
        int repeat = 0;
        while (CollectionUtils.isEmpty(brokerAddrs) && repeat < 10) {
            BaseUtil.threadSleep(20);
            brokerAddrs = sysResourceService.getBrokerAddrs();
            repeat++;
        }
        logger.info("repeat {} times in {}() method to get brokerAddrs {}", repeat, methodName, brokerAddrs);
        return brokerAddrs;
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
