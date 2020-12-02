package com.netease.nim.chatroom.demo.entertainment.constant;

/**
 * Created by hzxuwen on 2016/7/25.
 */
public class PushLinkConstant {

    public final static String ROOM_ID = "roomid";
    public final static String MEETING_NAME = "meetingName";
    public final static String MEETING_UID = "meetingUid";
    public final static String ORIENTATION = "orientation";
    public final static String PK_ROOM_NAME = "room_name";

    /**
     * 聊天室类型， -1表示未开始直播；1，音频直播；2，视频直播
     */
    public final static String LIVE_TYPE = "type";

    /**
     * 聊天室推流类型 ， 1：主播推流      2：房间推流
     */
    public final static String PUSH_TYPE = "push_type";

    /**
     * 连麦请求类型，请求语音连麦或者视频连麦。
     */
    public final static String LINK_STYLE = "style";


    /**
     * 控制命令, 指令类型参考{@link PushMicNotificationType}
     */
    public final static String COMMAND = "command";

    /**
     * 连麦状态，指令类型参考{@link MicStateEnum}
     */
    public final static String LINK_STATE = "state";

    /**
     * 其他信息
     **/
    public final static String INFO = "info";
    public final static String NICK = "nick";
    public final static String AVATAR = "avatar";

    /**
     * pk邀请者
     */
    public final static String PK_INVITER = "pkinviter";

    /**
     * pk被邀请者
     */
    public final static String PK_INVITEE = "pkinvitee";


    /**
     * 是否处于pk状态
     */
    public final static String IS_PKING = "ispking";

    /**
     * PK 对端推流URL
     */
    public final static String PUSH_URL = "push_url";

    /**
     * PK 对端推流布局参数
     */
    public final static String LAYOUT_PARA = "layout_param";


}
