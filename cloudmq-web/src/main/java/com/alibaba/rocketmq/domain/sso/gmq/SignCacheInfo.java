package com.alibaba.rocketmq.domain.sso.gmq;

import com.gome.sso.domain.response.VerifyRespData;

import java.io.Serializable;


/**
 * @author: tianyuliang
 * @since: 2016/12/20
 */
public class SignCacheInfo implements Serializable {

    private String key;

    private VerifyRespData signInfo;

    public SignCacheInfo() {

    }

    public SignCacheInfo(String key, String value, String userName, Long createTime, Long expireTime) {
        this.key = key;
        this.signInfo = new VerifyRespData(value, userName, createTime, expireTime);
    }

    public SignCacheInfo(String key, VerifyRespData verifyData) {
        this.key = key;
        this.signInfo = new VerifyRespData(verifyData.getUserId(), verifyData.getUserName(), verifyData.getCreateTime(), verifyData.getExpireTime());
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public VerifyRespData getSignInfo() {
        return signInfo;
    }

    public void setSignInfo(VerifyRespData signInfo) {
        this.signInfo = signInfo;
    }
}
