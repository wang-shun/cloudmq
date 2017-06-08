package com.alibaba.rocketmq.util.base;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.rocketmq.domain.sso.response.RespData;
import org.apache.commons.collections.keyvalue.DefaultKeyValue;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;


/**
 * @author: tianyuliang
 * @since: 2016/12/2
 */
@SuppressWarnings("unchecked")
public class JsonUtil {

    private static final Logger logger = LoggerFactory.getLogger(JsonUtil.class);

    private final static String timeFormat = "yyyy-MM-dd HH:mm:ss";

    private final static SerializerFeature[] features =
            { SerializerFeature.WriteDateUseDateFormat, SerializerFeature.WriteMapNullValue,
                    SerializerFeature.WriteNullNumberAsZero, SerializerFeature.WriteNullStringAsEmpty,
                    SerializerFeature.WriteNullBooleanAsFalse, SerializerFeature.WriteNullListAsEmpty };


    public static String toJson(Object obj) {
        return JSON.toJSONStringWithDateFormat(obj, timeFormat, features);
    }


    /**
     * 将JSON字符串转换为JavaBean对象。
     */
    public static <T> T toObject(String jsonText, Class<T> clazz) {
        T result = null;
        try {
            result = JSON.parseObject(unifiedEmptyJSON(jsonText), clazz);
        }
        catch (JSONException e) {
            logger.error("Parse json text to java generics object failed. data = \n" + jsonText, e);
        }
        return result;
    }


    /**
     * 将空白的JSON文本统一为"{}"
     */
    private static String unifiedEmptyJSON(String jsonText) {
        return StringUtils.isEmpty(jsonText) ? "{}" : jsonText.trim();
    }


    /**
     * 将JSON字符串转换为List对象
     */
    public static <T> T toGenericsObject(String jsonText, TypeReference<T> type) {
        T result = null;
        try {
            result = JSON.parseObject(unifiedEmptyJSON(jsonText), type);
        }
        catch (JSONException e) {
            logger.error("Parse json text to java generics object failed. data=\n" + jsonText, e);
        }
        return result;
    }


    /**
     * 将JSON字符串转换为List对象。
     */
    public static <T> List<T> toList(String jsonText, Class<T> itemClazz) {
        List<T> result = null;
        try {
            result = JSON.parseArray(unifiedEmptyJSON(jsonText), itemClazz);
        }
        catch (JSONException e) {
            logger.error("Parse json text to java List object failed. data = \n " + jsonText, e);
        }
        return result;
    }


    /**
     * 获取响应字符串Body节点对应的Bean对象
     *
     * @param respText
     *            响应消息的json字符串
     * @param clazz
     *            响应data节点对应的普通Bean对象
     * @return DefaultKeyValue body节点的Bean对象
     */
    private static <T> DefaultKeyValue getHttpResponseBody(String respText, Class<T> clazz) {
        DefaultKeyValue keyValue = new DefaultKeyValue();
        boolean isResponseOK = false;
        if (StringUtils.isEmpty(respText)) {
            keyValue.setKey(isResponseOK);
            return keyValue;
        }
        RespData respData = null;
        T dataWrapperObject = null;
        try {
            respData = JsonUtil.toGenericsObject(respText, new TypeReference<RespData>() {
            });
            isResponseOK = respData.getCode().equals(0);
            //logger.info("transformed response bean with isResponseOK = " + isResponseOK);

            // TODO: 天坑啊！ responseRoot对象下的data节点是一个map对象，需要再一次转化为具体的实例Bean对象
            Map<String, Object> paramMap = (Map<String, Object>) respData.getData();
            if (paramMap == null || paramMap.isEmpty()) {
                logger.warn("paramMap to dataWrapperObject is empty!");
                keyValue.setKey(isResponseOK);
                keyValue.setValue(dataWrapperObject);
                return keyValue;
            }
            dataWrapperObject = toObject(toJson(paramMap), clazz);
            logger.info("transformed map to clazz<T> successful.");
        }
        catch (NullPointerException e) {
            logger.error("transformed response bean with responseData invalid.", e);
            isResponseOK = false;
        }
        catch (RuntimeException e) {
            logger.error("transformed response bean with RuntimeException.", e);
        }
        catch (Exception e) {
            logger.error("transformed response bean with exception.", e);
            isResponseOK = false;
        }
        keyValue.setKey(isResponseOK);
        keyValue.setValue(dataWrapperObject);
        return keyValue;
    }


    public static <T> T toDataWrapperObject(String respText, Class<T> clazz) {
        T wrapperObject = null;
        DefaultKeyValue keyValue = getHttpResponseBody(respText, clazz);
        if (keyValue != null && keyValue.getKey() != null && (Boolean) keyValue.getKey()) {
            wrapperObject = (T) keyValue.getValue();
        }
        return wrapperObject;
    }

}
