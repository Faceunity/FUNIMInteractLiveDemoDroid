package com.netease.nim.chatroom.demo.entertainment.constant;

/**
 * 观众申请连麦模式
 * Created by hzxuwen on 2016/8/6.
 */
public enum MicApplyEnum {
    /**
     * 视频模式下的视频连麦
     */
    VIDEO_VIDEO(0),
    /**
     * 视频模式下的语音连麦
     */
    VIDEO_AUDIO(1),
    /**
     * 语音模式下的连麦
     */
    AUDIO(2);

    private int value;

    MicApplyEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static MicApplyEnum typeOfValue(int value) {
        for (MicApplyEnum e : values()) {
            if (e.getValue() == value) {
                return e;
            }
        }
        return VIDEO_VIDEO;
    }
}
