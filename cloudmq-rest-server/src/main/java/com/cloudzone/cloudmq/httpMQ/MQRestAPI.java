package com.cloudzone.cloudmq.httpMQ;

import com.cloudzone.cloudmq.api.open.base.*;
import com.cloudzone.cloudmq.api.open.factory.MQFactory;
import com.cloudzone.cloudmq.base.CloudSendResult;
import com.cloudzone.cloudmq.common.PropertiesConst;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Properties;

/**
 * @author tantexian, <my.oschina.net/tantexian>
 * @since 2017/6/12
 */
@Path("cloudapi/mq/v1")
public class MQRestAPI {
    private static volatile boolean isStart = false;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getInfo() {
        return "Welcome, I'm version v1.";
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public CloudSendResult send(@HeaderParam(PropertiesConst.Keys.ProducerGroupId) String producerGroupId,
                                @HeaderParam(PropertiesConst.Keys.TOPIC_NAME_AND_AUTH_KEY) String topicAndKey,
                                Msg msg) {
        System.out.println("HttpMQ Reveice send request \n[msg]:" + msg + " \n[ProducerGroupId]:" +
                producerGroupId + " \n[TOPIC_NAME_AND_AUTH_KEY]: " + topicAndKey);

        return doSend(producerGroupId, topicAndKey, msg);
    }

    /** TODO：此处还需要考虑到异常等情况，暂且未考虑
     * @author tantexian, <my.oschina.net/tantexian>
     * @since 2017/6/13
     */
    private CloudSendResult doSend(String producerGroupId, String topicAndKey, Msg msg) {
        Properties properties = new Properties();
        // 您在控制台创建的生产者组ID（ProducerGroupId）
        properties.put(PropertiesConst.Keys.ProducerGroupId, producerGroupId);
        // 设置topic名称和认证key
        properties.put(PropertiesConst.Keys.TOPIC_NAME_AND_AUTH_KEY, topicAndKey);

        Producer producer = MQFactory.createProducer(properties);
        // 在发送消息前，必须调用start方法来启动Producer，只需调用一次即可。
        if (!isStart) {
            producer.start();
        }

        SendResult sendResult = producer.send(msg);

        System.out.println("\n\nsend == " + sendResult);
        return CloudSendResult.convertoSelf(sendResult);
    }
}
