package com.cloudzone.cloudmq.common;

/**
 * @author tantexian
 * @since 2016/6/27
 */
public class MQTraceConstants {
    public static String NAMESRV_ADDR = "NAMESRV_ADDR";
    public static String ADDRSRV_URL = "ADDRSRV_URL";
    public static final String AccessKey = "AccessKey";
    public static final String SecretKey = "SecretKey";
    public static final String InstanceName = "InstanceName";
    public static final String AsyncBufferSize = "AsyncBufferSize";
    public static final String MaxBatchNum = "MaxBatchNum";
    public static final String WakeUpNum = "WakeUpNum";
    public static final String MaxMsgSize = "MaxMsgSize";
    public static final String groupName = "_INNER_TRACE_PRODUCER";
    public static final String traceTopic = "rmq_sys_TRACE_DATA_";
    public static final String default_region = "DEFAULT_REGION";
    public static char CONTENT_SPLITOR = 1;
    public static char FIELD_SPLITOR = 2;
}
