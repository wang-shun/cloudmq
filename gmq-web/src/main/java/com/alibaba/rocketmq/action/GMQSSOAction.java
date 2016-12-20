package com.alibaba.rocketmq.action;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.MessageFormat;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.alibaba.fastjson.JSON;
import com.alibaba.rocketmq.domain.sso.gmq.TokenInfo;
import com.alibaba.rocketmq.service.sso.GMQTokenService;
import com.alibaba.rocketmq.util.base.JsonUtil;
import com.gome.sso.client.service.GomeSSOService;
import com.gome.sso.domain.constant.RespCode;
import com.gome.sso.domain.response.RespData;
import com.gome.sso.domain.response.VerifyRespData;


/**
 * @author: tianyuliang
 * @since: 2016/11/23
 */
@Controller
@RequestMapping("/sso")
public class GMQSSOAction extends AbstractAction {

    static final Logger logger = LoggerFactory.getLogger(GMQSSOAction.class);
    private static final String ContentType = "text/html; charset=utf-8";
    private static final String UTF8 = "utf-8";
    private static final String LOGIN_SUCCESS = "isLoginSuccess";
    private final static String INDEX = "index";

    @Autowired
    GomeSSOService gomeSSOService;

    @Autowired
    GMQTokenService gmqTokenService;

    @Value("#{ssoConfigProperties['app.index.url']}")
    private String appLoginUrl;

    @Value("#{ssoConfigProperties['sso.app.homeUrl']}")
    private String appRedirectUrl;

    @Value("#{ssoConfigProperties['sso.loginUrl']}")
    private String ssoLoginUrl;

    @Value("#{ssoConfigProperties['sso.app.appKey']}")
    private String appKey;

    @Value("#{ssoConfigProperties['sso.tokenUrl']}")
    private String tokenVerifyUrl;


    /***
     * 调用SSO系统token过期删除接口
     *
     * @param param
     * @return
     */
    @RequestMapping("/exit.do")
    public RespData exit(HttpServletRequest request, HttpServletResponse response,
            @RequestParam(required = true) String param) {
        String token = JSON.parseObject(param).getString("token");
        try {
            gomeSSOService.removeToken(token);
            logger.info("sso exit success. token={}", token);
            return new RespData(RespCode.RESP_SUCCESS.getCode(), RespCode.RESP_SUCCESS.getMsg());
        }
        catch (Exception e) {
            logger.error("sso exit error. token={}", token, e);
            return new RespData(RespCode.SYSERROR.getCode(), RespCode.SYSERROR.getMsg());
        }
    }


    /***
     * 调用SSO系统退出接口
     */
    @RequestMapping("/logout.do")
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        try {
            String appKey = gomeSSOService.getAppKey();
            String ssoLogOutUrl = gomeSSOService.getSsoLogOutUrl();
            String redirectUrl = URLEncoder.encode(gomeSSOService.getAppHomeUrl(), "UTF-8");
            String logoutUrl =
                    MessageFormat.format("{0}?redirectUrl={1}&appKey={2}", ssoLogOutUrl, redirectUrl, appKey);
            response.sendRedirect(logoutUrl);
        }
        catch (IOException e) {
            logger.error("sso logout error.", e);
            // TODO:调整到失败页面？提示用户？
        }
    }


    @RequestMapping(value = "/index.do")
    public void index(HttpServletRequest request, HttpServletResponse response, ModelMap map) {
        HttpSession session = request.getSession(false);
        String param = null;
        try {
            if (!matchEmptySession(session)) {
                // 匹配cookies 读取用户名
                response.sendRedirect("/cluster/list.do");
                return;
            }
            String redirectUrl = URLEncoder.encode(appRedirectUrl, "UTF-8");
            param = buildUrlParam(redirectUrl);
        }
        catch (Exception e) {
            logger.error("handle index error.", e);
        }
        finally {
            response.addHeader("location", param);
            response.setStatus(302);
        }
    }


    @RequestMapping(value = "/verify/token.do")
    public String verifySign(HttpServletRequest request, HttpServletResponse response, @RequestParam(required = true) ModelMap map) {
        VerifyRespData userData = null;
        try {
            TokenInfo ssoToken = new TokenInfo();
            ssoToken.setToken(map.get("sign").toString().trim());
            ssoToken.setAppKey(appKey);
            logger.info("token=" + ssoToken.getToken());
            userData = gmqTokenService.verifyToken(tokenVerifyUrl, ssoToken);
            return JsonUtil.toJson(userData);
        }
        catch (Exception e) {
            logger.error("handle verify token error. msg={0}", e.getMessage(), e);
            return JsonUtil.toJson(new VerifyRespData());
        }
    }

    public static void main(String[] args) {
        String user_a = "zhangsan";
        String user_b = "wangwu";
        logger.info("#### user={}  ####", user_a, user_b);
    }


    /**
     * 调用SSO系统登陆接口
     * 
     * @param request
     * @param response
     */
    @RequestMapping("/login.do")
    public void login(HttpServletRequest request, HttpServletResponse response) {

    }


    private boolean matchEmptySession(HttpSession session) throws IOException {
        return null == session || null == session.getAttribute(LOGIN_SUCCESS);
    }


    private String buildUrlParam(String redirectUrl) {
        return MessageFormat.format("{0}?loginUrl={1}&redirectUrl={2}", appLoginUrl, ssoLoginUrl,
            redirectUrl);
    }


    @Override
    protected String getFlag() {
        return "sso_flag";
    }


    @Override
    protected String getName() {
        return "sso";
    }
}
