package com.alibaba.rocketmq.service;

import com.alibaba.rocketmq.common.PropertyToArray;
import com.alibaba.rocketmq.common.Table;
import com.alibaba.rocketmq.dao.UserDao;
import com.alibaba.rocketmq.domain.User;
import com.alibaba.rocketmq.tools.command.user.UpdateUserSubCommand;
import org.apache.commons.cli.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.List;

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

    public List<User> findAll() {
        List<User> users = userDao.selectEntryList(new User());
        if (users.size() > 0) {
            return users;
        }
        return null;
    }

    final static UpdateUserSubCommand updateUserSubCommand = new UpdateUserSubCommand();


    public Collection<Option> getOptionsForUpdate() {
        return getOptions(updateUserSubCommand);
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

    public int update(int id,
                      String userName,
                      String email,
                      String mobile, String realName) {
        User user = new User();
        user.setId(id + "");
        user.setUserName(userName);
        user.setEmail(email);
        user.setMobile(mobile);
        user.setRealName(realName);
        return userDao.updateByKey(user);
    }

    public User findById(int userId) {
        return userDao.selectEntry(userId);
    }

    public void save(String userName,
                     String email,
                     String mobile, String realName) {
        User user = new User();
        user.setUserName(userName);
        user.setEmail(email);
        user.setMobile(mobile);
        user.setRealName(realName);
        userDao.insertEntry(user);
    }
}
