package com.netease.nim.chatroom.demo.entertainment.module;

import com.alibaba.fastjson.JSONObject;

/**
 * 点赞附件
 * Created by hzxuwen on 2016/3/30.
 */
public class LikeAttachment extends CustomAttachment {
    public LikeAttachment() {
        super(CustomAttachmentType.like);
    }

    @Override
    protected void parseData(JSONObject data) {

    }

    @Override
    protected JSONObject packData() {
        return null;
    }
}
