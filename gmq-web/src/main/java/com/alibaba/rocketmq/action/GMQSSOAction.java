package com.alibaba.rocketmq.action;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.alibaba.rocketmq.service.gmq.GMQLoginConfigService;
import com.gome.sso.common.util.CookieUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.alibaba.fastjson.JSON;
import com.gome.sso.client.service.GomeSSOService;
import com.gome.sso.domain.constant.RespCode;
import com.gome.sso.domain.response.RespData;


/**
 * @author: tianyuliang
 * @since: 2016/11/23
 */
@Controller
@RequestMapping("/sso")
public class GMQSSOAction extends AbstractAction {

    static final Logger logger = LoggerFactory.getLogger(GMQSSOAction.class);

    @Autowired
    private GomeSSOService gomeSSOService;

    @Autowired
    private GMQLoginConfigService gmqLoginConfigService;


    /***
     * 调用SSO系统token过期删除接口
     *
     * @param param
     * @return
     */
    @RequestMapping("/exit.do")
    public RespData exit(HttpServletRequest request, HttpServletResponse response, @RequestParam(required = true) String param) {
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
            String logoutUrl = MessageFormat.format("{0}?redirectUrl={1}&appKey={2}", ssoLogOutUrl, redirectUrl, appKey);
            System.out.println("logoutUrl=" + logoutUrl);

            response.sendRedirect(logoutUrl);
        }
        catch (IOException e) {
            logger.error("sso logout error.", e);
            // TODO:跳转到失败页？提示用户？
        } finally {
            CookieUtil.removeCookie(response, "token");
            request.getSession().removeAttribute("userType");
            request.getSession().invalidate();
        }
    }


    @RequestMapping(value = "/index.do")
    public void index(HttpServletRequest request, HttpServletResponse response, ModelMap map) {
        String param = null;
        try {
            param = buildUrlParam();
        } catch (Exception e) {
            logger.error("build index url param error.", e);
            param = "/index.html";
        }
        response.addHeader("location", param);
        response.setStatus(302);
    }


    @RequestMapping(value = "/token.do", method = { RequestMethod.POST, RequestMethod.GET })
    public void verifySign(HttpServletRequest request, HttpServletResponse response) throws Exception{
        boolean isAdmin = this.handleUserType(request, response);
        String param = isAdmin ? gmqLoginConfigService.getAdminApi() : gmqLoginConfigService.getGuestApi();
        redirectUrl(request, response, param);
    }


    private void redirectUrl(HttpServletRequest request, HttpServletResponse response, String redirectUrl) {
        response.addHeader("location", redirectUrl);
        response.setStatus(302);
    }

    private String buildUrlParam() throws UnsupportedEncodingException {
        String appLoginUrl = gmqLoginConfigService.getAppLoginUrl();
        String ssoLoginUrl = gmqLoginConfigService.getSsoLoginUrl();
        String appRedirectUrl = URLEncoder.encode(gmqLoginConfigService.getAppRedirectUrl(), "UTF-8");
        return MessageFormat.format("{0}?loginUrl={1}&redirectUrl={2}", appLoginUrl, ssoLoginUrl, appRedirectUrl);
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
