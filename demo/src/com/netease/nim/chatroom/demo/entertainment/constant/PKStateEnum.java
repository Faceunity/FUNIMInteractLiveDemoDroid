package com.netease.nim.chatroom.demo.entertainment.constant;

/**
 *
 */
public enum PKStateEnum {
    /**
     * 正常状态
     */
    NONE(0),
    /**
     * 发起PK等待回应
     */
    WAITING(1),
    /**
     * 对方回复在线
     */
    ONLINE(2),
    /**
     * 收到PK申请
     */
    BE_INVITATION(3),
    /**
     * PK中
     */
    PKING(4),
    /**
     * 邀请PK超时
     */
    INVITE_TIME_OUT(5),
    /**
     * 退出PK
     */
    EXITED(6);

    private int value;

    PKStateEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static PKStateEnum typeOfValue(int value) {
        for (PKStateEnum e : values()) {
            if (e.getValue() == value) {
                return e;
            }
        }
        return NONE;
    }
}
