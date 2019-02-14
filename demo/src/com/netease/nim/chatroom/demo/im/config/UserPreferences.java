package com.netease.nim.chatroom.demo.im.config;

import android.content.Context;
import android.content.SharedPreferences;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.netease.nim.chatroom.demo.DemoCache;
import com.netease.nimlib.sdk.StatusBarNotificationConfig;

/**
 * Created by hzxuwen on 2015/4/13.
 */
public class UserPreferences {
    private final static String KEY_DOWNTIME_TOGGLE = "down_time_toggle";
    private final static String KEY_SB_NOTIFY_TOGGLE = "sb_notify_toggle";
    private final static String KEY_TEAM_ANNOUNCE_CLOSED = "team_announce_closed";
    private final static String KEY_STATUS_BAR_NOTIFICATION_CONFIG = "KEY_STATUS_BAR_NOTIFICATION_CONFIG";

    private final static String KEY_EARPHONE_MODE = "KEY_EARPHONE_MODE";

    private final static String KEY_USER_IDENTIFY = "KEY_USER_IDENTIFY";
    private final static String KEY_PLAYER_STRATEGY = "KEY_PLAYER_STRATEGY";

    public static void setPlayerStrategy(boolean isLowLatency) {
        saveBoolean(KEY_PLAYER_STRATEGY, isLowLatency);
    }

    public static int getPlayerStrategy() {
        return getBoolean(KEY_PLAYER_STRATEGY, true) ? 0 : 1; //0:直播低延时；1:点播抗抖动
    }

    public static void setTeacherIdentify(boolean isTeacher) {
        saveBoolean(KEY_USER_IDENTIFY, isTeacher);
    }

    public static boolean isTeacherIdentify() {
        return getBoolean(KEY_USER_IDENTIFY, false);
    }

    public static void setEarPhoneModeEnable(boolean on) {
        saveBoolean(KEY_EARPHONE_MODE, on);
    }

    public static boolean isEarPhoneModeEnable() {
        return getBoolean(KEY_EARPHONE_MODE, true);
    }

    public static void setNotificationToggle(boolean on) {
        saveBoolean(KEY_SB_NOTIFY_TOGGLE, on);
    }

    public static boolean getNotificationToggle() {
        return getBoolean(KEY_SB_NOTIFY_TOGGLE, true);
    }

    public static void setDownTimeToggle(boolean on) {
        saveBoolean(KEY_DOWNTIME_TOGGLE, on);
    }

    public static boolean getDownTimeToggle() {
        return getBoolean(KEY_DOWNTIME_TOGGLE, false);
    }

    public static void setStatusConfig(StatusBarNotificationConfig config) {
        saveStatusBarNotificationConfig(KEY_STATUS_BAR_NOTIFICATION_CONFIG, config);
    }

    public static StatusBarNotificationConfig getStatusConfig() {
        return getConfig(KEY_STATUS_BAR_NOTIFICATION_CONFIG);
    }

    private static StatusBarNotificationConfig getConfig(String key) {
        StatusBarNotificationConfig config = new StatusBarNotificationConfig();
        String jsonString = getSharedPreferences().getString(key, "");
        try {
            JSONObject jsonObject = JSONObject.parseObject(jsonString);
            if (jsonObject == null || config == null) {
                return null;
            }
            config.downTimeBegin = jsonObject.getString("downTimeBegin");
            config.downTimeEnd = jsonObject.getString("downTimeEnd");
            config.downTimeToggle = jsonObject.getBoolean("downTimeToggle");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return config;
    }

    private static void saveStatusBarNotificationConfig(String key, StatusBarNotificationConfig config) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("downTimeBegin", config.downTimeBegin);
            jsonObject.put("downTimeEnd", config.downTimeEnd);
            jsonObject.put("downTimeToggle", config.downTimeToggle);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        editor.putString(key, jsonObject.toString());
        editor.commit();
    }

    private static boolean getBoolean(String key, boolean value) {
        return getSharedPreferences().getBoolean(key, value);
    }

    private static void saveBoolean(String key, boolean value) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    static SharedPreferences getSharedPreferences() {
        return DemoCache.getContext().getSharedPreferences("Demo." + DemoCache.getAccount(), Context.MODE_PRIVATE);
    }
}
