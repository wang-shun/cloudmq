package com.alibaba.rocketmq.domain.gmq;

/**
 * @author: tianyuliang
 * @since: 2017/3/3
 */
public enum TrackTypeExt {

    /***
     * 订阅了，而且消费了（Offset越过了）
     */
    SUBSCRIBED_AND_CONSUMED(0, "消费成功"),

    /***
     * 订阅了，但是被过滤掉了
     */
    SUBSCRIBED_BUT_FILTERD(1, "未消费(消息被过滤)"),

    /***
     * 订阅了，但是是PULL，结果未知
     */
    SUBSCRIBED_BUT_PULL(2, "消费结果未知(消息Pull中)"),

    /***
     * 订阅了，但是没有消费（Offset小）
     */
    SUBSCRIBED_AND_NOT_CONSUME_YET(3, "未消费"),

    /***
     * 未知异常
     */
    UNKNOW_EXCEPTION(4, "未知异常");



    private int code;

    private String trackType;

    private TrackTypeExt(int code, String trackType) {
        this.code = code;
        this.trackType = trackType;
    }

    public static TrackTypeExt getTrackType(int code) {
        for (TrackTypeExt tt : TrackTypeExt.values()) {
            if (tt.code == code) {
                return tt;
            }
        }
        return null;
    }

    public static String getTrackTypeMsg(int code) {
        for (TrackTypeExt tt : TrackTypeExt.values()) {
            if (tt.code == code) {
                return tt.getTrackType();
            }
        }
        return "";
    }



    public String getTrackType() {
        return trackType;
    }


    public void setTrackType(String trackType) {
        this.trackType = trackType;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return trackType.toString();
    }
}
