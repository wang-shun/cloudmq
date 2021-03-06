package com.alibaba.rocketmq.action;

import com.alibaba.rocketmq.domain.gmq.User;
import com.alibaba.rocketmq.service.gmq.GMQUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;


/**
 * @author gaoyanlei
 * @since 2016/7/12
 */
@Controller
@RequestMapping("/user")
public class UserAction extends AbstractAction {

    @Autowired
    GMQUserService GMQUserService;


    @Override
    protected String getFlag() {
        return "user_flag";
    }


    @Override
    protected String getName() {
        return "User";
    }


    @RequestMapping(value = "/list.do", method = RequestMethod.GET)
    public String list(ModelMap map) {
        putPublicAttribute(map, "list");
        try {
            putTable(map, GMQUserService.findAll());
        } catch (Throwable t) {
            putAlertMsg(t, map);
        }
        return TEMPLATE;
    }


    @RequestMapping(value = "/saveOrUpdate.do", method = {RequestMethod.GET})
    public String AddView(ModelMap map, @RequestParam(required = false) Integer id) {
        putPublicAttribute(map, "add");
        if (id != null) {
            putTable(map, GMQUserService.findById(id));
        }
        return TEMPLATE;
    }


    @RequestMapping(value = "/saveOrUpdate.do", method = {RequestMethod.POST})
    @ResponseBody
    public String save(ModelMap map, @RequestBody User user) {
        Integer result = 0;
        try {
            putPublicAttribute(map, "list");
            result = GMQUserService.saveOrUpdate(user);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "{\"result\":" + result + "}";
    }


    @RequestMapping(value = "/login.do", method = RequestMethod.GET)
    public String list(@RequestParam(required = false) String userName,
                       @RequestParam(required = false) String password) {
        GMQUserService.checkUserTest(userName, password);
        return TEMPLATE;
    }

    @RequestMapping(value = "/resetPassword.do", method = RequestMethod.POST)
    public String resetPassword(ModelMap map, Integer userId,
                                String resetPassword) {
        try {
            User user = new User(userId);
            user.setPassword(resetPassword);
            GMQUserService.saveOrUpdate(user);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/user/list.do";
    }

    @RequestMapping(value = "/delete.do", produces = {"application/json;charset=UTF-8"}, method = {RequestMethod.DELETE})
    @ResponseBody
    public String delete(ModelMap map, HttpServletRequest request, @RequestBody User user) {
        Integer result = 0;

        try {
            putPublicAttribute(map, "list");
            result = GMQUserService.delete(user.getId());
        } catch (Throwable t) {
            putAlertMsg(t, map);
        }
        return "{\"result\":" + result + "}";
    }

}
