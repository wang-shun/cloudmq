package com.alibaba.rocketmq.filter;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 用户过滤器
 * @author: tianyuliang
 * @since: 2016/12/1
 */
public class UserFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserFilter.class);
    private static final String ContentType = "text/html; charset=utf-8";
    private static final String UTF8 = "utf-8";
    private static final String LOGIN_SUCCESS = "isLoginSuccess";

    private String loginUrl;
    private String[] testApi;
    private String[] loginApi;
    private String[] staticResouce;


    @Override
    public void init(FilterConfig config) throws ServletException {
        LOGGER.info("init the ResourceFilter.");
        String apiCfg = config.getInitParameter("testApi");
        String loginCfg = config.getInitParameter("logintApi");
        String resourceCfg = config.getInitParameter("staticResouce");
        String indexCfg = config.getInitParameter("indexApi");
        testApi = StringUtils.isNotBlank(apiCfg) ? apiCfg.trim().split(",") : null;
        loginApi = StringUtils.isNotBlank(loginCfg) ? loginCfg.trim().split(",") : null;
        staticResouce = StringUtils.isNotBlank(resourceCfg) ? resourceCfg.trim().split("|") : null;
        loginUrl = "<script language='javascript'>window.location.href='" + indexCfg + "'</script>";
    }


    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        request.setCharacterEncoding(UTF8);
        HttpServletResponse response = (HttpServletResponse) resp;
        response.setCharacterEncoding(UTF8);
        response.setContentType(ContentType);
        String currentURL = request.getRequestURI(); // 取得根目录所对应的绝对路径:
        // String targetURL = currentURL.substring(currentURL.indexOf("/", 1));
        // // 截取到当前文件名用于比较

        // 2016/7/29 不处理“被排除”的URL Add by yintongjiang
        if (matchTestApi(currentURL) || matchLoginApi(currentURL) || matchResource(currentURL)) {
            chain.doFilter(request, response);
            return;
        }


        chain.doFilter(request, response);

       /* // 权限控制
        HttpSession session = request.getSession(false);
        if (matchEmptySession(session)) {
            handleEmptySession(response);
            return;
        }
        chain.doFilter(request, response); */
    }


    @Override
    public void destroy() {
        LOGGER.info("destroied the ResourceFilter.");
    }


    private boolean matchTestApi(String currentURL) {
        if (null != testApi) {
            for (String api : testApi) {
                if (currentURL.startsWith(api.trim())) {
                    return true;
                }
            }
        }
        return false;
    }


    private boolean matchLoginApi(String currentURL) {
        if (null != loginApi) {
            for (String api : loginApi) {
                // TODO: 2016/7/29 tomcat和jetty路径不一样, index.do login.do
                if (currentURL.indexOf(api.trim()) > 0) {
                    return true;
                }
            }
        }
        return false;
    }


    private boolean matchResource(String currentURL) {
        if (null != staticResouce) {
            for (String resource : staticResouce) {
                if (currentURL.indexOf(resource.trim()) > 0) {
                    return true;
                }
            }
        }
        return false;
    }


    private boolean matchEmptySession(HttpSession session) throws IOException {
        return null == session || null == session.getAttribute(LOGIN_SUCCESS);
    }


    private void handleEmptySession(HttpServletResponse response) throws IOException {
        response.reset();
        response.setContentType(ContentType);
        PrintWriter pw = response.getWriter();
        pw.write(loginUrl);
        pw.flush();
        pw.close();
    }


}