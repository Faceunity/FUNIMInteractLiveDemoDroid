package com.netease.nim.chatroom.demo.entertainment.model;


import com.netease.nimlib.sdk.chatroom.model.ChatRoomInfo;

import java.io.Serializable;

public class LiveInfoMode implements Serializable {

    private String meetingName;

    private String roomId;

    private String pushUrl;

    private String pullUrl;

    private int liveType;


    private ChatRoomInfo chatRoomInfo;

    public String getMeetingName() {
        return meetingName;
    }

    public void setMeetingName(String meetingName) {
        this.meetingName = meetingName;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getPushUrl() {
        return pushUrl;
    }

    public void setPushUrl(String pushUrl) {
        this.pushUrl = pushUrl;
    }

    public String getPullUrl() {
        return pullUrl;
    }

    public void setPullUrl(String pullUrl) {
        this.pullUrl = pullUrl;
    }


    public void setLiveType(int liveType) {
        this.liveType = liveType;
    }

    public int getLiveType() {
        return liveType;
    }

    public void setChatRoomInfo(ChatRoomInfo roomInfo) {
        this.chatRoomInfo = roomInfo;
    }

    public ChatRoomInfo getChatRoomInfo() {
        return chatRoomInfo;
    }
}
