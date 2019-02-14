package com.netease.nim.chatroom.demo.entertainment.constant;

/**
 * Created by hzxuwen on 2016/7/25.
 */
public enum MicStateEnum {
    /**
     * 等待
     */
    NONE(0),
    /**
     * 申请连接
     */
    WAITING(1),
    /**
     * 连接中
     */
    CONNECTING(2),
    /**
     * 已连接
     */
    CONNECTED(3),
    /**
     * 正在离开
     */
    LEAVING(4);

    private int value;

    MicStateEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static MicStateEnum typeOfValue(int value) {
        for (MicStateEnum e : values()) {
            if (e.getValue() == value) {
                return e;
            }
        }
        return NONE;
    }
}
