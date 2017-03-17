package com.alibaba.rocketmq.service.sso;

import java.net.SocketTimeoutException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.rocketmq.domain.sso.gmq.TokenInfo;
import com.alibaba.rocketmq.service.AbstractService;
import com.alibaba.rocketmq.util.base.HttpUtil;
import com.alibaba.rocketmq.util.base.JsonUtil;
import com.gome.sso.domain.response.VerifyRespData;
import com.google.common.collect.Maps;


/**
 * @author: tianyuliang
 * @since: 2016/12/2
 */
@Service
@SuppressWarnings("unchecked")
public class GMQTokenService extends AbstractService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GMQTokenService.class);


    public VerifyRespData verifyToken(String verifyUrl, TokenInfo ssoToken) throws Exception {
        VerifyRespData verifyRespData = null;
        try {
            Map<String, Object> token = Maps.newHashMap();
            token.put("token", ssoToken.getToken());
            String respData = HttpUtil.send(HttpUtil.MethodType.POST, verifyUrl, JsonUtil.toJson(token), 3000, ssoToken.getAppKey());
            verifyRespData = JsonUtil.toDataWrapperObject(respData, VerifyRespData.class);
        }
        catch (SocketTimeoutException e) {
            LOGGER.error("send http request timeout. url={}", verifyUrl, e);
            // throw e; // ingore e;
        }
        catch (RuntimeException e) {
            LOGGER.error("http request with RuntimeException. url={}", verifyUrl, e);
            // throw e; // ingore e;
        }
        catch (Exception e) {
            LOGGER.error("http request with Exception. url={}", verifyUrl, e);
            // throw e; // ingore e;
        }
        return verifyRespData;
    }

}
