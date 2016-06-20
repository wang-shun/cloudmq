package com.gome.rocketmq.example.ttx.ha;

import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.common.consumer.ConsumeFromWhere;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.gome.rocketmq.common.MyUtils;
import com.gome.rocketmq.dao.MsgInfoDao;
import com.gome.rocketmq.domain.MsgInfo;

import java.util.List;


public class HAConsumer {

    private static MsgInfoDao msgInfoDao = (MsgInfoDao) MyUtils.getSpringBean("msgInfoDao");
    private static DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("HAConsumer");
    private static int ReceiverMsgNums = 0;
    private static int ReceiverMsgNums2 = 0;
    private static int MsgNums = 0;

    static {
        consumer.setNamesrvAddr(MyUtils.getNamesrvAddr());
    }

    /**
     * 当前例子是PushConsumer用法，使用方式给用户感觉是消息从RocketMQ服务器推到了应用客户端。<br>
     * 但是实际PushConsumer内部是使用长轮询Pull方式从Broker拉消息，然后再回调用户Listener方法<br>
     */
    public static void main(String[] args) throws InterruptedException, MQClientException {
        /**
         * 订阅指定topic下tags分别等于TagA
         */
        consumer.subscribe("HAProducerTopic", "TagA || tagA");

        /**
         * 设置Consumer第一次启动是从队列头部开始消费还是队列尾部开始消费<br>
         * 如果非第一次启动，那么按照上次消费的位置继续消费
         */
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);

        consumer.registerMessageListener(new MessageListenerConcurrently() {

            /**
             * 默认msgs里只有一条消息，可以通过设置consumeMessageBatchMaxSize参数来批量接收消息
             */
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs,
                                                            ConsumeConcurrentlyContext context) {
                ReceiverMsgNums = ReceiverMsgNums + msgs.size();
                for (MessageExt msg : msgs) {
                    ReceiverMsgNums2++;
                    MsgInfo msgInfo = new MsgInfo();
                    byte[] bodyBytes = msg.getBody();
                    String body = new String(bodyBytes);
                    int bodyHashcode = body.hashCode();
                    msgInfo.setBodyHashcode(bodyHashcode);
                    List<MsgInfo> msgInfos = msgInfoDao.selectEntryList(msgInfo);
                    if(msgInfos.size() == 0){
                        // 如果根据deleted = null条件没有找到，则判断是否为重复消息
                        msgInfo.setDeleted(1);
                        List<MsgInfo> msgInfosRepeat = msgInfoDao.selectEntryList(msgInfo);
                        if(msgInfosRepeat.size() > 0){
                            MsgInfo msgInfoRepeat = msgInfosRepeat.get(0);
                            msgInfoRepeat.setRepeatNum(msgInfosRepeat.size());
                            msgInfoDao.updateByKey(msgInfoRepeat);
                        }else {
                            // 如果deleted =null 或者 deleted =1都找不到消息那么，则该消息为异常消息，置deleted =44
                            msgInfo.setDeleted(44);
                            msgInfoDao.insertEntry(msgInfo);
                        }
                    }
                    for (MsgInfo info : msgInfos) {
                        MsgNums++;
                        info.setDeleted(1);
                        msgInfoDao.updateByKey(info);
                    }
                }
                System.out.println("---------------------ReceiverMsgNums :" + ReceiverMsgNums);
                System.out.println("---------------------ReceiverMsgNums2 :" + ReceiverMsgNums2);
                System.out.println("---------------------MsgNums :" + MsgNums);
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });

        /**
         * Consumer对象在使用之前必须要调用start初始化，初始化一次即可
         */
        consumer.start();

        System.out.println("Consumer Started.");
    }

}
