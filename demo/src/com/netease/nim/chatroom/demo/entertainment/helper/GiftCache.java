package com.netease.nim.chatroom.demo.entertainment.helper;

import java.util.HashMap;
import java.util.Map;

/**
 * 礼物缓存
 * Created by hzxuwen on 2016/4/8.
 */
public class GiftCache {

    public static GiftCache getInstance() {
        return InstanceHolder.instance;
    }

    private Map<String, Map<Integer, Integer>> cache = new HashMap<>();

    public void clear() {
        cache.clear();
    }

    public void saveGift(String roomId, int type) {
        Map<Integer, Integer> gifts = cache.get(roomId);
        if (gifts == null) {
            gifts = new HashMap<>();
            gifts.put(type, 1);
        } else if (!gifts.containsKey(type)) {
            gifts.put(type, 1);
        } else {
            int count = gifts.get(type);
            gifts.put(type, ++count);
        }
        cache.put(roomId, gifts);
    }

    public Map<Integer, Integer> getGift(String roomId) {
        return cache.get(roomId);
    }

    /**
     * ************************************ 单例 ***************************************
     */
    static class InstanceHolder {
        final static GiftCache instance = new GiftCache();
    }
}
