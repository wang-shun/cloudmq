package com.alibaba.rocketmq.action;

import com.alibaba.rocketmq.common.Table;
import com.alibaba.rocketmq.service.MessageService;
import com.alibaba.rocketmq.util.base.BaseUtil;
import com.alibaba.rocketmq.util.base.JsonUtil;
import com.google.common.collect.Maps;
import org.apache.commons.cli.Option;
import org.apache.commons.collections.map.LinkedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Map;

/**
 *
 * @author yankai913@gmail.com
 * @date 2014-2-17
 */
@Controller
@RequestMapping("/msg")
public class MessageAction extends AbstractAction {

    static final Logger LOGGER = LoggerFactory.getLogger(MessageAction.class);

    @Autowired
    MessageService messageService;


    protected String getFlag() {
        return "message_flag";
    }


    @RequestMapping(value = "/queryMsgById.do", method = { RequestMethod.GET, RequestMethod.POST })
    public String queryMsgById(ModelMap map, HttpServletRequest request,
                               @RequestParam(required = false) String msgId) {
        Collection<Option> options = messageService.getOptionsForQueryMsgById();
        //Bug fix.
        putPublicAttribute(map, "queryMsgById", options, request);
        try {
            if (request.getMethod().equals(GET)) {

            }
            else if (request.getMethod().equals(POST)) {
                checkOptions(options);
                Table table = messageService.queryMsgById(msgId);
                putTable(map, table);
            }
            else {
                throwUnknowRequestMethodException(request);
            }
        }
        catch (Throwable t) {
            putAlertMsg(t, map);
        }
        return TEMPLATE;
    }


    @RequestMapping(value = "/queryMsgDetail.do")
    public String queryMsgDetail(ModelMap map, @RequestParam(required = false) String msgId) {
        putPublicAttribute(map, "queryMsgDetail");
        try {
            Map<String, Object> param = new LinkedMap();
            if(!BaseUtil.isBlank(msgId)){
                param =  messageService.queryMsgDetail(msgId.trim());
            }
            putTable(map, param);
        }
        catch (Throwable t) {
            putAlertMsg(t, map);
        }
        return TEMPLATE;
    }


    @RequestMapping(value = "/queryMsgBody.do")
    @ResponseBody
    public String queryMsgBody(HttpServletResponse response, @RequestParam(required = true) String msgId) {
        Map<String, Object> param = Maps.newHashMap();
        String msgBody = "";
        try {
            msgBody = messageService.queryMsgBody(msgId.trim());
        }
        catch (Exception e) {
            LOGGER.error(MessageFormat.format("query msg body error. msgId={0}, msg={1}", msgId, e.getMessage()), e);
        } finally {
            // response.reset();
            // response.setContentType("application/json;charset=UTF-8");
            //TODO：2017/3/8 使用velocity模板后，输出的json报文如果含有中文则会乱码，因此改用byte[]，然后由js执行Base64解码 Add by tianyuliang
            param.put("msgBody", msgBody.getBytes());
            return JsonUtil.toJson(param);
        }
    }

    @RequestMapping(value = "/queryMsgByKey.do", method = { RequestMethod.GET, RequestMethod.POST })
    public String queryMsgByKey(ModelMap map, HttpServletRequest request,
                                @RequestParam(required = false) String topic, @RequestParam(required = false) String msgKey,
                                @RequestParam(required = false) String fallbackHours) {
        Collection<Option> options = messageService.getOptionsForQueryMsgByKey();
        putPublicAttribute(map, "queryMsgByKey", options, request);
        try {
            if (request.getMethod().equals(GET)) {

            }
            else if (request.getMethod().equals(POST)) {
                checkOptions(options);
                Table table = messageService.queryMsgByKey(topic, msgKey, fallbackHours);
                putTable(map, table);
            }
            else {
                throwUnknowRequestMethodException(request);
            }
        }
        catch (Throwable t) {
            putAlertMsg(t, map);
        }
        return TEMPLATE;
    }


    @RequestMapping(value = "/queryMsgByOffset.do", method = { RequestMethod.GET, RequestMethod.POST })
    public String queryMsgByOffset(ModelMap map, HttpServletRequest request,
                                   @RequestParam(required = false) String topic, @RequestParam(required = false) String brokerName,
                                   @RequestParam(required = false) String queueId, @RequestParam(required = false) String offset) {
        Collection<Option> options = messageService.getOptionsForQueryMsgByOffset();
        putPublicAttribute(map, "queryMsgByOffset", options, request);
        try {
            if (request.getMethod().equals(GET)) {

            }
            else if (request.getMethod().equals(POST)) {
                checkOptions(options);
                Table table = messageService.queryMsgByOffset(topic, brokerName, queueId, offset);
                putTable(map, table);
            }
            else {
                throwUnknowRequestMethodException(request);
            }
        }
        catch (Throwable t) {
            putAlertMsg(t, map);
        }
        return TEMPLATE;
    }


    @Override
    protected String getName() {
        return "Message";
    }
}
