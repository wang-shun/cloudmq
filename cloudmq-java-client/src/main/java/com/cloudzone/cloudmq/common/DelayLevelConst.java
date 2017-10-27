package com.cloudzone.cloudmq.common;

/**
 * 延时间隔等级（目前不支持任意时间设置，只支持以下等级内的时间间隔）
 *
 * 以下为与之相对应的存储配置文件值messageDelayLevelprivate <br/>
 * <code>
 *  String messageDelayLevel = "1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h";
 * </code>
 *
 * @author tantexian
 * @since 2016/6/28
 */
public enum DelayLevelConst {
    OneSecond("1s", 1),
    FiveSecond("5s", 2),
    TenSecond("10s", 3),
    ThirtySecond("30s", 4),
    OneMinute("1m", 5),
    TwoMinute("2m", 6),
    ThreeMinute("3m", 7),
    FourMinute("4m", 8),
    FiveMinute("5m", 9),
    SixMinute("6m", 10),
    SevenMinute("7m", 11),
    EightMinute("8m", 12),
    NineMinute("9m", 13),
    TenMinute("10m", 14),
    TwentyMinute("20m", 15),
    ThirtyMinute("30m", 16),
    OneHour("1h", 17),
    TwoHour("2h", 18);

    private String delayTime;
    private int delayLevel;


    DelayLevelConst(String delayTime, int delayLevel) {
        this.delayTime = delayTime;
        this.delayLevel = delayLevel;
    }


    public int val() {
        return this.delayLevel;
    }
}
