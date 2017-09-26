package com.alibaba.rocketmq.service.gmq;

/**
 * @author: tianyuliang
 * @since: 2016/12/20
 */
public class GMQLoginConfigService {

    private String indexApi;

    private String loginApi;

    private String staticResouce;

    private String adminApi;

    private String guestApi;

    private String appLoginUrl;

    private String appRedirectUrl;

    private String ssoLoginUrl;

    private String appKey;

    private String tokenVerifyUrl;

    private Integer maxInactiveInterval;

    public String getAdminApi() {
        return adminApi;
    }

    public void setAdminApi(String adminApi) {
        this.adminApi = adminApi;
    }

    public String getGuestApi() {
        return guestApi;
    }

    public void setGuestApi(String guestApi) {
        this.guestApi = guestApi;
    }

    public String getIndexApi() {
        return indexApi;
    }


    public void setIndexApi(String indexApi) {
        this.indexApi = indexApi;
    }


    public String getLoginApi() {
        return loginApi;
    }


    public void setLoginApi(String loginApi) {
        this.loginApi = loginApi;
    }


    public String getStaticResouce() {
        return staticResouce;
    }


    public void setStaticResouce(String staticResouce) {
        this.staticResouce = staticResouce;
    }


    public String getAppLoginUrl() {
        return appLoginUrl;
    }

    public void setAppLoginUrl(String appLoginUrl) {
        this.appLoginUrl = appLoginUrl;
    }

    public String getAppRedirectUrl() {
        return appRedirectUrl;
    }

    public void setAppRedirectUrl(String appRedirectUrl) {
        this.appRedirectUrl = appRedirectUrl;
    }

    public String getSsoLoginUrl() {
        return ssoLoginUrl;
    }

    public void setSsoLoginUrl(String ssoLoginUrl) {
        this.ssoLoginUrl = ssoLoginUrl;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getTokenVerifyUrl() {
        return tokenVerifyUrl;
    }

    public void setTokenVerifyUrl(String tokenVerifyUrl) {
        this.tokenVerifyUrl = tokenVerifyUrl;
    }

    public Integer getMaxInactiveInterval() {
        return maxInactiveInterval;
    }

    public void setMaxInactiveInterval(Integer maxInactiveInterval) {
        this.maxInactiveInterval = maxInactiveInterval;
    }
}
