/*
 * Copyright (C) 2006 The Android Open Source Project
 * Copyright (C) 2015 netease
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.nim.chatroom.demo.thirdparty.video;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.netease.nim.chatroom.demo.R;
import com.netease.nim.chatroom.demo.base.util.ScreenUtil;
import com.netease.nim.chatroom.demo.im.session.ModuleProxy;
import com.netease.nim.chatroom.demo.thirdparty.video.constant.VideoConstant;

import java.util.Locale;

/**
 * A view containing controls for a MediaPlayer. Typically contains the
 * buttons like "Play/Pause", "Rewind", "Fast Forward" and a progress
 * slider. It takes care of synchronizing the controls with the state
 * of the MediaPlayer.
 * <p>
 * The way to use this class is to instantiate it programmatically.
 * The MediaController will create a default set of controls
 * and put them in a window floating above your application. Specifically,
 * the controls will float above the view specified with setAnchorView().
 * The window will disappear if left idle for three seconds and reappear
 * when the user touches the anchor view.
 * <p>
 * Functions like show() and hide() have no effect when MediaController
 * is created in an xml layout.
 *
 * MediaController will hide and
 * show the buttons according to these rules:
 * <ul>
 * <li> The "previous" and "next" buttons are hidden until setPrevNextListeners()
 *   has been called
 * <li> The "previous" and "next" buttons are visible but disabled if
 *   setPrevNextListeners() was called with null listeners
 * <li> The "rewind" and "fastforward" buttons are shown unless requested
 *   otherwise by using the MediaController(Context, boolean) constructor
 *   with the boolean set to false
 * </ul>
 */
public class NEVideoController {
    private static final int TIME_OUT = 3000;

    private Context mContext;
    private Handler mHandler;

    private ModuleProxy moduleProxy; // 全屏时需要关闭输入区域proxy
    private MediaPlayerControl mPlayer; // 控制器

    private ViewGroup mRoot; // 控制条容器
    private ViewGroup videoLayout; // 整个播放器容器

    // 四个按钮区域
    private ImageView mPauseButton;  // 暂停
    private ImageView mMuteButton;   // 静音
    private ImageView mSetPlayerScaleButton; // 缩放

    private LinearLayout startPauseLayout; // 暂停
    private LinearLayout muteLayout; // 静音
    private LinearLayout scaleLayout; // 缩放
    private LinearLayout snapShotLayout; // 截图

    // 状态控制
    private boolean mute_flag = false;
    private boolean mPaused = false;
    private boolean mIsFullScreen = false;
    private boolean mShowing = false; // 当前是否显示控制条

    // 缩放模式
    private int mVideoScalingMode = VideoConstant.VIDEO_SCALING_MODE_FIT;

    public NEVideoController(Context context, ViewGroup videoLayout, ViewGroup root,
                             ModuleProxy moduleProxy, int mVideoScalingMode) {
        this.mContext = context;
        this.videoLayout = videoLayout;
        this.mRoot = root;
        this.mHandler = new Handler(context.getMainLooper());
        this.moduleProxy = moduleProxy;
        this.mVideoScalingMode = mVideoScalingMode;

        initListener();
    }

    private void initListener() {
        videoLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mShowing) {
                    hide();
                } else {
                    show();
                }
            }
        });
    }

    private void initControllerView(View v) {
        startPauseLayout = (LinearLayout) v.findViewById(R.id.play_pause_layout); //播放暂停按钮
        mPauseButton = (ImageView) v.findViewById(R.id.mediacontroller_play_pause);
        if (startPauseLayout != null) {
        	if (mPaused) {
        		mPauseButton.setImageResource(R.drawable.nemediacontroller_pause);
        		mPlayer.pause();
        	}
            startPauseLayout.requestFocus();
            startPauseLayout.setOnClickListener(mPauseListener);
        }
        
        mSetPlayerScaleButton = (ImageView) v.findViewById(R.id.video_player_scale);  //画面显示模式按钮
        scaleLayout = (LinearLayout) v.findViewById(R.id.scale_layout);
        if(scaleLayout != null) {
        	if (mPlayer.isHardware() && mPlayer.isInBackground()) {
				switch(mVideoScalingMode)
				{
				case VideoConstant.VIDEO_SCALING_MODE_FIT:
					mVideoScalingMode = VideoConstant.VIDEO_SCALING_MODE_FIT;
					mSetPlayerScaleButton.setImageResource(R.drawable.nemediacontroller_scale01);
					break;
				case VideoConstant.VIDEO_SCALING_MODE_NONE:
					mVideoScalingMode = VideoConstant.VIDEO_SCALING_MODE_NONE;
					mSetPlayerScaleButton.setImageResource(R.drawable.nemediacontroller_scale02);
					break;
				default:
					mVideoScalingMode = VideoConstant.VIDEO_SCALING_MODE_NONE;
				}
	            mPlayer.setVideoScalingMode(mVideoScalingMode);
        	}

            scaleLayout.requestFocus();
            scaleLayout.setOnClickListener(mSetPlayerScaleListener);
        }
        
        snapShotLayout = (LinearLayout) v.findViewById(R.id.snapShot_layout);
        if (snapShotLayout != null) {
            snapShotLayout.requestFocus();
            snapShotLayout.setOnClickListener(mSnapShotListener);
        }
       
        mMuteButton = (ImageView) v.findViewById(R.id.video_player_mute);  //静音按钮
        muteLayout = (LinearLayout) v.findViewById(R.id.mute_layout);
    	if (muteLayout != null) {
    		if (mPlayer.isHardware() && mPlayer.isInBackground()) {
            	if (mute_flag) {
            		mMuteButton.setImageResource(R.drawable.nemediacontroller_mute01);
    				mPlayer.setMute(true);
            	}
            }
            muteLayout.setOnClickListener(mMuteListener);
    	}
    }

    //设置MediaPlayer使之与要绑定的控件绑定在一起其参数是一个MediaController.MediaPlayerControl 静态接口的对象，
    //(而VideoView是MediaController.MediaPlayerControl静态接口的子实现类，这就使得我们可以更好的控制我们的视频播放进度)
    public void setMediaPlayer(MediaPlayerControl player) {
        mPlayer = player;

        initControllerView(mRoot);
        resetViews();
        updatePausePlay();
    }

    public void resetViews() {
        mute_flag = false;
        mMuteButton.setImageResource(R.drawable.nemediacontroller_mute02);
    }

    public void show() {
        if (!mShowing) {
            mRoot.setVisibility(View.VISIBLE);
            mShowing = true;
            postTimeOut();
        }
        updatePausePlay();
    }

    public void hide() {
        if (mShowing) {
            mRoot.setVisibility(View.GONE);
            mShowing = false;
            removeTimeOut();
        }
    }

    public boolean isShowing() {
        return mShowing;
    }


    public interface OnShownListener {
        public void onShown();
    }

    private OnShownListener mShownListener;

    //注册一个回调函数，在MediaController显示后被调用。
    public void setOnShownListener(OnShownListener l) {
        mShownListener = l;
    }

    public interface OnHiddenListener {
        public void onHidden();
    }

    private OnHiddenListener mHiddenListener;

    //注册一个回调函数，在MediaController隐藏后被调用。
    public void setOnHiddenListener(OnHiddenListener l) {
        mHiddenListener = l;
    }



    private static String stringForTime(long position) {
        int totalSeconds = (int) ((position / 1000.0)+0.5);

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

//        if (hours > 0) {
            return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds).toString();
//        } else {
//            return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds).toString();
//        }
    }
    
    private View.OnClickListener mMuteListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
            removeTimeOut();
			if (!mute_flag) {
				mMuteButton.setImageResource(R.drawable.nemediacontroller_mute01);
				mPlayer.setMute(true);
				mute_flag = true;
			}
			else {
				mMuteButton.setImageResource(R.drawable.nemediacontroller_mute02);
				mPlayer.setMute(false);
				mute_flag = false;
			}
            postTimeOut();
		}
    };
	
	private View.OnClickListener mSetPlayerScaleListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
            removeTimeOut();
            if (mIsFullScreen) {
                mVideoScalingMode = VideoConstant.VIDEO_SCALING_MODE_FIT;
                mSetPlayerScaleButton.setImageResource(R.drawable.nemediacontroller_scale01);
                mIsFullScreen = false;
                videoLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ScreenUtil.dip2px(217)));
            } else {
                moduleProxy.shouldCollapseInputPanel(); // 关闭输入区域
                mVideoScalingMode = VideoConstant.VIDEO_SCALING_MODE_FILL_BLACK;
                mSetPlayerScaleButton.setImageResource(R.drawable.nemediacontroller_scale02);
                mIsFullScreen = true;
                videoLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            }

            try {
            	mPlayer.setVideoScalingMode(mVideoScalingMode);
            } catch (NumberFormatException e) {
                
            }
            postTimeOut();
		}
	};

	private View.OnClickListener mSnapShotListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			removeTimeOut();
			if(mPlayer.getMediaType().equals("localaudio") || mPlayer.isHardware()) {
				AlertDialog alertDialog;
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
                alertDialogBuilder.setTitle("注意");
                if (mPlayer.getMediaType().equals("localaudio"))
                	alertDialogBuilder.setMessage("音频播放不支持截图！");
                else if (mPlayer.isHardware())
                	alertDialogBuilder.setMessage("硬件解码不支持截图！");
                alertDialogBuilder.setCancelable(false)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ;
                        }
                    });
                alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                
                return;
			}
			
			mPlayer.getSnapshot();
            postTimeOut();
		}
	};
	
    private View.OnClickListener mPauseListener = new View.OnClickListener() {
        public void onClick(View v) {
            removeTimeOut();
            doPauseResume();
            postTimeOut();
        }
    };

    private void updatePausePlay() {
        if (mRoot == null || mPauseButton == null)
            return;

        if (mPlayer.isPlaying()) {
            mPauseButton.setImageResource(R.drawable.nemediacontroller_play);
		}
        else {
            mPauseButton.setImageResource(R.drawable.nemediacontroller_pause);
        }
    }

    private void doPauseResume() {
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
            mPlayer.manualPause(true);
            mPaused = true;
            // 正在播放时，显示暂停按钮。所以点击暂停按钮，显示播放按钮
            mPauseButton.setImageResource(R.drawable.nemediacontroller_pause);
        }
        else {
            mPlayer.start();
            mPlayer.manualPause(false);
            mPaused = false;
            // 暂停时，显示播放按钮。所以点击了播放按钮，显示暂停按钮
            mPauseButton.setImageResource(R.drawable.nemediacontroller_play);
        }
    }

    /**
     * 移除3s控制条消失
     */
    private void removeTimeOut() {
        mHandler.removeCallbacks(showingRunnable);
    }

    /**
     * post 3s控制条消失
     */
    private void postTimeOut() {
        mHandler.postDelayed(showingRunnable, TIME_OUT);
    }

    Runnable showingRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    public interface MediaPlayerControl {
        void start();

        void pause();

        int getDuration();

        int getCurrentPosition();

        void seekTo(long pos);

        void seekAndChangeUrl(long pos, String path);

        boolean isPlaying();

        boolean isPaused();

        void manualPause(boolean paused);

        int getBufferPercentage();

        boolean canPause();

        boolean canSeekBackward();

        boolean canSeekForward();

        void getSnapshot();

        void setMute(boolean mute);

        void setVideoScalingMode(int videoScalingMode);

        String getMediaType();

        boolean isHardware();

        boolean isInBackground();
    }

}
