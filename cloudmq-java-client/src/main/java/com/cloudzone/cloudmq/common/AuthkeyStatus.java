package com.cloudzone.cloudmq.common;

/**
 * 消息类型的枚举
 * 注意这是和cloudzone数据库的类型一一对应不能随意更改index
 *
 * @author tantexian
 * @since 2017/1/9
 */
public enum AuthkeyStatus {

    NORMAL_MSG("普通消息", 0),
    ORDER_MSG("顺序消息", 1),
    DELAY_MSG("延迟消息", 2),
    SENDONEWAY("sendoneway", 3),
    TRANSACTION_MSG("事务消息", 4);

    // 成员变量
    private String name;
    private int index;


    private AuthkeyStatus(String name, int index) {
        this.name = name;
        this.index = index;
    }

    public static AuthkeyStatus getAuthkeyStatus(int index) {
        for (AuthkeyStatus authkeyStatus : AuthkeyStatus.values()) {
            if (authkeyStatus.getIndex() == index) {
                return authkeyStatus;
            }
        }
        return null;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
