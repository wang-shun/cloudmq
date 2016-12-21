package com.alibaba.rocketmq.util.base;

import com.alibaba.rocketmq.domain.sso.gmq.SignCacheInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: tianyuliang
 * @since: 2016/12/20
 */
public class CacheUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(CacheUtil.class);

    private static ConcurrentHashMap<String, SignCacheInfo> tokenMap = new ConcurrentHashMap();

    public CacheUtil() {
    }

    public static void setTokenData(String key, SignCacheInfo cacheInfo) {
        LOGGER.info("set cache token. sign={}", key);
        tokenMap.put(key, cacheInfo);
    }

    public static void setTokenData(String key, String value, String userName, Long createTime, Long expireTime) {
        LOGGER.info("set cache token. value={}", value);
        tokenMap.put(key, new SignCacheInfo(key, value, userName, createTime, expireTime));
    }

    public static void removeTokenData(SignCacheInfo cacheInfo) {
        LOGGER.info("remove cache token. value={}", cacheInfo.getKey());
        tokenMap.remove(cacheInfo.getKey());
    }

    public static void removeTokenData(String key) {
        LOGGER.info("remove cache token. value={}", key);
        tokenMap.remove(key);
    }

    public static SignCacheInfo getTokenData(SignCacheInfo cacheInfo) {
        return (SignCacheInfo) tokenMap.get(cacheInfo.getKey());
    }

    public static SignCacheInfo getTokenData(String key) {
        return (SignCacheInfo) tokenMap.get(key);
    }

    public static boolean contains(String key) {
        return tokenMap.containsKey(key);
    }

    public static boolean contains(SignCacheInfo cacheInfo) {
        return tokenMap.containsKey(cacheInfo.getKey());
    }

    public static ConcurrentHashMap<String, SignCacheInfo> getTokenMap() {
        return tokenMap;
    }
}
