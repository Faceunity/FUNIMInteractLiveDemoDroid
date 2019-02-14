package com.netease.nim.chatroom.demo.entertainment.constant;

/**
 * Created by hzxuwen on 2016/3/30.
 */
public enum GiftType {
    /**
     * 未知
     */
    UNKNOWN(-1),
    /**
     * 玫瑰
     */
    ROSE(0),
    /**
     * 巧克力
     */
    CHOCOLATE(1),

    /**
     * 可爱熊
     */
    BEAR(2),

    /**
     * 冰淇淋
     */
    ICECREAM(3);

    private int value;

    GiftType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static GiftType typeOfValue(int value) {
        for (GiftType e : values()) {
            if (e.getValue() == value) {
                return e;
            }
        }
        return UNKNOWN;
    }
}
