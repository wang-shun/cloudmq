package com.alibaba.rocketmq.action;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.rocketmq.domain.sso.gmq.GmqUser;
import com.alibaba.rocketmq.service.gmq.GMQUserService;
import org.apache.commons.cli.Option;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;

import com.alibaba.rocketmq.common.Table;


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


    @Autowired
    private GMQUserService gmqUserService;

    protected GmqUser handleSessionUser(HttpServletRequest request, HttpServletResponse response) {
        GmqUser gmqUser = null;
        String userName = (String)request.getSession().getAttribute("userName");
        if(StringUtils.isEmpty(userName)){
            return gmqUser;
        }

        String userType = "0";
        try{
            gmqUser = gmqUserService.queryAdminUser(userName);
            userType = gmqUser != null ? gmqUser.getUserType() : "0";
        } catch(SQLException e){
           // ingore e
        }
        request.getSession().setAttribute("userType", userType);
        return gmqUser;
    }

    protected boolean handleUserType(HttpServletRequest request, HttpServletResponse response) {
        GmqUser gmqUser = this.handleSessionUser(request, response);
        boolean isAdmin = gmqUser != null && gmqUser.getUserType().equals("1"); // 1:管理员  0:普通用户
        return isAdmin;
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
