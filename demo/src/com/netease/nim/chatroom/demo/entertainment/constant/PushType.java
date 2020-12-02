package com.netease.nim.chatroom.demo.entertainment.constant;


/**
 * Created by rubin on 2019/10/29.
 */
public enum PushType {

    /**
     * 主播推流
     */
    ANCHOR_PUSH_TYPE(1),

    /**
     * 房间推流
     */
    ROOM_PUSH_TYPE(2);

    private int value;

    PushType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static PushType typeOfValue(int value) {
        for (PushType e : values()) {
            if (e.getValue() == value) {
                return e;
            }
        }
        return ANCHOR_PUSH_TYPE;
    }
}
