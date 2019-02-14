package com.netease.nim.chatroom.demo.thirdparty.video;

import android.content.Context;
import android.os.Handler;
import android.view.View;

import com.netease.neliveplayer.sdk.NELivePlayer;
import com.netease.nim.chatroom.demo.base.util.log.LogUtil;

/**
 * Created by huangjun on 2016/3/28.
 */
public class VideoPlayer {
    public interface VideoPlayerProxy {
        boolean isDisconnected();
        void onError();
        void onCompletion();
        void onPrepared();
        void onInfo(NELivePlayer mp, int what, int extra);
    }

    private final String TAG = "NEVideoPlayer";

    public static final int VIDEO_ERROR_REOPEN_TIMEOUT = 10 * 1000;
    public static final int VIDEO_COMPLETED_REOPEN_TIMEOUT = 3 * 1000;

    private VideoPlayerProxy proxy;
    private NEVideoView videoView;
    private Handler handler;

    private String videoPath; // 拉流地址
    private boolean pauseInBackgroud = true;
    private boolean isHardWare = false;

    public VideoPlayer(Context context, NEVideoView videoView, NEVideoController mMediaController, String videoPath,
                       int bufferStrategy, VideoPlayerProxy proxy, int videoScaleMode) {
        this.handler = new Handler(context.getMainLooper());
        this.videoView = videoView;
        this.videoPath = videoPath;
        this.proxy = proxy;

        videoView.setBufferStrategy(bufferStrategy); //直播低延时/抗抖动
        videoView.setMediaType("livestream");
        videoView.setMediaController(mMediaController, videoScaleMode);
        videoView.setHardwareDecoder(isHardWare);// 硬件解码还是软件解码
        videoView.setPauseInBackground(pauseInBackgroud);
        videoView.setOnErrorListener(onVideoErrorListener);
        videoView.setOnPreparedListener(onVideoPreparedListener);
        videoView.setOnCompletionListener(onCompletionListener);
        videoView.setOnInfoListener(onInfoListener);
        videoView.setVisibility(View.VISIBLE);
    }

    public void onActivityResume() {
        if (pauseInBackgroud && videoView != null && !videoView.isPaused()) {
            videoView.start(); //锁屏打开后恢复播放
        }
    }

    public void onActivityPause() {
        if (pauseInBackgroud && videoView != null) {
            videoView.pause(); //锁屏时暂停
        }
    }

    public void resetVideo() {
        clearReopenVideoTask();
        resetResource();
    }

    public void resetResource() {
        if (videoView != null) {
            videoView.release_resource();
        }
    }

    // start
    public void openVideo() {
        LogUtil.i(TAG, "open video, path=" + videoPath);
        clearReopenVideoTask();
        videoView.requestFocus();
        videoView.setVideoPath(videoPath).start();

    }

    // onPrepared
    private NELivePlayer.OnPreparedListener onVideoPreparedListener = new NELivePlayer.OnPreparedListener() {
        @Override
        public void onPrepared(NELivePlayer neLivePlayer) {
            LogUtil.i(TAG, "video on prepared");

            proxy.onPrepared(); // 视频已经准备好了
        }
    };

    // onError
    private NELivePlayer.OnErrorListener onVideoErrorListener = new NELivePlayer.OnErrorListener() {
        @Override
        public boolean onError(NELivePlayer neLivePlayer, int i, int i1) {
            LogUtil.i(TAG, "video on error, post delay reopen task, delay " + VIDEO_ERROR_REOPEN_TIMEOUT);

            proxy.onError();
            postReopenVideoTask(VIDEO_ERROR_REOPEN_TIMEOUT);
            return true;
        }
    };

    // onCompletion
    private NELivePlayer.OnCompletionListener onCompletionListener = new NELivePlayer.OnCompletionListener() {
        @Override
        public void onCompletion(NELivePlayer neLivePlayer) {
            LogUtil.i(TAG, "video on completed, post delay reopen task, delay " + VIDEO_COMPLETED_REOPEN_TIMEOUT);

            proxy.onCompletion();
            postReopenVideoTask(VIDEO_COMPLETED_REOPEN_TIMEOUT);
        }
    };

    private NELivePlayer.OnInfoListener onInfoListener = new NELivePlayer.OnInfoListener() {
        @Override
        public boolean onInfo(NELivePlayer neLivePlayer, int i, int i1) {
            LogUtil.i(TAG, "video on info, what:" + i);
            proxy.onInfo(neLivePlayer, i, i1);
            return false;
        }
    };

    private Runnable reopenVideoRunnable = new Runnable() {
        @Override
        public void run() {
            if (proxy.isDisconnected()) {
                LogUtil.i(TAG, "reopen video task run but disconnected");
                return;
            }

            LogUtil.i(TAG, "reopen video task run");
            openVideo();
        }
    };

    public void postReopenVideoTask(long time) {
        handler.postDelayed(reopenVideoRunnable, time); // 开启reopen定时器
    }

    private void clearReopenVideoTask() {
        LogUtil.i(TAG, "clear reopen task");
        handler.removeCallbacks(reopenVideoRunnable);
    }
}
