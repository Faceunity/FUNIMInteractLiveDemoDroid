package com.netease.nim.chatroom.demo.entertainment.constant;

/**
 * Created by hzxuwen on 2016/7/25.
 */
public class PushLinkConstant {
    public final static String roomid = "roomid";
    public final static String meetingName = "meetingName";
    public final static String orientation = "orientation";
    public final static String pkRoomName = "room_name";

    /**
     * 聊天室类型， -1表示未开始直播；1，音频直播；2，视频直播
     */
    public final static String type = "type";
    /**
     * 连麦请求类型，请求语音连麦或者视频连麦。
     */
    public final static String style = "style";
    /**
     * 控制命令, 指令类型参考{@link PushMicNotificationType}
     */
    public final static String command = "command";
    /**
     * 连麦状态，指令类型参考{@link MicStateEnum}
     */
    public final static String state = "state";

    /** 其他信息 **/
    public final static String info = "info";
    public final static String nick = "nick";
    public final static String avatar = "avatar";
    public final static String pkInviter = "pkinviter";//pk邀请者
    public final static String pkInvitee = "pkinvitee";//pk被邀请者
    public final static String isPking = "ispking";//是否处于pk状态
}
