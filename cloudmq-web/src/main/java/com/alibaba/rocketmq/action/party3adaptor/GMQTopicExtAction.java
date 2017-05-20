package com.alibaba.rocketmq.action.party3adaptor;

import com.alibaba.rocketmq.action.AbstractAction;
import com.alibaba.rocketmq.service.gmq.GMQTopicService;
import com.alibaba.rocketmq.util.base.JsonUtil;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;


/**
 * Topic拓展接口，免登陆，提供给第三方接口调用
 * 
 * @author: tianyuliang
 * @since: 2017/3/3
 */
@Controller
@RequestMapping("/topicExt")
public class GMQTopicExtAction extends AbstractAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(GMQTopicExtAction.class);

    @Autowired
    private GMQTopicService gmqTopicService;



    @RequestMapping(value = "/queryTopic.do")
    @ResponseBody
    public String queryTopic() {
        String resout = "";  
        Map<String, Object> params = Maps.newHashMap();
        try {
            // topics:普通topic队列  retryTopics:重试队列    dlqTopics:死信队列
            params = gmqTopicService.getTopicList();
        }
        catch (Throwable t) {
            params.put("topics", null);
            params.put("retryTopics", null);
            params.put("dlqTopics", null);
            LOGGER.error("query topic error, msg=" + t.getMessage(), t);
        } finally {
            resout = JsonUtil.toJson(params);
        }
        return resout;
    }

    @Override
    protected String getFlag() {
        return "topicExt_flag";
    }

    @Override
    protected String getName() {
        return "topicExt";
    }


}
