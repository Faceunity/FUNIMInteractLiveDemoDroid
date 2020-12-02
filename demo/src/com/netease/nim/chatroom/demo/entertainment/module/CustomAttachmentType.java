package com.netease.nim.chatroom.demo.entertainment.module;

/**
 * Created by zhoujianghua on 2015/4/9.
 */
public interface CustomAttachmentType {
    // 多端统一
    int GIFT = 0; // 礼物

    int ADD_LIKE = 1; // 点赞

    int CONNECTED_MIC = 2; // 同意互动连接

    int DISCONNECT_MIC = 3; // 断开互动连接

    int USER_LEAVE = 6; //用户离开

    int USER_JOIN = 7; //用户加入
}
