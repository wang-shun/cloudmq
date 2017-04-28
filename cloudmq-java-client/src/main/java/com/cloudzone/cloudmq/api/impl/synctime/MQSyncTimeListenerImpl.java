package com.cloudzone.cloudmq.api.impl.synctime;

import com.cloudzone.cloudmq.api.impl.base.MQSyncTimeListener;
import com.cloudzone.cloudmq.common.PropertiesConst;
import com.cloudzone.cloudmq.common.ResultContent;
import com.cloudzone.cloudmq.log.GClientLogger;
import com.cloudzone.cloudmq.util.HttpTinyClient;
import com.cloudzone.cloudmq.util.UtilAll;
import org.slf4j.Logger;

import static com.alibaba.fastjson.JSON.parseObject;

/**
 * @author yintongjiang
 * @params
 * @since 2017/4/25
 */
public class MQSyncTimeListenerImpl implements MQSyncTimeListener {
    private static final Logger log = GClientLogger.getLog();

    @Override
    public void syncTime(long timestamp) {
        try {
            long clientTime = timestamp / 1000;
            String syncTimeUrl = UtilAll.getValue4Properties(PropertiesConst.Keys.PROPERTIES_PATH,
                    PropertiesConst.Keys.SYNCTIME_URL);
            HttpTinyClient.HttpResult result =
                    HttpTinyClient.httpGet(syncTimeUrl, null, null, "UTF-8", 3000);
            if (null != result && result.code == 200 && null != result.content) {
                ResultContent resultContent = parseObject(result.content, ResultContent.class);
                long serverTime = Long.parseLong(resultContent.getBody());
                if (Math.abs(serverTime - clientTime) > 1) {
                    log.error("客户端时间和服务端时间相差{}秒，超过1秒，请修正客户端时间！！！", Math.abs(serverTime - clientTime));
                }
            }
        } catch (Exception e) {
            log.error("###请求同步时间口异常！error={}###", e.getMessage());
        }
    }
}
