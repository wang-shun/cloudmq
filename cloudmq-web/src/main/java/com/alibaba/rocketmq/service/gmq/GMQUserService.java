package com.alibaba.rocketmq.service.gmq;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.rocketmq.domain.sso.gmq.GmqUser;
import com.alibaba.rocketmq.service.AbstractService;
import com.alibaba.rocketmq.util.base.BaseUtil;


/**
 * @author gaoyanlei
 * @since 2016/7/12
 */
@Service
public class GMQUserService extends AbstractService {

    static final Logger logger = LoggerFactory.getLogger(GMQUserService.class);

    @Value("#{configProperties['admin.userNames']}")
    private String adminUserNames;


    /**
     * 判断是否为管理员(校验realName字段和state字段)
     * 
     * @author tianyuliang
     * @since 2016/12/20
     * @params
     */
    public GmqUser queryAdminUser(String realName) {
        List<GmqUser> users = getAdminUsers();
        for (GmqUser user : users) {
            if (user.getRealName().equals(realName)) {
                logger.info("{}", user.toString());
                return user;
            }
        }
        return null;
    }


    /**
     * 查找全部用户
     */
    public List<GmqUser> findAll() {
        return getAdminUsers();
    }


    private List<GmqUser> getAdminUsers() {
        List<GmqUser> users = new ArrayList<>();
        if (BaseUtil.isBlank(adminUserNames)) {
            return users;
        }
        String[] values = adminUserNames.trim().split(",");
        if (values == null || values.length == 0) {
            return users;
        }
        GmqUser user = null;
        for (String value : values) {
            if (BaseUtil.isBlank(value)) {
                continue;
            }
            user = new GmqUser();
            user.setUserType("1"); // 1:admin 0:guest
            user.setUserName(value.trim());
            user.setRealName(value.trim());
            users.add(user);
        }
        return users;
    }

}
