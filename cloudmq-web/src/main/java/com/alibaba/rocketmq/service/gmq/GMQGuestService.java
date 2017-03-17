package com.alibaba.rocketmq.service.gmq;

import com.alibaba.rocketmq.common.protocol.body.GroupList;
import com.alibaba.rocketmq.domain.gmq.Cluster;
import com.alibaba.rocketmq.service.AbstractService;
import com.alibaba.rocketmq.tools.admin.DefaultMQAdminExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author: tianyuliang
 * @since: 2017/2/7
 */
@Service
public class GMQGuestService extends AbstractService {

    static final Logger logger = LoggerFactory.getLogger(GMQGuestService.class);

    public HashSet<String> queryTopicConsumeByWho(String topic) throws Throwable {
        Throwable t = null;
        DefaultMQAdminExt defaultMQAdminExt = getDefaultMQAdminExt();
        try {
            defaultMQAdminExt.start();
            return queryTopicConsumeByWho(defaultMQAdminExt, topic);
        }
        catch (Throwable e) {
            logger.error(e.getMessage(), e);
            t = e;
        }
        finally {
            shutdownDefaultMQAdminExt(defaultMQAdminExt);
        }
        throw t;
    }


    private HashSet<String> queryTopicConsumeByWho(DefaultMQAdminExt defaultMQAdminExt, String topicName) throws Throwable {
        HashSet<String> consumeGroups = new HashSet<String>();
        GroupList groups =  defaultMQAdminExt.queryTopicConsumeByWho(topicName);
        if (groups != null && groups.getGroupList() != null && groups.getGroupList().size() > 0) {
            consumeGroups = groups.getGroupList();
        }
        return consumeGroups;
    }






}
