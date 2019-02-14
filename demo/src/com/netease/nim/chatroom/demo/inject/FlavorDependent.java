package com.netease.nim.chatroom.demo.inject;

import android.app.Activity;

import com.netease.nim.chatroom.demo.entertainment.activity.IdentifyActivity;
import com.netease.nim.chatroom.demo.entertainment.helper.ChatRoomMemberCache;
import com.netease.nim.chatroom.demo.entertainment.helper.GiftCache;
import com.netease.nim.chatroom.demo.entertainment.module.CustomAttachParser;
import com.netease.nimlib.sdk.msg.attachment.MsgAttachmentParser;

/**
 * Created by huangjun on 2016/3/15.
 */
public class FlavorDependent implements IFlavorDependent{
    @Override
    public String getFlavorName() {
        return "entertainment";
    }

    @Override
    public Class<? extends Activity> getMainClass() {
        return IdentifyActivity.class;
    }

    @Override
    public MsgAttachmentParser getMsgAttachmentParser() {
        return new CustomAttachParser();
    }

    @Override
    public void onLogout() {
        ChatRoomMemberCache.getInstance().clear();
        GiftCache.getInstance().clear();
    }

    public static FlavorDependent getInstance() {
        return InstanceHolder.instance;
    }

    public static class InstanceHolder {
        public final static FlavorDependent instance = new FlavorDependent();
    }

    @Override
    public void onApplicationCreate() {

    }
}
