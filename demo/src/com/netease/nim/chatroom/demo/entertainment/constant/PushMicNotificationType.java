package com.netease.nim.chatroom.demo.entertainment.constant;

/**
 * Created by hzxuwen on 2016/7/22.
 */
public enum PushMicNotificationType {
    /**
     * 加入连麦队列通知
     */
    JOIN_QUEUE(1),
    /**
     * 退出连麦队列通知
     */
    EXIT_QUEUE(2),
    /**
     * 主播选择某人连麦
     */
    CONNECTING_MIC(3),
    /**
     * 主播断开某人连麦
     */
    DISCONNECT_MIC(4),
    /**
     * 观众拒绝主播的连麦选择
     */
    REJECT_CONNECTING(5),
    /**
     * 尝试发起PK邀请(无法判断对方是否在线，因此先尝试发送邀请，等对方回复在线后进行真正的PK邀请)
     */
    TRY_INVITE_ANCHOR(6),
    /**
     * 回复当前在线
     */
    REPLY_INVITATION(7),
    /**
     * 邀请主播PK通知
     */
    INVITE_ANCHOR(8),
    /**
     * 取消PK（邀请方和被邀请方都可能发送）
     */
    CANCEL_INTERACT(9),
    /**
     * 同意主播PK邀请
     */
    AGREE_INVITATION(10),
    /**
     * 拒绝主播PK邀请
     */
    REJECT_INVITATION(11),
    /**
     * 无效,对方有可能不是主播或者暂时不能参加PK
     */
    INVALID(12),
    /**
     * 正在PK中（主播正在PK中，无法接收新的PK请求）
     */
    ININTER_ACTIONS(13),
    /**
     * 对方已经退出音视频房间
     */
    EXITED(14);


    private int value;

    PushMicNotificationType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static PushMicNotificationType typeOfValue(int value) {
        for (PushMicNotificationType e : values()) {
            if (e.getValue() == value) {
                return e;
            }
        }
        return JOIN_QUEUE;
    }
}
