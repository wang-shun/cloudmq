package com.alibaba.rocketmq.service;

import com.alibaba.rocketmq.common.PropertyToArray;
import com.alibaba.rocketmq.common.Table;
import com.alibaba.rocketmq.dao.UserDao;
import com.alibaba.rocketmq.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.alibaba.rocketmq.common.Tool.str;

/**
 * @author gaoyanlei
 * @since 2016/7/12
 */
@Service
public class UserService extends AbstractService {
    @Autowired
    UserDao userDao;
    static final Logger logger = LoggerFactory.getLogger(UserService.class);


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

    public Table findAll() {
        List<User> users = userDao.selectEntryList(new User());
        int row = users.size();
        if (row > 0) {
            Table table = new Table(PropertyToArray.EntityToPropertyArray(User.class), row);
//            Table table = new Table(new String[]{"id", "realName", "userName", "createdTime"}, row);
            for (User user : users) {
                Object[] tr = table.createTR();
                tr[0] = user.getId();
                tr[1] = user.getRealName();
                tr[2] = user.getUserName();
                tr[3] = user.getCreatedTime();
                table.insertTR(tr);
            }
            return table;
        }
        return null;
    }

    public Table delate(int userId) {
        userDao.deleteByKey(userId);
        List<User> users = userDao.selectEntryList(new User());
        int row = users.size();
        if (row > 0) {
            Table table = new Table(PropertyToArray.EntityToPropertyArray(User.class), row);
//            Table table = new Table(new String[]{"id", "realName", "userName", "createdTime"}, row);
            for (User user : users) {
                Object[] tr = table.createTR();
                tr[0] = user.getId();
                tr[1] = user.getRealName();
                tr[2] = user.getUserName();
                tr[3] = user.getCreatedTime();
                table.insertTR(tr);
            }
            return table;
        }
        return null;
    }

}
