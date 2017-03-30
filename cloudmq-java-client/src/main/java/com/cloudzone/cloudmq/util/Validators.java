package com.cloudzone.cloudmq.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cloudzone.cloudmq.api.open.exception.AuthFailedException;
import com.cloudzone.cloudmq.common.*;

import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import static com.alibaba.fastjson.JSON.parseObject;


/**
 * @author gaoyanlei
 * @since 2017/1/9
 */
public class Validators {
    protected static final long WSADDR_INTERNAL_TIMEOUTMILLS = 3000;
    protected static final int HTTP_STATUS_SUCCESS = 200;
    protected static final String WSADDR_HOST_AND_PORTS = "http:xxx";


    /**
     * 检查参数设置，创建client与auth_key是否匹配
     *
     * @author yintongqiang
     * @since 2017/1/10
     */
    public static AuthKey checkTopicAndAuthKey(Properties properties, ProcessMsgType processMsgType) {
        try {
            String pGroupId = properties.getProperty(PropertiesConst.Keys.ProducerGroupId);
            String cGroupId = properties.getProperty(PropertiesConst.Keys.ConsumerGroupId);
            String topicAndAuthKey = properties.getProperty(PropertiesConst.Keys.TOPIC_NAME_AND_AUTH_KEY);
            String topicMsg = "TOPIC_NAME 不能为空！";
            String authKeyMsg = "AUTH_KEY 不能为空！";
            String pGroupIdMsg = "ProducerGroupId 不能为空！";
            String cGroupIdMsg = "ConsumerGroupId 不能为空！";

            if (processMsgType.getCode() == ProcessMsgType.PRODUCER_MSG.getCode() && UtilAll.isBlank(pGroupId)) {
                throw new AuthFailedException(pGroupIdMsg);
            }
            if (processMsgType.getCode() == ProcessMsgType.CONSUMER_MSG.getCode() && UtilAll.isBlank(cGroupId)) {
                throw new AuthFailedException(cGroupIdMsg);
            }
            ConcurrentHashMap<String, String> topicAndAuthKeyMap = new ConcurrentHashMap<>();
            List<String> topicList = new ArrayList<>();
            String ipAndPort = null;
            for (String topicAuthKey : topicAndAuthKey.split(";")) {
                String topic = topicAuthKey.split(":", 2)[0];
                String authKey = topicAuthKey.split(":", 2)[1];
                if (UtilAll.isBlank(topic)) {
                    throw new AuthFailedException(topicMsg);
                }
                if (UtilAll.isBlank(authKey)) {
                    throw new AuthFailedException(authKeyMsg);
                }
                AuthKey aKey = verifyTopicAndAuthKey(topic, authKey);
                if (null == ipAndPort) {
                    ipAndPort = aKey.getIpAndPort();
                } else {
                    if (!ipAndPort.equals(aKey.getIpAndPort())) {
                        throw new AuthFailedException("申请的topic不在同一个环境，请联系管理员！");
                    }
                }
                topicAndAuthKeyMap.put(topic, authKey);
                topicList.add(topic);

            }
            return new AuthKey(ipAndPort, new TopicAndAuthKey(topicAndAuthKeyMap, topicList.toArray(new String[topicList.size()]), processMsgType));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 认证
     *
     * @author yintongjiang
     * @since 2017/3/23
     */
    private static AuthKey verifyTopicAndAuthKey(String topic, String authKey)
            throws Exception {
        String authMsg = "认证失败，请检查是否正确！";
        // 未开通远程验证接口
        JSONObject verifyObject = new JSONObject();
        verifyObject.put(PropertiesConst.Keys.TOPIC_NAME, topic);
        verifyObject.put(PropertiesConst.Keys.AUTH_KEY, authKey);
        verifyObject.put(PropertiesConst.Keys.TYPE, PropertiesConst.Values.CLOUDMQ);

        String authUrl = UtilAll.getValue4Properties(PropertiesConst.Keys.PROPERTIES_PATH,
                PropertiesConst.Keys.WSADDR_AUTH);
        final URL url = new URL(authUrl);
        checkIpPortIsOpen(new ArrayList<String>() {{
            add(url.getHost() + ":" + url.getPort());
        }});
        // 远程验证
        HttpTinyClient.HttpResult result =
                HttpTinyClient.httpPost(authUrl, null, verifyObject, "UTF-8", WSADDR_INTERNAL_TIMEOUTMILLS);
        if (result == null || result.code != HTTP_STATUS_SUCCESS) {
            throw new AuthFailedException(authMsg);
        }

        // 验证是否成功
        String responseStr = UtilAll.clearNewLine(result.content);
        if (!UtilAll.isBlank(responseStr)) {
            ResultContent resultContent = parseObject(responseStr, ResultContent.class);
            if (HttpURLConnection.HTTP_OK != resultContent.getCode() || null == resultContent.body) {
                throw new AuthFailedException(topic + ":" + authKey + "," + authMsg);
            } else {
                JSONObject jsonObject = JSON.parseObject(resultContent.getBody());
                //多个地址用|隔开需约定好
                String[] ipPortArray = jsonObject.getString("ipAndPort").split("\\|");
                for (String ipPort : ipPortArray) {
                    checkIpPortIsOpen(java.util.Arrays.asList(ipPort.split(";")));
                }
                return new AuthKey(authKey, ipPortArray[0]);
            }
        }
        return null;
    }

    /**
     * 检查ip和端口是否打开
     *
     * @author yintongjiang
     * @since 2017/3/23
     */
    private static void checkIpPortIsOpen(List<String> ipPortList) throws Exception {
        List<String> failedList = new ArrayList<>();
        for (String ipPort : ipPortList) {
            Socket client;
            String host = ipPort.split(":")[0];
            int port = Integer.valueOf(ipPort.split(":")[1]);
            try {
                client = new Socket(host, port);
                client.close();
            } catch (Exception e) {
                e.printStackTrace();
                failedList.add(ipPort);

            }
        }
        if (!failedList.isEmpty()) {
            throw new Exception("当前机器连通 [" + org.apache.commons.lang.StringUtils.join(failedList.toArray(), ",")
                    + "] 异常，请检测IP，端口是否开通网络权限!!!\n");
        }
    }

    // TODO: 2017/3/30 还需做TPS的统计
    public static void checkTopic(Properties properties, String topic) {
        TopicAndAuthKey topicAndAuthKey = (TopicAndAuthKey) properties.get(PropertiesConst.Keys.TopicAndAuthKey);
        if (!topicAndAuthKey.getTopicAuthKeyMap().containsKey(topic)) {
            throw new AuthFailedException("申请的topic和" + topicAndAuthKey.getProcessMsgType().getDes() + "的topic不匹配,申请的topic为[" + topicAndAuthKey.topicArrayToString() + "]," + topicAndAuthKey.getProcessMsgType().getDes() + "的topic为[" + topic + "]");
        }
    }
}
