package com.alibaba.rocketmq.util;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author: tianyuliang
 * @since: 2016/7/22
 */
public class BaseUtil {

    static final Logger LOGGER = LoggerFactory.getLogger(BaseUtil.class);

    public static String readMemoryForJVM() {
        Integer one = 1024 * 1024; // 1M=1024*1024 byte
        Long max = Runtime.getRuntime().maxMemory() / one;
        Long free = Runtime.getRuntime().freeMemory() / one;
        Long total = Runtime.getRuntime().totalMemory() / one;
        return String.format("max:%sm,total:%sm,free:%sm,used:%sm", max.toString(), total.toString(), free.toString(), (total - free));
    }

    public static Map<String, Long> getMemory() {
        Map<String, Long> params = Maps.newHashMap();
        Integer one = 1024 * 1024; // 1M=1024*1024 byte
        Long max = Runtime.getRuntime().maxMemory() / one;
        Long free = Runtime.getRuntime().freeMemory() / one;
        Long total = Runtime.getRuntime().totalMemory() / one;
        params.put("max", max);
        params.put("free", free);
        params.put("total", total);
        params.put("used", total - free);
        return params;
    }

    public static void threadSleep(long sleepTime) {
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            LOGGER.error("thread sleep error. sleepTime={}ms", sleepTime, e);
        }
    }

}
