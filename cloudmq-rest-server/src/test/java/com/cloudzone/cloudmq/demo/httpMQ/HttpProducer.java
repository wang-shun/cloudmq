package com.cloudzone.cloudmq.demo.httpMQ;

import com.alibaba.fastjson.JSON;
import com.cloudzone.cloudmq.api.open.base.Msg;
import com.cloudzone.cloudmq.common.PropertiesConst;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequestWithBody;
import org.junit.Test;

/**
 * @author tantexian, <my.oschina.net/tantexian>
 * @since 2017/6/12
 */
public class HttpProducer {

    @Test
    public void testSend() {
        String url = "http://127.0.0.1:8989/cloudapi/mq/v1";
        HttpRequestWithBody req = Unirest.post(url);
        req.header("Content-Type", "application/json");

        req.header(PropertiesConst.Keys.ProducerGroupId, "httpMQTestProducerGroup");
        req.header(PropertiesConst.Keys.TOPIC_NAME_AND_AUTH_KEY, "jcpt-qa-simple-800:01b04fdc9aeb341dca32697c1840aeaa2");

        // 循环发送消息
        for (int i = 0; i < 10; i++) {
            Msg msg = new Msg(
                    // Msg Topic
                    "jcpt-qa-simple-800",
                    // Msg Tag 可理解为Gmail中的标签，对消息进行再归类，方便Consumer指定过滤条件在MQ服务器过滤
                    "TagA",
                    // Msg Body 可以是任何二进制形式的数据， MQ不做任何干预，
                    // 需要Producer与Consumer协商好一致的序列化和反序列化方式
                    ("Hello MQ " + i).getBytes());

            // 设置代表消息的业务关键属性，请尽可能全局唯一。（例如订单ID）。
            // 以方便您在无法正常收到消息情况下，可通过MQ控制台查询消息并补发。
            // 注意：不设置也不会影响消息正常收发
            msg.setKey("ORDERID_" + i);

            req.body(JSON.toJSONString(msg));

            try {
                HttpResponse res = req.asString();

                System.out.println("responseBody: " + res.getBody());
            } catch (UnirestException e) {
                e.printStackTrace();
            }
        }
    }

}
