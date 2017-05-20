package com.alibaba.rocketmq.service;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;

import com.alibaba.rocketmq.domain.gmq.MessageTrackExt;
import com.alibaba.rocketmq.domain.gmq.TrackTypeExt;
import com.google.common.collect.Maps;
import org.apache.commons.cli.Option;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.rocketmq.client.QueryResult;
import com.alibaba.rocketmq.client.consumer.DefaultMQPullConsumer;
import com.alibaba.rocketmq.client.consumer.PullResult;
import com.alibaba.rocketmq.common.MixAll;
import com.alibaba.rocketmq.common.Table;
import com.alibaba.rocketmq.common.UtilAll;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.alibaba.rocketmq.common.message.MessageQueue;
import com.alibaba.rocketmq.remoting.common.RemotingHelper;
import com.alibaba.rocketmq.tools.admin.DefaultMQAdminExt;
import com.alibaba.rocketmq.tools.admin.api.MessageTrack;
import com.alibaba.rocketmq.tools.command.message.QueryMsgByIdSubCommand;
import com.alibaba.rocketmq.tools.command.message.QueryMsgByKeySubCommand;
import com.alibaba.rocketmq.tools.command.message.QueryMsgByOffsetSubCommand;
import com.alibaba.rocketmq.validate.CmdTrace;


/**
 *
 * @author yankai913@gmail.com
 * @date 2014-2-17
 */
@Service
public class MessageService extends AbstractService {

    static final Logger logger = LoggerFactory.getLogger(MessageService.class);

    static final QueryMsgByIdSubCommand queryMsgByIdSubCommand = new QueryMsgByIdSubCommand();

    /**
     * 查看消息详情
     * @author tianyuliang
     * @since 2017/3/3
     * @params
     */
    public Map<String, Object> queryMsgDetail(String msgId) throws Throwable {
        Throwable t = null;
        Map<String, Object> param = null;
        DefaultMQAdminExt defaultMQAdminExt = getDefaultMQAdminExt();
        try {
            defaultMQAdminExt.start();
            MessageExt msg = defaultMQAdminExt.viewMessage(msgId);
            param = buildMsgDetailMap(msg);

            String bodyTmpFilePath = createBodyFile(msg.getMsgId(), msg.getBody());
            param.put("MessaeBodyPath", bodyTmpFilePath);

            List<MessageTrack> trackLists = queryMsgTrackList(defaultMQAdminExt, msg);
            param.put("messageTrack", transferMessageTrackExt(trackLists));

            return param;
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

    /**
     * 查看消息Body内容
     * @author tianyuliang
     * @since 2017/3/3
     * @params
     */
    public String queryMsgBody(String msgId) throws IOException {
        return readMsgBodyFile(msgId);
    }

    private Map<String, Object> buildMsgDetailMap(MessageExt msg) {
        Map<String, Object> param = new LinkedMap();
        param.put("MsgId", msg.getMsgId());

        // System.out.printf("%-20s %s\n",//
        // "Topic:",//
        // msg.getTopic()//
        // );
        param.put("Topic", msg.getTopic());

        // System.out.printf("%-20s %s\n",//
        // "Tags:",//
        // "[" + msg.getTags() + "]"//
        // );
        param.put("Tags", "[" + msg.getTags() + "]");

        // System.out.printf("%-20s %s\n",//
        // "Keys:",//
        // "[" + msg.getKeys() + "]"//
        // );
        param.put("Keys", "[" + msg.getKeys() + "]");

        // System.out.printf("%-20s %d\n",//
        // "Queue ID:",//
        // msg.getQueueId()//
        // );
        param.put("Queue ID", String.valueOf(msg.getQueueId()));

        // System.out.printf("%-20s %d\n",//
        // "Queue Offset:",//
        // msg.getQueueOffset()//
        // );
        param.put("Queue Offset", String.valueOf(msg.getQueueOffset()));

        // System.out.printf("%-20s %d\n",//
        // "CommitLog Offset:",//
        // msg.getCommitLogOffset()//
        // );
        param.put("CommitLog Offset", String.valueOf(msg.getCommitLogOffset()));

        // System.out.printf("%-20s %s\n",//
        // "Born Timestamp:",//
        // UtilAll.timeMillisToHumanString2(msg.getBornTimestamp())//
        // );
        param.put("Born Timestamp", UtilAll.timeMillisToHumanString2(msg.getBornTimestamp()));

        // System.out.printf("%-20s %s\n",//
        // "Store Timestamp:",//
        // UtilAll.timeMillisToHumanString2(msg.getStoreTimestamp())//
        // );
        param.put("Store Timestamp", UtilAll.timeMillisToHumanString2(msg.getStoreTimestamp()));

        // System.out.printf("%-20s %s\n",//
        // "Born Host:",//
        // RemotingHelper.parseSocketAddressAddr(msg.getBornHost())//
        // );
        param.put("Born Host", RemotingHelper.parseSocketAddressAddr(msg.getBornHost()));

        // System.out.printf("%-20s %s\n",//
        // "Store Host:",//
        // RemotingHelper.parseSocketAddressAddr(msg.getStoreHost())//
        // );
        param.put("Store Host", RemotingHelper.parseSocketAddressAddr(msg.getStoreHost()));

        // System.out.printf("%-20s %d\n",//
        // "System Flag:",//
        // msg.getSysFlag()//
        // );
        param.put("System Flag", String.valueOf(msg.getSysFlag()));

        // System.out.printf("%-20s %s\n",//
        // "Properties:",//
        // msg.getProperties() != null ? msg.getProperties().toString() :
        // ""//
        // );
        param.put("Properties", msg.getProperties() != null ? msg.getProperties().toString() : "");

        // System.out.printf("%-20s %s\n",//
        // "Message Body Path:",//
        // bodyTmpFilePath//
        // );
        return param;
    }

    private List<MessageTrack> queryMsgTrackList(DefaultMQAdminExt defaultMQAdminExt, MessageExt msg) throws Exception {
        List<MessageTrack> messageTrackList = null;
        try{
            messageTrackList = defaultMQAdminExt.messageTrackDetail(msg);
        } catch(Exception e){
            logger.error("query msg track list error. msgId="+ msg.getMsgId(), e);
            throw e;
        }
        return messageTrackList;
    }

    private List<MessageTrackExt> transferMessageTrackExt(List<MessageTrack> trackLists){
        List<MessageTrackExt> messageTrackList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(trackLists)){
            MessageTrackExt trackExt = null;
            for (MessageTrack track : trackLists) {
                trackExt = new MessageTrackExt();
                trackExt.setTrackType(track.getTrackType());
                trackExt.setConsumerGroup(track.getConsumerGroup());
                trackExt.setExceptionDesc(track.getExceptionDesc());
                trackExt.setTrackDescription(TrackTypeExt.getTrackTypeMsg(track.getTrackType().ordinal()));
                trackExt.setTrackCode(track.getTrackType().ordinal());
                messageTrackList.add(trackExt);
            }
        }
        return messageTrackList;
    }

    public Collection<Option> getOptionsForQueryMsgById() {
        return getOptions(queryMsgByIdSubCommand);
    }

    @CmdTrace(cmdClazz = QueryMsgByIdSubCommand.class)
    public Table queryMsgById(String msgId) throws Throwable {
        Throwable t = null;
        Map<String, Object> map = null;
        DefaultMQAdminExt defaultMQAdminExt = getDefaultMQAdminExt();
        try {
            defaultMQAdminExt.start();
            MessageExt msg = defaultMQAdminExt.viewMessage(msgId);
            map = buildMsgDetailMap(msg);

            String bodyTmpFilePath = createBodyFile(msg.getMsgId(), msg.getBody());
            map.put("Message Body Path:", bodyTmpFilePath);

            // 增加消息消费轨迹跟踪 add by tantexian 2016-6-2 08:53:03
            List<MessageTrack> messageTrackList = defaultMQAdminExt.messageTrackDetail(msg);
            map.put("messageTrack", messageTrackList.toString());

            Map<String, String> param = Maps.newHashMap();
            Iterator<Map.Entry<String, Object>> itor = map.entrySet().iterator();
            while(itor.hasNext()){
                Map.Entry<String, Object> entry = itor.next();
                param.put(entry.getKey(), (String) entry.getValue());
            }
            return Table.Map2VTable(param);
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


    private String createBodyFile(String msgId, byte[] body) throws IOException {
        String bodyFileName = MessageFormat.format("/tmp/rocketmq/msgbodys/{0}", msgId);
        File bodyFile = new File(bodyFileName);
        String msgbody = new String(body);
        logger.info(MessageFormat.format("write msg with msgId={0}, msgBody={1}", msgId, msgbody));
        FileUtils.writeStringToFile(bodyFile, msgbody, "UTF-8", false);
        return bodyFileName;
    }

    private String readMsgBodyFile(String msgId) throws IOException {
        String bodyFileName = MessageFormat.format("/tmp/rocketmq/msgbodys/{0}", msgId);
        File bodyFile = new File(bodyFileName);
        String msgbody = FileUtils.readFileToString(bodyFile, "UTF-8");
        logger.info(MessageFormat.format("read msg with msgId={0}, msgbody={1}", msgId, msgbody));
        return msgbody;
    }

    static final QueryMsgByKeySubCommand queryMsgByKeySubCommand = new QueryMsgByKeySubCommand();


    public Collection<Option> getOptionsForQueryMsgByKey() {
        return getOptions(queryMsgByKeySubCommand);
    }


    @CmdTrace(cmdClazz = QueryMsgByKeySubCommand.class)
    public Table queryMsgByKey(String topicName, String msgKey, String fallbackHours) throws Throwable {
        Throwable t = null;
        DefaultMQAdminExt defaultMQAdminExt = getDefaultMQAdminExt();
        try {
            defaultMQAdminExt.start();
            long h = 0;
            if (StringUtils.isNotBlank(fallbackHours)) {
                h = Long.parseLong(fallbackHours);
            }
            long end = System.currentTimeMillis() - (h * 60 * 60 * 1000);
            long begin = end - (6 * 60 * 60 * 1000);
            QueryResult queryResult = defaultMQAdminExt.queryMessage(topicName, msgKey, 32, begin, end);

            // System.out.printf("%-50s %-4s  %s\n",//
            // "#Message ID",//
            // "#QID",//
            // "#Offset");
            String[] thead = new String[] { "#Message ID", "#QID", "#Offset" };
            int row = queryResult.getMessageList().size();
            Table table = new Table(thead, row);

            for (MessageExt msg : queryResult.getMessageList()) {
                String[] data = new String[] { msg.getMsgId(), String.valueOf(msg.getQueueId()), String.valueOf(msg.getQueueOffset()) };
                table.insertTR(data);
                // System.out.printf("%-50s %-4d %d\n", msg.getMsgId(),
                // msg.getQueueId(), msg.getQueueOffset());
            }
            return table;
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

    static final QueryMsgByOffsetSubCommand queryMsgByOffsetSubCommand = new QueryMsgByOffsetSubCommand();


    public Collection<Option> getOptionsForQueryMsgByOffset() {
        return getOptions(queryMsgByOffsetSubCommand);
    }


    @CmdTrace(cmdClazz = QueryMsgByOffsetSubCommand.class)
    public Table queryMsgByOffset(String topicName, String brokerName, String queueId, String offset)
            throws Throwable {
        Throwable t = null;
        DefaultMQPullConsumer defaultMQPullConsumer = new DefaultMQPullConsumer(MixAll.TOOLS_CONSUMER_GROUP);

        defaultMQPullConsumer.setInstanceName(Long.toString(System.currentTimeMillis()));

        try {
            MessageQueue mq = new MessageQueue();
            mq.setTopic(topicName);
            mq.setBrokerName(brokerName);
            mq.setQueueId(Integer.parseInt(queueId));

            defaultMQPullConsumer.start();

            PullResult pullResult = defaultMQPullConsumer.pull(mq, "*", Long.parseLong(offset), 1);
            if (pullResult != null) {
                switch (pullResult.getPullStatus()) {
                    case FOUND:
                        Table table = queryMsgById(pullResult.getMsgFoundList().get(0).getMsgId());
                        return table;
                    case NO_MATCHED_MSG:
                    case NO_NEW_MSG:
                    case OFFSET_ILLEGAL:
                    default:
                        break;
                }
            }
            else {
                throw new IllegalStateException("pullResult is null");
            }
        }
        catch (Throwable e) {
            logger.error(e.getMessage(), e);
            t = e;
        }
        finally {
            defaultMQPullConsumer.shutdown();
        }
        throw t;
    }
}
