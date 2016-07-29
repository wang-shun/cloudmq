package com.alibaba.rocketmq.util;

import com.google.common.collect.Maps;
import java.util.Map;

/**
 * @author: tianyuliang
 * @since: 2016/7/22
 */
public class BaseUtil {


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
}
