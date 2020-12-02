package com.netease.nim.chatroom.demo.entertainment.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.faceunity.nama.FURenderer;
import com.faceunity.nama.ui.FaceUnityView;
import com.faceunity.nama.utils.CameraUtils;
import com.netease.nim.chatroom.demo.DemoCache;
import com.netease.nim.chatroom.demo.R;
import com.netease.nim.chatroom.demo.base.util.ScreenUtil;
import com.netease.nim.chatroom.demo.base.util.StringUtil;
import com.netease.nim.chatroom.demo.base.util.TimeUtil;
import com.netease.nim.chatroom.demo.base.util.log.LogUtil;
import com.netease.nim.chatroom.demo.entertainment.adapter.GiftAdapter;
import com.netease.nim.chatroom.demo.entertainment.adapter.InteractionAdapter;
import com.netease.nim.chatroom.demo.entertainment.constant.GiftConstant;
import com.netease.nim.chatroom.demo.entertainment.constant.GiftType;
import com.netease.nim.chatroom.demo.entertainment.constant.LiveType;
import com.netease.nim.chatroom.demo.entertainment.constant.MicStateEnum;
import com.netease.nim.chatroom.demo.entertainment.constant.PKStateEnum;
import com.netease.nim.chatroom.demo.entertainment.constant.PushLinkConstant;
import com.netease.nim.chatroom.demo.entertainment.constant.PushMicNotificationType;
import com.netease.nim.chatroom.demo.entertainment.constant.PushType;
import com.netease.nim.chatroom.demo.entertainment.helper.ChatRoomMemberCache;
import com.netease.nim.chatroom.demo.entertainment.helper.GiftCache;
import com.netease.nim.chatroom.demo.entertainment.helper.MicHelper;
import com.netease.nim.chatroom.demo.entertainment.http.ChatRoomHttpClient;
import com.netease.nim.chatroom.demo.entertainment.model.Gift;
import com.netease.nim.chatroom.demo.entertainment.model.InteractionMember;
import com.netease.nim.chatroom.demo.entertainment.model.LiveInfoMode;
import com.netease.nim.chatroom.demo.entertainment.model.SimpleAVChatStateObserver;
import com.netease.nim.chatroom.demo.im.ui.dialog.EasyAlertDialog;
import com.netease.nim.chatroom.demo.im.ui.dialog.EasyAlertDialogHelper;
import com.netease.nim.chatroom.demo.im.ui.widget.SwitchButton;
import com.netease.nim.chatroom.demo.im.util.file.AttachmentStore;
import com.netease.nim.chatroom.demo.permission.MPermission;
import com.netease.nim.chatroom.demo.permission.annotation.OnMPermissionDenied;
import com.netease.nim.chatroom.demo.permission.annotation.OnMPermissionGranted;
import com.netease.nim.chatroom.demo.permission.annotation.OnMPermissionNeverAskAgain;
import com.netease.nim.chatroom.demo.permission.util.MPermissionUtil;
import com.netease.nim.chatroom.demo.utils.PreferenceUtil;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.avchat.AVChatCallback;
import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.AVChatStateObserver;
import com.netease.nimlib.sdk.avchat.constant.AVChatAudioEffectEvent;
import com.netease.nimlib.sdk.avchat.constant.AVChatAudioMixingEvent;
import com.netease.nimlib.sdk.avchat.constant.AVChatDeviceEvent;
import com.netease.nimlib.sdk.avchat.constant.AVChatLiveMode;
import com.netease.nimlib.sdk.avchat.constant.AVChatMediaCodecMode;
import com.netease.nimlib.sdk.avchat.constant.AVChatNetworkQuality;
import com.netease.nimlib.sdk.avchat.constant.AVChatResCode;
import com.netease.nimlib.sdk.avchat.constant.AVChatType;
import com.netease.nimlib.sdk.avchat.constant.AVChatUserRole;
import com.netease.nimlib.sdk.avchat.constant.AVChatVideoCaptureOrientation;
import com.netease.nimlib.sdk.avchat.constant.AVChatVideoCropRatio;
import com.netease.nimlib.sdk.avchat.constant.AVChatVideoFrameRate;
import com.netease.nimlib.sdk.avchat.constant.AVChatVideoQuality;
import com.netease.nimlib.sdk.avchat.constant.AVChatVideoQualityStrategy;
import com.netease.nimlib.sdk.avchat.constant.AVChatVideoScalingType;
import com.netease.nimlib.sdk.avchat.model.AVChatChannelInfo;
import com.netease.nimlib.sdk.avchat.model.AVChatData;
import com.netease.nimlib.sdk.avchat.model.AVChatLiveTaskConfig;
import com.netease.nimlib.sdk.avchat.model.AVChatNetworkStats;
import com.netease.nimlib.sdk.avchat.model.AVChatParameters;
import com.netease.nimlib.sdk.avchat.model.AVChatVideoFrame;
import com.netease.nimlib.sdk.avchat.video.AVChatCameraCapturer;
import com.netease.nimlib.sdk.avchat.video.AVChatTextureViewRenderer;
import com.netease.nimlib.sdk.avchat.video.AVChatVideoCapturerFactory;
import com.netease.nimlib.sdk.chatroom.ChatRoomService;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomMember;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomUpdateInfo;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nimlib.sdk.msg.model.CustomNotification;
import com.netease.nrtc.sdk.common.ImageFormat;
import com.netease.nrtc.sdk.common.VideoFilterParameter;
import com.netease.nrtc.sdk.video.VideoFrame;
import com.netease.nrtc.video.coding.VideoFrameFormat;
import com.netease.vcloud.video.effect.VideoEffect;
import com.netease.vcloud.video.effect.VideoEffectFactory;

import org.json.JSONException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;


/**
 * 房间推流  ， 大部分逻辑是从 LiveActivity 复制过来的 (我是被逼的)
 */
public class RoomLiveActivity extends LivePlayerBaseActivity implements InteractionAdapter.MemberLinkListener, SensorEventListener {


    private static final String TAG = "RoomLiveActivity";

    private final int USER_JOIN_OVERTIME = 10 * 1000;
    private final int VIDEO_MARK_MODE_CLOSE = 0;
    private final int VIDEO_MARK_MODE_STATIC = 1;
    private final int VIDEO_MARK_MODE_DYNAMIC = 2;
    private final int MIRROR_MODE_CLOSE_ALL = 0;
    private final int MIRROR_MODE_LOCAL_OPEN = 1;
    private final int MIRROR_MODE_PUSH_OPEN = 2;
    private final int MIRROR_MODE_OPEN_ALL = 3;

    // view
    private View btnCloseRoom;
    private View btnLeaveRoom;
    private View startLayout;
    private ImageView startLiveBgIv;
    private Button startBtn;
    private ImageButton switchBtn;
    private TextView noGiftText;
    private ViewGroup liveFinishLayout;
    private ImageButton controlExtendBtn;
    private LinearLayout controlBtnTop;
    private LinearLayout controlBtnMid;
    private TextView hdBtn;
    private LinearLayout videoClarityLayout;
    private ImageView ivVideoClarityHd;
    private ImageView ivVideoClaritySd;
    private boolean isVideoClaritySd = false;
    private ImageButton screenSwitchBtn;
    private ImageButton screenCameraBtn;
    private ImageButton screenBeautyBtn;
    private LinearLayout startLiveSwitchLayout;
    private LinearLayout screenSwitchHorizontal;
    private LinearLayout screenSwitchVertical;
    private View screenSwitchCover;
    private ViewGroup interactionLayout; // 互动布局
    private TextView noApplyText; // 暂无互动申请
    private TextView applyCountText;
    private AVChatTextureViewRenderer videoRender; // 主播画面
    private ImageButton musicBtn;   //背景乐
    private ImageButton shotBtn;    //截图
    private View shotCover;         //截图成功动画效果
    private ViewGroup backgroundMusicLayout;
    private RelativeLayout musicBlankView;
    private LinearLayout musicContentView;
    private TextView musicSongFirstContent;
    private TextView musicSongSecondContent;
    private ImageView musicSongFirstControl;
    private ImageView musicSongSecondControl;
    private SeekBar musicSongVolumeControl;
    private TextView musicAudioEffectFirst;
    private TextView musicAudioEffectSecond;
    private ImageButton beautyBtn; // 美颜按钮
    private LinearLayout videoBeautyLayout;
    private LinearLayout videoBeautyStrength;
    private ImageView videoBeautyOriginIv;
    private ImageView videoBeautyNaturalIv;
    private boolean isVideoBeautyOriginCurrent = false; //美颜默认打开
    private boolean isVideoBeautyOriginLast = false; //美颜默认打开
    private ImageView flashBtn; //闪光灯
    private boolean isVideoFlashOpen = false; //闪光灯默认关闭
    private ImageButton markBtn;//水印
    private LinearLayout videoMarkLayout;
    private ImageView videoMarkStaticIv;
    private ImageView videoMarkDynamicIv;
    private ImageView videoMarkCloseIv;
    private int markMode; //0关闭，1静态，2动态
    private boolean isVideoFocalLengthPanelOpen = false; //放大缩小控制面板默认关闭
    private ImageButton focalLengthBtn;//放大缩小
    private LinearLayout focalLengthLayout;
    private SeekBar videoFocalLengthSb;
    private int lashMirrorMode = MIRROR_MODE_LOCAL_OPEN; //0都关闭；1本地镜像打开，推流镜像关闭；2本地镜像关闭，推流镜像打开；3都打开
    private ImageButton mirrorBtn;//镜像
    private LinearLayout videoMirrorLayout;
    private SwitchButton videoMirrorLocalSb;
    private SwitchButton videoMirrorPushSb;

    private View videoPKControlLayout;
    private TextView videoPKTitleView;
    private View videoPKInviteLayout;
    private EditText videoPKInviteAccount;
    private TextView videoPKTipsMsg;
    private Button videoPKTipsBtn;
    private View videoPKWaitingLayout;
    private TextView videoPKWaitingName;
    private View getVideoPKWaitingTips;
    private View videoPKConfirmLayout;
    private ImageButton pkBtn;
    private PKStateEnum pkStateEnum = PKStateEnum.NONE;
    private String pkAccount = null;
    private EasyAlertDialog pkDialog;

    private LinearLayout videoDisplayLayout;
    private ViewGroup videoPkLayout;
    private TextView videoPkDuration;
    private ViewGroup videoPkLivingLayout;
    protected AVChatTextureViewRenderer pkVideoRender;
    protected TextView pkVideoBg;
    private RelativeLayout pkVideoCloseBtn;
    private LinearLayout pkVideoCloseConfirm;
    /* 网络状态 */
    private ViewGroup networkStateLayout;
    private TextView netStateTipText; // 网络状态提示
    private ImageView netStateImage;
    private TextView netOperateText; // 网络操作提示
    // state
    private boolean disconnected = false; // 是否断网（断网重连用）
    private boolean isStartLive = false; // 是否开始直播推流
    private boolean isStartLiving = false; //是否正在开始直播
    private boolean isMusicFirstPlaying = false;
    private boolean isMusicFirstPause = false;
    private boolean isMusicSecondPlaying = false;
    private boolean isMusicSecondPause = false;
    private boolean isPermissionGrant = false;
    private boolean isReleaseEngine = true;
    private boolean isBeautyBtnCancel = false;
    private boolean isFilterTypeSet = false;
    private int networkQuality = -1;


    private VideoEffect mVideoEffect;
    private int mCurWidth, mCurHeight;
    private Bitmap mWaterMaskBitmapStatic;
    private Bitmap[] mWaterMaskBitmapDynamic;
    private volatile boolean isUnInitVideoEffect = false;// 是否销毁滤镜模块
    private volatile boolean mIsmWaterMaskAdded = false;
    private int rotation;
    private int mDropFramesWhenConfigChanged = 0; //丢帧数

    // data
    private List<Gift> giftList = new ArrayList<>(); // 礼物列表数据
    private int interactionCount = 0; // 互动申请人数
    private InteractionAdapter interactionAdapter; // 互动人员adapter
    private List<InteractionMember> interactionDataSource; // 互动人员列表
    private String clickAccount; // 选择的互动人员帐号
    private LinkedList<InteractionMember> currentInteractionMembers; // 当前连麦者

    /**
     * 主播离开时用来缓存连麦者，主播重新加入时，再放回currentInteractionMembers
     */
    private LinkedList<InteractionMember> cacheInteractionMembers = new LinkedList<>();

    private AVChatCameraCapturer mVideoCapturer;
    private Handler mVideoEffectHandler;
    private String pkMeetingName;

    private String musicPath;

    // 是否有观众连麦
    protected boolean isHasAudienceOnLink = false;
    private String peerPushAccount;
    private String peerPushUrl;
    private String peerLayoutPara;

    private LiveInfoMode liveInfoMode;


    // FU 美颜
    private FURenderer mFURenderer;
    private boolean mIsFirstFrame = true;
    private boolean mIsFuBeautyOpen;
    private int mSkippedFrames;
    private int mCameraFacing = Camera.CameraInfo.CAMERA_FACING_FRONT;
    private SensorManager mSensorManager;


    public static void start(Context context, boolean isVideo) {
        Intent intent = new Intent();
        intent.setClass(context, RoomLiveActivity.class);
        intent.putExtra(EXTRA_MODE, isVideo);
        intent.putExtra(EXTRA_CREATOR, true);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!copyMusicFile()) {
            return;
        }
        pushType = PushType.ROOM_PUSH_TYPE;

        String fuBeautyOpen = PreferenceUtil.getString(this, PreferenceUtil.KEY_FACEUNITY_ISON);
        mIsFuBeautyOpen = "true".equals(fuBeautyOpen);

        findViews();
        updateRoomUI(true);
        loadGift();
        registerLiveObservers(true);
        if (disconnected) {
            Toast.makeText(RoomLiveActivity.this, R.string.net_broken, Toast.LENGTH_SHORT).show();
            return;
        }
        startLiveSwitchLayout.setVisibility(View.GONE);
        requestLivePermission();
    }

    private boolean copyMusicFile() {

        if (Environment.getExternalStorageDirectory() != null) {
            musicPath = Environment.getExternalStorageDirectory().getAbsolutePath() +
                    File.separator +
                    getPackageName() +
                    File.separator +
                    "music" +
                    File.separator;

            AttachmentStore.copy(this, "music/first_song.mp3", musicPath, "first_song.mp3");
            AttachmentStore.copy(this, "music/second_song.mp3", musicPath, "second_song.mp3");
            AttachmentStore.copy(this, "music/test1.wav", musicPath, "test1.wav");
            AttachmentStore.copy(this, "music/test2.wav", musicPath, "test2.wav");
            return true;
        }

        Toast.makeText(this, "外部存储卡异常", Toast.LENGTH_LONG).show();
        finish();
        return false;
    }

    @Override
    protected int getActivityLayout() {
        return R.layout.room_live_player_activity;
    }

    @Override
    protected int getLayoutId() {
        return R.id.live_layout;
    }

    @Override
    protected int getControlLayout() {
        return liveType == LiveType.VIDEO_TYPE ? R.layout.room_live_video_control_layout : R.layout.room_live_audio_control_layout;
    }

    @Override
    protected void parseIntent() {
        super.parseIntent();
        Intent intent = getIntent();
        liveType = intent.getBooleanExtra(EXTRA_MODE, true) ? LiveType.VIDEO_TYPE : LiveType.AUDIO_TYPE;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        checkCloseLive();
    }

    @Override
    protected void onDestroy() {
        leaveAndReleaseAVRoom();
        giftList.clear();
        if (pkDialog != null && pkDialog.isShowing()) {
            pkDialog.dismiss();
        }
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
        }
        registerLiveObservers(false);
        super.onDestroy();

    }

    // 退出聊天室
    private void confirmCloseLive() {
        if (startLayout.getVisibility() == View.VISIBLE) {
            closeLive();
            return;
        }
        EasyAlertDialogHelper.createOkCancelDiolag(this, null, getString(R.string.finish_confirm),
                getString(R.string.confirm), getString(R.string.cancel), true,
                new EasyAlertDialogHelper.OnDialogActionListener() {
                    @Override
                    public void doCancelAction() {

                    }

                    @Override
                    public void doOkAction() {
                        closeLive();
                    }
                }).show();

    }

    private void closeLive() {
        isStartLive = false;
        leaveAndReleaseAVRoom();
        resetChatRoomLiveType();
        finish();
    }


    private void showLiveFinishLayout() {
        runOnUiThread(() -> {
            liveFinishLayout.setVisibility(View.VISIBLE);
            TextView masterNickText = findView(R.id.finish_master_name);
            masterNickText.setText(TextUtils.isEmpty(masterNick) ? (roomInfo == null ? "" : roomInfo.getCreator()) : masterNick);
        });
    }


    private void resetChatRoomLiveType() {
        if (pkStateEnum == PKStateEnum.ONLINE) {
            MicHelper.getInstance().sendCustomPKNotify(pkAccount, PushMicNotificationType.CANCEL_INTERACT.getValue(), null);
            pkStateEnum = PKStateEnum.NONE;
            return;
        }
        ChatRoomUpdateInfo chatRoomUpdateInfo = new ChatRoomUpdateInfo();
        Map<String, Object> map = new HashMap<>(1);
        map.put(PushLinkConstant.LIVE_TYPE, LiveType.NOT_ONLINE.getValue());
        chatRoomUpdateInfo.setExtension(map);
        NIMClient.getService(ChatRoomService.class).updateRoomInfo(roomId, chatRoomUpdateInfo, true, map).setCallback(null);
    }

    private void joinLiveAVRoom() {

        if (isDestroyed) {
            return;
        }
        LogUtil.i(TAG, "start join av room ");
        MicHelper.getInstance().joinAVRoom(meetingName, liveType == LiveType.VIDEO_TYPE, new AVChatCallback<AVChatData>() {
            @Override
            public void onSuccess(AVChatData avChatData) {
                if (isDestroyed) {
                    return;
                }
                LogUtil.i(TAG, "join av room success");
                dropQueue();
            }

            @Override
            public void onFailed(int code) {
                if (isDestroyed) {
                    return;
                }
                LogUtil.e(TAG, "join av room failed , code : " + code);
                if (code == AVChatResCode.ERROR_JOIN_ROOM_NON_EXISTENT) {
                    Toast.makeText(DemoCache.getContext(), "房间已经解散", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                Toast.makeText(DemoCache.getContext(), "join channel failed", Toast.LENGTH_SHORT).show();
                showLiveFinishLayout();
            }

            @Override
            public void onException(Throwable throwable) {
                if (isDestroyed) {
                    return;
                }
                LogUtil.e(TAG, "join av room exception  ", throwable);
                Toast.makeText(DemoCache.getContext(), "join channel failed", Toast.LENGTH_SHORT).show();
                showLiveFinishLayout();
            }
        });

    }

    protected void findViews() {
        super.findViews();
        rootView = findView(R.id.live_layout);
        videoRender = findView(R.id.video_render);
        btnCloseRoom = findView(R.id.btn_close_room);
        btnLeaveRoom = findView(R.id.btn_leave_room);
        startLayout = findViewById(R.id.start_layout);
        startLiveBgIv = findViewById(R.id.start_live_bg_iv);
        startBtn = findViewById(R.id.start_live_btn);
        switchBtn = findViewById(R.id.switch_btn);
        noGiftText = findView(R.id.no_gift_tip);
        interactionLayout = findView(R.id.live_interaction_layout);
        noApplyText = findView(R.id.no_apply_tip);
        applyCountText = findView(R.id.apply_count_text);
        fakeListText = findView(R.id.fake_list_text);
        if (liveType == LiveType.VIDEO_TYPE && getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            fakeListText.setVisibility(View.VISIBLE);
        } else {
            fakeListText.setVisibility(View.GONE);
        }

        //底部控制按钮
        controlExtendBtn = findView(R.id.control_extend_btn);
        controlBtnTop = findView(R.id.control_btn_top);
        controlBtnMid = findView(R.id.control_btn_mid);

        // 高清
        hdBtn = findView(R.id.hd_btn);
        findClarityLayout();
        //开始直播页面按钮
        LinearLayout startLiveControlLayout = findView(R.id.start_live_control_layout);
        screenSwitchBtn = findView(R.id.start_screen_btn);
        screenCameraBtn = findView(R.id.start_switch_btn);
        screenBeautyBtn = findView(R.id.start_beauty_btn);
        startLiveSwitchLayout = findView(R.id.live_screen_switch_layout);
        screenSwitchHorizontal = findView(R.id.screen_switch_horizontal);
        screenSwitchVertical = findView(R.id.screen_switch_vertical);
        screenSwitchCover = findView(R.id.live_screen_switch_cover);

        //背景乐
        musicBtn = findView(R.id.music_btn);
        findMusicLayout();

        //截图
        shotBtn = findView(R.id.shot_btn);
        shotCover = findView(R.id.live_shot_cover);

        //美颜
        beautyBtn = findView(R.id.beauty_btn);
        findBeautyLayout();

        //闪光灯
        flashBtn = findView(R.id.flash_btn);

        //水印
        markBtn = findView(R.id.mark_btn);
        findMarkLayout();

        //镜像
        mirrorBtn = findView(R.id.mirror_btn);
        findMirrorLayout();

        pkBtn = findView(R.id.pk_btn);
        findPKLayout();

        //焦距
        focalLengthBtn = findView(R.id.enlarge_btn);
        findFocalLengthLayout();

        // 直播结束
        liveFinishLayout = findView(R.id.live_finish_layout);
        Button liveFinishBtn = findView(R.id.finish_btn);
        liveFinishBtn.setOnClickListener(v -> exitChatRoom());

        // 初始化连线人员布局
        findInteractionMemberLayout();
        setListener();
        // 视频/音频，布局设置
        if (liveType == LiveType.AUDIO_TYPE) {
            switchBtn.setVisibility(View.GONE);
            hdBtn.setVisibility(View.GONE);
            beautyBtn.setVisibility(View.GONE);
            shotBtn.setVisibility(View.GONE);
            startLiveControlLayout.setVisibility(View.GONE);
        } else if (liveType == LiveType.VIDEO_TYPE) {
            switchBtn.setVisibility(View.VISIBLE);
            hdBtn.setVisibility(View.VISIBLE);
            beautyBtn.setVisibility(View.VISIBLE);
            shotBtn.setVisibility(View.VISIBLE);
            startLiveControlLayout.setVisibility(View.VISIBLE);
        }

        // 网络状态
        networkStateLayout = findView(R.id.network_state_layout);
        netStateTipText = findView(R.id.net_state_tip);
        netStateImage = findView(R.id.network_image);
        netOperateText = findView(R.id.network_operation_tip);
        findPkLayout();
    }


    // pk布局
    protected void findPkLayout() {
        videoDisplayLayout = findView(R.id.video_display_layout);

        videoPkLayout = findView(R.id.pk_video_layout);
        videoPkDuration = findView(R.id.pk_duration);
        videoPkLivingLayout = findView(R.id.pk_living_layout);
        pkVideoRender = findView(R.id.pk_video_render);
        pkVideoBg = findView(R.id.no_video_bg);
        pkVideoCloseBtn = findView(R.id.pk_close_btn);
        pkVideoCloseConfirm = findView(R.id.pk_close_confirm_layout);
        pkVideoCloseBtn.setOnClickListener(v -> switchPkVideoOrCloseDialog(false));
        findView(R.id.pk_close_confirm).setOnClickListener(v -> {
            switchPkVideoOrCloseDialog(true);
            MicHelper.getInstance().sendCustomPKNotify(pkAccount, PushMicNotificationType.EXITED.getValue(), null);
            leavePKAVRoom();
        });
        findView(R.id.pk_close_cancel).setOnClickListener(v -> switchPkVideoOrCloseDialog(true));

    }

    private void switchPkVideoOrCloseDialog(boolean isPkVideo) {
        if (isPkVideo) {
            pkVideoCloseBtn.setVisibility(View.VISIBLE);
            pkVideoCloseConfirm.setVisibility(View.GONE);
        } else {
            pkVideoCloseBtn.setVisibility(View.GONE);
            pkVideoCloseConfirm.setVisibility(View.VISIBLE);
        }
    }

    private Runnable pkDuration = new Runnable() {
        @Override
        public void run() {
            videoPkDuration.setText(TimeUtil.secToMinTime((int) ((System.currentTimeMillis() - pkDurationStart) / 1000)));
            getHandler().postDelayed(pkDuration, 1000);
        }
    };

    private long pkDurationStart;

    private void switchNormalOrPKLayout(final boolean isPk) {
        runOnUiThread(() -> {
            if (isPk) {
                RelativeLayout.LayoutParams videoDisplayParams = (RelativeLayout.LayoutParams) videoDisplayLayout.getLayoutParams();
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                    videoDisplayParams.height = ScreenUtil.dip2px(300);
                    videoDisplayParams.topMargin = ScreenUtil.dip2px(120);
                } else {
                    videoDisplayParams.height = ScreenUtil.dip2px(200);
                    videoDisplayParams.topMargin = ScreenUtil.dip2px(60);
                }

                videoDisplayLayout.setLayoutParams(videoDisplayParams);
                videoPkLayout.setVisibility(View.VISIBLE);
                videoPkDuration.setVisibility(View.VISIBLE);
                videoPkLivingLayout.setVisibility(View.VISIBLE);
                pkDurationStart = System.currentTimeMillis();
                getHandler().postDelayed(pkDuration, 1000);
            } else {
                RelativeLayout.LayoutParams videoDisplayParams = (RelativeLayout.LayoutParams) videoDisplayLayout.getLayoutParams();
                videoDisplayParams.height = LinearLayout.LayoutParams.MATCH_PARENT;
                videoDisplayParams.topMargin = ScreenUtil.dip2px(0);
                videoDisplayLayout.setLayoutParams(videoDisplayParams);
                videoPkDuration.setVisibility(View.GONE);
                videoPkLayout.setVisibility(View.GONE);
                videoPkLivingLayout.setVisibility(View.GONE);
                switchPkVideoOrCloseDialog(true);
                getHandler().removeCallbacks(pkDuration);
            }
        });

    }

    private void findFocalLengthLayout() {
        focalLengthLayout = findView(R.id.focal_length_layout);
        ImageView videoFocalLengthMinus = findView(R.id.focal_length_minus);
        videoFocalLengthMinus.setOnClickListener(focalLengthListener);
        ImageView videoFocalLengthPlus = findView(R.id.focal_length_plus);
        videoFocalLengthPlus.setOnClickListener(focalLengthListener);
        videoFocalLengthSb = findView(R.id.focal_length_sb);

        videoFocalLengthSb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mVideoCapturer.setZoom(seekBar.getProgress());
            }
        });

    }

    private void findMirrorLayout() {
        videoMirrorLayout = findView(R.id.video_mirror_layout);
        RelativeLayout videoMirrorBlankView = findView(R.id.video_mirror_blank_view);
        videoMirrorLocalSb = findView(R.id.video_mirror_local_sb);
        videoMirrorPushSb = findView(R.id.video_mirror_push_sb);
        TextView videoMirrorCancelBtn = findView(R.id.video_mirror_button_cancel);
        TextView videoMirrorConfirmBtn = findView(R.id.video_mirror_button_confirm);

        videoMirrorBlankView.setOnClickListener(mirrorListener);
        videoMirrorCancelBtn.setOnClickListener(mirrorListener);
        videoMirrorConfirmBtn.setOnClickListener(mirrorListener);
        videoMirrorLocalSb.setOnChangedListener((v, checkState) -> AVChatManager.getInstance().setParameter(AVChatParameters.KEY_VIDEO_LOCAL_PREVIEW_MIRROR, checkState));
        videoMirrorPushSb.setOnChangedListener((v, checkState) -> AVChatManager.getInstance().setParameter(AVChatParameters.KEY_VIDEO_TRANSPORT_MIRROR, checkState));
    }

    private void findPKLayout() {
        videoPKControlLayout = findView(R.id.video_pk_layout);
        View videoPKBlankView = findView(R.id.video_pk_blank_view);
        videoPKBlankView.setOnClickListener(pkBlankListener);
        videoPKTitleView = findView(R.id.video_pk_title_view);
        videoPKInviteLayout = findView(R.id.video_pk_content_invite);
        videoPKInviteAccount = findView(R.id.video_pk_content_invite_account);
        videoPKTipsMsg = findView(R.id.video_pk_tips_msg);
        videoPKTipsBtn = findView(R.id.video_pk_tips_btn);
        videoPKTipsBtn.setOnClickListener(pkCancelListener);
        videoPKWaitingLayout = findView(R.id.video_pk_content_waiting);
        videoPKWaitingName = findView(R.id.video_pk_content_waiting_name);
        getVideoPKWaitingTips = findView(R.id.video_pk_content_waiting_tips);
        videoPKConfirmLayout = findView(R.id.video_pk_confirm_layout);
        View videoPKConfirmOK = findView(R.id.video_pk_button_confirm);
        videoPKConfirmOK.setOnClickListener(view -> {
            inputPanel.collapse(true);

            for (InteractionMember tmp : currentInteractionMembers) {
                if (tmp.getMicStateEnum() == MicStateEnum.CONNECTED) {
                    showPKMessage("你当前在和观众互动，请先结束互动再发起PK邀请");
                    return;
                }
            }

            pkAccount = videoPKInviteAccount.getText().toString();
            if (!TextUtils.isEmpty(pkAccount)) {
                if (pkAccount.equals(DemoCache.getAccount())) {
                    showPKMessage("你不能与自己进行PK，请重新输入其他主播ID");
                    return;
                }
                //先尝试
                pkStateEnum = PKStateEnum.WAITING;
                showPKWaitingLayout();
                getHandler().postDelayed(pkAccountTimeOutRunnable, USER_JOIN_OVERTIME);
                //发送主播是否在线询问消息
                MicHelper.getInstance().sendCustomPKNotify(pkAccount, PushMicNotificationType.TRY_ROOM_PUSH_INVITE_ANCHOR.getValue(), null);
            }
        });
        View videoPKConfirmCancel = findView(R.id.video_pk_button_cancel);
        videoPKConfirmCancel.setOnClickListener(pkCancelListener);
    }

    Observer<CustomNotification> customPKNotificationObserver = new Observer<CustomNotification>() {
        @Override
        public void onEvent(CustomNotification customNotification) {
            if (customNotification == null) {
                return;
            }

            String content = customNotification.getContent();
            try {
                JSONObject json = JSON.parseObject(content);
                int id = json.getIntValue(PushLinkConstant.COMMAND);
                final String fromPKMeetingName = json.getString(PushLinkConstant.PK_ROOM_NAME);

                JSONObject info = json.getJSONObject(PushLinkConstant.INFO);
                final String nickName = info.getString(PushLinkConstant.NICK);
                peerPushUrl = info.getString(PushLinkConstant.PUSH_URL);
                peerLayoutPara = info.getString(PushLinkConstant.LAYOUT_PARA);
                final String account = customNotification.getFromAccount();
                PushMicNotificationType type = PushMicNotificationType.typeOfValue(id);
                LogUtil.i(TAG, "custom notification : json : " + json + " , accid : " + account);
                switch (type) {
                    // A : PK 发起者 ， B: PK 接收者
                    // 收到A 的邀请 (不过是主播推流) ， B 直接拒绝
                    case TRY_INVITE_ANCHOR:
                        MicHelper.getInstance().sendCustomPKNotify(account, PushMicNotificationType.INVALID.getValue(), null);
                        break;
                    // 收到A 的邀请 (房间推流) ， B 回复在线
                    case TRY_ROOM_PUSH_INVITE_ANCHOR:
                        MicHelper.getInstance().sendCustomPKNotify(account, PushMicNotificationType.REPLY_INVITATION.getValue(), null);
                        break;
                    // 收到 B 的在线应答 ， 邀请B进行PK
                    case REPLY_INVITATION:
                        pkStateEnum = PKStateEnum.ONLINE;
                        getHandler().removeCallbacks(pkAccountTimeOutRunnable);
                        MicHelper.getInstance().sendCustomPKNotify(account, PushMicNotificationType.INVITE_ANCHOR.getValue(), null, liveInfoMode.getPushUrl(), getPushLayoutPara());
                        break;
                    // 收到A的PK邀请
                    case INVITE_ANCHOR:
                        if (isHasAudienceOnLink || roomInfo == null || TextUtils.isEmpty(peerPushUrl)) {
                            MicHelper.getInstance().sendCustomPKNotify(account, PushMicNotificationType.INVALID.getValue(), null);
                            return;
                        }
                        if (liveType == LiveType.AUDIO_TYPE) {
                            MicHelper.getInstance().sendCustomPKNotify(account, PushMicNotificationType.REJECT_INVITATION.getValue(), null);
                        } else if (pkStateEnum == PKStateEnum.PKING || pkStateEnum == PKStateEnum.BE_INVITATION || pkStateEnum == PKStateEnum.WAITING || pkStateEnum == PKStateEnum.ONLINE) {
                            MicHelper.getInstance().sendCustomPKNotify(account, PushMicNotificationType.ININTER_ACTIONS.getValue(), null);
                        } else {
                            pkStateEnum = PKStateEnum.BE_INVITATION;
                            peerPushAccount = account;
                            pkDialog = EasyAlertDialogHelper.createOkCancelDiolag(RoomLiveActivity.this, "PK 邀请", nickName + "邀请你PK",
                                    "接受", "拒绝", false,
                                    new EasyAlertDialogHelper.OnDialogActionListener() {
                                        @Override
                                        public void doCancelAction() {
                                            pkStateEnum = PKStateEnum.NONE;
                                            MicHelper.getInstance().sendCustomPKNotify(account, PushMicNotificationType.REJECT_INVITATION.getValue(), null);
                                        }

                                        @Override
                                        public void doOkAction() {
                                            //接受A的PK邀请
                                            inviteeJoinPKRoom(account, nickName);
                                        }
                                    });
                            pkDialog.show();
                        }

                        break;
                    // A 取消了PK 邀请
                    case CANCEL_INTERACT:
                        if (pkDialog != null && pkDialog.isShowing()) {
                            pkStateEnum = PKStateEnum.NONE;
                            pkDialog.dismiss();
                        }
                        break;

                    // B 接受了A邀请
                    case AGREE_INVITATION:
                        inviterJoinPKRoom(fromPKMeetingName, nickName);
                        break;
                    // B 拒绝了A邀请
                    case REJECT_INVITATION:
                        pkStateEnum = PKStateEnum.EXITED;
                        showPKMessage("很遗憾，" + nickName + "拒绝了你的PK邀请");
                        break;
                    case INVALID:
                        pkStateEnum = PKStateEnum.EXITED;
                        showPKMessage(nickName + "有可能不是主播或者暂时不能参加PK");
                        break;
                    // B 正忙
                    case ININTER_ACTIONS:
                        pkStateEnum = PKStateEnum.NONE;
                        showPKMessage("邀请的主播正在PK，请稍后发起邀请");
                        break;
                    // 有一方离开PK
                    case EXITED:
                        leavePKAVRoom();
                        break;
                    default:
                        break;
                }

            } catch (Exception e) {
                LogUtil.e(TAG, e.toString());
            }
        }
    };

    private String getPushLayoutPara() {

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            return "{\"version\":0,\"set_host_as_main\":false,\"host_area\":{\"adaption\":1,\"position_x\":0,\"position_y\":2000,\"width_rate\":5000,\"height_rate\":5000}," +
                    "\"special_show_mode\":true,\"n_host_area_number\":1,\"main_width\":480,\"main_height\":640," +
                    "\"background\":{\"rgb_r\":0,\"rgb_g\":0,\"rgb_b\":0}," +
                    "\"n_host_area_0\":{\"position_x\":5000,\"position_y\":2000,\"width_rate\":5000,\"height_rate\":5000,\"adaption\":1}}";
        }

        return "{\"version\":0,\"set_host_as_main\":false,\"host_area\":{\"adaption\":1,\"position_x\":0,\"position_y\":2500,\"width_rate\":5000,\"height_rate\":5000}," +
                "\"special_show_mode\":true,\"n_host_area_number\":1,\"main_width\":640,\"main_height\":480," +
                "\"background\":{\"rgb_r\":0,\"rgb_g\":0,\"rgb_b\":0}," +
                "\"n_host_area_0\":{\"position_x\":5000,\"position_y\":2500,\"width_rate\":5000,\"height_rate\":5000,\"adaption\":1}}";
    }

    // 等待PK主播是否在线超时处理
    Runnable pkAccountTimeOutRunnable = () -> {
        if (pkStateEnum == PKStateEnum.WAITING) {
            pkStateEnum = PKStateEnum.INVITE_TIME_OUT;
            showPKMessage("邀请PK主播此刻不在线，请稍后邀请");
        }
    };

    private void showPKMessage(String message) {
        videoPKControlLayout.setVisibility(View.VISIBLE);
        videoPKTitleView.setText("PK提醒");
        videoPKInviteLayout.setVisibility(View.GONE);
        videoPKConfirmLayout.setVisibility(View.GONE);
        videoPKWaitingLayout.setVisibility(View.GONE);
        videoPKTipsMsg.setVisibility(View.VISIBLE);
        videoPKTipsBtn.setVisibility(View.VISIBLE);
        videoPKTipsMsg.setText(message);
        videoPKTipsBtn.setText("知道了");
    }

    private void showPKWaitingLayout() {
        videoPKControlLayout.setVisibility(View.VISIBLE);
        videoPKTitleView.setText("等待对手");
        videoPKInviteLayout.setVisibility(View.GONE);
        videoPKConfirmLayout.setVisibility(View.GONE);
        videoPKTipsMsg.setVisibility(View.GONE);
        videoPKWaitingLayout.setVisibility(View.VISIBLE);
        videoPKTipsBtn.setVisibility(View.VISIBLE);
        getVideoPKWaitingTips.setVisibility(View.VISIBLE);
        videoPKTipsBtn.setText("取消等待");
        videoPKWaitingName.setText(pkAccount);
    }

    private void showPkInviteLayout() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        videoPKControlLayout.setVisibility(View.VISIBLE);
        videoPKTitleView.setText("邀请PK");
        videoPKInviteLayout.setVisibility(View.VISIBLE);
        videoPKConfirmLayout.setVisibility(View.VISIBLE);
        videoPKWaitingLayout.setVisibility(View.GONE);
        videoPKTipsMsg.setVisibility(View.GONE);
        videoPKTipsBtn.setVisibility(View.GONE);
    }

    private void showPKFinishLayout() {
        videoPKControlLayout.setVisibility(View.VISIBLE);
        videoPKTitleView.setText("PK主播");
        videoPKInviteLayout.setVisibility(View.GONE);
        videoPKConfirmLayout.setVisibility(View.GONE);
        videoPKTipsMsg.setVisibility(View.GONE);
        videoPKWaitingLayout.setVisibility(View.VISIBLE);
        videoPKTipsBtn.setVisibility(View.VISIBLE);
        getVideoPKWaitingTips.setVisibility(View.GONE);
        videoPKTipsBtn.setText("结束PK");
        videoPKWaitingName.setText(pkAccount);
    }

    private void hidePKFinishLayout() {
        runOnUiThread(() -> {
            videoPKControlLayout.setVisibility(View.GONE);
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        });
    }

    /**
     * 接受对方的PK邀请
     */
    private synchronized void inviteeJoinPKRoom(final String account, final String nickName) {

        LogUtil.i(TAG, "invitee start join pk av room ");
        releaseEngine();

        AVChatManager.getInstance().leaveRoom2(meetingName, new AVChatCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                createPKAVRoom(nickName, account);
            }

            @Override
            public void onFailed(int i) {
                LogUtil.e(TAG, "leave channel failed, code:" + i);
            }

            @Override
            public void onException(Throwable throwable) {
                LogUtil.e(TAG, "leave channel exception, throwable:" + throwable.getMessage());
            }
        });

    }

    /**
     * 对方接受了自己的PK邀请
     */
    private synchronized void inviterJoinPKRoom(final String fromPKMeetingName, final String nickName) {

        LogUtil.i(TAG, "inviter start join pk av room ");
        releaseEngine();

        AVChatManager.getInstance().leaveRoom2(meetingName, new AVChatCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                if (isDestroyed) {
                    return;
                }
                // delay 一下，等待SDK 释放完成 ，要不然下一通可能无法正常启动，SDK 引擎 释放比较慢
                uiHandler.postDelayed(() -> {

                    if (isDestroyed) {
                        return;
                    }
                    LogUtil.i(TAG, "inviter start pk , leave old av room success ");

                    //切换布局
                    switchNormalOrPKLayout(true);
                    hidePKFinishLayout();
                    startEngine();
                    MicHelper.getInstance().joinAVRoom(fromPKMeetingName, liveType == LiveType.VIDEO_TYPE, new MicHelper.ChannelCallback() {
                        @Override
                        public void onJoinChannelSuccess() {
                            if (isDestroyed) {
                                return;
                            }
                            isUnInitVideoEffect = false;
                            pkStateEnum = PKStateEnum.PKING;
                            pkMeetingName = fromPKMeetingName;

                            MicHelper.getInstance().sendUpdateRoomExtension(meetingUid, meetingName, liveType, true, masterNick, nickName, roomInfo, roomId);
                        }

                        @Override
                        public void onJoinChannelFailed() {
                            if (isDestroyed) {
                                return;
                            }
                            Toast.makeText(DemoCache.getContext(), "join pk av room  failed", Toast.LENGTH_SHORT).show();
                            showLiveFinishLayout();
                        }
                    });
                }, RELEASE_SDK_DELAY_TIME);

            }

            @Override
            public void onFailed(int i) {
                LogUtil.e(TAG, "try pk but leave old av room failed, code:" + i);
            }

            @Override
            public void onException(Throwable throwable) {
                LogUtil.e(TAG, "try pk but leave old av room exception, throwable:" + throwable.getMessage());
            }
        });
    }

    private void createPKAVRoom(final String nickName, final String account) {

        LogUtil.i(TAG, "start create pk av room ");
        startEngine();
        pkMeetingName = StringUtil.get36UUID();

        List<AVChatLiveTaskConfig> pkPushTasks = getPKPushConfigs();

        AVChatManager.getInstance().createRoom(pkMeetingName, null, pkPushTasks, new AVChatCallback<AVChatChannelInfo>() {
            @Override
            public void onSuccess(AVChatChannelInfo avChatChannelInfo) {
                Toast.makeText(RoomLiveActivity.this, "创建的房间名：" + pkMeetingName, Toast.LENGTH_SHORT).show();
                //切换布局
                switchNormalOrPKLayout(true);
                hidePKFinishLayout();
                MicHelper.getInstance().joinAVRoom(pkMeetingName, liveType == LiveType.VIDEO_TYPE, new MicHelper.ChannelCallback() {

                    @Override
                    public void onJoinChannelSuccess() {
                        LogUtil.i(TAG, "create pk av room  success");
                        isUnInitVideoEffect = false;
                        pkStateEnum = PKStateEnum.PKING;
                        MicHelper.getInstance().sendUpdateRoomExtension(meetingUid, meetingName, liveType, true, masterNick, nickName, roomInfo, roomId);
                        MicHelper.getInstance().sendCustomPKNotify(account, PushMicNotificationType.AGREE_INVITATION.getValue(), pkMeetingName);
                    }

                    @Override
                    public void onJoinChannelFailed() {
                        LogUtil.i(TAG, "create pk av room  failed");
                        showLiveFinishLayout();
                    }
                });
            }

            @Override
            public void onFailed(int i) {
                if (i == ResponseCode.RES_EEXIST) {
                    // 417表示该频道已经存在
                    LogUtil.e(TAG, "create room 417, enter room");
                    Toast.makeText(RoomLiveActivity.this, "创建的房间名：" + pkMeetingName, Toast.LENGTH_SHORT).show();
                } else {
                    startBtn.setText(R.string.live_start);
                    LogUtil.e(TAG, "create room failed, code:" + i);
                    Toast.makeText(RoomLiveActivity.this, "create room failed, code:" + i, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onException(Throwable throwable) {
                isStartLiving = false;
                startBtn.setText(R.string.live_start);
                LogUtil.e(TAG, "create room onException, throwable:" + throwable.getMessage());
                Toast.makeText(RoomLiveActivity.this, "create room onException, throwable:" + throwable.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    /**
     * 生成房间推流 task 配置 ， 房间推不用配置 AVChatParameters.KEY_SESSION_LIVE_URL
     * 这里配置了PK双方的两条task
     * 配置了也无效了
     */
    private List<AVChatLiveTaskConfig> getPKPushConfigs() {

        List<AVChatLiveTaskConfig> pkPushTasks = new ArrayList<>();

        AVChatLiveTaskConfig selfTask = new AVChatLiveTaskConfig();
        selfTask.setTaskId(String.valueOf(System.currentTimeMillis() - 1));
        selfTask.setPushUrl(liveInfoMode.getPushUrl());
        selfTask.setMainPictureAccount(DemoCache.getAccount());
        selfTask.setLayoutMode(AVChatLiveMode.LAYOUT_ENHANCE);
        selfTask.setLayoutPara(getPushLayoutPara());
        pkPushTasks.add(selfTask);

        AVChatLiveTaskConfig pkPeerTask = new AVChatLiveTaskConfig();
        pkPeerTask.setTaskId(String.valueOf(System.currentTimeMillis() + 1));
        pkPeerTask.setPushUrl(peerPushUrl);
        pkPeerTask.setMainPictureAccount(peerPushAccount);

        if (!TextUtils.isEmpty(peerLayoutPara)) {
            pkPeerTask.setLayoutMode(AVChatLiveMode.LAYOUT_ENHANCE);
            pkPeerTask.setLayoutPara(peerLayoutPara);
        }
        pkPushTasks.add(pkPeerTask);
        return pkPushTasks;
    }

    private synchronized void leavePKAVRoom() {
        if (pkStateEnum == PKStateEnum.NONE) {
            LogUtil.i(TAG, "leavePKAVRoom has already , pkMeetingName:" + pkMeetingName);
            return;
        }
        LogUtil.i(TAG, "start leave pk av room");
        pkStateEnum = PKStateEnum.NONE;

        releaseEngine();

        AVChatManager.getInstance().leaveRoom2(pkMeetingName, new AVChatCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                LogUtil.i(TAG, "leave pk av room success");
                leavePKAVRoomSuccess();
            }

            @Override
            public void onFailed(int i) {
                LogUtil.e(TAG, "leave pk av room failed, code:" + i);
            }

            @Override
            public void onException(Throwable throwable) {
                LogUtil.e(TAG, "leave pk av room exception, throwable:" + throwable.getMessage());
            }
        });
    }

    private void leavePKAVRoomSuccess() {

        MicHelper.getInstance().sendUpdateRoomExtension(meetingUid, meetingName, liveType, false, masterNick, pkAccount, roomInfo, roomId);
        switchNormalOrPKLayout(false);
        hidePKFinishLayout();

        startEngine();

        MicHelper.getInstance().joinAVRoom(meetingName, liveType == LiveType.VIDEO_TYPE, new MicHelper.ChannelCallback() {

            @Override
            public void onJoinChannelSuccess() {
                isUnInitVideoEffect = false;
            }

            @Override
            public void onJoinChannelFailed() {
                if (isDestroyed) {
                    return;
                }
                LogUtil.e(TAG, "leave pk av room but  join old live failed ");
                reCreateLiveAVRoom();
            }
        });
    }

    private void reCreateLiveAVRoom() {
        AVChatManager.getInstance().createRoom(meetingName, null, getLiveTaskConfig(), new AVChatCallback<AVChatChannelInfo>() {
            @Override
            public void onSuccess(AVChatChannelInfo avChatChannelInfo) {
                LogUtil.i(TAG, "leavePKAVRoomSuccess 创建的房间名：" + meetingName);
                MicHelper.getInstance().joinAVRoom(meetingName, liveType == LiveType.VIDEO_TYPE, new MicHelper.ChannelCallback() {

                    @Override
                    public void onJoinChannelSuccess() {
                        isUnInitVideoEffect = false;
                    }

                    @Override
                    public void onJoinChannelFailed() {
                        if (isDestroyed) {
                            return;
                        }
                        showLiveFinishLayout();
                    }
                });
            }

            @Override
            public void onFailed(int i) {
                if (i == ResponseCode.RES_EEXIST) {
                    // 417表示该频道已经存在
                    LogUtil.e(TAG, "leavePKAVRoomSuccess create room 417, enter room");
                    Toast.makeText(RoomLiveActivity.this, "创建的房间名：" + meetingName, Toast.LENGTH_SHORT).show();
                } else {
                    startBtn.setText(R.string.live_start);
                    showLiveFinishLayout();
                    LogUtil.e(TAG, "leavePKAVRoomSuccess create room failed, code:" + i);
                    Toast.makeText(RoomLiveActivity.this, "create room failed, code:" + i, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onException(Throwable throwable) {
                isStartLiving = false;
                startBtn.setText(R.string.live_start);
                showLiveFinishLayout();
                LogUtil.e(TAG, "leavePKAVRoomSuccess create room onException, throwable:" + throwable.getMessage());
                Toast.makeText(RoomLiveActivity.this, "create room onException, throwable:" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private OnClickListener pkBlankListener = v -> hidePKFinishLayout();


    private OnClickListener pkCancelListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            inputPanel.collapse(true);// 收起软键盘
            if (pkStateEnum == PKStateEnum.WAITING || pkStateEnum == PKStateEnum.ONLINE) { //取消等待
                getHandler().removeCallbacks(pkAccountTimeOutRunnable);
                MicHelper.getInstance().sendCustomPKNotify(pkAccount, PushMicNotificationType.CANCEL_INTERACT.getValue(), null);
                pkStateEnum = PKStateEnum.NONE;
            } else if (pkStateEnum == PKStateEnum.PKING) { //正在PK，结束PK
                MicHelper.getInstance().sendCustomPKNotify(pkAccount, PushMicNotificationType.EXITED.getValue(), null);
                leavePKAVRoom();
            }
            showPkInviteLayout();
            videoPKControlLayout.setVisibility(View.GONE);
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        }
    };

    private void findMarkLayout() {
        videoMarkLayout = findView(R.id.video_mark_layout);
        RelativeLayout videoMarkBlankView = findView(R.id.video_mark_blank_view);
        RelativeLayout videoMarkStaticRl = findView(R.id.video_mark_static_rl);
        RelativeLayout videoMarkDynamicRl = findView(R.id.video_mark_dynamic_rl);
        RelativeLayout videoMarkCloseRl = findView(R.id.video_mark_close_rl);
        videoMarkStaticIv = findView(R.id.video_mark_static_iv);
        videoMarkDynamicIv = findView(R.id.video_mark_dynamic_iv);
        videoMarkCloseIv = findView(R.id.video_mark_close_iv);
        TextView videoMarkCancelBtn = findView(R.id.video_mark_button_cancel);

        videoMarkBlankView.setOnClickListener(markListener);
        videoMarkStaticRl.setOnClickListener(markListener);
        videoMarkDynamicRl.setOnClickListener(markListener);
        videoMarkCloseRl.setOnClickListener(markListener);
        videoMarkCancelBtn.setOnClickListener(markListener);
    }

    private void findBeautyLayout() {
        videoBeautyLayout = findView(R.id.video_beauty_layout);
        LinearLayout videoBeautyContentView = findView(R.id.background_beauty_content_view);
        RelativeLayout videoBeautyBlankView = findView(R.id.video_beauty_blank_view);
        LinearLayout videoBeautyOrigin = findView(R.id.video_beauty_origin);
        LinearLayout videoBeautyNatural = findView(R.id.video_beauty_natural);
        TextView videoBeautyCancel = findView(R.id.video_beauty_button_cancel);
        TextView videoBeautyConfirm = findView(R.id.video_beauty_button_confirm);
        videoBeautyStrength = findView(R.id.beauty_strength);
        videoBeautyOriginIv = findView(R.id.video_beauty_origin_iv);
        videoBeautyNaturalIv = findView(R.id.video_beauty_natural_iv);

        SeekBar videoBeautyDipStrengthControlSb = findView(R.id.beauty_dip_strength_control);
        SeekBar videoBeautyContrastStrengthControlSb = findView(R.id.beauty_contrast_strength_control);

        videoBeautyBlankView.setOnClickListener(beautyListener);
        videoBeautyContentView.setOnClickListener(beautyListener);
        videoBeautyOrigin.setOnClickListener(beautyListener);
        videoBeautyNatural.setOnClickListener(beautyListener);
        videoBeautyCancel.setOnClickListener(beautyListener);
        videoBeautyConfirm.setOnClickListener(beautyListener);

        videoBeautyDipStrengthControlSb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mVideoEffect == null) {
                    return;
                }
                mVideoEffect.setBeautyLevel(seekBar.getProgress() / 20);
            }
        });
        videoBeautyContrastStrengthControlSb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mVideoEffect == null) {
                    return;
                }
                mVideoEffect.setFilterLevel((float) seekBar.getProgress() / 100);
            }
        });

        FaceUnityView beautyControlView = findView(R.id.fu_beauty_control);
        if (mIsFuBeautyOpen) {
            if (mFURenderer == null) {
                mFURenderer = new FURenderer.Builder(this)
                        .setCreateEglContext(true)
                        .setInputTextureType(FURenderer.INPUT_TEXTURE_2D)
                        .setCameraFacing(mCameraFacing)
                        .setInputImageOrientation(CameraUtils.getCameraOrientation(mCameraFacing))
                        .build();
            }
            beautyControlView.setModuleManager(mFURenderer);
            mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            beautyControlView.setVisibility(View.GONE);
        }
    }

    private void findClarityLayout() {
        videoClarityLayout = findView(R.id.video_clarity_layout);
        RelativeLayout videoClarityBlankView = findView(R.id.video_clarity_blank_view);
        RelativeLayout rlVideoClarityHd = findView(R.id.video_clarity_hd_rl);
        ivVideoClarityHd = findView(R.id.video_clarity_hd_iv);
        RelativeLayout rlVideoClaritySd = findView(R.id.video_clarity_sd_rl);
        ivVideoClaritySd = findView(R.id.video_clarity_sd_iv);
        TextView btnVideoClarityCancel = findView(R.id.video_clarity_button_cancel);

        videoClarityBlankView.setOnClickListener(clarityListener);
        rlVideoClarityHd.setOnClickListener(clarityListener);
        rlVideoClaritySd.setOnClickListener(clarityListener);
        btnVideoClarityCancel.setOnClickListener(clarityListener);
    }

    // 初始化礼物布局
    protected void findGiftLayout() {
        super.findGiftLayout();
        adapter = new GiftAdapter(giftList, this);
        giftView.setAdapter(adapter);
    }

    protected void findMusicLayout() {
        backgroundMusicLayout = findView(R.id.background_music_layout);
        musicBlankView = findView(R.id.background_music_blank_view);
        musicContentView = findView(R.id.background_music_content_view);
        musicSongFirstContent = findView(R.id.music_song_first_content);

        musicSongSecondContent = findView(R.id.music_song_second_content);
        musicSongFirstControl = findView(R.id.music_song_first_control);
        musicSongFirstControl.setOnClickListener(musicListener);
        musicSongSecondControl = findView(R.id.music_song_second_control);
        musicSongSecondControl.setOnClickListener(musicListener);
        musicAudioEffectFirst = findView(R.id.audio_effect_first);
        musicAudioEffectFirst.setOnClickListener(musicListener);
        musicAudioEffectSecond = findView(R.id.audio_effect_second);
        musicAudioEffectSecond.setOnClickListener(musicListener);
        musicSongVolumeControl = findView(R.id.music_song_volume_control);

        musicSongFirstControl.setEnabled(true);
        musicSongSecondControl.setEnabled(true);
        musicSongVolumeControl.setEnabled(true);


        musicSongVolumeControl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                AVChatManager.getInstance().setAudioMixingPlaybackVolume(seekBar.getProgress() * 1.0f / 100);
                AVChatManager.getInstance().setAudioMixingSendVolume(seekBar.getProgress() * 1.0f / 100);
            }
        });

    }


    private OnClickListener focalLengthListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.focal_length_minus:
                    int minus = videoFocalLengthSb.getProgress() > 0 ? (videoFocalLengthSb.getProgress() - 1) : 0;
                    videoFocalLengthSb.setProgress(minus);
                    mVideoCapturer.setZoom(minus);
                    break;
                case R.id.focal_length_plus:
                    int plus = videoFocalLengthSb.getProgress() >= videoFocalLengthSb.getMax() ? videoFocalLengthSb.getMax() : (videoFocalLengthSb.getProgress() + 1);
                    videoFocalLengthSb.setProgress(plus);
                    mVideoCapturer.setZoom(plus);
                    break;
            }

        }
    };

    private OnClickListener mirrorListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.video_mirror_button_cancel:
                case R.id.video_mirror_blank_view:
                    restoreMirror();
                    videoMirrorLayout.setVisibility(View.GONE);
                    break;
                case R.id.video_mirror_button_confirm:
                    videoMirrorLayout.setVisibility(View.GONE);
                    if (!videoMirrorLocalSb.isChoose()) {
                        if (!videoMirrorPushSb.isChoose()) {
                            lashMirrorMode = MIRROR_MODE_CLOSE_ALL;
                        } else {
                            lashMirrorMode = MIRROR_MODE_PUSH_OPEN;
                        }
                    } else {
                        if (!videoMirrorPushSb.isChoose()) {
                            lashMirrorMode = MIRROR_MODE_LOCAL_OPEN;
                        } else {
                            lashMirrorMode = MIRROR_MODE_OPEN_ALL;
                        }
                    }

                    break;
            }

        }
    };

    private void restoreMirror() {
        boolean localMirror = lashMirrorMode == MIRROR_MODE_LOCAL_OPEN || lashMirrorMode == MIRROR_MODE_OPEN_ALL;
        AVChatManager.getInstance().setParameter(AVChatParameters.KEY_VIDEO_LOCAL_PREVIEW_MIRROR, localMirror);
        boolean pushMirror = lashMirrorMode == MIRROR_MODE_PUSH_OPEN || lashMirrorMode == MIRROR_MODE_OPEN_ALL;
        AVChatManager.getInstance().setParameter(AVChatParameters.KEY_VIDEO_TRANSPORT_MIRROR, pushMirror);
    }

    private OnClickListener markListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.video_mark_static_rl:
                    markMode = VIDEO_MARK_MODE_STATIC;
                    AVChatManager.getInstance().setParameter(AVChatParameters.KEY_VIDEO_FRAME_FILTER, true);
                    updateMarkLayout();
                    closeMarkLayout(true);
                    break;
                case R.id.video_mark_dynamic_rl:
                    markMode = VIDEO_MARK_MODE_DYNAMIC;
                    AVChatManager.getInstance().setParameter(AVChatParameters.KEY_VIDEO_FRAME_FILTER, true);
                    updateMarkLayout();
                    closeMarkLayout(true);
                    break;
                case R.id.video_mark_close_rl:
                    markMode = VIDEO_MARK_MODE_CLOSE;
                    updateMarkLayout();
                    closeMarkLayout(true);
                    break;
                case R.id.video_mark_button_cancel:
                case R.id.video_mark_blank_view:
                    closeMarkLayout(false);
                    break;
            }

        }
    };

    private void closeMarkLayout(boolean isDelayed) {
        getHandler().postDelayed(() -> videoMarkLayout.setVisibility(View.GONE), isDelayed ? 500 : 0);
    }

    private OnClickListener beautyListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.video_beauty_natural:
                    isVideoBeautyOriginCurrent = false;
                    updateBeautyLayout(false);
                    break;
                case R.id.video_beauty_origin:
                    isVideoBeautyOriginCurrent = true;
                    updateBeautyLayout(true);
                    break;
                case R.id.video_beauty_button_cancel:
                case R.id.video_beauty_blank_view:
                    videoBeautyLayout.setVisibility(View.GONE);
                    isBeautyBtnCancel = true;
                    updateBeautyLayout(isVideoBeautyOriginLast);
                    updateBeautyIcon(isVideoBeautyOriginLast);
                    break;
                case R.id.video_beauty_button_confirm:
                    isBeautyBtnCancel = false;
                    isVideoBeautyOriginLast = isVideoBeautyOriginCurrent;
                    updateBeautyIcon(isVideoBeautyOriginLast);
                    videoBeautyLayout.setVisibility(View.GONE);
                    break;
            }

        }
    };


    // 更新美颜按钮显示
    private void updateBeautyLayout(boolean isBeautyClose) {
        videoBeautyOriginIv.setSelected(isBeautyClose);
        videoBeautyNaturalIv.setSelected(!isBeautyClose);
        videoBeautyStrength.setVisibility(isBeautyClose ? View.GONE : View.VISIBLE);
    }

    private void updateBeautyIcon(boolean isBeautyClose) {
        if (isBeautyClose) {
            screenBeautyBtn.setBackgroundResource(R.drawable.ic_beauty_close_selector);
            beautyBtn.setBackgroundResource(R.drawable.ic_beauty_close_selector);
        } else {
            screenBeautyBtn.setBackgroundResource(R.drawable.ic_beauty_open_selector);
            beautyBtn.setBackgroundResource(R.drawable.ic_beauty_open_selector);
        }
    }

    private OnClickListener clarityListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.video_clarity_hd_rl:
                    isVideoClaritySd = false;
                    ivVideoClarityHd.setVisibility(View.VISIBLE);
                    ivVideoClaritySd.setVisibility(View.GONE);
                    setVideoQuality(AVChatVideoQuality.QUALITY_720P);
                    hdBtn.setText("高清");
                    closeClarityLayout(true);
                    break;
                case R.id.video_clarity_sd_rl:
                    isVideoClaritySd = true;
                    ivVideoClarityHd.setVisibility(View.GONE);
                    ivVideoClaritySd.setVisibility(View.VISIBLE);
                    setVideoQuality(AVChatVideoQuality.QUALITY_480P);
                    hdBtn.setText("普清");
                    closeClarityLayout(true);
                    break;
                case R.id.video_clarity_button_cancel:
                case R.id.video_clarity_blank_view:
                    closeClarityLayout(false);
                    break;
            }

        }
    };

    private void closeClarityLayout(boolean isDelayed) {
        getHandler().postDelayed(() -> videoClarityLayout.setVisibility(View.GONE), isDelayed ? 500 : 0);
    }


    private OnClickListener musicListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.music_song_first_control:
                    if (isMusicFirstPlaying) {
                        AVChatManager.getInstance().pauseAudioMixing();
                        musicSongFirstControl.setImageResource(R.drawable.background_music_control_play);
                        musicSongFirstContent.setText(R.string.background_music_song_first_play);
                        isMusicFirstPlaying = false;
                        isMusicFirstPause = true;
                    } else {
                        if (isMusicSecondPlaying) {
                            //先停止一下  ， 要不然下一首会播放失败
                            AVChatManager.getInstance().stopAudioMixing();
                            resetMusicLayoutViews(false, true);
                        }

                        isMusicFirstPlaying = true;
                        musicSongFirstControl.setImageResource(R.drawable.background_music_control_pause);
                        musicSongFirstContent.setText(R.string.background_music_song_first_pause);
                        if (isMusicFirstPause) {
                            AVChatManager.getInstance().resumeAudioMixing();
                        } else {
                            String songPath = musicPath + "first_song.mp3";
                            if (new File(songPath).exists()) {
                                float volume = musicSongVolumeControl.getProgress() * 1.0f / 100;
                                AVChatManager.getInstance().startAudioMixing(songPath, false, false, 100, volume);

                            } else {
                                Toast.makeText(getBaseContext(), songPath + "文件不存在", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                    break;
                case R.id.music_song_second_control:

                    if (isMusicSecondPlaying) {
                        AVChatManager.getInstance().pauseAudioMixing();
                        musicSongSecondControl.setImageResource(R.drawable.background_music_control_play);
                        musicSongSecondContent.setText(R.string.background_music_song_second_play);
                        isMusicSecondPlaying = false;
                        isMusicSecondPause = true;
                    } else {
                        if (isMusicFirstPlaying) {
                            //先停止一下  ， 要不然下一首会播放失败
                            AVChatManager.getInstance().stopAudioMixing();
                            resetMusicLayoutViews(true, false);
                        }

                        isMusicSecondPlaying = true;
                        musicSongSecondControl.setImageResource(R.drawable.background_music_control_pause);
                        musicSongSecondContent.setText(R.string.background_music_song_second_pause);
                        if (isMusicSecondPause) {
                            AVChatManager.getInstance().resumeAudioMixing();
                        } else {
                            String songPath = musicPath + "second_song.mp3";
                            float volume = musicSongVolumeControl.getProgress() * 1.0f / 100;
                            AVChatManager.getInstance().startAudioMixing(songPath, false, false, 100, volume);
                        }
                    }
                    break;
                case R.id.audio_effect_first://播放音效1
                    boolean playResult1 = AVChatManager.getInstance().playAudioEffect(1, 1, true, 0.1f);
                    updateAudioEffectView(!playResult1);
                    musicAudioEffectFirst.setSelected(true);

                    break;
                case R.id.audio_effect_second://播放音效2
                    boolean playResult2 = AVChatManager.getInstance().playAudioEffect(2, 1, true, 0.1f);
                    updateAudioEffectView(!playResult2);
                    musicAudioEffectSecond.setSelected(true);
                    break;
            }
        }
    };

    private void updateAudioEffectView(boolean enable) {

        musicAudioEffectFirst.setEnabled(enable);
        musicAudioEffectSecond.setEnabled(enable);

        musicAudioEffectFirst.setSelected(false);
        musicAudioEffectSecond.setSelected(false);
    }


    private OnClickListener pkListener = view -> {
        switch (pkStateEnum) {
            case NONE:
            case EXITED:
            case INVITE_TIME_OUT:
                showPkInviteLayout();
                break;
            case WAITING:
            case ONLINE:
                showPKWaitingLayout();
                break;
            case BE_INVITATION:
                showPKMessage("您当前有PK申请，不能发起邀请");
                break;
            case PKING:
                showPKFinishLayout();
                break;
            default:
                break;
        }
    };

    private void resetMusicLayoutViews(boolean resetFirstSong, boolean resetSecondSong) {

        if (resetFirstSong) {
            musicSongFirstControl.setImageResource(R.drawable.background_music_control_play);
            musicSongFirstContent.setText(R.string.background_music_song_first_play);
            isMusicFirstPlaying = false;
            isMusicFirstPause = false;
        }

        if (resetSecondSong) {
            musicSongSecondControl.setImageResource(R.drawable.background_music_control_play);
            musicSongSecondContent.setText(R.string.background_music_song_second_play);
            isMusicSecondPlaying = false;
            isMusicSecondPause = false;
        }
    }

    private void findInteractionMemberLayout() {
        GridView interactionGridView = findView(R.id.apply_grid_view);
        interactionDataSource = new ArrayList<>();
        currentInteractionMembers = new LinkedList<>();
        interactionAdapter = new InteractionAdapter(interactionDataSource, this, this);
        interactionGridView.setAdapter(interactionAdapter);
        interactionGridView.setOnItemClickListener((parent, view, position, id) -> {
            InteractionMember member = (InteractionMember) interactionAdapter.getItem(position);
            member.setSelected(true);

            if (clickAccount != null && !clickAccount.equals(member.getAccount())) {
                for (InteractionMember m : interactionDataSource) {
                    if (m.getAccount().equals(clickAccount)) {
                        m.setSelected(false);
                        break;
                    }
                }
            }

            clickAccount = member.getAccount();
            interactionAdapter.notifyDataSetChanged();
        });
    }

    @Override
    protected void enterChatRoomSuccess() {
        super.enterChatRoomSuccess();
        startLayout.setVisibility(View.GONE);
        ChatRoomMember roomMember = ChatRoomMemberCache.getInstance().getChatRoomMember(roomId, roomInfo.getCreator());
        if (roomMember != null) {
            masterNick = roomMember.getNick();
        }
        masterNameText.setText(TextUtils.isEmpty(masterNick) ? roomInfo.getCreator() : masterNick);
        liveInfoMode.setChatRoomInfo(roomInfo);
    }

    // 主播进来清空队列
    private void dropQueue() {
        NIMClient.getService(ChatRoomService.class).dropQueue(roomId).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
            }

            @Override
            public void onFailed(int i) {
                LogUtil.e(TAG, "drop queue failed, code:" + i);
            }

            @Override
            public void onException(Throwable throwable) {

            }
        });
    }

    // 取出缓存的礼物
    private void loadGift() {
        HashMap<Integer, Integer> gifts = GiftCache.getInstance().getGift(roomId);
        if (gifts == null) {
            return;
        }
        for (Map.Entry<Integer, Integer> entry : gifts.entrySet()) {
            int type = entry.getKey();
            int count = entry.getValue();
            giftList.add(new Gift(GiftType.typeOfValue(type), GiftConstant.titles[type], count, GiftConstant.images[type]));
        }
    }

    private void setListener() {
        screenSwitchBtn.setOnClickListener(buttonClickListener);
        screenCameraBtn.setOnClickListener(buttonClickListener);
        screenBeautyBtn.setOnClickListener(buttonClickListener);
        startBtn.setOnClickListener(buttonClickListener);
        screenSwitchHorizontal.setOnClickListener(buttonClickListener);
        screenSwitchVertical.setOnClickListener(buttonClickListener);
        screenSwitchCover.setOnClickListener(buttonClickListener);
        btnCloseRoom.setOnClickListener(buttonClickListener);
        btnLeaveRoom.setOnClickListener(buttonClickListener);
        controlExtendBtn.setOnClickListener(buttonClickListener);
        switchBtn.setOnClickListener(buttonClickListener);
        beautyBtn.setOnClickListener(buttonClickListener);
        interactionBtn.setOnClickListener(buttonClickListener);
        interactionLayout.setOnClickListener(buttonClickListener);
        giftBtn.setOnClickListener(buttonClickListener);
        giftLayout.setOnClickListener(buttonClickListener);
        musicBtn.setOnClickListener(buttonClickListener);
        musicBlankView.setOnClickListener(buttonClickListener);
        musicContentView.setOnClickListener(buttonClickListener);
        if (liveType == LiveType.VIDEO_TYPE) {
            hdBtn.setOnClickListener(buttonClickListener);
            shotBtn.setOnClickListener(buttonClickListener);
            flashBtn.setOnClickListener(buttonClickListener);
            markBtn.setOnClickListener(buttonClickListener);
            mirrorBtn.setOnClickListener(buttonClickListener);
            focalLengthBtn.setOnClickListener(buttonClickListener);
            pkBtn.setOnClickListener(pkListener);
        }
    }

    //音量强度汇报监听
    protected AVChatStateObserver stateObserver = new SimpleAVChatStateObserver() {
        @Override
        public void onDisconnectServer(int i) {
            Toast.makeText(RoomLiveActivity.this, "与音视频服务器已断开连接，自动退出", Toast.LENGTH_SHORT).show();
            exitChatRoom();
        }

        @Override
        public void onUserLeave(String account, int i) {
            LogUtil.i(TAG, "onUserLeave , accid : " + account + ", code : " + i);
            if (pkStateEnum == PKStateEnum.PKING) {
                leavePKAVRoom();
                return;
            }

            // 连麦者离开房间
            MicHelper.getInstance().popQueue(roomId, account);
            MicHelper.getInstance().sendBrokeMicMsg(roomId, account);

            getHandler().removeCallbacks(userLeaveRunnable);

            removeInteractionView(account);
        }

        @Override
        public void onJoinedChannel(int code, String audioFile, String videoFile, int elapsed) {
            if (code != AVChatResCode.JoinChannelCode.OK) {
                Toast.makeText(RoomLiveActivity.this, "加入频道失败，code:" + code, Toast.LENGTH_SHORT).show();
                showLiveFinishLayout();
            }
            AVChatManager.getInstance().setSpeaker(true);
            LogUtil.i(TAG, "onJoinedChannel , code: " + code);
        }

        @Override
        public void onUserJoined(String account) {
            LogUtil.i(TAG, "onUserJoined , accid : " + account);
            if (pkStateEnum == PKStateEnum.PKING) {
                //pk模式
                onPkUserJoined(account);
                return;
            }

            btnLeaveRoom.setVisibility(View.VISIBLE);

            // 1、主播显示旁路直播画面
            // 2、主播发送全局自定义消息告诉观众有人连麦拉
            InteractionMember interactionMember = getByAccount(account);

            if (interactionMember == null) {
                interactionMember = getFromCacheByAccount(account);
                if (interactionMember != null) {
                    currentInteractionMembers.add(interactionMember);
                }
            }

            if (interactionMember == null) {
                LogUtil.e(TAG, "onUserJoined : " + account + " can not find in currentInteractionMembers");
                return;
            }

            int emptyIndex = getEmptyInteractionView();
            int preIndex = getInteractionViewIndexByAccount(account);

            if (emptyIndex == -1 || preIndex != -1) {
                LogUtil.e(TAG, "onUserJoined : " + account + "can not find suitable index , empty = " + emptyIndex + " , pre = " + preIndex);
                return;
            }
            interactionMember.setMicStateEnum(MicStateEnum.CONNECTED);
            getHandler().removeCallbacks(userJoinRunnable);

            Long uid = AVChatManager.getInstance().getUidByAccount(account);
            if (uid != null) {
                interactionMember.setMeetingUid(Long.toString(uid));
            }

            MicHelper.getInstance().sendConnectedMicMsg(roomId, interactionMember);
            MicHelper.getInstance().updateMemberInChatRoom(roomId, interactionMember);
            removeMemberFromList(account);

            InteractionView interactionView = interactionGroupView[emptyIndex];
            if (interactionView.audienceLivingLayout.getVisibility() == View.VISIBLE && interactionMember.getAvChatType() == AVChatType.VIDEO) {
                // 如果是已经有连麦的人，下一个连麦人上麦，不隐藏小窗口，直接切换画面
                AVChatManager.getInstance().setupRemoteVideoRender(account, null, false, 0);
                AVChatManager.getInstance().setupRemoteVideoRender(account, interactionView.bypassVideoRender, false, AVChatVideoScalingType.SCALE_ASPECT_BALANCED);
                updateOnMicName(emptyIndex, interactionMember.getName());
                interactionView.audienceLoadingLayout.setVisibility(View.GONE);
                interactionView.livingBg.setVisibility(View.VISIBLE);
            } else {
                String meetingUid = String.valueOf(AVChatManager.getInstance().getUidByAccount(account));
                showOtherMicLinkedView(emptyIndex, account, meetingUid, interactionMember.getName(), interactionMember.getAvChatType().getValue());
            }
            interactionView.account = account;
            interactionView.meetingUid = meetingUid;
        }

        @Override
        public void onTakeSnapshotResult(String s, boolean b, String s1) {
            //截取用户图像后的结果通知。
            if (b) {
                // 把文件插入到系统图库
                String fileName = s1.substring(s1.lastIndexOf("/") + 1);
                try {
                    MediaStore.Images.Media.insertImage(getContentResolver(), s1, fileName, null);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                if (Build.VERSION.SDK_INT > 19) {
                    MediaScannerConnection.scanFile(RoomLiveActivity.this, new String[]{s1}, new String[]{"image/jpeg"}, null);
                } else {
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + s1)));
                }

                Toast.makeText(RoomLiveActivity.this, R.string.shot_success, Toast.LENGTH_SHORT).show();
                shotCover.setVisibility(View.VISIBLE);
                shotCover.setAnimation(AnimationUtils.loadAnimation(RoomLiveActivity.this, R.anim.shot_anim_finish));
                shotCover.postDelayed(() -> shotCover.setVisibility(View.GONE), 1000);
            } else {
                Toast.makeText(RoomLiveActivity.this, R.string.shot_failed, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public boolean onVideoFrameFilter(AVChatVideoFrame frame, boolean maybeDualInput) {

            //如果用户不需要对视频进行美颜，这里直接返回true即可，以下示例是使用sdk提供的美颜和水印功能，用户也可以在此接入第三方的美颜sdk
            //sdk提供的滤镜模块（美颜和水印功能）要求4.3以上版本
            if (frame == null || isUnInitVideoEffect) {
                return true;
            }
            boolean result = true;
            try {
                result = videoFrameFilter(frame, maybeDualInput);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }

        private boolean videoFrameFilter(AVChatVideoFrame frame, boolean maybeDualInput) {

            //onVideoFrameFilter回调不在主线程，VideoEffect初始化必须要和onVideoFrameFilter回调不在主线程在同一个线程
            if (mVideoEffect == null) {
                mVideoEffectHandler = new Handler();
                mVideoEffect = VideoEffectFactory.getVCloudEffect();
                mVideoEffect.init(RoomLiveActivity.this, true, false);
                //需要delay 否则filter设置不成功
                //mVideoEffect.setBeautyLevel(5);
                //mVideoEffect.setFilterLevel(0.5f);
                //mVideoEffect.setFilterType(VideoEffect.FilterType.nature);
                return false;
            }

            //分辨率、清晰度变化后设置丢帧数为2
            if (mCurWidth != frame.width || mCurHeight != frame.height) {
                mCurWidth = frame.width;
                mCurHeight = frame.height;
                notifyCapturerConfigChange();
            }

            if (markMode != VIDEO_MARK_MODE_CLOSE) {
                if (mWaterMaskBitmapStatic == null) {
                    try {
                        InputStream is = getResources().getAssets().open("mark/video_mark_static.png");
                        mWaterMaskBitmapStatic = BitmapFactory.decodeStream(is);
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (mWaterMaskBitmapDynamic == null) {
                    mWaterMaskBitmapDynamic = new Bitmap[23];
                    for (int i = 0; i < 23; i++) {
                        String resName = "mark/video_mark_dynamic_" + i + ".png";
                        try {
                            InputStream is = getResources().getAssets().open(resName);
                            mWaterMaskBitmapDynamic[i] = BitmapFactory.decodeStream(is);
                            is.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (markMode == VIDEO_MARK_MODE_STATIC && (!mIsmWaterMaskAdded || rotation != frame.rotation)) {
                    rotation = frame.rotation;
                    mIsmWaterMaskAdded = true;
                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                        if (frame.rotation == 0) {
                            mVideoEffect.addWaterMark(null, 0, 0);
                            mVideoEffect.addWaterMark(mWaterMaskBitmapStatic, frame.width / 2, frame.height / 2);
                        } else {
                            mVideoEffect.addWaterMark(null, 0, 0);
                            mVideoEffect.addWaterMark(mWaterMaskBitmapStatic, frame.height / 2, frame.width / 2);
                        }
                    } else {
                        if (frame.rotation == 0) {
                            mVideoEffect.addWaterMark(null, 0, 0);
                            mVideoEffect.addWaterMark(mWaterMaskBitmapStatic, frame.width / 2, frame.height / 2);
                        } else {
                            mVideoEffect.addWaterMark(null, 0, 0);
                            mVideoEffect.addWaterMark(mWaterMaskBitmapStatic, frame.height / 2, frame.width / 2);
                        }
                    }
                    mVideoEffect.closeDynamicWaterMark(true);
                }
                if (markMode == VIDEO_MARK_MODE_DYNAMIC && (!mIsmWaterMaskAdded || rotation != frame.rotation)) {
                    rotation = frame.rotation;
                    mIsmWaterMaskAdded = true;
                    mVideoEffect.addWaterMark(null, 0, 0);
                    mVideoEffect.closeDynamicWaterMark(false);
                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                        if (frame.rotation == 0) {
                            mVideoEffect.addDynamicWaterMark(null, frame.width / 2, frame.height / 2, 23, AVChatVideoFrameRate.FRAME_RATE_15, true);
                            mVideoEffect.addDynamicWaterMark(mWaterMaskBitmapDynamic, frame.width / 2, frame.height / 2, 23, AVChatVideoFrameRate.FRAME_RATE_15, true);
                        } else {
                            mVideoEffect.addDynamicWaterMark(null, frame.height / 2, frame.width / 2, 23, AVChatVideoFrameRate.FRAME_RATE_15, true);
                            mVideoEffect.addDynamicWaterMark(mWaterMaskBitmapDynamic, frame.height / 2, frame.width / 2, 23, AVChatVideoFrameRate.FRAME_RATE_15, true);
                        }
                    } else {
                        if (frame.rotation == 0) {
                            mVideoEffect.addDynamicWaterMark(null, frame.width / 2, frame.height / 2, 23, AVChatVideoFrameRate.FRAME_RATE_15, true);
                            mVideoEffect.addDynamicWaterMark(mWaterMaskBitmapDynamic, frame.width / 2, frame.height / 2, 23, AVChatVideoFrameRate.FRAME_RATE_15, true);
                        } else {
                            mVideoEffect.addDynamicWaterMark(null, frame.height / 2, frame.width / 2, 23, AVChatVideoFrameRate.FRAME_RATE_15, true);
                            mVideoEffect.addDynamicWaterMark(mWaterMaskBitmapDynamic, frame.height / 2, frame.width / 2, 23, AVChatVideoFrameRate.FRAME_RATE_15, true);
                        }
                    }
                }
            } else {
                mVideoEffect.addWaterMark(null, 0, 0);
                mVideoEffect.closeDynamicWaterMark(true);
                mIsmWaterMaskAdded = false;
            }

            VideoEffect.DataFormat format = frame.format == ImageFormat.I420 ? VideoEffect.DataFormat.YUV420 : VideoEffect.DataFormat.NV21;
            boolean needMirrorData = (markMode != VIDEO_MARK_MODE_CLOSE && maybeDualInput);
            VideoEffect.YUVData[] result;
            if ((!isBeautyBtnCancel && !isVideoBeautyOriginCurrent) || (isBeautyBtnCancel && !isVideoBeautyOriginLast)) {
                byte[] intermediate = mVideoEffect.filterBufferToRGBA(format, frame.data, frame.width, frame.height);

                if (!isFilterTypeSet) {
                    isFilterTypeSet = true;
                    mVideoEffect.setBeautyLevel(5);
                    mVideoEffect.setFilterLevel(0.5f);
                    mVideoEffect.setFilterType(VideoEffect.FilterType.nature);
                    return false;
                }

                result = mVideoEffect.TOYUV420(intermediate, VideoEffect.DataFormat.RGBA, frame.width, frame.height,
                        frame.rotation, 90, frame.width, frame.height, needMirrorData, true);
            } else {
                result = mVideoEffect.TOYUV420(frame.data, format, frame.width, frame.height,
                        frame.rotation, 90, frame.width, frame.height, needMirrorData, true);
            }
            synchronized (this) {
                if (mDropFramesWhenConfigChanged-- > 0) {
                    return false;
                }
            }
            System.arraycopy(result[0].data, 0, frame.data, 0, result[0].data.length);
            frame.width = result[0].width;
            frame.height = result[0].height;
            frame.dataLen = result[0].data.length;
            frame.rotation = 0;

            if (needMirrorData) {
                System.arraycopy(result[1].data, 0, frame.dataMirror, 0, result[1].data.length);
            }
            frame.dualInput = needMirrorData;
            //默认都是转换成I420
            frame.format = ImageFormat.I420;
            return true;
        }



        private byte[] i420Byte;
        private byte[] readbackByte;

        /**
         * 视频数据外部处理接口, 此接口需要同步执行. 操作运行在视频数据发送线程上,处理速度过慢会导致帧率过低
         * @param input          待处理数据
         * @param outputFrames   {@link com.netease.nrtc.sdk.video.VideoFrame[0]} 处理后的数据，{@link com.netease.nrtc.sdk.video.VideoFrame[1]} 处理后的镜像数据。
         *                       在实际使用过程中，用户需要根据自己需求来决定是否真正需要输入镜像数据，一般在使用到水印等外部处理时才会需要真正输入两路数据，其他情况可以忽略此参数。
         * @param videoFilterParameter 待处理数据的参数
         * @return 返回true成功
         */
        @Override
        public boolean onVideoFrameFilter(VideoFrame input, VideoFrame[] outputFrames, VideoFilterParameter videoFilterParameter) {
            if (!mIsFuBeautyOpen || mFURenderer == null) {
                return true;
            }
            VideoFrame.Buffer buffer = input.getBuffer();
            int width = buffer.getWidth();
            int height = buffer.getHeight();
            int rotation = input.getRotation();
            int format = buffer.getFormat();
//            Log.e(TAG, "onVideoFrameFilter: frameRotation " + videoFilterParameter.frameRotation
//                    + " width " + width + " height " + height  + ", rotation " + rotation
//            + ", input " + input);
            if (mIsFirstFrame) {
                mVideoEffectHandler = new Handler(Looper.myLooper());
                mFURenderer.onSurfaceCreated();
                int dataSize = width * height * 3 / 2;
                i420Byte = new byte[dataSize];
                readbackByte = new byte[dataSize];
                mIsFirstFrame = false;
            }

            // I420 格式
            if (format == VideoFrameFormat.kVideoI420) {
                buffer.toBytes(i420Byte);
                // FU 美颜滤镜
                mFURenderer.onDrawFrameSingleInput(i420Byte, width, height,
                        FURenderer.INPUT_FORMAT_I420_BUFFER, readbackByte, width, height);
                if (mSkippedFrames > 0) {
                    mSkippedFrames--;
                    VideoFrame.Buffer rotatedBuffer = buffer.rotate(videoFilterParameter.frameRotation);
                    VideoFrame outputFrame = new VideoFrame(rotatedBuffer, rotation, input.getTimestampMs());
                    outputFrames[0] = outputFrame;
                } else {
                    // 数据回传
                    try {
                        VideoFrame.Buffer outputBuffer = VideoFrame.asBuffer(readbackByte, format, width, height);
                        VideoFrame.Buffer rotatedBuffer = outputBuffer.rotate(videoFilterParameter.frameRotation);
                        VideoFrame outputFrame = new VideoFrame(rotatedBuffer, rotation, input.getTimestampMs());
                        outputFrames[0] = outputFrame;
                    } catch (IllegalAccessException e) {
                        Log.e(TAG, "onVideoFrameFilter: ", e);
                    }
                }
            }
            input.release();
            return true;
        }


        @Override
        public void onReportSpeaker(Map<String, Integer> speakers, int mixedEnergy) {
            super.onReportSpeaker(speakers, mixedEnergy);
            if (noNeedUpdateVolume() || roomInfo == null) {
                return;
            }
            masterVolume.setText("音量:" + speakers.get(roomInfo.getCreator()));
            for (InteractionView interactionView : interactionGroupView) {
                if (speakers.containsKey(interactionView.account)) {
                    interactionView.audienceVolume.setText("音量:" + speakers.get(interactionView.account));
                }
            }
        }

        @Override
        public void onAudioMixingProgressUpdated(long progress, long duration) {
        }

        @Override
        public void onAudioMixingEvent(int event) {
            if (event == AVChatAudioMixingEvent.MIXING_FINISHED) {
                resetMusicLayoutViews(true, true);
            } else if (event == AVChatAudioMixingEvent.MIXING_ERROR) {
                Toast.makeText(getBaseContext(), "伴音播放失败event:" + event, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onDeviceEvent(int event, String desc) {
            if (event == AVChatDeviceEvent.VIDEO_CAMERA_SWITCH_OK) {
                notifyCapturerConfigChange();
                mSkippedFrames = 3;
                mCameraFacing = mCameraFacing == Camera.CameraInfo.CAMERA_FACING_FRONT
                        ? Camera.CameraInfo.CAMERA_FACING_BACK : Camera.CameraInfo.CAMERA_FACING_FRONT;
                if (mFURenderer != null) {
                    mFURenderer.onCameraChanged(mCameraFacing, CameraUtils.getCameraOrientation(mCameraFacing));
                    if (mFURenderer.getMakeupModule() != null) {
                        mFURenderer.getMakeupModule().setIsMakeupFlipPoints(mCameraFacing == Camera.CameraInfo.CAMERA_FACING_FRONT
                                ? 0 : 1);
                    }
                }
            }
        }

        @Override
        public void onFirstVideoFrameRendered(String s) {

            LogUtil.i(TAG, "on first video render : accid : " + s + " , pk : " + pkStateEnum.getValue() + ", pk accid : " + pkAccount);

            if (pkStateEnum == PKStateEnum.PKING && TextUtils.equals(s, pkAccount)) {
                pkVideoBg.setVisibility(View.GONE);
                return;
            }

            int index = getInteractionViewIndexByAccount(s);
            if (index != -1) {
                interactionGroupView[index].livingBg.setVisibility(View.GONE);
            }
        }

        @Override
        public void onNetworkQuality(String s, int i, AVChatNetworkStats avChatNetworkStats) {
            if (liveType != LiveType.VIDEO_TYPE || roomInfo == null) {
                return;
            }
            if (s.equals(DemoCache.getAccount()) && s.equals(roomInfo.getCreator())) {
                if (networkQuality == -1) {
                    networkQuality = i;
                }

                netStateImage.setVisibility(View.VISIBLE);
                switch (networkQuality) {
                    case AVChatNetworkQuality.VIDEO_OFF:
                        netStateTipText.setText(R.string.network_bad);
                        netStateImage.setImageResource(R.drawable.ic_network_bad);
                        netOperateText.setVisibility(View.VISIBLE);
                        netOperateText.setText(R.string.switch_to_audio_live);
                        break;
                    case AVChatNetworkQuality.BAD:
                        netStateTipText.setText(R.string.network_poor);
                        netStateImage.setImageResource(R.drawable.ic_network_poor);
                        AVChatParameters avChatParameters = new AVChatParameters();
                        avChatParameters.setRequestKey(AVChatParameters.KEY_VIDEO_QUALITY);
                        AVChatParameters parameters = AVChatManager.getInstance().getParameters(avChatParameters);
                        int quality = parameters.getInteger(AVChatParameters.KEY_VIDEO_QUALITY);
                        if (quality == AVChatVideoQuality.QUALITY_720P) {
                            netOperateText.setVisibility(View.VISIBLE);
                            netOperateText.setText(R.string.reduce_live_clarity);
                        } else {
                            netOperateText.setVisibility(View.GONE);
                        }
                        break;
                    case AVChatNetworkQuality.ORDINARY:
                        netStateTipText.setText(R.string.network_good);
                        netStateImage.setImageResource(R.drawable.ic_network_good);
                        netOperateText.setVisibility(View.GONE);
                        break;
                    case AVChatNetworkQuality.EXCELLENT:
                        netStateTipText.setText(R.string.network_excellent);
                        netStateImage.setImageResource(R.drawable.ic_network_excellent);
                        netOperateText.setVisibility(View.GONE);
                        break;
                }

                networkQuality = i;
            }
        }


        @Override
        public void onAudioEffectPreload(int effectId, int event) {
            if (event != AVChatAudioEffectEvent.AUDIO_EFFECT_PRELOAD_SUCCESS) {
                Toast.makeText(RoomLiveActivity.this, effectId + " 号音效加载失败", Toast.LENGTH_LONG).show();
            }
        }


        @Override
        public void onAudioEffectPlayEvent(int i, int i1) {
            if (i1 == AVChatAudioEffectEvent.AUDIO_EFFECT_PLAY_COMPLETE) {
                updateAudioEffectView(true);
            }
        }
    };

    private void removeInteractionView(String account) {
        int index = getInteractionViewIndexByAccount(account);
        Iterator<InteractionMember> iterator = currentInteractionMembers.iterator();
        while (iterator.hasNext()) {
            if (account.equals(iterator.next().getAccount())) {
                iterator.remove();
                if (index != -1) {
                    doCloseInteractionView(index);
                }
                break;
            }
        }
        isHasAudienceOnLink = currentInteractionMembers.size() > 0;
        if (!isHasAudienceOnLink) {
            btnLeaveRoom.setVisibility(View.GONE);
        }
    }

    private void registerLiveObservers(boolean register) {
        AVChatManager.getInstance().observeAVChatState(stateObserver, register);
        NIMClient.getService(MsgServiceObserve.class).observeCustomNotification(customPKNotificationObserver, register);
    }


    OnClickListener buttonClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.start_live_btn:
                    if (disconnected) {
                        // 如果网络不通
                        Toast.makeText(RoomLiveActivity.this, R.string.net_broken, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (isStartLiving) {
                        return;
                    }
                    if (!isPermissionGrant) {
                        Toast.makeText(RoomLiveActivity.this, R.string.permission_is_not_available, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    isStartLiving = true;
                    startBtn.setText(R.string.live_prepare);
                    startLiveSwitchLayout.setVisibility(View.GONE);

                    if (liveInfoMode == null) {
                        masterCreateChatRoom(liveType == LiveType.VIDEO_TYPE);
                    } else {
                        masterContinueLive();
                    }

                    break;
                case R.id.start_screen_btn:
                    if (liveInfoMode != null) {
                        Toast.makeText(RoomLiveActivity.this, R.string.cannot_switch_layout, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    startLiveSwitchLayout.setVisibility(startLiveSwitchLayout.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                    if (startLiveSwitchLayout.getVisibility() == View.VISIBLE) {
                        updateLiveSwitchLayout(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
                    }
                    break;
                case R.id.start_switch_btn:
                    mVideoCapturer.switchCamera();
                    break;
                case R.id.start_beauty_btn:
                case R.id.beauty_btn:
//                    showBeautyLayout();
                    break;
                case R.id.screen_switch_horizontal:
                    updateLiveSwitchLayout(false);
                    getHandler().postDelayed(() -> {
                        startLiveSwitchLayout.setVisibility(View.GONE);
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    }, 300);

                    break;
                case R.id.screen_switch_vertical:
                    updateLiveSwitchLayout(true);
                    getHandler().postDelayed(() -> {
                        startLiveSwitchLayout.setVisibility(View.GONE);
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    }, 300);
                    break;
                case R.id.live_screen_switch_cover:
                    startLiveSwitchLayout.setVisibility(View.GONE);
                    break;

                case R.id.btn_close_room:
                    checkCloseLive();
                    break;

                case R.id.btn_leave_room:
                    confirmLeaveAVRoom();
                    break;

                case R.id.control_extend_btn:
                    updateControlUI();
                    break;
                case R.id.switch_btn:
                    mVideoCapturer.switchCamera();
                    videoFocalLengthSb.setProgress(0);
                    mVideoCapturer.setZoom(0);
                    if (isVideoFlashOpen) {
                        updateFlashIcon();
                    }
                    break;
                case R.id.interaction_btn:
                    showInteractionLayout();
                    break;
                case R.id.live_interaction_layout:
                    interactionLayout.setVisibility(View.GONE);
                    break;
                case R.id.gift_btn:
                    showGiftLayout();
                    break;
                case R.id.gift_layout:
                    giftLayout.setVisibility(View.GONE);
                    break;
                case R.id.music_btn:
                    showMusicLayout();
                    break;
                case R.id.background_music_blank_view:
                    backgroundMusicLayout.setVisibility(View.GONE);
                    break;
                case R.id.shot_btn:
                    boolean isSuccess = AVChatManager.getInstance().takeSnapshot(DemoCache.getAccount());
                    if (!isSuccess) {
                        Toast.makeText(RoomLiveActivity.this, R.string.shot_failed, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.hd_btn:
                    showClarityLayout();
                    break;
                case R.id.flash_btn:
                    openCloseFlash();
                    break;
                case R.id.mark_btn:
                    openCloseMark();
                    break;
                case R.id.mirror_btn:
                    openCloseMirror();
                    break;
                case R.id.enlarge_btn:
                    openCloseFocalLength();
                    break;
            }
        }
    };


    private void confirmLeaveAVRoom() {

        EasyAlertDialogHelper.createOkCancelDiolag(this,
                null,
                getString(R.string.leave_confirm),
                getString(R.string.confirm),
                getString(R.string.cancel), true,
                new EasyAlertDialogHelper.OnDialogActionListener() {
                    @Override
                    public void doCancelAction() {

                    }

                    @Override
                    public void doOkAction() {
                        justLeaveAVRoom();

                    }
                }).show();
    }

    private void justLeaveAVRoom() {
        leaveAndReleaseAVRoom();
        startLayout.setVisibility(View.VISIBLE);
        startBtn.setText(R.string.live_start);
        updateRoomUI(true);

        cacheInteractionMembers.clear();
        if (currentInteractionMembers != null) {
            cacheInteractionMembers.addAll(currentInteractionMembers);
            for (InteractionMember member : cacheInteractionMembers) {
                removeInteractionView(member.getAccount());
            }
        }
        isStartLiving = false;
        isUnInitVideoEffect = false;
        roomInfo = null;
        startPreview();
        MicHelper.getInstance().sendUserLeaveMsg(roomId, DemoCache.getAccount());
    }


    private void checkCloseLive() {
        if (isStartLive) {
            confirmCloseLive();
        } else {
            closeLive();
        }
    }

    private void openCloseMirror() {
        videoMirrorLocalSb.setCheck(lashMirrorMode == MIRROR_MODE_LOCAL_OPEN || lashMirrorMode == MIRROR_MODE_OPEN_ALL);
        videoMirrorPushSb.setCheck(lashMirrorMode == MIRROR_MODE_PUSH_OPEN || lashMirrorMode == MIRROR_MODE_OPEN_ALL);
        videoMirrorLayout.setVisibility(View.VISIBLE);
    }

    private void openCloseMark() {
        videoMarkLayout.setVisibility(View.VISIBLE);
        updateMarkLayout();
    }

    private void updateMarkLayout() {
        videoMarkStaticIv.setVisibility(markMode == VIDEO_MARK_MODE_STATIC ? View.VISIBLE : View.GONE);
        videoMarkDynamicIv.setVisibility(markMode == VIDEO_MARK_MODE_DYNAMIC ? View.VISIBLE : View.GONE);
        videoMarkCloseIv.setVisibility(markMode == VIDEO_MARK_MODE_CLOSE ? View.VISIBLE : View.GONE);
        mIsmWaterMaskAdded = false;
    }

    private void openCloseFocalLength() {
        if (isVideoFocalLengthPanelOpen) {
            isVideoFocalLengthPanelOpen = false;
            focalLengthLayout.setVisibility(View.GONE);
            focalLengthBtn.setBackgroundResource(R.drawable.ic_enlarge_close_selector);
        } else {
            isVideoFocalLengthPanelOpen = true;
            focalLengthLayout.setVisibility(View.VISIBLE);
            focalLengthBtn.setBackgroundResource(R.drawable.ic_enlarge_open_selector);
        }
    }

    private void openCloseFlash() {
        //更新闪关灯
        if (mVideoCapturer.setFlash(!isVideoFlashOpen) != 0) {
            Toast.makeText(RoomLiveActivity.this, R.string.flash_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        updateFlashIcon();
        if (!isVideoFlashOpen) {
            Toast.makeText(RoomLiveActivity.this, R.string.flash_close, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(RoomLiveActivity.this, R.string.flash_open, Toast.LENGTH_SHORT).show();
        }
    }

    private void updateFlashIcon() {
        if (isVideoFlashOpen) {
            isVideoFlashOpen = false;
            flashBtn.setBackgroundResource(R.drawable.ic_flash_close_selector);
        } else {
            isVideoFlashOpen = true;
            flashBtn.setBackgroundResource(R.drawable.ic_flash_open_selector);
        }
    }


    private void showBeautyLayout() {
        videoBeautyLayout.setVisibility(View.VISIBLE);
        updateBeautyLayout(isVideoBeautyOriginLast);
    }

    private void showClarityLayout() {
        inputPanel.collapse(true);
        videoClarityLayout.setVisibility(View.VISIBLE);
        ivVideoClarityHd.setVisibility(isVideoClaritySd ? View.GONE : View.VISIBLE);
        ivVideoClaritySd.setVisibility(isVideoClaritySd ? View.VISIBLE : View.GONE);
    }

    private void updateControlUI() {
        final boolean isHide = controlBtnMid.getVisibility() == View.VISIBLE;
        final TranslateAnimation translateAnimationTop;
        final TranslateAnimation translateAnimationMid;
        if (isHide) {
            translateAnimationTop = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0,
                    Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 2);
            translateAnimationMid = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0,
                    Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 1);
        } else {
            translateAnimationTop = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0,
                    Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 2, Animation.RELATIVE_TO_SELF, 0);
            translateAnimationMid = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0,
                    Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 1, Animation.RELATIVE_TO_SELF, 0);
        }
        translateAnimationTop.setDuration(500);
        translateAnimationMid.setDuration(500);

        translateAnimationMid.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                if (isHide) {
                    controlExtendBtn.setBackgroundResource(R.drawable.control_extend_top_selector);
                } else {
                    controlExtendBtn.setBackgroundResource(R.drawable.control_extend_bottom_selector);
                }
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (isHide) {
                    controlBtnMid.setVisibility(View.INVISIBLE);
                } else {
                    controlBtnMid.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        controlBtnMid.post(() -> {
            controlBtnMid.setVisibility(View.VISIBLE);
            controlBtnMid.startAnimation(translateAnimationMid);
        });

        if (controlBtnTop == null) {
            return;
        }
        translateAnimationTop.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (isHide) {
                    controlBtnTop.setVisibility(View.INVISIBLE);
                } else {
                    controlBtnTop.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        controlBtnTop.post(() -> {
            controlBtnTop.setVisibility(View.VISIBLE);
            controlBtnTop.startAnimation(translateAnimationTop);
        });
    }

    private void masterCreateChatRoom(final boolean isVideoMode) {

        meetingName = StringUtil.get36UUID();
        Map<String, Object> ext = createChatRoomExt();
        JSONObject jsonObject = null;
        try {
            jsonObject = parseMap(ext);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (jsonObject == null) {
            Toast.makeText(RoomLiveActivity.this, "创建直播间失败，Json 解析失败", Toast.LENGTH_SHORT).show();
            return;
        }

        LogUtil.i(TAG, "master start create chat room : " + jsonObject);
        boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        ChatRoomHttpClient.getInstance().masterCreateRoom(DemoCache.getAccount(), jsonObject.toString(), isVideoMode, isPortrait,
                new ChatRoomHttpClient.ChatRoomHttpCallback<ChatRoomHttpClient.EnterRoomParam>() {
                    @Override
                    public void onSuccess(ChatRoomHttpClient.EnterRoomParam enterRoomParam) {
                        if (isDestroyed) {
                            return;
                        }
                        roomId = enterRoomParam.getRoomId();
                        pullUrl = enterRoomParam.getPullUrl();
                        liveInfoMode = new LiveInfoMode();
                        liveInfoMode.setMeetingName(meetingName);
                        liveInfoMode.setRoomId(roomId);
                        liveInfoMode.setPullUrl(enterRoomParam.getPullUrl());
                        liveInfoMode.setPushUrl(enterRoomParam.getPushUrl());
                        liveInfoMode.setLiveType(liveType.getValue());

                        findInputViews();
                        videoFocalLengthSb.setMax(mVideoCapturer.getMaxZoom() > 6 ? 5 : mVideoCapturer.getMaxZoom() - 1);
                        mVideoCapturer.setZoom(videoFocalLengthSb.getProgress());
                        startLayout.setVisibility(View.GONE);
                        LogUtil.i(TAG, "master create chat room success , pull : " + liveInfoMode.getPullUrl() + " , push: " + liveInfoMode.getPushUrl());

                        createLiveAVRoom();
                    }

                    @Override
                    public void onFailed(int code, String errorMsg) {
                        isStartLiving = false;
                        startBtn.setText(R.string.live_start);
                        Toast.makeText(RoomLiveActivity.this, "创建直播间失败，code:" + code + ", errorMsg:" + errorMsg, Toast.LENGTH_SHORT).show();
                        LogUtil.i(TAG, "master create chat room failed , code : " + code + ", msg " + errorMsg);
                    }
                });
    }


    private void masterContinueLive() {

        meetingName = liveInfoMode.getMeetingName();
        roomId = liveInfoMode.getRoomId();
        roomInfo = liveInfoMode.getChatRoomInfo();
        findInputViews();
        videoFocalLengthSb.setMax(mVideoCapturer.getMaxZoom() > 6 ? 5 : mVideoCapturer.getMaxZoom() - 1);
        mVideoCapturer.setZoom(videoFocalLengthSb.getProgress());
        startLayout.setVisibility(View.GONE);

        joinLiveAVRoom();
        enterChatRoom();
        isStartLive = true;
        MicHelper.getInstance().sendUserJoinMsg(roomId, DemoCache.getAccount());
    }


    private void createLiveAVRoom() {

        LogUtil.i(TAG, "start create av room ");
        AVChatManager.getInstance().createRoom(meetingName, null, getLiveTaskConfig(), new AVChatCallback<AVChatChannelInfo>() {
            @Override
            public void onSuccess(AVChatChannelInfo avChatChannelInfo) {
                if (isDestroyed) {
                    return;
                }
                LogUtil.i(TAG, "create av room success");
                Toast.makeText(RoomLiveActivity.this, "创建的房间名：" + meetingName, Toast.LENGTH_SHORT).show();
                joinLiveAVRoom();
                enterChatRoom();
                isStartLive = true;
            }

            @Override
            public void onFailed(int i) {
                if (isDestroyed) {
                    return;
                }
                if (i == ResponseCode.RES_EEXIST) {
                    // 417表示该频道已经存在
                    LogUtil.e(TAG, "create av room 417, enter room");
                    Toast.makeText(RoomLiveActivity.this, "创建的房间名：" + meetingName, Toast.LENGTH_SHORT).show();
                    joinLiveAVRoom();
                    enterChatRoom();
                    isStartLive = true;
                } else {
                    isStartLiving = false;
                    startBtn.setText(R.string.live_start);
                    LogUtil.e(TAG, "create av room failed, code:" + i);
                    Toast.makeText(RoomLiveActivity.this, "create room failed, code:" + i, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onException(Throwable throwable) {
                if (isDestroyed) {
                    return;
                }
                isStartLiving = false;
                startBtn.setText(R.string.live_start);
                LogUtil.e(TAG, "create room onException, throwable:" + throwable.getMessage());
                Toast.makeText(RoomLiveActivity.this, "create room onException, throwable:" + throwable.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    /**
     * 生成房间推流 task 配置 ， 房间推不用配置 AVChatParameters.KEY_SESSION_LIVE_URL
     * 配置了也无效了
     */
    private List<AVChatLiveTaskConfig> getLiveTaskConfig() {
        List<AVChatLiveTaskConfig> livePushTasks = new ArrayList<>();
        AVChatLiveTaskConfig selfTask = new AVChatLiveTaskConfig();
        selfTask.setTaskId(String.valueOf(System.currentTimeMillis() - 1));
        selfTask.setPushUrl(liveInfoMode.getPushUrl());
        selfTask.setMainPictureAccount(DemoCache.getAccount());
        selfTask.setLayoutMode(AVChatLiveMode.LAYOUT_FLOATING_RIGHT_VERTICAL);
        livePushTasks.add(selfTask);
        LogUtil.i(TAG, "live push task id : " + selfTask.getTaskId());
        return livePushTasks;
    }

    // 主播直播前预览
    private void startPreview() {
        startEngine();
    }


    private void startEngine() {

        if (!isReleaseEngine) {
            LogUtil.e(TAG, "startEngine err , not released ");
            return;
        }
        AVChatManager.getInstance().enableRtc();
        isReleaseEngine = false;
        if (mVideoCapturer == null) {
            mVideoCapturer = AVChatVideoCapturerFactory.createCamera2Capturer(true, false);
        }
        AVChatManager.getInstance().setupVideoCapturer(mVideoCapturer);
        AVChatParameters parameters = new AVChatParameters();
        parameters.setBoolean(AVChatParameters.KEY_SESSION_LIVE_MODE, true);
        parameters.setInteger(AVChatParameters.KEY_SESSION_MULTI_MODE_USER_ROLE, AVChatUserRole.NORMAL);
        parameters.setString(AVChatParameters.KEY_VIDEO_ENCODER_MODE, AVChatMediaCodecMode.MEDIA_CODEC_SOFTWARE);
        parameters.setString(AVChatParameters.KEY_VIDEO_DECODER_MODE, AVChatMediaCodecMode.MEDIA_CODEC_AUTO);
        parameters.setInteger(AVChatParameters.KEY_VIDEO_QUALITY, AVChatVideoQuality.QUALITY_720P);
        parameters.setBoolean(AVChatParameters.KEY_AUDIO_REPORT_SPEAKER, true);//开启声音强度汇报
        parameters.setInteger(AVChatParameters.KEY_VIDEO_FRAME_RATE, AVChatVideoFrameRate.FRAME_RATE_25);

        parameters.setInteger(AVChatParameters.KEY_VIDEO_FIXED_CROP_RATIO, AVChatVideoCropRatio.CROP_RATIO_16_9);
        parameters.setBoolean(AVChatParameters.KEY_VIDEO_ROTATE_IN_RENDING, false);

        int videoOrientation = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ?
                AVChatVideoCaptureOrientation.ORIENTATION_PORTRAIT : AVChatVideoCaptureOrientation.ORIENTATION_LANDSCAPE_RIGHT;

        parameters.setInteger(AVChatParameters.KEY_VIDEO_CAPTURE_ORIENTATION, videoOrientation);
        parameters.setBoolean(AVChatParameters.KEY_VIDEO_FRAME_FILTER_NEW, true);
        AVChatManager.getInstance().setParameters(parameters);
        AVChatManager.getInstance().setVideoQualityStrategy(AVChatVideoQualityStrategy.ScreenSharing);
        if (liveType == LiveType.VIDEO_TYPE) {
            AVChatManager.getInstance().enableVideo();
            AVChatManager.getInstance().setupLocalVideoRender(null, false, 0);
            AVChatManager.getInstance().setupLocalVideoRender(videoRender, false, AVChatVideoScalingType.SCALE_ASPECT_BALANCED);
            AVChatManager.getInstance().startVideoPreview();
        }
        loadAudioEffect();
    }


    private void releaseEngine() {
        if (liveType == LiveType.VIDEO_TYPE) {
            releaseVideoEffect();
            AVChatManager.getInstance().stopVideoPreview();
            AVChatManager.getInstance().disableVideo();
        }
        AVChatManager.getInstance().disableRtc();
        isReleaseEngine = true;
    }


    private JSONObject parseMap(Map map) throws JSONException {
        if (map == null) {
            return null;
        }

        JSONObject obj = new JSONObject();
        for (Object o : map.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            String key = String.valueOf(entry.getKey());
            Object value = entry.getValue();
            obj.put(key, value);
        }

        return obj;
    }

    private void updateLiveSwitchLayout(boolean isPortrait) {
        if (!isPortrait) {
            screenSwitchHorizontal.setSelected(true);
            screenSwitchVertical.setSelected(false);
        } else {
            screenSwitchHorizontal.setSelected(false);
            screenSwitchVertical.setSelected(true);
        }
    }

    // set video quality
    private void setVideoQuality(int quality) {
        AVChatParameters parameters = new AVChatParameters();
        parameters.setInteger(AVChatParameters.KEY_VIDEO_QUALITY, quality);
        AVChatManager.getInstance().setParameters(parameters);
    }

    // 显示礼物布局
    private void showGiftLayout() {
        inputPanel.collapse(true);// 收起软键盘
        giftLayout.setVisibility(View.VISIBLE);
        adapter.notifyDataSetChanged();
        if (adapter.getCount() == 0) {
            // 暂无礼物
            noGiftText.setVisibility(View.VISIBLE);
        } else {
            noGiftText.setVisibility(View.GONE);
        }
    }

    protected void updateGiftList(GiftType type) {
        if (!updateGiftCount(type)) {
            giftList.add(new Gift(type, GiftConstant.titles[type.getValue()], 1, GiftConstant.images[type.getValue()]));
        }
        GiftCache.getInstance().saveGift(roomId, type.getValue());
    }

    // 更新收到礼物的数量
    private boolean updateGiftCount(GiftType type) {
        for (Gift gift : giftList) {
            if (type == gift.getGiftType()) {
                gift.setCount(gift.getCount() + 1);
                return true;
            }
        }
        return false;
    }

    private void showMusicLayout() {
        inputPanel.collapse(true);
        backgroundMusicLayout.setVisibility(View.VISIBLE);
    }

    private void leaveAndReleaseAVRoom() {
        if (isReleaseEngine) {
            return;
        }
        releaseVideoEffect();
        isReleaseEngine = true;
        String meetName = meetingName;
        if (pkStateEnum == PKStateEnum.PKING) {
            meetName = pkMeetingName;
            MicHelper.getInstance().sendCustomPKNotify(pkAccount, PushMicNotificationType.EXITED.getValue(), null);
            pkStateEnum = PKStateEnum.NONE;
        }
        MicHelper.getInstance().leaveAndReleaseAVRoom(liveType == LiveType.VIDEO_TYPE, liveType == LiveType.VIDEO_TYPE, meetName);
    }

    private void releaseVideoEffect() {

        if (liveType != LiveType.VIDEO_TYPE) {
            return;
        }
        // 释放资源
        isUnInitVideoEffect = true;
        if (mVideoEffectHandler != null) {
            final CountDownLatch countDownLatch = new CountDownLatch(1);
            mVideoEffectHandler.post(() -> {

                if (mFURenderer != null) {
                    mFURenderer.onSurfaceDestroyed();
                    mFURenderer = null;
                }
                countDownLatch.countDown();

                isFilterTypeSet = false;
                mVideoEffect = null;
            });
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        isUnInitVideoEffect = false;
        findViews();
        updateRoomUI(true);
        loadGift();
        updateBeautyIcon(isVideoBeautyOriginLast);
        if (liveType == LiveType.VIDEO_TYPE && AVChatManager.getInstance().checkPermission(this).size() == 0) {
            startLiveBgIv.setVisibility(View.GONE);
            AVChatParameters parameters = new AVChatParameters();
            int videoOrientation = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT
                    ? AVChatVideoCaptureOrientation.ORIENTATION_PORTRAIT : AVChatVideoCaptureOrientation.ORIENTATION_LANDSCAPE_RIGHT;

            parameters.setInteger(AVChatParameters.KEY_VIDEO_CAPTURE_ORIENTATION, videoOrientation);
            AVChatManager.getInstance().setParameters(parameters);
            AVChatManager.getInstance().setupLocalVideoRender(null, false, 0);
            AVChatManager.getInstance().setupLocalVideoRender(videoRender, false, AVChatVideoScalingType.SCALE_ASPECT_BALANCED);
        }
        if (AVChatManager.getInstance().checkPermission(this).size() != 0 &&
                getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            startLiveBgIv.setBackgroundResource(R.drawable.live_start_landscape_bg);
        }
    }


    // 网络连接成功
    protected void onConnected() {
        if (!disconnected) {
            return;
        }
        changeNetWorkTip(true);
        disconnected = false;
    }

    // 网络断开
    protected void onDisconnected() {
        disconnected = true;
        changeNetWorkTip(false);
    }

    private void changeNetWorkTip(boolean isShow) {
        if (networkStateLayout == null) {
            networkStateLayout = findView(R.id.network_state_layout);
        }
        networkStateLayout.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        MPermission.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @OnMPermissionGranted(LIVE_PERMISSION_REQUEST_CODE)
    public void onLivePermissionGranted() {
        if (liveType == LiveType.VIDEO_TYPE) {
            startLiveBgIv.setVisibility(View.GONE);
        }
        Toast.makeText(RoomLiveActivity.this, "授权成功", Toast.LENGTH_SHORT).show();
        isPermissionGrant = true;
        startPreview();
    }

    @OnMPermissionDenied(LIVE_PERMISSION_REQUEST_CODE)
    public void onLivePermissionDenied() {
        List<String> deniedPermissions = MPermission.getDeniedPermissions(this, LIVE_PERMISSIONS);
        String tip = "您拒绝了权限" + MPermissionUtil.toString(deniedPermissions) + "，无法开启直播";
        Toast.makeText(RoomLiveActivity.this, tip, Toast.LENGTH_SHORT).show();
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

        Toast.makeText(RoomLiveActivity.this, sb.toString(), Toast.LENGTH_LONG).show();
    }


    // 主播让观众下麦的超时
    Runnable userLeaveRunnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(RoomLiveActivity.this, "超时，请重新连麦", Toast.LENGTH_SHORT).show();
            if (currentInteractionMembers.getLast() != null)
                currentInteractionMembers.getLast().setMicStateEnum(MicStateEnum.LEAVING);
            updateMemberListUI(currentInteractionMembers.getLast(), MicStateEnum.NONE);
        }
    };

    // 主播选择观众连麦的超时
    Runnable userJoinRunnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(RoomLiveActivity.this, "连麦超时", Toast.LENGTH_SHORT).show();
            if (currentInteractionMembers.getLast() != null)
                currentInteractionMembers.getLast().setMicStateEnum(MicStateEnum.NONE);
            interactionAdapter.notifyDataSetChanged();
        }
    };

    // 显示互动布局
    private void showInteractionLayout() {
        interactionLayout.setVisibility(View.VISIBLE);
        switchInteractionUI();
    }

    /**
     * 观众申请连麦
     */
    @Override
    protected void audienceApplyJoinQueue(CustomNotification customNotification, JSONObject json) {
        // 已经在连麦队列中，修改连麦申请的模式
        for (InteractionMember dataSource : interactionDataSource) {
            if (dataSource.getAccount().equals(customNotification.getFromAccount())) {
                if (!json.containsKey(PushLinkConstant.LINK_STYLE)) {
                    return;
                }
                dataSource.setAvChatType(AVChatType.typeOfValue(json.getIntValue(PushLinkConstant.LINK_STYLE)));
                interactionAdapter.notifyDataSetChanged();
                return;
            }
        }
        interactionCount++;
        saveToLocalInteractionList(customNotification.getFromAccount(), json);
        updateQueueUI();
    }

    // 主播保存互动观众
    private void saveToLocalInteractionList(String account, JSONObject jsonObject) {
        JSONObject info = (JSONObject) jsonObject.get(PushLinkConstant.INFO);
        String nick = info.getString(PushLinkConstant.NICK);
        AVChatType style = AVChatType.typeOfValue(jsonObject.getIntValue(PushLinkConstant.LINK_STYLE));
        if (!TextUtils.isEmpty(account)) {
            String meetingUid = String.valueOf(AVChatManager.getInstance().getUidByAccount(account));
            interactionDataSource.add(new InteractionMember(account, meetingUid, nick, AVATAR_DEFAULT, style));
        }
        interactionAdapter.notifyDataSetChanged();
    }

    // 显示互动人数
    private void updateInteractionNumbers() {
        if (interactionCount <= 0) {
            interactionCount = 0;
            interactionBtn.setText("");
            interactionBtn.setBackgroundResource(R.drawable.ic_interaction_normal);
        } else {
            interactionBtn.setBackgroundResource(R.drawable.ic_interaction_numbers);
            interactionBtn.setText(String.valueOf(interactionCount));
        }
    }

    // 有无连麦人的布局切换
    private void switchInteractionUI() {
        if (interactionCount <= 0) {
            noApplyText.setVisibility(View.VISIBLE);
            applyCountText.setVisibility(View.GONE);
            interactionDataSource.clear();
        } else {
            noApplyText.setVisibility(View.GONE);
            applyCountText.setVisibility(View.VISIBLE);
            applyCountText.setText(String.format("有%d人想要连线", interactionCount));
        }
        interactionAdapter.notifyDataSetChanged();
    }

    //   观众取消连麦申请
    @Override
    protected void audienceExitQueue(CustomNotification customNotification) {
        cancelLinkMember(customNotification.getFromAccount());
    }

    // 取消连麦申请 界面变化
    private void cancelLinkMember(String account) {
        removeCancelLinkMember(account);
        updateQueueUI();
    }

    // 移除取消连麦人员
    private void removeCancelLinkMember(String account) {
        if (interactionDataSource == null || interactionDataSource.isEmpty()) {
            return;
        }
        for (InteractionMember m : interactionDataSource) {
            if (m.getAccount().equals(account)) {
                interactionDataSource.remove(m);
                interactionCount--;
                break;
            }
        }
    }

    // 更新连麦列表和连麦人数
    private void updateQueueUI() {
        updateInteractionNumbers();
        switchInteractionUI();
    }

    /**
     * MemberLinkListener
     **/
    @Override
    public void onClick(InteractionMember member) {
        // 选择某人进行视频连线
        if (pkStateEnum == PKStateEnum.PKING) {
            Toast.makeText(this, "PK中，无法和观众连麦", Toast.LENGTH_SHORT).show();
        } else if (currentInteractionMembers.size() < maxInteractionMembers) {
            doLink(member);
            getHandler().postDelayed(userJoinRunnable, USER_JOIN_OVERTIME);
        } else {
            Toast.makeText(this, "人数已满，请先下麦一位观众", Toast.LENGTH_SHORT).show();
        }
    }

    // 主播选择某人连麦
    private void doLink(InteractionMember member) {
        if (member == null) {
            return;
        }
        currentInteractionMembers.addLast(member);
        updateMemberListUI(member, MicStateEnum.CONNECTING);

        // 发送通知告诉被选中连麦的人
        MicHelper.getInstance().sendLinkNotify(roomId, member);
    }

    // 连麦列表显示正在连麦中
    private void updateMemberListUI(InteractionMember member, MicStateEnum micStateEnum) {
        member.setMicStateEnum(micStateEnum);
        interactionAdapter.notifyDataSetChanged();
        interactionLayout.setVisibility(View.GONE);
    }

    @Override
    protected void showOtherMicLinkedView(int index, String account, String meetingUid, String nick, int linkStyle) {

        isHasAudienceOnLink = currentInteractionMembers.size() > 0;
        super.showOtherMicLinkedView(index, account, meetingUid, nick, linkStyle);

        InteractionView interactionView = interactionGroupView[index];

        interactionView.rootViewLayout.setVisibility(View.VISIBLE);
        interactionView.audienceLoadingLayout.setVisibility(View.GONE);
        interactionView.livingBg.setVisibility(View.VISIBLE);

        if (liveType == LiveType.VIDEO_TYPE && linkStyle == AVChatType.VIDEO.getValue()) {
            interactionView.bypassVideoRender.setVisibility(View.VISIBLE);
            interactionView.audienceLivingLayout.setVisibility(View.VISIBLE);
            interactionView.audioModeBypassLayout.setVisibility(View.GONE);
            AVChatManager.getInstance().setupRemoteVideoRender(account, null, false, 0);
            AVChatManager.getInstance().setupRemoteVideoRender(account, interactionView.bypassVideoRender, false, AVChatVideoScalingType.SCALE_ASPECT_BALANCED);
        } else if (linkStyle == AVChatType.AUDIO.getValue()) {
            interactionView.audienceLivingLayout.setVisibility(View.GONE);
            interactionView.audioModeBypassLayout.setVisibility(View.VISIBLE);
        }
    }

    // 移除互动布局中的申请连麦成员
    private void removeMemberFromList(String account) {
        currentInteractionMembers.getLast().setMicStateEnum(MicStateEnum.CONNECTED);
        cancelLinkMember(account);
    }


    private InteractionMember getByAccount(String account) {
        InteractionMember interactionMember = null;
        for (InteractionMember tmp : currentInteractionMembers) {
            if (tmp.getAccount().equals(account)) {
                interactionMember = tmp;
                break;
            }
        }
        return interactionMember;
    }


    private InteractionMember getFromCacheByAccount(String account) {
        InteractionMember interactionMember = null;
        for (InteractionMember tmp : cacheInteractionMembers) {
            if (tmp.getAccount().equals(account)) {
                interactionMember = tmp;
                break;
            }
        }
        return interactionMember;
    }

    // 断开连麦
    @Override
    protected void doCloseInteraction(InteractionView interactionView) {

        if (interactionView == null || interactionView.account == null) {
            LogUtil.e(TAG, "do close interaction , err ");
            return;
        }
        InteractionMember interactionMember = null;
        for (InteractionMember member : currentInteractionMembers) {
            if (TextUtils.equals(interactionView.account, member.getAccount())) {
                interactionMember = member;
                break;
            }
        }
        if (interactionMember == null) {
            LogUtil.e(TAG, "do close interaction , but not find member , accid : " + interactionView.account);
            return;
        }

        if (interactionMember.getMicStateEnum() == MicStateEnum.CONNECTED) {
            MicHelper.getInstance().masterBrokeMic(roomId, interactionMember.getAccount());
        } else if (interactionMember.getMicStateEnum() == MicStateEnum.CONNECTING) {

            LogUtil.i(TAG, "do close interaction ,but connecting , accid : " + interactionMember.getAccount());

            // 正在连麦中被关闭了,从显示队列中删除，并刷新数字
            for (InteractionMember member : interactionDataSource) {
                if (member.getAccount().equals(interactionMember.getAccount())) {
                    interactionDataSource.remove(member);
                    interactionAdapter.notifyDataSetChanged();
                    interactionCount--;
                    updateInteractionNumbers();
                    break;
                }
            }
        }
        interactionMember.setMicStateEnum(MicStateEnum.LEAVING);
        currentInteractionMembers.remove(interactionMember);
        isHasAudienceOnLink = currentInteractionMembers.size() > 0;
    }

    // 隐藏旁路直播.移除内存队列
    @Override
    protected void resetConnectionView(int index) {
        super.resetConnectionView(index);
        interactionGroupView[index].bypassVideoRender.setVisibility(View.GONE);
    }

    // 被观众拒绝
    @Override
    protected void rejectLinkByAudience(String account) {

        Toast.makeText(RoomLiveActivity.this, "被观众拒绝", Toast.LENGTH_SHORT).show();
        InteractionMember interactionMember = getByAccount(account);
        if (interactionMember == null) {
            LogUtil.e(TAG, "rejectConnecting : " + account + " can not find");
            return;
        }
        interactionMember.setMicStateEnum(MicStateEnum.NONE);
        getHandler().removeCallbacks(userJoinRunnable);
        cancelLinkMember(interactionMember.getAccount());
        currentInteractionMembers.remove(interactionMember);
        isHasAudienceOnLink = currentInteractionMembers.size() > 0;
    }


    private void onPkUserJoined(String user) {
        pkAccount = user;
        AVChatManager.getInstance().setupRemoteVideoRender(user, null, false, 0);
        AVChatManager.getInstance().setupRemoteVideoRender(user, pkVideoRender, false, AVChatVideoScalingType.SCALE_ASPECT_BALANCED);

    }


    protected synchronized void notifyCapturerConfigChange() {
        mDropFramesWhenConfigChanged = 2;
    }

    //预加载音效
    private void loadAudioEffect() {
        String audioPathFirst = musicPath + "test1.wav";
        String audioPathSecond = musicPath + "test2.wav";
        AVChatManager.getInstance().preloadAudioEffect(1, audioPathFirst);
        AVChatManager.getInstance().preloadAudioEffect(2, audioPathSecond);
    }

    private void exitChatRoom() {
        resetChatRoomLiveType();
        ChatRoomMemberCache.getInstance().clearRoomCache(roomId);
        leaveAndReleaseAVRoom();
        NIMClient.getService(ChatRoomService.class).exitChatRoom(roomId);
        finish();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        if (mFURenderer != null && (Math.abs(x) > 3 || Math.abs(y) > 3)) {
            if (Math.abs(x) > Math.abs(y)) {
                mFURenderer.onDeviceOrientationChanged(x > 0 ? 0 : 180);
            } else {
                mFURenderer.onDeviceOrientationChanged(y > 0 ? 90 : 270);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}

