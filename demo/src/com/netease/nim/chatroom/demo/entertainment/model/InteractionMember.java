package com.netease.nim.chatroom.demo.entertainment.model;

import com.netease.nim.chatroom.demo.entertainment.constant.MicStateEnum;
import com.netease.nimlib.sdk.avchat.constant.AVChatType;

/**
 * Created by hzxuwen on 2016/7/15.
 */
public class InteractionMember {
    private String account;
    private String name;
    private String avatar;
    private AVChatType avChatType;
    private boolean isSelected;
    private MicStateEnum micStateEnum;

    public InteractionMember(String account, String name, String avatar, AVChatType avChatType) {
        this.account = account;
        this.name = name;
        this.avatar = avatar;
        this.avChatType = avChatType;
        this.isSelected = false;
        this.micStateEnum = MicStateEnum.NONE;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public AVChatType getAvChatType() {
        return avChatType;
    }

    public void setAvChatType(AVChatType avChatType) {
        this.avChatType = avChatType;
    }

    public MicStateEnum getMicStateEnum() {
        return micStateEnum;
    }

    public void setMicStateEnum(MicStateEnum micStateEnum) {
        this.micStateEnum = micStateEnum;
    }
}
