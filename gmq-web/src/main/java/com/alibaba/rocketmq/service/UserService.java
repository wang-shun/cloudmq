package com.alibaba.rocketmq.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.rocketmq.dao.UserDao;
import com.alibaba.rocketmq.domain.User;
import com.alibaba.rocketmq.util.MyBeanUtils;


/**
 * @author gaoyanlei
 * @since 2016/7/12
 */
@Service
public class UserService extends AbstractService {
    @Autowired
    UserDao userDao;
    static final Logger logger = LoggerFactory.getLogger(UserService.class);


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
