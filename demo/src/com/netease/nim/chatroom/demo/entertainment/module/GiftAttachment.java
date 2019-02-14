package com.netease.nim.chatroom.demo.entertainment.module;

import com.alibaba.fastjson.JSONObject;
import com.netease.nim.chatroom.demo.entertainment.constant.GiftType;

/**
 * 礼物附件
 * Created by hzxuwen on 2016/3/28.
 */
public class GiftAttachment extends CustomAttachment {
    private final String KEY_PRESENT = "present";
    private final String KEY_COUNT = "count";

    private GiftType giftType;
    private int count;

    GiftAttachment() {
        super(CustomAttachmentType.gift);
    }

    public GiftAttachment(GiftType giftType, int count) {
        this();
        this.giftType = giftType;
        this.count = count;
    }

    @Override
    protected void parseData(JSONObject data) {
        this.giftType = GiftType.typeOfValue(data.getIntValue(KEY_PRESENT));
        this.count = data.getIntValue(KEY_COUNT);
    }

    @Override
    protected JSONObject packData() {
        JSONObject data = new JSONObject();
        data.put(KEY_PRESENT, giftType.getValue());
        data.put(KEY_COUNT, count);
        return data;
    }

    public GiftType getGiftType() {
        return giftType;
    }

    public void setGiftType(GiftType giftType) {
        this.giftType = giftType;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
