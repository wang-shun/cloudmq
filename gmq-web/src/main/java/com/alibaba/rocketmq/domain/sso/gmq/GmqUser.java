package com.alibaba.rocketmq.domain.sso.gmq;

import com.alibaba.rocketmq.domain.gmq.User;


/**
 * @author: tianyuliang
 * @since: 2016/12/2
 */
public class GmqUser extends User {

    /**
     * 用户类型  1:管理员  0:普通用户 ,  通常情况下，gmq的数据库有某个用户的数据，则默认该用户为管理员
     */
    private String userType;


    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }
}
