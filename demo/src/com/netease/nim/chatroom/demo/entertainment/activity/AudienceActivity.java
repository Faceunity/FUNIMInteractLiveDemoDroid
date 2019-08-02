package com.netease.nim.chatroom.demo.entertainment.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.netease.neliveplayer.proxy.config.NEPlayerConfig;
import com.netease.neliveplayer.sdk.NELivePlayer;
import com.netease.neliveplayer.sdk.model.NESDKConfig;
import com.netease.nim.chatroom.demo.DemoCache;
import com.netease.nim.chatroom.demo.R;
import com.netease.nim.chatroom.demo.base.util.log.LogUtil;
import com.netease.nim.chatroom.demo.entertainment.adapter.GiftAdapter;
import com.netease.nim.chatroom.demo.entertainment.constant.GiftType;
import com.netease.nim.chatroom.demo.entertainment.constant.LiveType;
import com.netease.nim.chatroom.demo.entertainment.constant.MicApplyEnum;
import com.netease.nim.chatroom.demo.entertainment.constant.MicStateEnum;
import com.netease.nim.chatroom.demo.entertainment.constant.PushLinkConstant;
import com.netease.nim.chatroom.demo.entertainment.constant.PushMicNotificationType;
import com.netease.nim.chatroom.demo.entertainment.helper.ChatRoomMemberCache;
import com.netease.nim.chatroom.demo.entertainment.helper.MicHelper;
import com.netease.nim.chatroom.demo.entertainment.helper.SimpleCallback;
import com.netease.nim.chatroom.demo.entertainment.http.ChatRoomHttpClient;
import com.netease.nim.chatroom.demo.entertainment.model.SimpleAVChatStateObserver;
import com.netease.nim.chatroom.demo.entertainment.model.SyncContent;
import com.netease.nim.chatroom.demo.entertainment.module.ConnectedAttachment;
import com.netease.nim.chatroom.demo.entertainment.module.GiftAttachment;
import com.netease.nim.chatroom.demo.entertainment.module.LikeAttachment;
import com.netease.nim.chatroom.demo.im.config.UserPreferences;
import com.netease.nim.chatroom.demo.im.ui.dialog.EasyAlertDialogHelper;
import com.netease.nim.chatroom.demo.permission.MPermission;
import com.netease.nim.chatroom.demo.permission.annotation.OnMPermissionDenied;
import com.netease.nim.chatroom.demo.permission.annotation.OnMPermissionGranted;
import com.netease.nim.chatroom.demo.permission.annotation.OnMPermissionNeverAskAgain;
import com.netease.nim.chatroom.demo.permission.util.MPermissionUtil;
import com.netease.nim.chatroom.demo.thirdparty.video.NEVideoView;
import com.netease.nim.chatroom.demo.thirdparty.video.VideoPlayer;
import com.netease.nim.chatroom.demo.thirdparty.video.constant.VideoConstant;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.AVChatStateObserver;
import com.netease.nimlib.sdk.avchat.constant.AVChatResCode;
import com.netease.nimlib.sdk.avchat.constant.AVChatType;
import com.netease.nimlib.sdk.avchat.constant.AVChatUserRole;
import com.netease.nimlib.sdk.avchat.constant.AVChatVideoCaptureOrientation;
import com.netease.nimlib.sdk.avchat.constant.AVChatVideoCropRatio;
import com.netease.nimlib.sdk.avchat.constant.AVChatVideoScalingType;
import com.netease.nimlib.sdk.avchat.model.AVChatParameters;
import com.netease.nimlib.sdk.avchat.video.AVChatCameraCapturer;
import com.netease.nimlib.sdk.avchat.video.AVChatTextureViewRenderer;
import com.netease.nimlib.sdk.avchat.video.AVChatVideoCapturerFactory;
import com.netease.nimlib.sdk.chatroom.ChatRoomMessageBuilder;
import com.netease.nimlib.sdk.chatroom.ChatRoomService;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomMember;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomMessage;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.CustomNotification;
import com.netease.nimlib.sdk.util.Entry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 观众端
 * Created by hzxuwen on 2016/3/18.
 */
public class AudienceActivity extends LivePlayerBaseActivity implements VideoPlayer.VideoPlayerProxy {

    private static final String TAG = AudienceActivity.class.getSimpleName();
    private final int BASIC_PERMISSION_REQUEST_CODE = 110;

    // view
    private AVChatTextureViewRenderer videoRender;
    private NEVideoView videoView;
    private ViewGroup liveFinishLayout;
    private TextView finishTipText;
    private TextView finishNameText;
    private TextView preparedText;
    private ImageButton switchBtn;
    private LinearLayout pkInfoLayout;
    private TextView pkFromTv;
    private TextView pkToTv;
    private boolean isPk;

    /**
     * 互动
     **/
    private ViewGroup interactionLayout; // 互动根布局
    private ViewGroup videoInteractionInitLayout; // 互动初始布局
    private ViewGroup applyingLayout; // 正在进行互动申请布局
    private TextView applyingTip; //  正在进行连线的文案
    private ViewGroup audioInteractInitLayout; // 音频模式的互动初始布局
    private TextView applyMasterNameText; // 主播名称text
    private ViewGroup interactionTipsLayout; // 互动直播提示


    // 播放器
    private VideoPlayer videoPlayer;
    // 发送礼物频率控制使用
    private long lastClickTime = 0;
    // 选中的礼物
    private int giftPosition = -1;

    // 申请连麦的模式
    private MicApplyEnum micApplyEnum;

    // 自己的连麦类型
    private int selfLinkType;

    // 推流是否开始
    private boolean isStartLive = false;

    // 我是否已经申请连麦
    private boolean isSelfAlreadyApply = false;

    // 主播是否同意我连麦（为了确保权限时使用）
    private boolean isMasterAgreeToLink = false;

    // 是否我自己连上麦
    protected boolean isMeOnMic = false;

    private AVChatCameraCapturer mVideoCapturer;


    // 连麦观众的信息
    private List<Entry<String, String>> linkedInfoList = new ArrayList<>();
    // 加入音视频房间的帐号
    private Set<String> joinedChannelAccountSet = new HashSet<>();


    public static void start(Context context, String roomId) {
        Intent intent = new Intent();
        intent.setClass(context, AudienceActivity.class);
        intent.putExtra(EXTRA_ROOM_ID, roomId);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        findViews();
        findInputViews();
        updateRoomUI(true);
        enterRoom();
        requestBasicPermission(); // 申请APP基本权限.同意之后，请求拉流
        NESDKConfig config = new NESDKConfig();
        config.privateConfig = new NEPlayerConfig();
        NELivePlayer.init(this, config);
    }


    @Override
    protected void onEnterRoomDone() {
        super.onEnterRoomDone();
        if (roomInfo != null) {
            registerAudienceObservers(true);
        }
    }

    @Override
    protected void parseIntent() {
        super.parseIntent();
        roomId = getIntent().getStringExtra(EXTRA_ROOM_ID);
    }

    @Override
    protected int getActivityLayout() {
        return R.layout.audience_activity;
    }

    @Override
    protected int getLayoutId() {
        return R.id.audience_layout;
    }

    @Override
    protected int getControlLayout() {
        return R.layout.audience_control_layout;
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 恢复播放
        if (videoPlayer != null) {
            videoPlayer.onActivityResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        registerAudienceObservers(false);
        // 释放资源
        if (videoPlayer != null) {
            videoPlayer.resetVideo();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishLive();
    }

    private void finishLive() {
        if (isStartLive) {
            logoutChatRoom();
        } else {
            NIMClient.getService(ChatRoomService.class).exitChatRoom(roomId);
            clearChatRoom();
        }
    }

    // 离开聊天室
    private void logoutChatRoom() {
        EasyAlertDialogHelper.createOkCancelDiolag(this, null, getString(R.string.finish_confirm),
                getString(R.string.confirm), getString(R.string.cancel), true,
                new EasyAlertDialogHelper.OnDialogActionListener() {
                    @Override
                    public void doCancelAction() {

                    }

                    @Override
                    public void doOkAction() {
                        if (isMeOnMic) {
                            MicHelper.getInstance().leaveChannel(selfLinkType == AVChatType.VIDEO.getValue(),
                                    liveType == LiveType.VIDEO_TYPE,
                                    true,
                                    meetingName);
                            mVideoCapturer = null;
                        }
                        NIMClient.getService(ChatRoomService.class).exitChatRoom(roomId);
                        clearChatRoom();
                    }
                }).show();
    }

    private void clearChatRoom() {
        ChatRoomMemberCache.getInstance().clearRoomCache(roomId);
        finish();
    }

    @Override
    protected void onConnected() {
    }

    @Override
    protected void onDisconnected() {
    }

    // 初始化
    private void fetchLiveUrl() {
        ChatRoomHttpClient.getInstance().studentEnterRoom(DemoCache.getAccount(), roomId,
                new ChatRoomHttpClient.ChatRoomHttpCallback<ChatRoomHttpClient.EnterRoomParam>() {
                    @Override
                    public void onSuccess(ChatRoomHttpClient.EnterRoomParam enterRoomParam) {
                        if (enterRoomParam.getAvType().equals("AUDIO")) {
                            liveType = LiveType.AUDIO_TYPE;
                        } else if (enterRoomParam.getAvType().equals("VIDEO")) {
                            liveType = LiveType.VIDEO_TYPE;
                            setRequestedOrientation(enterRoomParam.getOrientation() == 1 ? ActivityInfo.SCREEN_ORIENTATION_PORTRAIT : ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                        }
                        pullUrl = enterRoomParam.getPullUrl();
                        LogUtil.d(TAG, "fetchLiveUrl pullUrl：" + pullUrl);
                        initAudienceParam();
                        videoView.setOnVolumeCallBack(new NEVideoView.OnVolumeCallBack() {
                            @Override
                            public void callBack(String json) {
                                if (noNeedUpdateVolume()) {
                                    return;
                                }
                                SyncContent syncContent = new SyncContent();
                                List<SyncContent.NodesInfo.NodesInfoBean> nodeList = syncContent.getNodesInfoBeanByJson(json);
                                for (SyncContent.NodesInfo.NodesInfoBean node : nodeList) {
                                    for (InteractionView interactionView : interactionGroupView) {
                                        if (!TextUtils.isEmpty(meetingUid) && meetingUid.equals(node.getUid())) {
                                            masterVolume.setText("音量:" + node.getEnergy());
                                            break;
                                        }

                                        if (!TextUtils.isEmpty(interactionView.meetingUid) && interactionView.meetingUid.equals(node.getUid())) {
                                            interactionView.audienceVolume.setText("音量:" + node.getEnergy());
                                            break;
                                        }
                                    }
                                }

                            }
                        });
                    }

                    @Override
                    public void onFailed(int code, String errorMsg) {
                        if (code == -1) {
                            Toast.makeText(AudienceActivity.this, "无法连接服务器, code=" + code, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(AudienceActivity.this, "观众进入房间失败, code=" + code + ", errorMsg:" + errorMsg, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void initAudienceParam() {
        // 初始化拉流
        videoRender.setVisibility(View.GONE);
        videoView.setVisibility(View.VISIBLE);
        videoPlayer = new VideoPlayer(AudienceActivity.this,
                videoView,
                null,
                pullUrl,
                UserPreferences.getPlayerStrategy(),
                this,
                VideoConstant.VIDEO_SCALING_MODE_FILL_BLACK);

        videoPlayer.openVideo();

    }

    protected void findViews() {
        super.findViews();
        rootView = findView(R.id.audience_layout);
        videoRender = findView(R.id.video_render);
        videoView = findView(R.id.video_view);
        videoRender.setVisibility(View.GONE);
        View closeBtn = findView(R.id.close_btn);
        interactionBtn = findView(R.id.interaction_btn);
        ImageButton likeBtn = findView(R.id.like_btn);
        switchBtn = findView(R.id.switch_btn);

        closeBtn.setOnClickListener(buttonClickListener);
        interactionBtn.setOnClickListener(buttonClickListener);
        giftBtn.setOnClickListener(buttonClickListener);
        likeBtn.setOnClickListener(buttonClickListener);
        switchBtn.setOnClickListener(buttonClickListener);

        // 互动
        interactionLayout = findView(R.id.audience_interaction_layout);
        videoInteractionInitLayout = findView(R.id.init_layout);
        // 视频连接按钮
        Button videoLinkBtn = findView(R.id.vedio_link_btn);
        // 音频链接按钮
        Button audioLinkBtn = findView(R.id.audio_link_btn);
        applyingLayout = findView(R.id.applying_layout);
        applyingTip = findView(R.id.applying_tip);
        // 取消互动申请按钮
        Button cancelLinkBtn = findView(R.id.cancel_link_btn);
        audioInteractInitLayout = findView(R.id.audio_mode_init_layout);
        // 音频模式的音频连接按钮
        Button audioInteractionLinkBtn = findView(R.id.audio_mode_link);
        applyMasterNameText = findView(R.id.apply_master_name);
        interactionTipsLayout = findView(R.id.tips_layout);
        // 互动直播提示按钮
        Button interactionTipsBtn = findView(R.id.tips_btn);

        interactionLayout.setOnClickListener(buttonClickListener);
        videoLinkBtn.setOnClickListener(buttonClickListener);
        audioLinkBtn.setOnClickListener(buttonClickListener);
        cancelLinkBtn.setOnClickListener(buttonClickListener);
        audioInteractionLinkBtn.setOnClickListener(buttonClickListener);
        interactionTipsBtn.setOnClickListener(buttonClickListener);

        // 直播结束布局
        liveFinishLayout = findView(R.id.live_finish_layout);
        View liveFinishBtn = findView(R.id.finish_close_btn);
        finishTipText = findView(R.id.finish_tip_text);
        finishNameText = findView(R.id.finish_master_name);
        finishTipText.setText(R.string.loading);

        liveFinishBtn.setOnClickListener(buttonClickListener);

        preparedText = findView(R.id.prepared_text);

        pkInfoLayout = findView(R.id.pk_info_layout);
        pkFromTv = findView(R.id.pk_from_tv);
        pkToTv = findView(R.id.pk_to_tv);

    }

    //切换是否pk布局
    private void switchNormalOrPKLayout() {
        isShowFinishLayout = false;
        liveFinishLayout.setVisibility(View.GONE);
        if (isPk) {
            pkInfoLayout.setVisibility(View.VISIBLE);
        } else {
            pkInfoLayout.setVisibility(View.GONE);
        }
    }

    // 初始化礼物布局
    protected void findGiftLayout() {
        super.findGiftLayout();
        Button sendGiftBtn = findView(R.id.send_gift_btn);
        sendGiftBtn.setOnClickListener(buttonClickListener);

        adapter = new GiftAdapter(this);
        giftView.setAdapter(adapter);

        giftLayout.setOnClickListener(buttonClickListener);
        giftView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                giftPosition = position;
            }
        });

    }

    protected void updateUI() {
        super.updateUI();
        ChatRoomMemberCache.getInstance().fetchMember(roomId, roomInfo.getCreator(), new SimpleCallback<ChatRoomMember>() {
            @Override
            public void onResult(boolean success, ChatRoomMember result) {
                if (success) {
                    masterNick = result.getNick();
                    String nick = TextUtils.isEmpty(masterNick) ? result.getAccount() : masterNick;
                    masterNameText.setText(nick);
                    finishNameText.setText(nick);
                }
            }
        });

        fetchMicListWhenAudioAudience();
    }

    private void fetchMicListWhenAudioAudience() {
        if (!isMeOnMic && liveType == LiveType.AUDIO_TYPE) {
            fetchMicList();
        }
    }

    AVChatStateObserver stateObserver = new SimpleAVChatStateObserver() {
        @Override
        public void onJoinedChannel(int i, String s, String s1, int i1) {
            if (i == AVChatResCode.JoinChannelCode.OK && liveType == LiveType.AUDIO_TYPE) {
                AVChatManager.getInstance().setSpeaker(true);
            }
        }

        @Override
        public void onUserJoined(String s) {
            if (liveType == LiveType.VIDEO_TYPE && TextUtils.equals(s, roomInfo.getCreator())) {
                AVChatManager.getInstance().setupRemoteVideoRender(roomInfo.getCreator(), null, false, 0);
                AVChatManager.getInstance().setupRemoteVideoRender(roomInfo.getCreator(), videoRender, false, AVChatVideoScalingType.SCALE_ASPECT_BALANCED);
            } else if (!TextUtils.equals(s, roomInfo.getCreator())) {
                joinedChannelAccountSet.add(s);
                showOtherOnMicMember();
            }
            if (liveType != LiveType.VIDEO_TYPE) {
                preparedText.setVisibility(View.GONE);
            }
        }

        @Override
        public void onUserLeave(String s, int i) {
            LogUtil.d(TAG, "onUserLeave");
            if (s.equals(roomInfo.getCreator())) {
                MicHelper.getInstance().leaveChannel(selfLinkType == AVChatType.VIDEO.getValue(),
                        liveType == LiveType.VIDEO_TYPE,
                        true,
                        meetingName);
                mVideoCapturer = null;
            } else if (s.equals(DemoCache.getAccount())) {
                closeAllMicView();
                switchVideoPlayer();
            } else {
                int index = getInteractionViewIndexByAccount(s);
                doCloseInteractionView(index);
            }
        }

        @Override
        public void onCallEstablished() {
            LogUtil.d(TAG, "audience onCallEstablished");
            AVChatManager.getInstance().enableAudienceRole(false);
        }


        @Override
        public void onFirstVideoFrameRendered(String s) {
            if (liveFinishLayout.getVisibility() == View.VISIBLE) {
                liveFinishLayout.setVisibility(View.GONE);
            }
            if (s.equals(roomInfo.getCreator())) {
                preparedText.setVisibility(View.GONE);
            }
            if (s.equals(DemoCache.getAccount())) {
                interactionGroupView[0].livingBg.setVisibility(View.GONE);
            }
        }

        @Override
        public void onReportSpeaker(Map<String, Integer> speakers, int mixedEnergy) {

            if (noNeedUpdateVolume()) {
                return;
            }
            masterVolume.setText("音量:" + String.valueOf(speakers.get(roomInfo.getCreator())));
            for (InteractionView interactionView : interactionGroupView) {
                if (speakers.containsKey(interactionView.account)) {
                    interactionView.audienceVolume.setText("音量:" + speakers.get(interactionView.account));
                }
            }
        }
    };

    private void registerAudienceObservers(boolean register) {
        AVChatManager.getInstance().observeAVChatState(stateObserver, register);
    }

    private View.OnClickListener buttonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.close_btn:
                    finishLive();
                    break;
                case R.id.finish_close_btn:
                    NIMClient.getService(ChatRoomService.class).exitChatRoom(roomId);
                    clearChatRoom();
                    break;
                case R.id.interaction_btn:
                    showInteractionLayout();
                    break;
                case R.id.gift_btn:
                    showGiftLayout();
                    break;
                case R.id.like_btn:
                    periscopeLayout.addHeart();
                    sendLike();
                    break;
                case R.id.switch_btn:
                    mVideoCapturer.switchCamera();
                    break;
                case R.id.gift_layout:
                    giftLayout.setVisibility(View.GONE);
                    giftPosition = -1;
                    break;
                case R.id.send_gift_btn:
                    sendGift();
                    break;
                case R.id.audience_interaction_layout:
                    interactionLayout.setVisibility(View.GONE);
                    break;
                case R.id.vedio_link_btn:
                    if (isPk) {
                        showPKingInfo();
                        return;
                    }
                    micApplyEnum = MicApplyEnum.VIDEO_VIDEO;
                    requestLivePermission();
                    break;
                case R.id.audio_link_btn:
                    if (isPk) {
                        showPKingInfo();
                        return;
                    }
                    micApplyEnum = MicApplyEnum.VIDEO_AUDIO;
                    requestLivePermission();
                    break;
                case R.id.cancel_link_btn:
                    cancelLinking();
                    break;
                case R.id.audio_mode_link:
                    if (isPk) {
                        showPKingInfo();
                        return;
                    }
                    micApplyEnum = MicApplyEnum.AUDIO;
                    requestLivePermission();
                    break;
                case R.id.tips_btn:
                    interactionLayout.setVisibility(View.GONE);
                    break;
            }
        }
    };

    private void showPKingInfo() {
        videoInteractionInitLayout.setVisibility(View.GONE);
        interactionTipsLayout.setVisibility(View.VISIBLE);
        audioInteractInitLayout.setVisibility(View.GONE);
    }

    // 发送点赞爱心
    private void sendLike() {
        if (isFastClick()) {
            return;
        }

        LikeAttachment attachment = new LikeAttachment();
        ChatRoomMessage message = ChatRoomMessageBuilder.createChatRoomCustomMessage(roomId, attachment);
        setMemberType(message);
        NIMClient.getService(ChatRoomService.class).sendMessage(message, false);

    }

    // 发送爱心频率控制
    private boolean isFastClick() {
        long currentTime = System.currentTimeMillis();
        long time = currentTime - lastClickTime;
        if (time > 0 && time < 1000) {
            return true;
        }
        lastClickTime = currentTime;
        return false;
    }


    // 显示礼物列表
    private void showGiftLayout() {
        giftLayout.setVisibility(View.VISIBLE);
        inputPanel.collapse(true);
    }

    // 发送礼物
    private void sendGift() {
        if (giftPosition == -1) {
            Toast.makeText(AudienceActivity.this, "请选择礼物", Toast.LENGTH_SHORT).show();
            return;
        }
        giftLayout.setVisibility(View.GONE);
        GiftAttachment attachment = new GiftAttachment(GiftType.typeOfValue(giftPosition), 1);
        ChatRoomMessage message = ChatRoomMessageBuilder.createChatRoomCustomMessage(roomId, attachment);
        setMemberType(message);
        NIMClient.getService(ChatRoomService.class).sendMessage(message, false);
        giftAnimation.showGiftAnimation(message);
        giftPosition = -1; // 发送完毕，置空
    }

    private void setMemberType(ChatRoomMessage message) {
        Map<String, Object> ext = new HashMap<>();
        ChatRoomMember chatRoomMember = ChatRoomMemberCache.getInstance().getChatRoomMember(roomId, DemoCache.getAccount());
        if (chatRoomMember != null && chatRoomMember.getMemberType() != null) {
            ext.put("type", chatRoomMember.getMemberType().getValue());
            message.setRemoteExtension(ext);
        }
    }


    // 基本权限管理
    private void requestBasicPermission() {
        MPermission.with(AudienceActivity.this)
                .addRequestCode(BASIC_PERMISSION_REQUEST_CODE)
                .permissions(Manifest.permission.READ_PHONE_STATE)
                .request();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        MPermission.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    @OnMPermissionGranted(BASIC_PERMISSION_REQUEST_CODE)
    public void onBasicPermissionSuccess() {
        fetchLiveUrl();
        Toast.makeText(this, "授权成功", Toast.LENGTH_SHORT).show();
    }

    @OnMPermissionDenied(BASIC_PERMISSION_REQUEST_CODE)
    public void onBasicPermissionFailed() {
        finish();
        Toast.makeText(this, "授权失败", Toast.LENGTH_SHORT).show();
    }

    @OnMPermissionGranted(LIVE_PERMISSION_REQUEST_CODE)
    public void onLivePermissionGranted() {
        Toast.makeText(AudienceActivity.this, "授权成功", Toast.LENGTH_SHORT).show();
        if (isMasterAgreeToLink) {
            doMicLinking();
        } else {
            if (micApplyEnum == MicApplyEnum.VIDEO_VIDEO) {
                doVideoLink();
            } else if (micApplyEnum == MicApplyEnum.VIDEO_AUDIO) {
                doAudioLinkAtVideoMode();
            } else {
                doAudioLinkAtAudioMode();
            }
        }
    }

    @OnMPermissionDenied(LIVE_PERMISSION_REQUEST_CODE)
    public void onLivePermissionDenied() {
        List<String> deniedPermissions = MPermission.getDeniedPermissions(this, LIVE_PERMISSIONS);
        String tip = "您拒绝了权限" + MPermissionUtil.toString(deniedPermissions) + "，无法开启直播";
        Toast.makeText(AudienceActivity.this, tip, Toast.LENGTH_SHORT).show();
        if (isMasterAgreeToLink) {
            LogUtil.d(TAG, "permission denied, send reject");
            MicHelper.getInstance().sendCustomNotify(roomId,
                    roomInfo.getCreator(),
                    PushMicNotificationType.REJECT_CONNECTING.getValue(),
                    null,
                    true);
        }
    }

    @OnMPermissionNeverAskAgain(LIVE_PERMISSION_REQUEST_CODE)
    public void onLivePermissionDeniedAsNeverAskAgain() {
        List<String> deniedPermissions = MPermission.getDeniedPermissionsWithoutNeverAskAgain(this, LIVE_PERMISSIONS);
        List<String> neverAskAgainPermission = MPermission.getNeverAskAgainPermissions(this, LIVE_PERMISSIONS);
        StringBuilder sb = new StringBuilder();
        sb.append("无法开启直播，请到系统设置页面开启权限");
        sb.append(MPermissionUtil.toString(neverAskAgainPermission));
        if (deniedPermissions != null && !deniedPermissions.isEmpty()) {
            sb.append(",下次询问请授予权限");
            sb.append(MPermissionUtil.toString(deniedPermissions));
        }

        Toast.makeText(AudienceActivity.this, sb.toString(), Toast.LENGTH_LONG).show();
        if (isMasterAgreeToLink) {
            LogUtil.d(TAG, "permission denied as never ask again, send reject");
            MicHelper.getInstance().sendCustomNotify(roomId,
                    roomInfo.getCreator(),
                    PushMicNotificationType.REJECT_CONNECTING.getValue(),
                    null,
                    true);
        }
    }

    private void fetchMicList() {
        NIMClient.getService(ChatRoomService.class).fetchQueue(roomId).setCallback(new RequestCallback<List<Entry<String, String>>>() {
            @Override
            public void onSuccess(List<Entry<String, String>> entries) {
                linkedInfoList.addAll(entries);
                showOtherOnMicMember();
            }

            @Override
            public void onFailed(int i) {

            }

            @Override
            public void onException(Throwable throwable) {

            }
        });
    }


    //展示其他连麦者(不包括自己)的UI
    private void showOtherOnMicMember() {

        //是否需要保证相应的用户已经在linkedInfoList 及 joinedChannelAccountSet 中
        boolean needCheck = isMeOnMic;
        if (needCheck) {
            Iterator<Entry<String, String>> entries = linkedInfoList.iterator();
            while (entries.hasNext()) {
                Entry<String, String> entry = entries.next();
                String account = entry.key;
                if (joinedChannelAccountSet.contains(account)) {
                    showSingleMicMember(entry);
                    entries.remove();
                }
            }
            return;
        }

        // 不需要check
        for (Entry<String, String> entry : linkedInfoList) {
            showSingleMicMember(entry);
        }
        linkedInfoList.clear();
    }


    private void showSingleMicMember(Entry<String, String> entry) {

        String ext = entry.value;
        String account = entry.key;
        String nick = null;
        String meetingUid = null;
        MicStateEnum micStateEnum = null;
        int style = 0;
        try {
            JSONObject jsonObject = JSONObject.parseObject(ext);
            if (jsonObject != null) {
                JSONObject info = (JSONObject) jsonObject.get(PushLinkConstant.info);
                nick = info.getString(PushLinkConstant.nick);
                meetingUid = info.getString(PushLinkConstant.meetingUid);
                micStateEnum = MicStateEnum.typeOfValue(jsonObject.getIntValue(PushLinkConstant.state));
                style = jsonObject.getInteger(PushLinkConstant.style);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (micStateEnum == MicStateEnum.CONNECTED && !account.equals(DemoCache.getAccount())) {
            int index = getOtherEmptyIndex(account);
            showOtherMicLinkedView(index, account, meetingUid, nick, style);
        }

    }

    /********************** 观众连麦请求 ***************************/

    // 显示连麦布局
    private void showInteractionLayout() {

        interactionLayout.setVisibility(View.VISIBLE);
        interactionTipsLayout.setVisibility(View.GONE);
        if (liveType == LiveType.VIDEO_TYPE) {
            videoInteractionInitLayout.setVisibility(View.VISIBLE);
            audioInteractInitLayout.setVisibility(View.GONE);
        } else if (liveType == LiveType.AUDIO_TYPE) {
            videoInteractionInitLayout.setVisibility(View.GONE);
            audioInteractInitLayout.setVisibility(View.VISIBLE);
        }
    }

    // 申请视频连接
    private void doVideoLink() {

        showPushLinkLayout(true, R.string.video_applying);
        String ext = getPushLinkExt(AVChatType.VIDEO.getValue());
        ChatRoomHttpClient.getInstance().pushMicLink(roomId, DemoCache.getAccount(), ext, new ChatRoomHttpClient.ChatRoomHttpCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // 加入连麦队列成功，发送自定义通知给主播
                sendPushMicLinkNotify(AVChatType.VIDEO.getValue(), PushMicNotificationType.JOIN_QUEUE.getValue());
            }

            @Override
            public void onFailed(int code, String errorMsg) {
                LogUtil.d(TAG, "join queue failed, code:" + code);
                if (code == 419) {
                    Toast.makeText(AudienceActivity.this, "队列已满", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AudienceActivity.this, "join queue failed, code:" + code + ", errorMsg:" + errorMsg, Toast.LENGTH_SHORT).show();
                }
                revertPushUI();
            }
        });
    }

    // 申请语音连接 (视频直播)
    private void doAudioLinkAtVideoMode() {

        showPushLinkLayout(true, R.string.audio_applying);
        String ext = getPushLinkExt(AVChatType.AUDIO.getValue());
        ChatRoomHttpClient.getInstance().pushMicLink(roomId, DemoCache.getAccount(), ext, new ChatRoomHttpClient.ChatRoomHttpCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // 加入连麦队列成功，发送自定义通知给主播
                sendPushMicLinkNotify(AVChatType.AUDIO.getValue(), PushMicNotificationType.JOIN_QUEUE.getValue());
            }

            @Override
            public void onFailed(int code, String errorMsg) {
                LogUtil.d(TAG, "http push mic link errorMsg:" + errorMsg);
                Toast.makeText(AudienceActivity.this, "http push mic link errorMsg:" + errorMsg, Toast.LENGTH_SHORT).show();
                revertPushUI();
            }
        });
    }

    // 申请语音连接 (语音直播)
    private void doAudioLinkAtAudioMode() {

        showPushLinkLayout(false, R.string.audio_applying);
        String ext = getPushLinkExt(AVChatType.AUDIO.getValue());
        ChatRoomHttpClient.getInstance().pushMicLink(roomId, DemoCache.getAccount(), ext, new ChatRoomHttpClient.ChatRoomHttpCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // 加入连麦队列成功，发送自定义通知给主播
                sendPushMicLinkNotify(AVChatType.AUDIO.getValue(), PushMicNotificationType.JOIN_QUEUE.getValue());
            }

            @Override
            public void onFailed(int code, String errorMsg) {
                Toast.makeText(AudienceActivity.this, "http push mic link, errorMsg:" + errorMsg, Toast.LENGTH_SHORT).show();
                revertPushUI();
            }
        });
    }

    // 显示连接申请布局
    private void showPushLinkLayout(boolean isVideoMode, int applyingMode) {

        if (isVideoMode) {
            videoInteractionInitLayout.setVisibility(View.GONE);
        } else {
            audioInteractInitLayout.setVisibility(View.GONE);
        }
        applyingLayout.setVisibility(View.VISIBLE);
        applyMasterNameText.setText(TextUtils.isEmpty(masterNick) ? roomInfo.getCreator() : masterNick);
        applyingTip.setText(applyingMode);
    }

    // 连麦申请的扩展字段
    private String getPushLinkExt(int style) {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(PushLinkConstant.style, style);
        jsonObject.put(PushLinkConstant.state, MicStateEnum.WAITING.getValue());

        JSONObject infoJSON = new JSONObject();
        infoJSON.put(PushLinkConstant.nick, DemoCache.getUserInfo().getName());
        infoJSON.put(PushLinkConstant.avatar, AVATAR_DEFAULT);
        jsonObject.put(PushLinkConstant.info, infoJSON);

        return jsonObject.toString();
    }

    // 发送自定义通知给主播
    private void sendPushMicLinkNotify(int style, int command) {

        CustomNotification notification = new CustomNotification();
        notification.setSessionId(roomInfo.getCreator());
        notification.setSessionType(SessionTypeEnum.P2P);

        JSONObject json = new JSONObject();
        json.put(PushLinkConstant.roomid, roomId);
        json.put(PushLinkConstant.style, style);
        json.put(PushLinkConstant.command, command);
        JSONObject infoJSON = new JSONObject();
        infoJSON.put(PushLinkConstant.nick, DemoCache.getUserInfo().getName());
        infoJSON.put(PushLinkConstant.avatar, AVATAR_DEFAULT);
        json.put(PushLinkConstant.info, infoJSON);
        notification.setContent(json.toString());

        NIMClient.getService(MsgService.class).sendCustomNotification(notification).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                LogUtil.d(TAG, "send push mic success");
                isSelfAlreadyApply = true;
            }

            @Override
            public void onFailed(int i) {
                LogUtil.d(TAG, "send push mic failed, code:" + i);
                Toast.makeText(AudienceActivity.this, "申请失败, code:" + i, Toast.LENGTH_SHORT).show();
                revertPushUI();
            }

            @Override
            public void onException(Throwable throwable) {

            }
        });
    }

    // 撤销连麦申请布局
    private void revertPushUI() {
        if (liveType == LiveType.VIDEO_TYPE) {
            videoInteractionInitLayout.setVisibility(View.VISIBLE);
            audioInteractInitLayout.setVisibility(View.GONE);
        } else if (liveType == LiveType.AUDIO_TYPE) {
            videoInteractionInitLayout.setVisibility(View.GONE);
            audioInteractInitLayout.setVisibility(View.VISIBLE);
        }
        applyingLayout.setVisibility(View.GONE);
    }

    // 取消连麦申请
    private void cancelLinking() {
        revertPushUI();
        ChatRoomHttpClient.getInstance().popMicLink(roomId, DemoCache.getAccount(), new ChatRoomHttpClient.ChatRoomHttpCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // 取消连麦成功，发送自定义通知给主播
                MicHelper.getInstance().sendCustomNotify(roomId, roomInfo.getCreator(), PushMicNotificationType.EXIT_QUEUE.getValue(), null, true);
                isSelfAlreadyApply = false;
            }

            @Override
            public void onFailed(int code, String errorMsg) {
                LogUtil.d(TAG, "join queue failed, code:" + code);
                Toast.makeText(AudienceActivity.this, "join queue failed, errorMsg:" + errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }


    // 收到连麦通知
    @Override
    protected void onAgreeLinkedByMaster(JSONObject jsonObject) {

        if (!isSelfAlreadyApply) {
            // 我是第一次进来，上次状态清空，所以不连麦
            MicHelper.getInstance().sendCustomNotify(roomId,
                    roomInfo.getCreator(),
                    PushMicNotificationType.REJECT_CONNECTING.getValue(),
                    null,
                    true);
            return;
        }

        if (!jsonObject.containsKey(PushLinkConstant.style)) {
            return;
        }

        isMasterAgreeToLink = true;
        selfLinkType = jsonObject.getIntValue(PushLinkConstant.style);

        requestLivePermission();
    }


    private void doMicLinking() {

        AVChatManager.getInstance().enableRtc();
        if (mVideoCapturer == null) {
            mVideoCapturer = AVChatVideoCapturerFactory.createCameraCapturer(true);
            AVChatManager.getInstance().setupVideoCapturer(mVideoCapturer);
        }
        AVChatParameters parameters = new AVChatParameters();
        AVChatManager.getInstance().setParameter(AVChatParameters.KEY_AUDIO_REPORT_SPEAKER, true);
        parameters.setBoolean(AVChatParameters.KEY_SESSION_LIVE_MODE, true);
        parameters.setInteger(AVChatParameters.KEY_SESSION_MULTI_MODE_USER_ROLE, AVChatUserRole.NORMAL);
        parameters.setInteger(AVChatParameters.KEY_VIDEO_FIXED_CROP_RATIO, AVChatVideoCropRatio.CROP_RATIO_16_9);

        int videoOrientation = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ?
                AVChatVideoCaptureOrientation.ORIENTATION_PORTRAIT : AVChatVideoCaptureOrientation.ORIENTATION_LANDSCAPE_RIGHT;

        parameters.setInteger(AVChatParameters.KEY_VIDEO_CAPTURE_ORIENTATION, videoOrientation);
        parameters.setBoolean(AVChatParameters.KEY_VIDEO_ROTATE_IN_RENDING, false);
        AVChatManager.getInstance().setParameters(parameters);
        if (liveType == LiveType.VIDEO_TYPE) {
            AVChatManager.getInstance().enableVideo();
        }
        if (selfLinkType == AVChatType.VIDEO.getValue()) {
            AVChatManager.getInstance().startVideoPreview();
        }
        MicHelper.getInstance().joinChannel(meetingName,
                selfLinkType == AVChatType.VIDEO.getValue(),
                new MicHelper.ChannelCallback() {
                    @Override
                    public void onJoinChannelSuccess() {
                        // 打开话筒
                        AVChatManager.getInstance().setSpeaker(true);
                        // 释放拉流
                        releaseVideoPlayer();

                        //查找所有连麦者
                        fetchMicList();

                        // 显示连麦者自己的小画面
                        showSelfMicView();

                        preparedText.setVisibility(View.GONE);
                        videoView.setVisibility(View.GONE);
                        videoRender.setVisibility(View.VISIBLE);
                        interactionLayout.setVisibility(View.GONE);
                    }

                    @Override
                    public void onJoinChannelFailed() {
                        Toast.makeText(DemoCache.getContext(), "join channel failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // 释放拉流
    private void releaseVideoPlayer() {
        if (videoPlayer != null) {
            videoPlayer.resetVideo();
        }
        videoPlayer = null;
    }

    @Override
    protected void onMicLinkedMsg(ChatRoomMessage message) {

        // 在视频直播的模式下，如果自己不是连麦者，不用显示其他连麦者
        // 如果是音频直接模式下，不管自己是不是连麦者，都需要显示下其他连麦者
        if (!isMeOnMic && liveType == LiveType.VIDEO_TYPE) {
            return;
        }

        ConnectedAttachment attachment = (ConnectedAttachment) message.getAttachment();
        showOtherMicLinkedView(getOtherEmptyIndex(attachment.getAccount()),
                attachment.getAccount(),
                attachment.getMeetingUid(),
                attachment.getNick(),
                attachment.getStyle());

    }

    private int getOtherEmptyIndex(String account) {

        // 第 0 个 固定成观众自己的
        for (int i = 1; i < interactionGroupView.length; i++) {
            if (TextUtils.equals(account, interactionGroupView[i].account)) {
                doCloseInteractionView(i);
            }
        }

        int index = 1;
        if (interactionGroupView[index].account != null) {
            index = 2;
        }
        return index;
    }


    @Override
    protected void showOtherMicLinkedView(int index, String account, String meetingUid, String nick, int linkStyle) {

        if (DemoCache.getAccount().equals(account)) {
            return;
        }

        super.showOtherMicLinkedView(index, account, meetingUid, nick, linkStyle);
        InteractionView interactionView = interactionGroupView[index];
        interactionView.meetingUid = meetingUid;

        interactionView.account = account;
        interactionView.rootViewLayout.setVisibility(View.VISIBLE);
        interactionView.connectionViewCloseBtn.setVisibility(View.GONE);
        interactionView.audienceVolume.setVisibility(View.VISIBLE);

        if (linkStyle == AVChatType.AUDIO.getValue()) {
            interactionView.audienceLoadingLayout.setVisibility(View.GONE);
            interactionView.audioModeBypassLayout.setVisibility(View.VISIBLE);
            interactionView.bypassVideoRender.setVisibility(View.GONE);

        } else {
            interactionView.audienceLivingLayout.setVisibility(View.VISIBLE);
            interactionView.audioModeBypassLayout.setVisibility(View.GONE);
            interactionView.bypassVideoRender.setVisibility(View.VISIBLE);
            interactionView.livingBg.setVisibility(View.GONE);
            try {
                AVChatManager.getInstance().setupRemoteVideoRender(account, null, false, 0);
                AVChatManager.getInstance().setupRemoteVideoRender(account, interactionView.bypassVideoRender, false, AVChatVideoScalingType.SCALE_ASPECT_BALANCED);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private void showSelfMicView() {

        // 我是连麦者，所以永远是第一个窗口
        isMeOnMic = true;
        InteractionView interactionView = interactionGroupView[0]; //自己作为最底部的画面
        interactionView.account = DemoCache.getAccount();
        interactionView.rootViewLayout.setVisibility(View.VISIBLE);
        interactionView.audienceLoadingLayout.setVisibility(View.GONE);
        interactionView.rootViewLayout.setVisibility(View.VISIBLE);

        switchBtn.setVisibility(selfLinkType == AVChatType.VIDEO.getValue() ? View.VISIBLE : View.GONE);
        interactionBtn.setVisibility(View.GONE);
        inputPanel.hideInputPanel();
        inputPanel.collapse(true);
        controlContainer.setVisibility(View.VISIBLE);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);//设置置底
        controlContainer.setLayoutParams(lp);//动态改变布局


        if (liveType == LiveType.VIDEO_TYPE && selfLinkType == AVChatType.VIDEO.getValue()) {
            interactionView.audienceLivingLayout.setVisibility(View.VISIBLE);
            interactionView.audioModeBypassLayout.setVisibility(View.GONE);
            interactionView.bypassVideoRender.setVisibility(View.VISIBLE);
            AVChatManager.getInstance().setupLocalVideoRender(null, false, 0);
            AVChatManager.getInstance().setupLocalVideoRender(interactionView.bypassVideoRender, false, AVChatVideoScalingType.SCALE_ASPECT_BALANCED);
        } else if (selfLinkType == AVChatType.AUDIO.getValue()) {
            interactionView.bypassVideoRender.setVisibility(View.GONE);
            interactionView.audienceLivingLayout.setVisibility(View.GONE);
            interactionView.audioModeBypassLayout.setVisibility(View.VISIBLE);
        }

    }


    // 主动断开连麦
    @Override
    protected void doCloseInteraction(int index) {
        preparedText.setVisibility(View.VISIBLE);
        MicHelper.getInstance().audienceBrokeMic(selfLinkType == AVChatType.VIDEO.getValue(),
                liveType == LiveType.VIDEO_TYPE,
                meetingName);
        mVideoCapturer = null;
        switchVideoPlayer();
    }

    private void closeAllMicView() {
        // 隐藏布局（无论是否处于频道中，都要清理界面上的布局，如连麦者姓名）
        for (int i = 0; i < maxInteractionMembers; i++) {
            doCloseInteractionView(i);
        }
        resetSelfConnectionView();

        joinedChannelAccountSet.clear();
        linkedInfoList.clear();
        fetchMicListWhenAudioAudience();
    }

    // 收到主播断开连麦通知
    @Override
    protected void onMicDisconnectByMaster() {

        closeAllMicView();
        // 确保还处在频道中时，才要切换成拉流模式
        if (videoPlayer != null) {
            return;
        }

        preparedText.setVisibility(View.VISIBLE);
        // 离开频道
        if (liveType != LiveType.NOT_ONLINE) {
            MicHelper.getInstance().leaveChannel(selfLinkType == AVChatType.VIDEO.getValue(),
                    liveType == LiveType.VIDEO_TYPE,
                    true,
                    meetingName);
            mVideoCapturer = null;
            // 切换拉流
            switchVideoPlayer();
        }
    }

    @Override
    protected void onMicDisConnectedMsg(String account) {

        if (account == null || TextUtils.equals(account, DemoCache.getAccount())) {
            closeAllMicView();
            reOpenVideoPlay();
            return;
        }

        if (!TextUtils.isEmpty(account)) {
            int index = getInteractionViewIndexByAccount(account);
            doCloseInteractionView(index);
            return;
        }

    }

    protected void resetSelfConnectionView() {

        LogUtil.i(TAG, "reset Connection view");
        InteractionView selfInteractionView = interactionGroupView[0];
        selfInteractionView.livingBg.setVisibility(View.GONE);
        isMasterAgreeToLink = false;
        if (isMeOnMic) {
            isMeOnMic = false;
            isSelfAlreadyApply = false;
            interactionBtn.setVisibility(View.VISIBLE);
            switchBtn.setVisibility(View.GONE);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);

            lp.addRule(RelativeLayout.ABOVE, R.id.messageActivityBottomLayout);//设置在输入框上方
            controlContainer.setLayoutParams(lp);//动态改变布局
            selfInteractionView.bypassVideoRender.setVisibility(View.GONE);
            resetApplyLayout();
        }
        super.resetConnectionView(0);
    }

    private void resetApplyLayout() {
        videoInteractionInitLayout.setVisibility(View.VISIBLE);
        applyingLayout.setVisibility(View.GONE);
    }

    private void switchVideoPlayer() {
        if (videoPlayer == null && liveType != LiveType.NOT_ONLINE) {
            initAudienceParam();
        }
    }

    @Override
    protected void onReceiveChatRoomInfoUpdate(Map<String, Object> extension) {
        if (extension == null) {
            return;
        }

        if (extension.get(PushLinkConstant.type) != null) {
            liveType = LiveType.typeOfValue((int) extension.get(PushLinkConstant.type));
            if (liveType == LiveType.NOT_ONLINE) {
                pkInfoLayout.setVisibility(View.GONE);
                isPk = false;
                showFinishLayout();
                closeAllMicView();
                reOpenVideoPlay();
                return;
            }
        }
        parseRoomPkInfo(extension);

    }

    protected void parseRoomPkInfo(Map<String, Object> extension) {
        if (extension.get((PushLinkConstant.isPking)) == null) {
            return;
        }

        isPk = (boolean) extension.get((PushLinkConstant.isPking));
        switchNormalOrPKLayout();
        String pkFrom = (String) extension.get((PushLinkConstant.pkInviter));
        String pkTo = (String) extension.get((PushLinkConstant.pkInvitee));
        pkFromTv.setText(pkFrom);
        pkToTv.setText(pkTo);
    }

    private void reOpenVideoPlay() {
        LogUtil.d(TAG, "reOpenVideoPlay");
        if (videoPlayer != null) {
            videoPlayer.resetResource();
        } else {
            // videoPlayer为null 则在连麦中。退出channel
            LogUtil.d(TAG, "leaveRoom, meetingName:" + meetingName);
            MicHelper.getInstance().leaveChannel(selfLinkType == AVChatType.VIDEO.getValue(),
                    liveType == LiveType.VIDEO_TYPE,
                    true,
                    meetingName);
            mVideoCapturer = null;
            videoPlayer = new VideoPlayer(AudienceActivity.this,
                    videoView,
                    null,
                    pullUrl,
                    UserPreferences.getPlayerStrategy(),
                    this,
                    VideoConstant.VIDEO_SCALING_MODE_FILL_BLACK);
        }
        videoPlayer.postReopenVideoTask(VideoPlayer.VIDEO_COMPLETED_REOPEN_TIMEOUT);
    }


    /*************************** VideoPlayer.VideoPlayerProxy ***************************/

    @Override
    public boolean isDisconnected() {
        return false;
    }

    @Override
    public void onError() {
        LogUtil.d(TAG, "on error, show finish interaction_group_layout");
        preparedText.setVisibility(View.VISIBLE);
        if (!isShowFinishLayout) {
            isShowFinishLayout = true;
        } else {
            showFinishLayout();
        }
    }

    @Override
    public void onCompletion() {
        LogUtil.d(TAG, "on completion, show finish interaction_group_layout");
        if (!isShowFinishLayout) {
            isShowFinishLayout = true;
        } else {
            isStartLive = false;
            showFinishLayout();
        }
        TextView masterNickText = findView(R.id.finish_master_name);
        masterNickText.setText(TextUtils.isEmpty(masterNick) ? roomInfo.getCreator() : masterNick);
    }

    @Override
    public void onPrepared() {
        LogUtil.d(TAG, "on prepared, hide preparedText");
        if (liveType == LiveType.NOT_ONLINE) {
            return;
        }
        isStartLive = true;
        preparedText.setVisibility(View.GONE);
        liveFinishLayout.setVisibility(View.GONE);
        videoRender.setVisibility(View.GONE);
        videoView.setVisibility(View.VISIBLE);
        showModeLayout();
    }


    @Override
    public void onInfo(NELivePlayer mp, int what, int extra) {

    }


    private boolean isShowFinishLayout = false;

    // 显示直播已结束布局
    private void showFinishLayout() {
        liveFinishLayout.setVisibility(View.VISIBLE);
        finishTipText.setText(R.string.live_finish);
        inputPanel.collapse(true);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        findViews();
        initAudienceParam();
    }


}
