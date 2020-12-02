package com.netease.nim.chatroom.demo.entertainment.helper;

import android.text.TextUtils;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.netease.nim.chatroom.demo.DemoCache;
import com.netease.nim.chatroom.demo.base.util.log.LogUtil;
import com.netease.nim.chatroom.demo.entertainment.constant.LiveType;
import com.netease.nim.chatroom.demo.entertainment.constant.MicStateEnum;
import com.netease.nim.chatroom.demo.entertainment.constant.PushLinkConstant;
import com.netease.nim.chatroom.demo.entertainment.constant.PushMicNotificationType;
import com.netease.nim.chatroom.demo.entertainment.http.ChatRoomHttpClient;
import com.netease.nim.chatroom.demo.entertainment.model.InteractionMember;
import com.netease.nim.chatroom.demo.entertainment.module.ConnectedAttachment;
import com.netease.nim.chatroom.demo.entertainment.module.DisconnectAttachment;
import com.netease.nim.chatroom.demo.entertainment.module.UserJoinAttachment;
import com.netease.nim.chatroom.demo.entertainment.module.UserLeaveAttachment;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.avchat.AVChatCallback;
import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.constant.AVChatType;
import com.netease.nimlib.sdk.avchat.constant.AVChatVideoScalingType;
import com.netease.nimlib.sdk.avchat.model.AVChatData;
import com.netease.nimlib.sdk.chatroom.ChatRoomMessageBuilder;
import com.netease.nimlib.sdk.chatroom.ChatRoomService;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomInfo;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomMessage;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomUpdateInfo;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.CustomNotification;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by hzxuwen on 2016/7/26.
 */
public class MicHelper {

    private static final String TAG = MicHelper.class.getSimpleName();

    public static MicHelper getInstance() {
        return InstanceHolder.instance;
    }


    public interface ChannelCallback {
        void onJoinChannelSuccess();

        void onJoinChannelFailed();
    }

    // 主播断开连麦
    public void masterBrokeMic(String roomId, String account) {
        // 主播通知连麦者断开
        sendCustomNotify(roomId, account, PushMicNotificationType.DISCONNECT_MIC.getValue(), null, false);
    }

    // 连麦者主动断开连麦
    public void audienceBrokeMic(boolean isVideoMode, boolean isDisableVideo, String meetingName) {
        leaveAndReleaseAVRoom(isVideoMode, isDisableVideo, meetingName);
    }

    // 去应用服务器移除队列
    public void popQueue(String roomId, String account) {
        ChatRoomHttpClient.getInstance().popMicLink(roomId, account, new ChatRoomHttpClient.ChatRoomHttpCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                LogUtil.d(TAG, "pop queue success");
            }

            @Override
            public void onFailed(int code, String errorMsg) {
                LogUtil.d(TAG, "pop queue failed, code:" + code + ", errorMsg:" + errorMsg);
                Toast.makeText(DemoCache.getContext(), "pop queque failed, errorMsg:" + errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 发送正在连麦通知
    public void sendLinkNotify(String roomId, InteractionMember member) {
        JSONObject json = new JSONObject();
        json.put(PushLinkConstant.LINK_STYLE, member.getAvChatType().getValue());
        MicHelper.getInstance().sendCustomNotify(roomId, member.getAccount(), PushMicNotificationType.CONNECTING_MIC.getValue(), json, true);
    }

    public void sendCustomNotify(String roomId, String toAccount, final int command, JSONObject json, boolean isSendOnline) {
        CustomNotification notification = new CustomNotification();
        notification.setSessionId(toAccount);
        notification.setSessionType(SessionTypeEnum.P2P);

        if (json == null) {
            json = new JSONObject();
        }
        json.put(PushLinkConstant.ROOM_ID, roomId);
        json.put(PushLinkConstant.COMMAND, command);
        notification.setContent(json.toString());
        notification.setSendToOnlineUserOnly(isSendOnline);
        NIMClient.getService(MsgService.class).sendCustomNotification(notification).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                LogUtil.d(TAG, "send custom json :" + notification.getContent());
            }

            @Override
            public void onFailed(int i) {
                LogUtil.d(TAG, "send custom notify failed, code:" + i);
            }

            @Override
            public void onException(Throwable throwable) {
                LogUtil.e(TAG, "send customNotification exception, throwable:" + throwable.getMessage());
            }
        });
    }

    //发生聊天室更新信息
    public void sendUpdateRoomExtension(String meetingUid, String meetingName, LiveType liveType, boolean typePk, String fromNickName, String toNickName, ChatRoomInfo roomInfo, String roomId) {
        LogUtil.i(TAG, "sendUpdateRoomExtension,fromNickName:" + fromNickName + ",toNickName:" + toNickName + ",roomId:" + roomId);
        ChatRoomUpdateInfo chatRoomUpdateInfo = new ChatRoomUpdateInfo();
        chatRoomUpdateInfo.setName(roomInfo.getName());
        Map<String, Object> notifyExt = new HashMap<>();
        notifyExt.put(PushLinkConstant.PK_INVITER, fromNickName);
        notifyExt.put(PushLinkConstant.PK_INVITEE, toNickName);
        notifyExt.put(PushLinkConstant.IS_PKING, typePk);
        notifyExt.put(PushLinkConstant.MEETING_NAME, meetingName);
        notifyExt.put(PushLinkConstant.MEETING_UID, meetingUid);
        notifyExt.put("type", liveType == LiveType.VIDEO_TYPE ? AVChatType.VIDEO.getValue() : AVChatType.AUDIO.getValue());
        chatRoomUpdateInfo.setExtension(notifyExt);
        NIMClient.getService(ChatRoomService.class).updateRoomInfo(roomId, chatRoomUpdateInfo, true, notifyExt);
    }

    /**
     * 发送PK邀请命令
     *
     * @param toAccount   需要邀请的主播
     * @param command     PK命令
     * @param meetingName 音视频房间号
     */
    public void sendCustomPKNotify(String toAccount, final int command, String meetingName) {
        sendCustomPKNotify(toAccount, command, meetingName, null, null);
    }

    public void sendCustomPKNotify(String toAccount, final int command, String meetingName, String pushUrl, String layoutParam) {
        CustomNotification notification = new CustomNotification();
        notification.setSessionId(toAccount);
        notification.setSessionType(SessionTypeEnum.P2P);

        JSONObject json = new JSONObject();
        json.put(PushLinkConstant.PK_ROOM_NAME, meetingName);
        json.put(PushLinkConstant.LINK_STYLE, AVChatType.VIDEO.getValue());
        json.put(PushLinkConstant.COMMAND, command);

        JSONObject infoJSON = new JSONObject();
        infoJSON.put(PushLinkConstant.NICK, DemoCache.getUserInfo().getName());
        infoJSON.put(PushLinkConstant.AVATAR, "avatar_default");

        if (!TextUtils.isEmpty(pushUrl)) {
            infoJSON.put(PushLinkConstant.PUSH_URL, pushUrl);
        }

        if (!TextUtils.isEmpty(layoutParam)) {
            infoJSON.put(PushLinkConstant.LAYOUT_PARA, layoutParam);
        }

        json.put(PushLinkConstant.INFO, infoJSON);
        notification.setContent(json.toString());

        NIMClient.getService(MsgService.class).sendCustomNotification(notification).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                LogUtil.d(TAG, "send custom type:" + command);
            }

            @Override
            public void onFailed(int i) {
                LogUtil.d(TAG, "send custom notify failed, code:" + i);
            }

            @Override
            public void onException(Throwable throwable) {
                LogUtil.e(TAG, "send customNotification exception, throwable:" + throwable.getMessage());
            }
        });
    }

    /**
     * 连麦者成功连上麦后，主播全局通知
     */
    public void sendConnectedMicMsg(String roomId, InteractionMember member) {
        if (member == null) {
            return;
        }
        ConnectedAttachment attachment = new ConnectedAttachment(member.getAccount(),
                member.getMeetingUid(),
                member.getName(),
                member.getAvatar(),
                member.getAvChatType().getValue());

        ChatRoomMessage message = ChatRoomMessageBuilder.createChatRoomCustomMessage(roomId, attachment);
        NIMClient.getService(ChatRoomService.class).sendMessage(message, false);

    }

    // 发送断开连麦自定义消息通知全局
    public void sendBrokeMicMsg(String roomId, String account) {

        DisconnectAttachment attachment = new DisconnectAttachment(account);
        ChatRoomMessage message = ChatRoomMessageBuilder.createChatRoomCustomMessage(roomId, attachment);
        NIMClient.getService(ChatRoomService.class).sendMessage(message, false);
    }


    public void sendUserLeaveMsg(String roomId, String account) {
        UserLeaveAttachment attachment = new UserLeaveAttachment(account);
        ChatRoomMessage message = ChatRoomMessageBuilder.createChatRoomCustomMessage(roomId, attachment);
        NIMClient.getService(ChatRoomService.class).sendMessage(message, false);
    }

    public void sendUserJoinMsg(String roomId, String account) {
        UserJoinAttachment attachment = new UserJoinAttachment(account);
        ChatRoomMessage message = ChatRoomMessageBuilder.createChatRoomCustomMessage(roomId, attachment);
        NIMClient.getService(ChatRoomService.class).sendMessage(message, false);
    }

    // 更新成员连麦状态
    public void updateMemberInChatRoom(String roomId, InteractionMember member) {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(PushLinkConstant.LINK_STYLE, member.getAvChatType().getValue());
        jsonObject.put(PushLinkConstant.LINK_STATE, MicStateEnum.CONNECTED.getValue());

        JSONObject info = new JSONObject();
        info.put(PushLinkConstant.NICK, member.getName());
        info.put(PushLinkConstant.AVATAR, member.getAvatar());
        info.put(PushLinkConstant.MEETING_UID, AVChatManager.getInstance().getUidByAccount(member.getAccount()));
        jsonObject.put(PushLinkConstant.INFO, info);

        NIMClient.getService(ChatRoomService.class).updateQueue(roomId, member.getAccount(), jsonObject.toString()).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                LogUtil.d(TAG, "update queue success");
            }

            @Override
            public void onFailed(int i) {
                LogUtil.d(TAG, "update queue failed, code:" + i);
            }

            @Override
            public void onException(Throwable throwable) {
                LogUtil.e(TAG, "update queue exception, throwable:" + throwable.getMessage());
            }
        });
    }

    /**************************** 音视频房间操作 **********************************/

    public void joinAVRoom(String meetingName, boolean isVideo, final ChannelCallback callback) {

        LogUtil.d(TAG, "joinAVRoom , isVideo : " + isVideo + " meetingName: " + meetingName);

        if (meetingName == null) {
            LogUtil.d(TAG, "meeting name is null,return");
            return;
        }
        AVChatManager.getInstance().joinRoom2(meetingName, isVideo ? AVChatType.VIDEO : AVChatType.AUDIO, new AVChatCallback<AVChatData>() {
            @Override
            public void onSuccess(AVChatData avChatData) {
                LogUtil.d(TAG, "join channel success");
                callback.onJoinChannelSuccess();

            }

            @Override
            public void onFailed(int i) {
                LogUtil.e(TAG, "join channel failed, code:" + i);
                AVChatManager.getInstance().leaveRoom2(meetingName, null);
                callback.onJoinChannelFailed();
            }

            @Override
            public void onException(Throwable throwable) {
                LogUtil.e(TAG, "join channel exception, throwable:" + throwable.getMessage());
            }
        });
    }

    public void joinAVRoom(String meetingName, boolean isVideo, final AVChatCallback<AVChatData> callback) {

        LogUtil.d(TAG, "joinAVRoom,isVideo:" + isVideo + " meetingName:" + meetingName);

        if (meetingName == null) {
            LogUtil.d(TAG, "meeting name is null,return");
            callback.onFailed(-1);
            return;
        }
        AVChatManager.getInstance().joinRoom2(meetingName, isVideo ? AVChatType.VIDEO : AVChatType.AUDIO, new AVChatCallback<AVChatData>() {
            @Override
            public void onSuccess(AVChatData avChatData) {
                LogUtil.d(TAG, "join channel success");
                callback.onSuccess(avChatData);

            }

            @Override
            public void onFailed(int i) {
                LogUtil.e(TAG, "join channel failed, code:" + i);
                AVChatManager.getInstance().leaveRoom2(meetingName, null);
                callback.onFailed(i);
            }

            @Override
            public void onException(Throwable throwable) {
                LogUtil.e(TAG, "join channel exception, throwable:" + throwable.getMessage());
                callback.onException(throwable);
            }
        });
    }

    // 离开音视频房间
    public void leaveAndReleaseAVRoom(boolean isVideoMode, boolean isDisableVideo, String meetingName) {
        LogUtil.d(TAG, "leaveRoom,isVideoMode:" + isVideoMode + " isDisableVideo:" + isDisableVideo + " meetingName:" + meetingName);
        if (meetingName == null) {
            LogUtil.d(TAG, "meeting name is null,return");
            return;
        }
        if (isVideoMode) {
            AVChatManager.getInstance().setupLocalVideoRender(null, false, AVChatVideoScalingType.SCALE_ASPECT_BALANCED);
            AVChatManager.getInstance().stopVideoPreview();
        }
        if (isDisableVideo) {
            AVChatManager.getInstance().disableVideo();
        }
        AVChatManager.getInstance().leaveRoom2(meetingName, new AVChatCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                LogUtil.d(TAG, "leave channel success");
            }

            @Override
            public void onFailed(int i) {
                LogUtil.e(TAG, "leave channel failed, code:" + i);
            }

            @Override
            public void onException(Throwable throwable) {
                LogUtil.e(TAG, "leave channel exception, throwable:" + throwable.getMessage());
            }
        });
        AVChatManager.getInstance().disableRtc();
    }

    /**
     * ************************************ 单例 ***************************************
     */
    static class InstanceHolder {
        final static MicHelper instance = new MicHelper();
    }
}
