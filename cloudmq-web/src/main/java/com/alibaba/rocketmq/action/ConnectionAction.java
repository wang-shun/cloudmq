package com.alibaba.rocketmq.action;

import java.util.*;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSON;
import com.alibaba.rocketmq.domain.gmq.Cluster;
import com.alibaba.rocketmq.service.gmq.GMQGuestService;
import com.alibaba.rocketmq.service.gmq.GMQTopicService;
import com.alibaba.rocketmq.util.base.BaseUtil;
import com.google.common.collect.Maps;
import org.apache.commons.cli.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.alibaba.rocketmq.common.protocol.body.ConsumerConnection;
import com.alibaba.rocketmq.common.protocol.body.ProducerConnection;
import com.alibaba.rocketmq.service.ConnectionService;
import org.springframework.web.bind.annotation.ResponseBody;


/**
 * 
 * @author yankai913@gmail.com
 * @date 2014-2-16
 */
@Controller
@RequestMapping("/connection")
@SuppressWarnings("unchecked")
public class ConnectionAction extends AbstractAction {

    static final Logger LOGGER = LoggerFactory.getLogger(ConnectionAction.class);

    @Autowired
    private ConnectionService connectionService;
    @Autowired
    private GMQGuestService gmqGuestService;
    @Autowired
    private GMQTopicService gmqTopicService;

    @RequestMapping(value = "/consumerConnection.do")
    public String consumerConnection(ModelMap map,
                                     @RequestParam(required = false) String consumerGroupId,
                                     @RequestParam(required = false) String topic) {
        putPublicAttribute(map, "consumerConnection");
        try {
            Map<String, Object> params = Maps.newHashMap();
            List<String> topics = (ArrayList)gmqTopicService.getTopicList().get("topics");
            params.put("topics", topics);

            if(!BaseUtil.isBlank(topic)){
                params.put("topic", topic.trim());
            }
            if(!BaseUtil.isBlank(consumerGroupId)){
                params.put("consumerGroupId", consumerGroupId.trim());
            }

            if(!BaseUtil.isBlank(consumerGroupId)){
                ConsumerConnection consumerConnection = connectionService.getConsumerConnection(consumerGroupId.trim());
                params.put("consumerConnection", consumerConnection);
            }

            putTable(map, params);
        }
        catch (Throwable t) {
            putAlertMsg(t, map);
        }
        return TEMPLATE;
    }

    @RequestMapping(value = "/producerConnection.do")
    public String producerConnection(ModelMap map, HttpServletRequest request,
                                     @RequestParam(required = false) String producerGroup,
                                     @RequestParam(required = false) String topic) {
        Collection<Option> options = connectionService.getOptionsForGetProducerConnection();
        putPublicAttribute(map, "producerConnection", options, request);
        try {
            if(!producerGroup.isEmpty()){
                checkOptions(options);
                ProducerConnection pc = connectionService.getProducerConnection(producerGroup, topic);
                map.put("pc", pc);
            }
        }
        catch (Throwable e) {
            putAlertMsg(e, map);
        }
        return TEMPLATE;
    }


    @RequestMapping(value = "/queryConsumerGroupId.do", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String queryConsumerGroupId(@RequestParam(required = true) String topic) {
        Map<String, Object> params = Maps.newHashMap();
        List<String> groups = new ArrayList<>();
        try{
            HashSet<String> hs = gmqGuestService.queryTopicConsumeByWho(topic.trim());
            for (String group : hs) {
                groups.add(group);
            }
        } catch(Throwable e){
            LOGGER.error(("query consumerGroupId error. topic=" + topic + ", msg=" + e.getMessage()), e);
            // ingore e;
        }
        params.put("groups", groups);
        params.put("topic", topic);
        return JSON.toJSONString(params);
    }

    @Override
    protected String getFlag() {
        return "connection_flag";
    }

    @Override
    protected String getName() {
        return "Connection";
    }
}
