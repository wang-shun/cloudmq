package com.cloudzone.cloudmq.api.impl.heartbeat;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cloudzone.cloudmq.api.impl.base.MQHeartbeatListener;
import com.cloudzone.cloudmq.api.open.exception.GomeClientException;
import com.cloudzone.cloudmq.common.HeartbeatData;
import com.cloudzone.cloudmq.common.PropertiesConst;
import com.cloudzone.cloudmq.common.ResultContent;
import com.cloudzone.cloudmq.log.GClientLogger;
import com.cloudzone.cloudmq.util.HttpTinyClient;
import com.cloudzone.cloudmq.util.UtilAll;
import org.slf4j.Logger;

import java.io.IOException;

import static com.alibaba.fastjson.JSON.parseObject;

/**
 * @author yintongqiang
 * @params 从cloudzone定时获取信息
 * @since 2017/4/12
 */
public class MQHeartbeatListenerImpl implements MQHeartbeatListener {
    private static final Logger log = GClientLogger.getLog();

    @Override
    public HeartbeatData doHeartbeat(String topic, String authKey) {
        try {
            JSONObject verifyObject = new JSONObject();
            verifyObject.put(PropertiesConst.Keys.TYPE, PropertiesConst.Values.CLOUDMQ);
            verifyObject.put(PropertiesConst.Keys.TOPIC_NAME, topic);
            verifyObject.put(PropertiesConst.Keys.AUTH_KEY, authKey);
            String heartbeatUrl = UtilAll.getValue4Properties(PropertiesConst.Keys.PROPERTIES_PATH,
                    PropertiesConst.Keys.HEARTBEAT_URL);
            HttpTinyClient.HttpResult result =
                    HttpTinyClient.httpPost(heartbeatUrl, null, verifyObject, "UTF-8", 3000);
            if (null != result && result.code == 200 && null != result.content) {
                ResultContent resultContent = parseObject(result.content, ResultContent.class);
                if (null != resultContent.getBody()) {
                    return JSON.parseObject(resultContent.getBody(), HeartbeatData.class);
                } else {
                    log.error("###请求心跳接口异常！error={}###", resultContent.getMsg());
                }
            }
        } catch (Exception e) {
            log.error("###请求心跳接口异常！error={}###", e.getMessage());
        }
        return null;
    }
}
