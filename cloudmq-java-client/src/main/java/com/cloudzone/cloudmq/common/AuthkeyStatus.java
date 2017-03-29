package com.cloudzone.cloudmq.common;

/**
 * 权限状态枚举类 <br/>
 * 0-只读，1-可读写，2-可持久化
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
