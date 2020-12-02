package com.netease.nim.chatroom.demo.entertainment.module;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.netease.nimlib.sdk.msg.attachment.MsgAttachment;
import com.netease.nimlib.sdk.msg.attachment.MsgAttachmentParser;

/**
 * Created by zhoujianghua on 2015/4/9.
 */
public class CustomAttachParser implements MsgAttachmentParser {

    private static final String KEY_TYPE = "type";
    private static final String KEY_DATA = "data";

    @Override
    public MsgAttachment parse(String json) {
        CustomAttachment attachment = null;
        try {
            JSONObject object = JSON.parseObject(json);
            int type = object.getInteger(KEY_TYPE);
            JSONObject data = object.getJSONObject(KEY_DATA);
            switch (type) {
                case CustomAttachmentType.GIFT:
                    attachment = new GiftAttachment();
                    break;
                case CustomAttachmentType.ADD_LIKE:
                    attachment = new LikeAttachment();
                    break;
                case CustomAttachmentType.CONNECTED_MIC:
                    attachment = new ConnectedAttachment();
                    break;
                case CustomAttachmentType.DISCONNECT_MIC:
                    attachment = new DisconnectAttachment();
                    break;
                case CustomAttachmentType.USER_LEAVE:
                    attachment = new UserLeaveAttachment();
                    break;
                case CustomAttachmentType.USER_JOIN:
                    attachment = new UserJoinAttachment();
                    break;
                default:
                    attachment = new DefaultCustomAttachment();
                    break;
            }

            attachment.fromJson(data);
        } catch (Exception e) {

        }

        return attachment;
    }

    public static String packData(int type, JSONObject data) {
        JSONObject object = new JSONObject();
        object.put(KEY_TYPE, type);
        if (data != null) {
            object.put(KEY_DATA, data);
        }

        return object.toJSONString();
    }
}
