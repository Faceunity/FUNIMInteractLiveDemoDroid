package com.netease.nim.chatroom.demo.entertainment.module;

import com.alibaba.fastjson.JSONObject;

/**
 * 同意连麦附件
 * Created by hzxuwen on 2016/3/28.
 */
public class ConnectedAttachment extends CustomAttachment {
    private final String KEY_UID = "uid";
    private final String KEY_NICK = "nick";
    private final String KEY_AVATAR = "AVATAR";
    private final String KEY_STYLE = "style";

    private String account;
    private String nick;
    private String avatar;
    private int style;

    ConnectedAttachment() {
        super(CustomAttachmentType.connectedMic);
    }

    public ConnectedAttachment(String account, String nick, String avatar, int style) {
        this();
        this.account = account;
        this.nick = nick;
        this.avatar = avatar;
        this.style = style;
    }

    @Override
    protected void parseData(JSONObject data) {
        this.account = data.getString(KEY_UID);
        this.nick = data.getString(KEY_NICK);
        this.avatar = data.getString(KEY_AVATAR);
        this.style = data.getIntValue(KEY_STYLE);
    }

    @Override
    protected JSONObject packData() {
        JSONObject data = new JSONObject();
        data.put(KEY_UID, account);
        data.put(KEY_NICK, nick);
        data.put(KEY_AVATAR, avatar);
        data.put(KEY_STYLE, style);
        return data;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public int getStyle() {
        return style;
    }

    public void setStyle(int style) {
        this.style = style;
    }
}
