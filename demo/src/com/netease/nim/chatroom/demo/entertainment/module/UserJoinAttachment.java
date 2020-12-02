package com.netease.nim.chatroom.demo.entertainment.module;

import com.alibaba.fastjson.JSONObject;


public class UserJoinAttachment extends CustomAttachment {

    private final String KEY_UID = "uid";

    private String account;

    public UserJoinAttachment() {
        super(CustomAttachmentType.USER_JOIN);
    }

    public UserJoinAttachment(String account) {
        this();
        this.account = account;
    }

    @Override
    protected void parseData(JSONObject data) {
        this.account = data.getString(KEY_UID);
    }

    @Override
    protected JSONObject packData() {
        JSONObject data = new JSONObject();
        data.put(KEY_UID, account);
        return data;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

}
