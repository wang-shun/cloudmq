package com.cloudzone.cloudmq.demo.TpsMeter;

import com.cloudzone.cloudlimiter.base.AcquireStatus;
import com.cloudzone.cloudlimiter.base.IntervalModel;
import com.cloudzone.cloudlimiter.base.MeterListener;
import com.cloudzone.cloudlimiter.factory.CloudFactory;
import com.cloudzone.cloudlimiter.limiter.RealTimeLimiter;
import com.cloudzone.cloudlimiter.meter.CloudMeter;
import com.cloudzone.cloudlimiter.meter.Meterinfo;
import com.cloudzone.cloudmq.api.open.base.Msg;
import com.cloudzone.cloudmq.api.open.base.Producer;
import com.cloudzone.cloudmq.api.open.base.SendResult;
import com.cloudzone.cloudmq.api.open.factory.MQFactory;
import com.cloudzone.cloudmq.common.PropertiesConst;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static com.cloudzone.cloudlimiter.base.AcquireStatus.ACQUIRE_SUCCESS;

/**
 * @author tantexian<my.oschina.net/tantexian>
 * @since 2017/4/27
 */
public class SendMsgTPSWithMeter {

    public static void main(String[] args) {
        AtomicInteger input = new AtomicInteger(0);
        final AtomicLong meterResultSec = new AtomicLong(0);
        final AtomicLong meterResultMin = new AtomicLong(0);
        RealTimeLimiter realTimeLimiter = CloudFactory.createRealTimeLimiter(10);
        CloudMeter cloudMeter = CloudFactory.createCloudMeter();
        cloudMeter.setIntervalModel(IntervalModel.SECOND);
        cloudMeter.registerListener(new MeterListener() {
            @Override
            public AcquireStatus acquireStats(List<Meterinfo> meterinfos) {
                for (Meterinfo meterInfo : meterinfos) {
                    System.out.println(meterInfo);
                }
                return ACQUIRE_SUCCESS;
            }
        });


        Properties properties = new Properties();
        // 您在控制台创建的生产者组 ID（ ProducerGroupId）
        properties.put(PropertiesConst.Keys.ProducerGroupId, "SimpleProducerGroupId-test");
        // TOPIC_NAME_AND_AUTH_KEY 的值, 即为 Topic 和 AuthKey 的键值对
        properties.put(PropertiesConst.Keys.TOPIC_NAME_AND_AUTH_KEY,
                "ttx-test-tps-200:0592547a64ceb483b9173da139440a53e");
        Producer producer = MQFactory.createProducer(properties);
        // 在发送消息前，必须调用 start 方法来启动 Producer，只需调用一次即可。
        producer.start();

        while (true) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            int min = calendar.get(Calendar.MINUTE);
            int minMod = min % 10;
            double rate;
            // 1,2,3 设置为200/min
            // 4,5,6 设置为500/min
            // 7,8,9,0 设置为1000/min
            // 由于TPS限制时间，和meter统计时间不在同一个时间点取值，因此会出现某些数据误差
            // 此处设置可以保证，第0,1,2,4,5,6,8 结尾的秒的数据一定是准确的
            double howManySeconds = 60D;
            if (minMod >= 0 && minMod <= 3) {
                rate = 200D / howManySeconds;
            } else if (minMod >= 4 && minMod <= 7) {
                rate = 500D / howManySeconds;
            } else {
                rate = 1000D / howManySeconds;
            }

            if (rate != realTimeLimiter.getRate()) {
                realTimeLimiter.setRate(rate);
                System.out.println(min + "s -> setRate == " + rate + " " + (rate * 60L) + "/minute");
            }
            realTimeLimiter.acquire();
            cloudMeter.request();
            Msg msg = new Msg(
                    // Msg Topic
                    "ttx-test-tps-200",
                    // Msg Tag 可理解为 Gmail 中的标签，对消息进行再归类，方便 Consumer 指定过滤条件在 MQ 服务器过滤
                    "TagA",
                    // Msg Body 可以是任何二进制形式的数据， MQ 不做任何干预，
                    // 需要 Producer 与 Consumer 协商好一致的序列化和反序列化方式
                    ("Hello MQ " + new Date()).getBytes());
            // 设置代表消息的业务关键属性，请尽可能全局唯一。（例如订单 ID）。
            // 以方便您在无法正常收到消息情况下，可通过 MQ 控制台查询消息并补发。
            // 注意：不设置也不会影响消息正常收发
            msg.setKey("ORDERID_");
            // 发送消息，只要不抛异常就是成功
            // 建议业务程序自行记录生产及消费 log 日志，
            // 以方便您在无法正常收到消息情况下，可通过 MQ 控制台或者 MQ 日志查询消息并补发。
            SendResult sendResult = producer.send(msg);
            //System.out.println(sendResult);
            input.addAndGet(1);
        }

        /*cloudMeter.shutdown();

        System.out.println("input == " + input + " meterResultSec == " + meterResultSec + "  meterResultMin == " + meterResultMin);*/
    }
}
