package com.alibaba.rocketmq.interceptor;

import com.alibaba.rocketmq.domain.sso.gmq.TokenInfo;
import com.alibaba.rocketmq.service.gmq.GMQLoginConfigService;
import com.alibaba.rocketmq.util.base.BaseUtil;
import com.cloudzone.cloudsso.common.util.CookieUtil;
import com.cloudzone.cloudsso.domain.response.VerifyRespData;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.Calendar;


/**
 * @author: tianyuliang
 * @since: 2016/12/20
 */
@Component("loginHandlerInterceptor")
public class LoginHandlerInterceptor implements HandlerInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginHandlerInterceptor.class);

    private final static Integer redirectCode = 302;

    @Autowired
    private GMQLoginConfigService gmqLoginConfigService;

//    @Autowired
//    private GMQTokenService gmqTokenService;


    public LoginHandlerInterceptor() {
    }


    public void init() {
        LOGGER.info("init gmq login interceptor.");
    }


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object o) throws Exception {
//        if (excludeIndexApi(request)) {
//            redirectUrl(request, response, buildUrlParam());
//            return true;
//        }
        String userName = BaseUtil.getDefaultValue(request.getParameter("userName"), "");
        if (BaseUtil.isBlank(userName)){
            LOGGER.warn("userName is empty. login failed.");
            redirectUrl(request, response, "/sso-fail.html");
            return false;
        }

        String sign = BaseUtil.getDefaultValue(request.getParameter("sign"), "");
        String timesmap = BaseUtil.getDefaultValue(request.getParameter("timesmap"), "");
        try {
            TokenInfo ssoToken = new TokenInfo();
            ssoToken.setToken(sign);
            ssoToken.setAppKey(gmqLoginConfigService.getAppKey());
            LOGGER.info("timesmap={}, token={}", timesmap, ssoToken.getToken());

            // gmqTokenService.verifyToken(gmqLoginConfigService.getTokenVerifyUrl(),ssoToken);
            VerifyRespData userData = new VerifyRespData();
            userData.setUserName(userName.trim());
            userData.setUserId("1");
            Long timestamp = Calendar.getInstance().getTime().getTime();
            userData.setCreateTime(timestamp - 2 * 60 * 60);
            userData.setExpireTime(timestamp);

            if (userData == null) {
                LOGGER.info("token verify end, but verifyRespData is empty.");
                redirectUrl(request, response, buildUrlParam());
                return false;
            }
            Long cookieExpireTime = userData.getExpireTime() - userData.getCreateTime();
            CookieUtil.setCookie(response, "token", sign, cookieExpireTime.intValue());
            //CacheUtil.setTokenData(sign, new SignCacheInfo(sign, userData));
            request.getSession().setAttribute("userName", userData.getUserName());
            request.getSession().setAttribute("userId", userData.getUserId());
            request.getSession().setMaxInactiveInterval(gmqLoginConfigService.getMaxInactiveInterval());
            return true;
        }
        catch (Exception e) {
            LOGGER.error("handle verify token error. msg=" + e.getMessage(), e);
            redirectUrl(request, response, "/sso-fail.html");
        }
        return false;
    }


    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object o,
            ModelAndView mv) throws Exception {

    }


    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object o,
            Exception e) throws Exception {

    }


    /***
     * 使用此方逻辑会导致/token.do等类型的接口无法被拦截器处理，暂不使用
     * 
     * @param request
     * @return
     */
    private boolean excludeResource(HttpServletRequest request) {
        String currentURL = request.getRequestURI();
        return matchResource(currentURL);
    }


    private boolean excludeIndexApi(HttpServletRequest request) {
        String currentURL = request.getRequestURI();
        return currentURL.trim().equals(gmqLoginConfigService.getIndexApi());
    }


    private boolean matchResource(String currentURL) {
        if (StringUtils.isNotBlank(gmqLoginConfigService.getStaticResouce())) {
            String staticResouce[] = gmqLoginConfigService.getStaticResouce().trim().split("|");
            for (String resource : staticResouce) {
                if (currentURL.indexOf(resource.trim()) > 0) {
                    return true;
                }
            }
        }
        return false;
    }


    private void redirectUrl(HttpServletRequest request, HttpServletResponse response, String redirectUrl) {
        response.addHeader("location", redirectUrl);
        response.setStatus(redirectCode);
    }


    private String buildUrlParam() throws UnsupportedEncodingException {
        String appLoginUrl = gmqLoginConfigService.getAppLoginUrl();
        String ssoLoginUrl = gmqLoginConfigService.getSsoLoginUrl();
        String appRedirectUrl = URLEncoder.encode(gmqLoginConfigService.getAppRedirectUrl(), "UTF-8");
        return MessageFormat.format("{0}?loginUrl={1}&redirectUrl={2}", appLoginUrl, ssoLoginUrl, appRedirectUrl);
    }

}
