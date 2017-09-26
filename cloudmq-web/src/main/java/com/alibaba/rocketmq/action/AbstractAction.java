package com.alibaba.rocketmq.action;

import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.cli.Option;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;

import com.alibaba.rocketmq.common.Table;
import com.alibaba.rocketmq.domain.sso.gmq.GmqUser;
import com.alibaba.rocketmq.domain.sso.gmq.TokenInfo;
import com.alibaba.rocketmq.service.gmq.GMQLoginConfigService;
import com.alibaba.rocketmq.service.gmq.GMQUserService;
import com.alibaba.rocketmq.util.base.BaseUtil;
import com.cloudzone.cloudsso.common.util.CookieUtil;
import com.cloudzone.cloudsso.domain.response.VerifyRespData;


/**
 * @author yankai913@gmail.com
 * @date 2014-2-17
 */
public abstract class AbstractAction {

    protected abstract String getFlag();

    protected abstract String getName();

    public static final String TITLE = "title";

    public static final String BODY_PAGE = "bodyPage";

    public static final String FORM_ACTION = "action";

    public static final String KEY_TABLE = "table";

    public static final String TBODY_DATA = "tbodyData";

    public static final String TEMPLATE = "template";

    public static final String POST = "POST";

    public static final String GET = "GET";

    @SuppressWarnings("unused")
    public static final String DELETE = "DELETE";

    // 新增导航栏类型 2016/7/29 Add by tianyuliang
    public static final String NAVIGATION = "navigation";

    // 新增类型 2016/7/15 Add by gaoyanlei
    public static final String OPTIONS = "options";


    private final static Integer redirectCode = 302;

    @Autowired
    private GMQUserService gmqUserService;

    @Autowired
    protected GMQLoginConfigService gmqLoginConfigService;

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAction.class);


    protected GmqUser handleSessionUser(HttpServletRequest request, HttpServletResponse response) {
        GmqUser gmqUser = null;
        Boolean handleResult = handleDefaultRequest(request, response);
        if (!handleResult) {
            return null;
        }

        String userName = (String)request.getSession().getAttribute("userName");
        if (BaseUtil.isBlank(userName)){
            return gmqUser;
        }

        gmqUser = gmqUserService.queryAdminUser(userName);
        String userType = gmqUser != null ? gmqUser.getUserType() : "0";
        request.getSession().setAttribute("userType", userType);
        return gmqUser;
    }

    protected boolean handleUserType(HttpServletRequest request, HttpServletResponse response) {
        GmqUser gmqUser = this.handleSessionUser(request, response);
        boolean isAdmin = gmqUser != null && gmqUser.getUserType().equals("1"); // 1:管理员  0:普通用户

        return isAdmin;
    }

    private boolean handleDefaultRequest(HttpServletRequest request, HttpServletResponse response){
        String userName = BaseUtil.getDefaultValue(request.getParameter("userName"), "");
        if (BaseUtil.isBlank(userName)){
            LOGGER.warn("userName is empty, self is guest.");
            //TODO: redirectUrl(request, response, "/sso-fail.html");
            return false;
        }

        String sign = BaseUtil.getDefaultValue(request.getParameter("sign"), "");
        String timesmap = BaseUtil.getDefaultValue(request.getParameter("timesmap"), "");
        try {
            TokenInfo ssoToken = new TokenInfo();
            ssoToken.setToken(sign);
            ssoToken.setAppKey(gmqLoginConfigService.getAppKey());
            if (!BaseUtil.isBlank(sign)){
                LOGGER.info("login timesmap={}, token={}", timesmap, ssoToken.getToken());
            }

            //TODO: gmqTokenService.verifyToken(gmqLoginConfigService.getTokenVerifyUrl(),ssoToken);
            VerifyRespData userData = new VerifyRespData();
            userData.setUserName(userName.trim());
            userData.setUserId("1");
            Long timestamp = Calendar.getInstance().getTime().getTime();
            userData.setCreateTime(timestamp - 2 * 60 * 60);
            userData.setExpireTime(timestamp);

            Long cookieExpireTime = userData.getExpireTime() - userData.getCreateTime();
            CookieUtil.setCookie(response, "token", sign, cookieExpireTime.intValue());
            //TODO: CacheUtil.setTokenData(sign, new SignCacheInfo(sign, userData));
            request.getSession().setAttribute("userName", userData.getUserName());
            request.getSession().setAttribute("userId", userData.getUserId());
            request.getSession().setMaxInactiveInterval(gmqLoginConfigService.getMaxInactiveInterval());
            return true;
        }
        catch (Exception e) {
            LOGGER.error("handle verify token error. msg={}", e.getMessage(), e);
            //TODO: redirectUrl(request, response, "/sso-fail.html");
        }
        return false;
    }

    public void redirectUrl(HttpServletRequest request, HttpServletResponse response, String redirectUrl) {
        response.addHeader("location", redirectUrl);
        response.setStatus(redirectCode);
    }


    // 新增 2016/7/15 Add by gaoyanlei
    protected void putTable(ModelMap map, Object object) {
        map.put(TBODY_DATA, object);
    }

    // 新增导航栏额外参数 2016/7/29 Add by tianyuliang
    protected void putNavigation(ModelMap map, Object object) {
        map.put(NAVIGATION, object);
    }

    protected void putTable(ModelMap map, Table table) {
        map.put(KEY_TABLE, table);
    }

    protected void putPublicAttribute(ModelMap map, String title, Collection<Option> options, HttpServletRequest request) {
        putPublicAttribute(map, title, options);
        @SuppressWarnings("unchecked")
        Enumeration<String> enumer = request.getParameterNames();
        while (enumer.hasMoreElements()) {
            String key = enumer.nextElement();
            String value = request.getParameter(key);
            addOptionValue(options, key, value);
        }
    }

    protected void putPublicAttribute(ModelMap map, String title, Collection<Option> options, Map object) {
        putPublicAttribute(map, title, options);
        @SuppressWarnings("unchecked")
        Iterator entries = object.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry entry = (Map.Entry) entries.next();
            String key = (String) entry.getKey();
            Object value = entry.getValue();
            addOptionValue(options, key, value);
        }
    }


    protected void putPublicAttribute(ModelMap map, String title, Collection<Option> options) {
        putPublicAttribute(map, title);
        putOptions(map, options);
    }


    protected void putOptions(ModelMap map, Collection<Option> options) {
        map.put(OPTIONS, options);
    }


    protected void putPublicAttribute(ModelMap map, String title) {
        map.put(getFlag(), "active");
        map.put(TITLE, getName() + ":" + title);
        map.put(BODY_PAGE, getName().toLowerCase() + "/" + title + ".vm");
        map.put(FORM_ACTION, title + ".do");
    }


    @SuppressWarnings("unchecked")
    protected void addOptionValue(Collection<Option> options, String key, Object value) {
        if (value == null) {
            return;
        }
        if (value instanceof String) {
            String tempVal = (String) value;
            if (StringUtils.isBlank(tempVal)) {
                return;
            }
        }
        for (Option opt : options) {
            if (opt.getLongOpt().equals(key)) {
                opt.getValuesList().add(value);
            }
        }
    }


    protected void checkOptions(Collection<Option> options) {
        for (Option option : options) {
            if (option.isRequired()) {
                String value = option.getValue();
                if (StringUtils.isBlank(value)) {
                    throw new IllegalStateException("option: key =[" + option.getLongOpt() + "], required=["
                            + option.isRequired() + "] is blank!");
                }
            }
        }
    }

    public static final String ALERT_MSG = "alertMsg";


    protected void putAlertMsg(Throwable t, ModelMap map) {
        map.put(ALERT_MSG, t.getMessage());
    }

    public static final String ALERT_TRUE = "alertTrue";


    protected void putAlertTrue(ModelMap map) {
        map.put(ALERT_TRUE, true);
    }

    protected void throwUnknowRequestMethodException(HttpServletRequest request) {
        throw new IllegalStateException("unknown request method: " + request.getMethod());
    }
}
