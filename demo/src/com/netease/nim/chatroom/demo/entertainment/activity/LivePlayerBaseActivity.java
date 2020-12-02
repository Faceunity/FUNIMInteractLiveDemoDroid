package com.netease.nim.chatroom.demo.entertainment.activity;

import android.Manifest;
import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.netease.nim.chatroom.demo.DemoCache;
import com.netease.nim.chatroom.demo.R;
import com.netease.nim.chatroom.demo.base.ui.TActivity;
import com.netease.nim.chatroom.demo.base.util.log.LogUtil;
import com.netease.nim.chatroom.demo.entertainment.adapter.GiftAdapter;
import com.netease.nim.chatroom.demo.entertainment.constant.GiftType;
import com.netease.nim.chatroom.demo.entertainment.constant.LiveType;
import com.netease.nim.chatroom.demo.entertainment.constant.PushLinkConstant;
import com.netease.nim.chatroom.demo.entertainment.constant.PushMicNotificationType;
import com.netease.nim.chatroom.demo.entertainment.constant.PushType;
import com.netease.nim.chatroom.demo.entertainment.helper.ChatRoomMemberCache;
import com.netease.nim.chatroom.demo.entertainment.helper.GiftAnimation;
import com.netease.nim.chatroom.demo.entertainment.module.ChatRoomMsgListPanel;
import com.netease.nim.chatroom.demo.entertainment.module.ConnectedAttachment;
import com.netease.nim.chatroom.demo.entertainment.module.DisconnectAttachment;
import com.netease.nim.chatroom.demo.entertainment.module.GiftAttachment;
import com.netease.nim.chatroom.demo.entertainment.module.LikeAttachment;
import com.netease.nim.chatroom.demo.entertainment.module.UserJoinAttachment;
import com.netease.nim.chatroom.demo.entertainment.module.UserLeaveAttachment;
import com.netease.nim.chatroom.demo.im.session.Container;
import com.netease.nim.chatroom.demo.im.session.ModuleProxy;
import com.netease.nim.chatroom.demo.im.session.actions.BaseAction;
import com.netease.nim.chatroom.demo.im.session.emoji.MoonUtil;
import com.netease.nim.chatroom.demo.im.session.input.InputConfig;
import com.netease.nim.chatroom.demo.im.session.input.InputPanel;
import com.netease.nim.chatroom.demo.im.ui.dialog.DialogMaker;
import com.netease.nim.chatroom.demo.im.ui.dialog.EasyAlertDialogHelper;
import com.netease.nim.chatroom.demo.im.ui.periscope.PeriscopeLayout;
import com.netease.nim.chatroom.demo.permission.MPermission;
import com.netease.nimlib.sdk.AbortableFuture;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nimlib.sdk.auth.AuthServiceObserver;

import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.constant.AVChatType;
import com.netease.nimlib.sdk.avchat.video.AVChatTextureViewRenderer;
import com.netease.nimlib.sdk.chatroom.ChatRoomService;
import com.netease.nimlib.sdk.chatroom.ChatRoomServiceObserver;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomInfo;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomKickOutEvent;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomMember;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomMessage;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomNotificationAttachment;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomStatusChangeData;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomUpdateInfo;
import com.netease.nimlib.sdk.chatroom.model.EnterChatRoomData;
import com.netease.nimlib.sdk.chatroom.model.EnterChatRoomResultData;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nimlib.sdk.msg.attachment.MsgAttachment;
import com.netease.nimlib.sdk.msg.constant.NotificationType;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.CustomNotification;
import com.netease.nimlib.sdk.msg.model.IMMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 直播端和观众端的基类
 * Created by hzxuwen on 2016/4/5.
 */
public abstract class LivePlayerBaseActivity extends TActivity implements ModuleProxy {
    private static final String TAG = LivePlayerBaseActivity.class.getSimpleName();

    class InteractionView {
        RelativeLayout rootViewLayout; // 连麦画面布局
        ViewGroup audienceLivingLayout; // 连麦观众正在播放画面
        ViewGroup audienceLoadingLayout; // 连麦观众等待画面
        ViewGroup audioModeBypassLayout; // 音频模式旁路直播画面
        ViewGroup connectionCloseConfirmLayout; // 连麦关闭确认画面
        TextView loadingNameText; // 连麦的观众姓名，等待中的画面
        TextView loadingClosingText; // 正在连接/已关闭文案
        View connectionViewCloseBtn; // 关闭连麦画面按钮
        TextView connectionCloseConfirmTipsTv;
        TextView connectionCloseConfirm; // 连麦关闭确认
        TextView connectionCloseCancel; // 连麦关闭取消
        TextView audienceVolume;//观众音量
        TextView livingBg; // 防止用户关闭权限，没有图像时显示
        AVChatTextureViewRenderer bypassVideoRender; // 旁路直播画面
        TextView onMicNameText; // 连麦的观众姓名
        String account;
        String meetingUid;//avchat对应Id
    }

    protected final int LIVE_PERMISSION_REQUEST_CODE = 100;
    protected final int UPDATE_VOLUME_INTERVAL = 500;
    protected final static String EXTRA_ROOM_ID = "ROOM_ID";
    protected final static String EXTRA_MODE = "EXTRA_MODE";
    protected final static String EXTRA_CREATOR = "EXTRA_CREATOR";
    private final static int FETCH_ONLINE_PEOPLE_COUNTS_DELTA = 10 * 1000;
    protected final static String AVATAR_DEFAULT = "avatar_default";

    protected final int RELEASE_SDK_DELAY_TIME = 100;

    private Timer timer;

    // 聊天室信息
    protected String roomId;
    protected ChatRoomInfo roomInfo;
    protected String pullUrl; // 拉流地址
    protected String masterNick; // 主播昵称
    protected String meetingName; // 音视频会议房间名称
    protected String meetingUid; // 主播音视频Uid
    protected boolean isCreator; // 是否是主播
    protected int screenOrientation; //屏幕方向

    // modules
    protected InputPanel inputPanel;
    protected ChatRoomMsgListPanel messageListPanel;
    private EditText messageEditText;

    // view
    protected ViewGroup rootView;
    protected TextView masterNameText;
    protected TextView masterVolume;
    private TextView onlineCountText; // 在线人数view
    protected GridView giftView; // 礼物列表
    protected PeriscopeLayout periscopeLayout; // 点赞爱心布局
    protected ImageButton giftBtn; // 礼物按钮
    protected ViewGroup giftLayout; // 礼物布局
    protected LinearLayout controlContainer; // 右下角几个image button布局
    protected ViewGroup roomOwnerLayout; // master名称布局
    protected ViewGroup roomNameLayout; //房间名称
    protected TextView roomName; //房间名
    protected TextView interactionBtn; // 互动按钮
    protected TextView fakeListText; // 占坑用的view，message listview可以浮动上下
    protected ViewGroup videoModeBgLayout; // 主播画面视频模式背景
    protected ViewGroup audioModeBgLayout; // 主播画面音频模式背景
    protected int maxInteractionMembers = 3; //最多同时三人连麦
    protected InteractionView[] interactionGroupView = new InteractionView[maxInteractionMembers];

    // data
    protected GiftAdapter adapter;
    protected GiftAnimation giftAnimation; // 礼物动画
    private AbortableFuture<EnterChatRoomResultData> enterRequest;


    // state
    protected LiveType liveType; // 直播类型
    protected PushType pushType = PushType.ANCHOR_PUSH_TYPE; // 推流类型
    protected boolean isDestroyed = false;

    private long updateVolumeTime = 0;

    protected Handler uiHandler;

    protected abstract int getActivityLayout(); // activity布局文件

    protected abstract int getLayoutId(); // 根布局资源id

    protected abstract int getControlLayout(); // 控制按钮布局

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogUtil.i(TAG, "************************************** Start Live *****************************************");
        LogUtil.i(TAG, "device info : " + Build.MANUFACTURER + "#" + Build.MODEL + "#" + Build.BRAND + "#" + Build.VERSION.SDK_INT);
        super.onCreate(savedInstanceState);
        LogUtil.i(TAG, "onCreate : " + this);
        setContentView(getActivityLayout());
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        parseIntent();
        registerObservers(true);
        uiHandler = new Handler();
    }

    protected void parseIntent() {
        isCreator = getIntent().getBooleanExtra(EXTRA_CREATOR, false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (messageListPanel != null) {
            messageListPanel.onResume();
        }
    }

    @Override
    public void onBackPressed() {
        if ((inputPanel != null)) {
            inputPanel.collapse(true);
        }

        if ((messageListPanel != null)) {
            messageListPanel.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtil.i(TAG, "onDestroy : " + this);
        isDestroyed = true;
        registerObservers(false);
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        adapter = null;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setContentView(getActivityLayout());
        controlContainer.removeAllViews();
        findViews();
    }


    // 权限控制
    protected static final String[] LIVE_PERMISSIONS = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_PHONE_STATE};

    protected void requestLivePermission() {
        MPermission.with(this)
                .addRequestCode(LIVE_PERMISSION_REQUEST_CODE)
                .permissions(LIVE_PERMISSIONS)
                .request();
    }


    private void registerObservers(boolean register) {
        NIMClient.getService(ChatRoomServiceObserver.class).observeReceiveMessage(chatRoomMsgObserver, register);
        NIMClient.getService(ChatRoomServiceObserver.class).observeOnlineStatus(chatRoomOnlineStatusObserver, register);
        NIMClient.getService(ChatRoomServiceObserver.class).observeKickOutEvent(chatRoomKickOutObserver, register);
        NIMClient.getService(MsgServiceObserve.class).observeCustomNotification(customNotificationObserver, register);
        NIMClient.getService(AuthServiceObserver.class).observeOnlineStatus(loginStatusObserver, register);
    }

    Observer<List<ChatRoomMessage>> chatRoomMsgObserver = new Observer<List<ChatRoomMessage>>() {
        @Override
        public void onEvent(List<ChatRoomMessage> messages) {
            if (messages == null || messages.isEmpty() || roomInfo == null) {
                LogUtil.i(TAG, "chat room msg , but  return  roomInfo : " + roomInfo);
                return;
            }
            for (ChatRoomMessage message : messages) {

                MsgAttachment rawAttachment = message.getAttachment();
                if (rawAttachment != null) {
                    LogUtil.i(TAG, "chat room msg : " + rawAttachment.toJson(false));
                }

                if (rawAttachment instanceof GiftAttachment) {
                    // 收到礼物消息
                    GiftType type = ((GiftAttachment) message.getAttachment()).getGiftType();
                    updateGiftList(type);
                    giftAnimation.showGiftAnimation(message);
                } else if (rawAttachment instanceof LikeAttachment) {
                    // 收到点赞爱心
                    periscopeLayout.addHeart();
                } else if (rawAttachment instanceof ChatRoomNotificationAttachment) {
                    // 通知类消息
                    ChatRoomNotificationAttachment attachment = (ChatRoomNotificationAttachment) rawAttachment;
                    if (attachment.getType() == NotificationType.ChatRoomMemberIn) {
                        parseChatRoomExt(attachment.getExtension());
                    } else if (attachment.getType() == NotificationType.ChatRoomInfoUpdated) {
                        onReceiveChatRoomInfoUpdate(attachment.getExtension());
                    }
                } else if (rawAttachment instanceof ConnectedAttachment) {
                    // 观众收到旁路直播连接消息
                    onMicLinkedMsg(message);
                } else if (rawAttachment instanceof DisconnectAttachment) {
                    // 观众收到旁路直播断开消息
                    DisconnectAttachment attachment = (DisconnectAttachment) rawAttachment;
                    int index = getInteractionViewIndexByAccount(attachment.getAccount());
                    if (TextUtils.equals(attachment.getAccount(), roomInfo.getCreator())) {
                        if (index != -1) {
                            resetConnectionView(index);
                        }
                    } else {
                        onMicDisConnectedMsg(attachment.getAccount());
                    }
                } else if (rawAttachment instanceof UserLeaveAttachment) {
                    userLeave(((UserLeaveAttachment) rawAttachment).getAccount());
                } else if (rawAttachment instanceof UserJoinAttachment) {
                    userJoin(((UserJoinAttachment) rawAttachment).getAccount());
                } else {
                    messageListPanel.onIncomingMessage(message);
                }
            }
        }
    };

    protected void userJoin(String account) {

    }

    protected void userLeave(String account) {
    }

    Observer<CustomNotification> customNotificationObserver = new Observer<CustomNotification>() {
        @Override
        public void onEvent(CustomNotification customNotification) {
            if (customNotification == null) {
                return;
            }
            String content = customNotification.getContent();
            try {
                JSONObject json = JSON.parseObject(content);
                String fromRoomId = json.getString(PushLinkConstant.ROOM_ID);
                if (!TextUtils.equals(roomId, fromRoomId)) {
                    return;
                }
                int id = json.getIntValue(PushLinkConstant.COMMAND);
                LogUtil.i(TAG, "receive custom notification : " + json);
                if (id == PushMicNotificationType.JOIN_QUEUE.getValue()) {
                    // 加入连麦队列
                    audienceApplyJoinQueue(customNotification, json);
                } else if (id == PushMicNotificationType.EXIT_QUEUE.getValue()) {
                    // 退出连麦队列
                    audienceExitQueue(customNotification);
                } else if (id == PushMicNotificationType.CONNECTING_MIC.getValue()) {
                    // 主播选中某人连麦
                    onAgreeLinkedByMaster(json);
                } else if (id == PushMicNotificationType.DISCONNECT_MIC.getValue()) {
                    // 被主播断开连麦
                    onMicDisconnectByMaster();
                } else if (id == PushMicNotificationType.REJECT_CONNECTING.getValue()) {
                    // 观众由于重新进入了房间而拒绝连麦
                    rejectLinkByAudience(customNotification.getFromAccount());
                }

            } catch (Exception e) {
                LogUtil.e(TAG, e.toString());
            }
        }
    };

    Observer<ChatRoomStatusChangeData> chatRoomOnlineStatusObserver = (Observer<ChatRoomStatusChangeData>) chatRoomStatusChangeData -> {
        if (chatRoomStatusChangeData.status == StatusCode.CONNECTING) {
            DialogMaker.updateLoadingMessage("连接中...");
        } else if (chatRoomStatusChangeData.status == StatusCode.UNLOGIN) {
            onOnlineStatusChanged(false);
            Toast.makeText(LivePlayerBaseActivity.this, R.string.nim_status_unlogin, Toast.LENGTH_SHORT).show();
        } else if (chatRoomStatusChangeData.status == StatusCode.LOGINING) {
            DialogMaker.updateLoadingMessage("登录中...");
        } else if (chatRoomStatusChangeData.status == StatusCode.LOGINED) {
            onOnlineStatusChanged(true);
        } else if (chatRoomStatusChangeData.status == StatusCode.NET_BROKEN) {
            onOnlineStatusChanged(false);
            Toast.makeText(LivePlayerBaseActivity.this, R.string.net_broken, Toast.LENGTH_SHORT).show();
        }
        LogUtil.i(TAG, "chat room online status:" + chatRoomStatusChangeData.status.name());
    };


    // 用户状态变化
    Observer<StatusCode> loginStatusObserver = (Observer<StatusCode>) code -> {
        LogUtil.i(TAG, "user login status:" + StatusCode.typeOfValue(code.getValue()).name());
        if (code.wontAutoLogin()) {
            clearChatRoom();
        }
    };

    Observer<ChatRoomKickOutEvent> chatRoomKickOutObserver = (Observer<ChatRoomKickOutEvent>) chatRoomKickOutEvent -> {
        Toast.makeText(LivePlayerBaseActivity.this, "被踢出聊天室，原因:" + chatRoomKickOutEvent.getReason(), Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getApplicationContext(), IdentifyActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        clearChatRoom();
    };


    // 断网重连
    protected void onOnlineStatusChanged(boolean isOnline) {
        if (isOnline) {
            onConnected();
        } else {
            onDisconnected();
        }
    }

    protected abstract void onConnected(); // 网络连上

    protected abstract void onDisconnected(); // 网络断开


    protected void findViews() {

        roomName = findView(R.id.room_name);
        masterNameText = findView(R.id.master_name);
        onlineCountText = findView(R.id.online_count_text);
        roomOwnerLayout = findView(R.id.room_owner_layout);
        roomNameLayout = findView(R.id.room_name_layout);
        masterVolume = findView(R.id.master_volume);

        //控制布局
        findControlViews();

        interactionBtn = findView(R.id.interaction_btn);
        giftBtn = findView(R.id.gift_btn);
        // 分享按钮
        ImageButton shareBtn = findView(R.id.share_btn);
        shareBtn.setOnClickListener(baseClickListener);
        // 礼物列表
        findGiftLayout();
        // 点赞的爱心布局
        periscopeLayout = findViewById(R.id.periscope);

        // 互动连麦布局
        findInteractionViews();
    }

    protected void findControlViews() {
        controlContainer = findView(R.id.control_container);
        View view = LayoutInflater.from(this).inflate(getControlLayout(), null);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        controlContainer.addView(view, lp);
    }

    protected void findInputViews() {
        Container container = new Container(this, roomId, SessionTypeEnum.ChatRoom, this);
        View view = findViewById(getLayoutId());
        if (messageListPanel == null) {
            messageListPanel = new ChatRoomMsgListPanel(container, view);
        }
        InputConfig inputConfig = new InputConfig();
        inputConfig.isTextAudioSwitchShow = false;
        inputConfig.isMoreFunctionShow = false;
        inputConfig.isEmojiButtonShow = false;
        if (inputPanel == null) {
            inputPanel = new InputPanel(container, view, getActionList(), inputConfig);
        } else {
            inputPanel.reload(container, inputConfig);
        }
        messageEditText = findView(R.id.editTextMessage);

        //文字模式按钮
        TextView inputBtn = findView(R.id.input_btn);
        inputBtn.setOnClickListener(baseClickListener);
        inputPanel.hideInputPanel();
        inputPanel.collapse(true);
    }

    // 初始化礼物布局
    protected void findGiftLayout() {
        giftLayout = findView(R.id.gift_layout);
        giftView = findView(R.id.gift_grid_view);

        // 礼物动画布局1
        RelativeLayout giftAnimationViewDown = findView(R.id.gift_animation_view);
        // 礼物动画布局2
        RelativeLayout giftAnimationViewUp = findView(R.id.gift_animation_view_up);

        giftAnimation = new GiftAnimation(giftAnimationViewDown, giftAnimationViewUp);
    }

    // 更新礼物列表，由子类定义
    protected void updateGiftList(GiftType type) {

    }

    // 互动连麦布局
    private void findInteractionViews() {

        audioModeBgLayout = findView(R.id.audio_mode_background);
        videoModeBgLayout = findView(R.id.video_layout);
        ViewGroup interaction_group_layout = findView(R.id.interaction_group_layout);

        ViewGroup micNameLayout = findView(R.id.on_mic_name_layout);
        for (int i = 0; i < interactionGroupView.length; i++) {
            final InteractionView interactionView = new InteractionView();
            int rootResTd = 0;
            int micNameId = 0;
            switch (i) {
                case 0:
                    rootResTd = R.id.interaction_view_layout_1;
                    micNameId = R.id.on_mic_name_1;
                    break;
                case 1:
                    rootResTd = R.id.interaction_view_layout_2;
                    micNameId = R.id.on_mic_name_2;
                    break;
                case 2:
                    rootResTd = R.id.interaction_view_layout_3;
                    micNameId = R.id.on_mic_name_3;
                    break;
                default:
                    break;
            }
            interactionView.rootViewLayout = findViewById(interaction_group_layout, rootResTd);
            interactionView.bypassVideoRender = findViewById(interactionView.rootViewLayout, R.id.bypass_video_render);
            interactionView.loadingNameText = findViewById(interactionView.rootViewLayout, R.id.loading_name);
            interactionView.onMicNameText = findViewById(micNameLayout, micNameId);
            interactionView.audienceLoadingLayout = findViewById(interactionView.rootViewLayout, R.id.audience_loading_layout);
            interactionView.audienceLivingLayout = findViewById(interactionView.rootViewLayout, R.id.audience_living_layout);
            interactionView.livingBg = findViewById(interactionView.rootViewLayout, R.id.no_video_bg);
            interactionView.connectionViewCloseBtn = findViewById(interactionView.rootViewLayout, R.id.interaction_close_btn);
            interactionView.audienceVolume = findViewById(interactionView.rootViewLayout, R.id.audience_volume);
            interactionView.connectionCloseConfirmLayout = findViewById(interactionView.rootViewLayout, R.id.interaction_close_confirm_layout);
            interactionView.connectionCloseConfirmTipsTv = findViewById(interactionView.rootViewLayout, R.id.interaction_close_confirm_tips_tv);
            interactionView.connectionCloseConfirm = findViewById(interactionView.rootViewLayout, R.id.close_confirm);
            interactionView.connectionCloseCancel = findViewById(interactionView.rootViewLayout, R.id.close_cancel);
            interactionView.loadingClosingText = findViewById(interactionView.rootViewLayout, R.id.loading_closing_text);
            interactionView.audioModeBypassLayout = findViewById(interactionView.rootViewLayout, R.id.audio_mode_audience_layout);
            interactionView.connectionViewCloseBtn.setOnClickListener(view -> {
                interactionView.connectionViewCloseBtn.setVisibility(View.GONE);
                interactionView.connectionCloseConfirmLayout.setVisibility(View.VISIBLE);
                interactionView.connectionCloseConfirmTipsTv.setText(R.string.interaction_close_title);
            });
            final int index = i;
            interactionView.connectionCloseConfirm.setOnClickListener(view -> {
                doCloseInteraction(interactionView);
                doCloseInteractionView(index);
            });
            interactionView.connectionCloseCancel.setOnClickListener(view -> {
                interactionView.connectionCloseConfirmLayout.setVisibility(View.GONE);
                interactionView.connectionViewCloseBtn.setVisibility(View.VISIBLE);
            });

            interactionGroupView[i] = interactionView;
        }


        showModeLayout();
    }

    // 连麦布局显示
    protected void showModeLayout() {
        if (liveType == LiveType.VIDEO_TYPE) {
            videoModeBgLayout.setVisibility(View.VISIBLE);
            audioModeBgLayout.setVisibility(View.GONE);
        } else if (liveType == LiveType.AUDIO_TYPE) {
            videoModeBgLayout.setVisibility(View.GONE);
            audioModeBgLayout.setVisibility(View.VISIBLE);
        }
    }


    public void enterChatRoom() {
        if (isDestroyed) {
            return;
        }

        LogUtil.i(TAG, "start enter chat room  ");
        DialogMaker.showProgressDialog(this, null, "", true, dialog -> {
            if (enterRequest != null) {
                enterRequest.abort();
                onEnterRoomDone();
                finish();
            }
        }).setCanceledOnTouchOutside(false);

        EnterChatRoomData data = new EnterChatRoomData(roomId);
        enterRequest = NIMClient.getService(ChatRoomService.class).enterChatRoom(data);
        enterRequest.setCallback(new RequestCallback<EnterChatRoomResultData>() {
            @Override
            public void onSuccess(EnterChatRoomResultData result) {
                if (isDestroyed) {
                    return;
                }
                LogUtil.i(TAG, "enter chat room  success");
                roomInfo = result.getRoomInfo();
                if (roomInfo == null) {
                    Toast.makeText(LivePlayerBaseActivity.this, "获取聊天室信息异常", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                ChatRoomMember self = result.getMember();
                self.setRoomId(roomInfo.getRoomId());
                ChatRoomMemberCache.getInstance().saveSelfMember(self);

                enterChatRoomSuccess();
                onEnterRoomDone();
            }

            @Override
            public void onFailed(int code) {
                LogUtil.e(TAG, "enter chat room  failed , code : " + code);
                onEnterRoomDone();
                if (code == ResponseCode.RES_CHATROOM_BLACKLIST) {
                    Toast.makeText(LivePlayerBaseActivity.this, "你已被拉入黑名单，不能再进入", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LivePlayerBaseActivity.this, "enter chat room failed, code=" + code, Toast.LENGTH_SHORT).show();
                }
                if (isCreator) {
                    finish();
                }
            }

            @Override
            public void onException(Throwable exception) {
                LogUtil.e(TAG, "enter chat room  exception  ", exception);
                onEnterRoomDone();
                Toast.makeText(LivePlayerBaseActivity.this, "enter chat room exception, e=" + exception.getMessage(), Toast.LENGTH_SHORT).show();
                if (isCreator) {
                    finish();
                }
            }
        });
    }

    // 获取当前直播的模式
    protected void parseChatRoomExt(Map<String, Object> ext) {
        if (ext == null) {
            LogUtil.i(TAG, "parseChatRoomExt , ext is null  ");
            return;
        }
        LogUtil.i(TAG, "parseChatRoomExt ext : " + ext);

        if (ext.containsKey(PushLinkConstant.MEETING_NAME)) {
            String newName = (String) ext.get(PushLinkConstant.MEETING_NAME);
            LogUtil.i(TAG, "meetingName :" + meetingName + ",newName : " + newName);
            if (meetingName != null && !TextUtils.equals(meetingName, newName)) {
                Toast.makeText(LivePlayerBaseActivity.this, "房间信息已变更，直播已结束", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            meetingName = newName;
        }

        if (ext.containsKey(PushLinkConstant.LIVE_TYPE)) {
            int type = (int) ext.get(PushLinkConstant.LIVE_TYPE);
            liveType = LiveType.typeOfValue(type);
        }

        if (ext.containsKey(PushLinkConstant.MEETING_UID)) {
            meetingUid = String.valueOf(ext.get(PushLinkConstant.MEETING_UID));
        }

        if (ext.containsKey(PushLinkConstant.PUSH_TYPE)) {
            int pushIntType = (int) ext.get(PushLinkConstant.PUSH_TYPE);
            pushType = PushType.typeOfValue(pushIntType);
        }

        if (ext.containsKey(PushLinkConstant.ORIENTATION)) {
            screenOrientation = (int) ext.get(PushLinkConstant.ORIENTATION);
            if ((getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? 1 : 2) != screenOrientation) {
                setRequestedOrientation(screenOrientation == 1 ? ActivityInfo.SCREEN_ORIENTATION_PORTRAIT : ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        }

    }

    // 观众解析房间信息。
    protected void parseRoomPkInfo(Map<String, Object> extension) {
        if (extension == null) {
            LogUtil.i(TAG, "parseRoomPkInfo , ext is null  ");
            return;
        }
        LogUtil.i(TAG, "parseRoomPkInfo ext : " + extension);
    }

    protected void onEnterRoomDone() {
        enterRequest = null;
        DialogMaker.dismissProgressDialog();
    }

    protected void enterChatRoomSuccess() {

        if (isCreator) {
            updateChatRoomInfo();
        } else {
            Map<String, Object> ext = roomInfo.getExtension();
            parseChatRoomExt(ext);
            parseRoomPkInfo(ext);
        }
        roomName.setText(roomId);
        masterNameText.setText(roomInfo.getCreator());
        onlineCountText.setText(String.format("%s人", String.valueOf(roomInfo.getOnlineUserCount())));
        fetchOnlineCount();
        updateRoomUI(false);
    }

    protected void updateChatRoomInfo() {

        if (!isCreator || TextUtils.isEmpty(roomId)) {
            return;
        }
        ChatRoomUpdateInfo chatRoomUpdateInfo = new ChatRoomUpdateInfo();
        Map<String, Object> ext = createChatRoomExt();
        chatRoomUpdateInfo.setExtension(ext);
        NIMClient.getService(ChatRoomService.class).updateRoomInfo(roomId, chatRoomUpdateInfo, true, ext);
    }

    protected Map<String, Object> createChatRoomExt() {
        Map<String, Object> notifyExt = new HashMap<>();
        if (liveType == LiveType.VIDEO_TYPE) {
            notifyExt.put(PushLinkConstant.LIVE_TYPE, AVChatType.VIDEO.getValue());
        } else if (liveType == LiveType.AUDIO_TYPE) {
            notifyExt.put(PushLinkConstant.LIVE_TYPE, AVChatType.AUDIO.getValue());
        }
        notifyExt.put(PushLinkConstant.MEETING_NAME, meetingName);
        notifyExt.put(PushLinkConstant.PUSH_TYPE, pushType.getValue());
        boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        notifyExt.put(PushLinkConstant.ORIENTATION, isPortrait ? 1 : 2);
        return notifyExt;
    }

    // 聊天室信息相关界面
    protected void updateRoomUI(boolean isHide) {
        if (isHide) {
            controlContainer.setVisibility(View.GONE);
            roomOwnerLayout.setVisibility(View.GONE);
            roomNameLayout.setVisibility(View.GONE);
        } else {
            controlContainer.setVisibility(View.VISIBLE);
            roomOwnerLayout.setVisibility(View.VISIBLE);
            roomNameLayout.setVisibility(View.VISIBLE);
        }
    }

    // 一分钟轮询一次在线人数
    private void fetchOnlineCount() {
        if (timer == null) {
            timer = new Timer();
        }

        //开始一个定时任务
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                NIMClient.getService(ChatRoomService.class).fetchRoomInfo(roomId).setCallback(new RequestCallback<ChatRoomInfo>() {
                    @Override
                    public void onSuccess(final ChatRoomInfo param) {
                        onlineCountText.setText(String.format("%s人", String.valueOf(param.getOnlineUserCount())));
                    }

                    @Override
                    public void onFailed(int code) {
                        LogUtil.d(TAG, "fetch room info failed:" + code);
                    }

                    @Override
                    public void onException(Throwable exception) {
                        LogUtil.d(TAG, "fetch room info exception:" + exception);
                    }
                });
            }
        }, FETCH_ONLINE_PEOPLE_COUNTS_DELTA, FETCH_ONLINE_PEOPLE_COUNTS_DELTA);
    }

    private void clearChatRoom() {
        ChatRoomMemberCache.getInstance().clearRoomCache(roomId);
        getHandler().postDelayed(this::finish, 50);
    }


    protected void onReceiveChatRoomInfoUpdate(Map<String, Object> extension) {
        LogUtil.i(TAG, "receive chat room update  , ext : " + extension);
        parseChatRoomExt(extension);
    }

    @Override
    public boolean sendMessage(IMMessage msg) {
        ChatRoomMessage message = (ChatRoomMessage) msg;

        Map<String, Object> ext = new HashMap<>();
        ChatRoomMember chatRoomMember = ChatRoomMemberCache.getInstance().getChatRoomMember(roomId, DemoCache.getAccount());
        if (chatRoomMember != null && chatRoomMember.getMemberType() != null) {
            ext.put("type", chatRoomMember.getMemberType().getValue());
            message.setRemoteExtension(ext);
        }

        NIMClient.getService(ChatRoomService.class).sendMessage(message, false)
                .setCallback(new RequestCallback<Void>() {
                    @Override
                    public void onSuccess(Void param) {
                    }

                    @Override
                    public void onFailed(int code) {
                        if (code == ResponseCode.RES_CHATROOM_MUTED) {
                            Toast.makeText(DemoCache.getContext(), "用户被禁言", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(DemoCache.getContext(), "消息发送失败：code:" + code, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onException(Throwable exception) {
                        Toast.makeText(DemoCache.getContext(), "消息发送失败！", Toast.LENGTH_SHORT).show();
                    }
                });
        messageListPanel.onMsgSend(msg);
        return true;
    }

    @Override
    public void onInputPanelExpand() {
        controlContainer.setVisibility(View.GONE);
        if (fakeListText != null) {
            fakeListText.setVisibility(View.GONE);
        }
        if (isCreator) {
            for (InteractionView interactionView : interactionGroupView) {
                interactionView.rootViewLayout.setVisibility(View.GONE);
                interactionView.bypassVideoRender.setVisibility(View.GONE);
            }

        }
    }

    @Override
    public void shouldCollapseInputPanel() {
        inputPanel.collapse(false);
        controlContainer.setVisibility(View.VISIBLE);
        if (fakeListText != null) {
            fakeListText.setVisibility(View.VISIBLE);
        }
        if (isCreator) {
            for (InteractionView interactionView : interactionGroupView) {
                interactionView.rootViewLayout.setVisibility(View.VISIBLE);
                interactionView.bypassVideoRender.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public boolean isLongClickEnabled() {
        return false;
    }

    // 操作面板集合
    protected List<BaseAction> getActionList() {
        return new ArrayList<>();
    }

    //  连麦相关操作
    View.OnClickListener baseClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.input_btn:
                    startInputActivity();
                    break;
                case R.id.share_btn:
                    if (pullUrl != null) {
                        LogUtil.i(TAG, "pullUrl:" + pullUrl);
                        ClipboardManager cm = (ClipboardManager) getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
                        // 将文本内容放到系统剪贴板里。
                        cm.setText(pullUrl);
                        EasyAlertDialogHelper.showOneButtonDiolag(LivePlayerBaseActivity.this,
                                R.string.share_address_dialog_title, R.string.share_address_dialog_message,
                                R.string.share_address_dialog_know, false, null);
                    }

                    break;
            }
        }
    };


    /**
     * 观众申请加入连麦队列
     */
    protected void audienceApplyJoinQueue(CustomNotification customNotification, JSONObject json) {

    }

    /**
     * 观众主动退出连麦队列
     */
    protected void audienceExitQueue(CustomNotification customNotification) {

    }

    /**
     * 主播同意自己的连麦请求
     */
    protected void onAgreeLinkedByMaster(JSONObject jsonObject) {

    }

    /**
     * 主播同意后， 观众拒绝连麦
     */
    protected void rejectLinkByAudience(String account) {

    }

    /**
     * 收到有人连麦成功消息
     */
    protected void onMicLinkedMsg(ChatRoomMessage message) {
    }

    /**
     * 收到有人断开连麦消息
     */
    protected void onMicDisConnectedMsg(String account) {

        LogUtil.i(TAG, "on mic disconnect : " + account);
    }


    /**
     * 展示其他连麦者的UI
     */
    protected void showOtherMicLinkedView(int index, String account, String meetingUid, String nick, int linkStyle) {
        updateOnMicName(index, nick);
    }

    /**
     * 设置连麦者昵称
     */
    protected void updateOnMicName(int index, String nick) {
        LogUtil.d(TAG, index + " updateOnMicName: " + nick);
        if (nick == null) {
            return;
        }
        interactionGroupView[index].onMicNameText.setVisibility(View.VISIBLE);
        interactionGroupView[index].onMicNameText.setText(nick);
    }

    // 断开连麦,由子类实现
    protected abstract void doCloseInteraction(InteractionView interactionView);


    // 主播断开连麦者，由观众实现
    protected void onMicDisconnectByMaster() {

    }

    protected int getEmptyInteractionView() {
        int index = -1;
        for (int i = 0; i < interactionGroupView.length; i++) {
            if (interactionGroupView[i].account == null) {
                index = i;
                break;
            }
        }
        return index;
    }

    protected int getInteractionViewIndexByAccount(String account) {
        int index = -1;
        if (account == null) {
            LogUtil.d(TAG, " getInteractionViewIndexByAccount account is null");
            return index;
        }
        for (int i = 0; i < interactionGroupView.length; i++) {
            if (account.equals(interactionGroupView[i].account)) {
                index = i;
                break;
            }
        }
        return index;
    }


    @Override
    protected void showKeyboardDelayed(View focus) {
        super.showKeyboardDelayed(focus);
    }

    protected void doCloseInteractionView(final int index) {
        if (index == -1) {
            return;
        }
        InteractionView interactionView = interactionGroupView[index];
        interactionView.loadingClosingText.setText(R.string.link_closed);
        interactionView.audienceLoadingLayout.setVisibility(View.VISIBLE);
        interactionView.loadingNameText.setText(!TextUtils.isEmpty(interactionView.account) ? interactionView.account : "");
        interactionView.livingBg.setVisibility(View.GONE);
        interactionView.connectionViewCloseBtn.setVisibility(View.GONE);
        interactionView.connectionCloseConfirmLayout.setVisibility(View.GONE);
        interactionView.bypassVideoRender.release();
        interactionView.bypassVideoRender.setVisibility(View.GONE);
        interactionView.account = null;
        resetConnectionView(index);
    }

    protected void resetConnectionView(int index) {
        InteractionView interactionView = interactionGroupView[index];
        interactionView.rootViewLayout.setVisibility(View.GONE);
        interactionView.connectionCloseConfirmLayout.setVisibility(View.GONE);
        interactionView.audienceLivingLayout.setVisibility(View.GONE);
        interactionView.audienceLoadingLayout.setVisibility(View.GONE);
        interactionView.connectionViewCloseBtn.setVisibility(View.VISIBLE);
        interactionView.onMicNameText.setVisibility(View.GONE);
    }


    protected boolean noNeedUpdateVolume() {
        if (System.currentTimeMillis() - updateVolumeTime < UPDATE_VOLUME_INTERVAL) {
            return true;
        }
        updateVolumeTime = System.currentTimeMillis();
        return false;
    }

    /**
     * ***************************** 部分机型键盘弹出会造成布局挤压的解决方案 ***********************************
     */
    private InputConfig inputConfig = new InputConfig(false, false, false);

    private void startInputActivity() {
        InputActivity.startActivityForResult(this, messageEditText.getText().toString(),
                inputConfig, text -> inputPanel.onTextMessageSendButtonPressed(text));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (resultCode != Activity.RESULT_OK || requestCode != InputActivity.REQ_CODE || data == null) {
            return;
        }

        // 设置EditText显示的内容
        String text = data.getStringExtra(InputActivity.EXTRA_TEXT);
        MoonUtil.identifyFaceExpression(DemoCache.getContext(), messageEditText, text, ImageSpan.ALIGN_BOTTOM);
        messageEditText.setSelection(text.length());
        inputPanel.hideInputPanel();
        // 根据mode显示表情布局或者键盘布局
        int mode = data.getIntExtra(InputActivity.EXTRA_MODE, InputActivity.MODE_KEYBOARD_COLLAPSE);
        if (mode == InputActivity.MODE_SHOW_EMOJI) {
            inputPanel.toggleEmojiLayout();
        } else if (mode == InputActivity.MODE_SHOW_MORE_FUNC) {
            inputPanel.toggleActionPanelLayout();
        }
    }

}
