package com.alibaba.rocketmq.service.gmq;

import java.sql.SQLException;
import java.util.List;

import com.alibaba.rocketmq.domain.sso.gmq.GmqUser;
import com.alibaba.rocketmq.service.AbstractService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.rocketmq.dao.UserDao;
import com.alibaba.rocketmq.domain.gmq.User;
import com.alibaba.rocketmq.util.bean.MyBeanUtils;


/**
 * @author gaoyanlei
 * @since 2016/7/12
 */
@Service
public class GMQUserService extends AbstractService {

    static final Logger logger = LoggerFactory.getLogger(GMQUserService.class);
    @Autowired
    UserDao userDao;

    /**
     * login test
     *
     * @author gaoyanlei
     * @since 2016/7/19
     */
    public boolean checkUserTest(String userName, String password) {
        User user = new User();
        user.setUserName(userName);
        user.setPassword(password);
        List<User> users = userDao.selectEntryList(user);
        if (users.size() > 0) {
            return true;
        }
        return false;
    }


     /**
      * 判断是否为管理员
      * @author tianyuliang
      * @since 2016/12/20
      * @params 用户名
      */
    public boolean verifyAdminUser(String realName) {
        User user = new User();
        user.setRealName(realName);
        List<User> users = userDao.selectEntryList(user);
        return users != null && users.size() > 0;
    }

    /**
     * 判断是否为管理员(校验realName字段和state字段)
     * @author tianyuliang
     * @since 2016/12/20
     * @params
     */
    public GmqUser queryAdminUser(String realName) throws SQLException{
        GmqUser gmqUser = null;
        User user = new User();
        user.setRealName(realName);
        List<User> users = userDao.selectEntryList(user);
        if(users != null && users.size() > 0 && users.get(0).getState() == 0) {
            gmqUser = new GmqUser();
            gmqUser.setUserName(users.get(0).getUserName());
            gmqUser.setUserType("1");
            gmqUser.setRealName(users.get(0).getRealName());
        }
        return gmqUser;
    }



    /**
     * login test
     *
     * @author gaoyanlei
     * @since 2016/7/19
     */
    public User loginUser(String userName, String password) {
        User user = new User();
        user.setUserName(userName);
        user.setPassword(password);
        List<User> users = userDao.selectEntryList(user);
        return users.isEmpty() ? null : users.get(0);
    }


    /**
     * 查找全部用户
     *
     * @author gaoyanlei
     * @since 2016/7/19
     */
    public List<User> findAll() {
        List<User> users = userDao.selectEntryList(new User());
        if (users.size() > 0) {
            return users;
        }
        return null;
    }


    /**
     * 保存 修改用户
     *
     * @author gaoyanlei
     * @since 2016/7/19
     */
    public int saveOrUpdate(User user) throws Exception {
        if (user.getId() == null) {
            return userDao.insertEntry(user);
        }
        else {
            User u = this.findById(user.getId());
            MyBeanUtils.copyBeanNotNull2Bean(user, u);
            return userDao.updateByKey(u);
        }
    }


    /**
     * 按用户ID查询用户
     *
     * @author gaoyanlei
     * @since 2016/7/19
     */
    public User findById(int userId) {
        return userDao.selectEntry(userId);
    }


    /**
     * 删除用户
     *
     * @author gaoyanlei
     * @since 2016/7/19
     */
    public int delete(int userId) {
        return userDao.deleteByKey(userId);
    }

}
