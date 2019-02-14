package com.netease.nim.chatroom.demo.entertainment.http;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.netease.nim.chatroom.demo.DemoCache;
import com.netease.nim.chatroom.demo.base.http.NimHttpClient;
import com.netease.nim.chatroom.demo.base.util.log.LogUtil;
import com.netease.nim.chatroom.demo.config.DemoServers;

import java.util.HashMap;
import java.util.Map;

/**
 * 网易云信Demo聊天室Http客户端。第三方开发者请连接自己的应用服务器。
 * <p/>
 * Created by huangjun on 2016/2/22.
 */
public class ChatRoomHttpClient {

    public class EnterRoomParam {
        /**
         * 创建房间成功返回的房间号
         */
        private String roomId;
        /**
         * 推流地址
         */
        private String pushUrl;
        /**
         * 拉流地址
         */
        private String pullUrl;


        private String avType;

        private int orientation;

        public String getRoomId() {
            return roomId;
        }

        public void setRoomId(String roomId) {
            this.roomId = roomId;
        }

        public String getPushUrl() {
            return pushUrl;
        }

        public void setPushUrl(String pushUrl) {
            this.pushUrl = pushUrl;
        }

        public String getPullUrl() {
            return pullUrl;
        }

        public void setPullUrl(String pullUrl) {
            this.pullUrl = pullUrl;
        }

        public String getAvType() {
            return avType;
        }

        public void setAvType(String avType) {
            this.avType = avType;
        }

        public int getOrientation() {
            return orientation;
        }

        public void setOrientation(int orientation) {
            this.orientation = orientation;
        }
    }

    private static final String TAG = ChatRoomHttpClient.class.getSimpleName();

    // code
    private static final int RESULT_CODE_SUCCESS = 200;

    // api
    private static final String API_NAME_CHAT_ROOM_LIST = "homeList";
    private static final String API_NAME_MASTER_ENTRANCE = "hostEntrance";
    private static final String API_NAME_REQUEST_ADDRESS = "requestAddress";
    private static final String API_NAME_PUSH_MIC_LINK = "pushMicLink";
    private static final String API_NAME_POP_MIC_LINK = "popMicLink";

    // header
    private static final String HEADER_KEY_APP_KEY = "appkey";
    private static final String HEADER_KEY_CONTENT_TYPE = "Content-type";

    // result
    private static final String RESULT_KEY_ERROR_MSG = "errmsg";
    private static final String RESULT_KEY_RES = "res";
    private static final String RESULT_KEY_MSG = "msg";
    private static final String RESULT_KEY_TOTAL = "total";
    private static final String RESULT_KEY_LIST = "list";
    private static final String RESULT_KEY_NAME = "name";
    private static final String RESULT_KEY_CREATOR = "creator";
    private static final String RESULT_KEY_STATUS = "status";
    private static final String RESULT_KEY_ANNOUNCEMENT = "announcement";
    private static final String RESULT_KEY_EXT = "avType";
    private static final String RESULT_KEY_ROOM_ID = "roomid";
    private static final String RESULT_KEY_BROADCAST_URL = "broadcasturl";
    private static final String RESULT_KEY_ONLINE_USER_COUNT = "onlineusercount";
    private static final String RESULT_KEY_AVTYPE = "avType";
    private static final String RESULT_KEY_ORIENTATION = "orientation";

    private static final String RESULT_KEY_LIVE = "live";
    private static final String RESULT_KEY_PUSH_URL = "pushUrl";
    private static final String RESULT_KEY_PULL_URL = "rtmpPullUrl";

    // request
    private static final String REQUEST_USER_UID = "uid"; // 用户id
    private static final String REQUEST_ROOM_NAME = "name"; // 直播间名称
    private static final String REQUEST_STREAM_TYPE = "type"; // 推流类型（0:rtmp；1:hls；2:http），默认为0
    private static final String REQUEST_ROOM_ID = "roomid"; // 直播间id
    private static final String REQUEST_ROOM_EXT = "ext"; // 直播间扩展字段
    private static final String REQUEST_AVTYPE = "avType"; // 主播直播类型
    private static final String REQUEST_ORIENTATION = "orientation"; // 主播直播方向

    // param
    public static final String KEY_VIDEO = "VIDEO";
    public static final String KEY_AUDIO = "AUDIO";

    public interface ChatRoomHttpCallback<T> {
        void onSuccess(T t);

        void onFailed(int code, String errorMsg);
    }

    private static ChatRoomHttpClient instance;

    public static synchronized ChatRoomHttpClient getInstance() {
        if (instance == null) {
            instance = new ChatRoomHttpClient();
        }

        return instance;
    }

    private ChatRoomHttpClient() {
        NimHttpClient.getInstance().init(DemoCache.getContext());
    }

    /**
     * 主播创建直播间
     */
    public void masterEnterRoom(String account, String ext, boolean isVideoMode,boolean isPortrait, final ChatRoomHttpCallback<EnterRoomParam> callback) {
        String url = DemoServers.chatRoomAPIServer() + API_NAME_MASTER_ENTRANCE;

        Map<String, String> headers = new HashMap<>(2);
        String appKey = readAppKey();
        headers.put(HEADER_KEY_APP_KEY, appKey);
        headers.put(HEADER_KEY_CONTENT_TYPE, "application/x-www-form-urlencoded;charset=utf-8");

        StringBuilder body = new StringBuilder();
        body.append(REQUEST_USER_UID).append("=").append(account).append("&")
                .append(REQUEST_ROOM_EXT).append("=").append(ext).append("&")
                .append(REQUEST_AVTYPE).append("=").append(isVideoMode ? KEY_VIDEO : KEY_AUDIO).append("&")
                .append(REQUEST_ORIENTATION).append("=").append(isPortrait ? 1 : 2);
        String bodyString = body.toString();

        NimHttpClient.getInstance().execute(url, headers, bodyString, new NimHttpClient.NimHttpCallback() {
            @Override
            public void onResponse(String response, int code, String errorMsg) {
                if (code != 0) {
                    LogUtil.e(TAG, "masterEnterRoom failed : code = " + code + ", errorMsg = " + errorMsg);
                    if (callback != null) {
                        callback.onFailed(code, errorMsg);
                    }
                    return;
                }

                try {
                    // ret 0
                    JSONObject res = JSONObject.parseObject(response);
                    // res 1
                    int resCode = res.getIntValue(RESULT_KEY_RES);
                    if (resCode == RESULT_CODE_SUCCESS) {
                        // msg 1
                        JSONObject msg = res.getJSONObject(RESULT_KEY_MSG);
                        EnterRoomParam param = new EnterRoomParam();
                        param.setRoomId(msg.getString(RESULT_KEY_ROOM_ID));

                        if (msg != null) {
                            JSONObject live = msg.getJSONObject(RESULT_KEY_LIVE);
                            param.setPushUrl(live.getString(RESULT_KEY_PUSH_URL));
                            param.setPullUrl(live.getString(RESULT_KEY_PULL_URL));
                        }
                        // reply
                        callback.onSuccess(param);
                    } else {
                        LogUtil.e(TAG, "masterEnterRoom failed : code = " + code + ", errorMsg = " + res.getString(RESULT_KEY_ERROR_MSG));
                        callback.onFailed(resCode, res.getString(RESULT_KEY_ERROR_MSG));
                    }
                } catch (JSONException e) {
                    LogUtil.e(TAG, "NimHttpClient onResponse on JSONException, e=" + e.getMessage());
                    callback.onFailed(-1, e.getMessage());
                }
            }
        });
    }

    public void studentEnterRoom(String account, String roomId, final ChatRoomHttpCallback<EnterRoomParam> callback) {
        String url = DemoServers.chatRoomAPIServer() + API_NAME_REQUEST_ADDRESS;

        Map<String, String> headers = new HashMap<>(2);
        String appKey = readAppKey();
        headers.put(HEADER_KEY_APP_KEY, appKey);
        headers.put(HEADER_KEY_CONTENT_TYPE, "application/json; charset=utf-8");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(REQUEST_ROOM_ID, roomId);
        jsonObject.put(REQUEST_USER_UID, account);

        NimHttpClient.getInstance().execute(url, headers, jsonObject.toString(), new NimHttpClient.NimHttpCallback() {
            @Override
            public void onResponse(String response, int code, String errorMsg) {
                if (code != 0) {
                    LogUtil.e(TAG, "studentEnterRoom failed : code = " + code + ", errorMsg = " + errorMsg);
                    if (callback != null) {
                        callback.onFailed(code, errorMsg);
                    }
                    return;
                }

                try {
                    // ret 0
                    JSONObject res = JSONObject.parseObject(response);
                    // res 1
                    int resCode = res.getIntValue(RESULT_KEY_RES);
                    if (resCode == RESULT_CODE_SUCCESS) {
                        // msg 1
                        JSONObject msg = res.getJSONObject(RESULT_KEY_MSG);

                        String url = "";
                        String avType = "";
                        int orientation = 1;

                        if (msg != null) {
                            JSONObject live = msg.getJSONObject(RESULT_KEY_LIVE);
                            url = live.getString(RESULT_KEY_PULL_URL);
                            avType = live.getString(RESULT_KEY_AVTYPE);
                            orientation = live.getIntValue(RESULT_KEY_ORIENTATION);
                        }
                        EnterRoomParam enterRoomParam = new EnterRoomParam();
                        enterRoomParam.setPullUrl(url);
                        enterRoomParam.setAvType(avType);
                        enterRoomParam.setOrientation(orientation);
                        // reply
                        callback.onSuccess(enterRoomParam);
                    } else {
                        LogUtil.e(TAG, "studentEnterRoom failed : code = " + code + ", errorMsg = " + res.getString(RESULT_KEY_ERROR_MSG));
                        callback.onFailed(resCode, res.getString(RESULT_KEY_ERROR_MSG));
                    }
                } catch (JSONException e) {
                    LogUtil.e(TAG, "NimHttpClient onResponse on JSONException, e=" + e.getMessage());
                    callback.onFailed(-1, e.getMessage());
                }
            }
        });
    }

    /**
     * 向队列中添加连麦请求
     * @param roomId    聊天室房间号
     * @param account   请求连麦用户id
     * @param ext       连麦请求附加属性
     * @param callback  请求回调
     */
    public void pushMicLink(String roomId, String account, String ext, final ChatRoomHttpCallback<Void> callback) {
        String url = DemoServers.chatRoomAPIServer() + API_NAME_PUSH_MIC_LINK;

        Map<String, String> headers = new HashMap<>(2);
        String appKey = readAppKey();
        headers.put(HEADER_KEY_APP_KEY, appKey);
        headers.put(HEADER_KEY_CONTENT_TYPE, "application/x-www-form-urlencoded;charset=utf-8");

        StringBuilder body = new StringBuilder();
        body.append(REQUEST_ROOM_ID).append("=").append(roomId)
                .append("&")
                .append(REQUEST_USER_UID).append("=").append(account)
                .append("&")
                .append(REQUEST_ROOM_EXT).append("=").append(ext);
        String bodyString = body.toString();

        NimHttpClient.getInstance().execute(url, headers, bodyString, new NimHttpClient.NimHttpCallback() {
            @Override
            public void onResponse(String response, int code, String errorMsg) {
                if (code != 0) {
                    LogUtil.e(TAG, "pushMicLink failed : code = " + code + ", errorMsg = " + errorMsg);
                    if (callback != null) {
                        callback.onFailed(code, errorMsg);
                    }
                    return;
                }

                try {
                    // ret 0
                    JSONObject res = JSONObject.parseObject(response);
                    // res 1
                    int resCode = res.getIntValue(RESULT_KEY_RES);
                    if (resCode == RESULT_CODE_SUCCESS) {
                        callback.onSuccess(null);
                    } else {
                        LogUtil.e(TAG, "pushMicLink failed : code = " + code + ", errorMsg = " + res.getString(RESULT_KEY_ERROR_MSG));
                        callback.onFailed(resCode, res.getString(RESULT_KEY_ERROR_MSG));
                    }
                } catch (JSONException e) {
                    LogUtil.e(TAG, "NimHttpClient onResponse on JSONException, e=" + e.getMessage());
                    callback.onFailed(-1, e.getMessage());
                }
            }
        });
    }

    /**
     * 从队列中取出连麦请求
     * @param roomId 聊天室房间号
     * @param account 麦序中的用户id，不填的话取出麦序中的第一个用户
     * @param callback 请求回调
     */
    public void popMicLink(String roomId, String account, final ChatRoomHttpCallback<Void> callback) {
        String url = DemoServers.chatRoomAPIServer() + API_NAME_POP_MIC_LINK;

        Map<String, String> headers = new HashMap<>(2);
        String appKey = readAppKey();
        headers.put(HEADER_KEY_APP_KEY, appKey);
        headers.put(HEADER_KEY_CONTENT_TYPE, "application/x-www-form-urlencoded;charset=utf-8");

        StringBuilder body = new StringBuilder();
        body.append(REQUEST_ROOM_ID).append("=").append(roomId)
                .append("&")
                .append(REQUEST_USER_UID).append("=").append(account);
        String bodyString = body.toString();

        NimHttpClient.getInstance().execute(url, headers, bodyString, new NimHttpClient.NimHttpCallback() {
            @Override
            public void onResponse(String response, int code, String errorMsg) {
                if (code != 0) {
                    LogUtil.e(TAG, "popMicLink failed : code = " + code + ", errorMsg = " + errorMsg);
                    if (callback != null) {
                        callback.onFailed(code, errorMsg);
                    }
                    return;
                }

                try {
                    // ret 0
                    JSONObject res = JSONObject.parseObject(response);
                    // res 1
                    int resCode = res.getIntValue(RESULT_KEY_RES);
                    if (resCode == RESULT_CODE_SUCCESS) {
                        callback.onSuccess(null);
                    } else {
                        LogUtil.e(TAG, "popMicLink failed : code = " + code + ", resCode:" + resCode + ", errorMsg = " + res.getString(RESULT_KEY_ERROR_MSG));
                        callback.onFailed(resCode, res.getString(RESULT_KEY_ERROR_MSG));
                    }
                } catch (JSONException e) {
                    LogUtil.e(TAG, "NimHttpClient onResponse on JSONException, e=" + e.getMessage());
                    callback.onFailed(-1, e.getMessage());
                }
            }
        });
    }

    private String readAppKey() {
        try {
            ApplicationInfo appInfo = DemoCache.getContext().getPackageManager()
                    .getApplicationInfo(DemoCache.getContext().getPackageName(), PackageManager.GET_META_DATA);
            if (appInfo != null) {
                return appInfo.metaData.getString("com.netease.nim.appKey");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
