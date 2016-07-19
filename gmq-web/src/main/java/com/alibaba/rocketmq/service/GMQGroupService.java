package com.alibaba.rocketmq.service;

import com.alibaba.rocketmq.common.admin.ConsumeStats;
import com.alibaba.rocketmq.common.admin.OffsetWrapper;
import com.alibaba.rocketmq.common.message.MessageQueue;
import com.alibaba.rocketmq.domain.ConsumerGroupVo;
import com.alibaba.rocketmq.tools.admin.DefaultMQAdminExt;
import org.apache.commons.collections.list.AbstractLinkedList;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.Msg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author: tianyuliang
 * @since: 2016/7/19
 */
@Service
public class GMQGroupService extends AbstractService {

    static final Logger logger = LoggerFactory.getLogger(GMQGroupService.class);

    public Map<String, Object> consumerProgress(String consumerGroupId) throws Throwable {
        Map<String, Object> params = new HashedMap();
        List<ConsumerGroupVo> list = new ArrayList<>();
        Throwable t = null;
        DefaultMQAdminExt defaultMQAdminExt = getDefaultMQAdminExt();
        try {
            defaultMQAdminExt.start();
            if (StringUtils.isNotBlank(consumerGroupId)) {
                ConsumeStats consumeStats = defaultMQAdminExt.examineConsumeStats(consumerGroupId);

                List<MessageQueue> mqList = new LinkedList<MessageQueue>();
                mqList.addAll(consumeStats.getOffsetTable().keySet());
                Collections.sort(mqList);
               // params.put("list", mqList);

                // "#Topic", "#Broker Name", "#QID", "#BrokerOffset", "#ConsumerOffset", "#DiffOffset"
                ConsumerGroupVo groupVo = null;
                long diffTotal = 0L;
                for (MessageQueue mq : mqList) {
                    groupVo = new ConsumerGroupVo();
                    groupVo.setBrokerName(mq.getBrokerName());
                    groupVo.setQueueId(mq.getQueueId());
                    groupVo.setTopic(mq.getTopic());

                    OffsetWrapper offsetWrapper = consumeStats.getOffsetTable().get(mq);
                    groupVo.setBrokerOffset(offsetWrapper.getBrokerOffset());
                    groupVo.setConsumerOffset(offsetWrapper.getConsumerOffset());

                    long diff = offsetWrapper.getBrokerOffset() - offsetWrapper.getConsumerOffset();
                    groupVo.setDiffOffset(diff);
                    list.add(groupVo);

                    diffTotal += diff;
                }
                params.put("list", list);
                params.put("tps", consumeStats.getConsumeTps());
                params.put("diffTotal", diffTotal);
                return params;
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            t = e;
        } finally {
            shutdownDefaultMQAdminExt(defaultMQAdminExt);
        }
        throw t;
    }

}
