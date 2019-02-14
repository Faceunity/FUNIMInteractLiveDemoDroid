/*
 * Copyright (C) 2006 The Android Open Source Project
 * Copyright (C) 2015 netease
 * @auther biwei
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

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Toast;

import com.netease.neliveplayer.sdk.NELivePlayer;
import com.netease.neliveplayer.sdk.NELivePlayer.*;
import com.netease.neliveplayer.sdk.model.NEMediaInfo;
import com.netease.neliveplayer.sdk.constant.NEPlayStatusType;
import com.netease.nim.chatroom.demo.base.util.StringUtil;
import com.netease.nim.chatroom.demo.base.util.log.LogUtil;
import com.netease.nim.chatroom.demo.im.util.storage.StorageType;
import com.netease.nim.chatroom.demo.im.util.storage.StorageUtil;
import com.netease.nim.chatroom.demo.thirdparty.video.constant.VideoConstant;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class NEVideoView extends SurfaceView implements NEVideoController.MediaPlayerControl{
    private static final String TAG = "NEVideoView";
    
    //states refer to MediaPlayer
    private static final int IDLE = 0;
    private static final int INITIALIZED = 1;
    private static final int PREPARING = 2;
    private static final int PREPARED = 3;
    private static final int STARTED = 4;
    private static final int PAUSED = 5;
    private static final int STOPED = 6;
    private static final int PLAYBACKCOMPLETED = 7;
    private static final int END = 8;
    private static final int RESUME = 9;
    private static final int ERROR = -1;
    
    private int mCurrState = IDLE;
    private int mNextState = IDLE;

    private int mVideoScalingMode = VideoConstant.VIDEO_SCALING_MODE_FIT;

    private Uri mUri;
    private long mDuration = 0 ;
    private long mPlayableDuration = 0;
    private SurfaceHolder mSurfaceHolder = null;
    private NELivePlayer mMediaPlayer = null;
    private boolean       mIsPrepared;
    private int mVideoWidth;
    private int mVideoHeight;
    private int mPixelSarNum;
    private int mPixelSarDen;
    private int mSurfaceWidth;
    private int mSurfaceHeight;
    private NEVideoController mMediaController;
    private View mBuffer;
    private NELivePlayer.OnCompletionListener mOnCompletionListener;
    private NELivePlayer.OnPreparedListener mOnPreparedListener;
    private NELivePlayer.OnErrorListener mOnErrorListener;
    private NELivePlayer.OnSeekCompleteListener mOnSeekCompleteListener;
    private NELivePlayer.OnInfoListener mOnInfoListener;
    private NELivePlayer.OnBufferingUpdateListener mOnBufferingUpdateListener;
    private int mCurrentBufferPercentage;
    private long mSeekWhenPrepared;
    private int mBufferStrategy = 0; //直播低延时
    private boolean mHardwareDecoder = false;
    private boolean mPauseInBackground = false;
    private Context mContext;
    public static String mVersion = null;
    private String mMediaType;
    private boolean mMute = false;
    private boolean isBackground;
    private boolean manualPause = false;

    private boolean isFirstPrepared = true; // 是否第一次准备好
    
    public NEVideoView(Context context) {
        super(context);
        mContext = context;
        initVideoView();
    }

    public NEVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        mContext = context;
        initVideoView();
    }

    public NEVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        initVideoView();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int width = getDefaultSize(mVideoWidth, widthMeasureSpec);
        int height = getDefaultSize(mVideoHeight, heightMeasureSpec);
       if (mVideoHeight > 0 && mVideoWidth > 0) {
           float videoRatio = (float) mVideoWidth / (float) mVideoHeight;
           float viewRatio = (float) width / (float) height;
           if (videoRatio >= viewRatio) {
               height = (int) (width / videoRatio);
           } else {
               width = (int) (height * videoRatio);
           }
       }
        setMeasuredDimension(width, height);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void setVideoScalingMode(int videoScalingMode) {
        LayoutParams layPara = getLayoutParams();
        int winWidth = 0;
        int winHeight = 0;
        Rect rect = new Rect();
        this.getWindowVisibleDisplayFrame(rect);//获取状态栏高度
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay(); //获取屏幕分辨率
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) { //new
            DisplayMetrics metrics = new DisplayMetrics();
            display.getRealMetrics(metrics);
            winWidth = metrics.widthPixels;
            winHeight = metrics.heightPixels - rect.top;
        } else { //old
            try {
                Method mRawWidth = Display.class.getMethod("getRawWidth");
                Method mRawHeight = Display.class.getMethod("getRawHeight");
                winWidth = (Integer) mRawWidth.invoke(display);
                winHeight = (Integer) mRawHeight.invoke(display) - rect.top;
            } catch (NoSuchMethodException e) {
                DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
                winWidth = dm.widthPixels;
                winHeight = dm.heightPixels - rect.top;
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        float winRatio = (float) winWidth / winHeight;
        if (mVideoWidth > 0 && mVideoHeight > 0) {
            float aspectRatio = (float) (mVideoWidth) / mVideoHeight;
            if (mPixelSarNum > 0 && mPixelSarDen > 0)
                aspectRatio = aspectRatio * mPixelSarNum / mPixelSarDen;
            mSurfaceHeight = mVideoHeight;
            mSurfaceWidth = mVideoWidth;

            if (VideoConstant.VIDEO_SCALING_MODE_NONE == videoScalingMode && mSurfaceWidth < winWidth && mSurfaceHeight < winHeight) {
                layPara.width = (int) (mSurfaceHeight * aspectRatio);
                layPara.height = mSurfaceHeight;
            } else if (VideoConstant.VIDEO_SCALING_MODE_FIT == videoScalingMode) { // 按宽度计算
                    layPara.width = winWidth;
                    layPara.height = (int) (winWidth / aspectRatio);
            } else if (VideoConstant.VIDEO_SCALING_MODE_FILL_BLACK == videoScalingMode) { // 全屏留黑边，信息全保留
                if (winRatio > aspectRatio) {
                    // 屏幕宽按照高来计算
                    layPara.width = (int) (aspectRatio * winHeight);
                    layPara.height = winHeight;
                } else {
                    // 屏幕窄按照宽来计算
                    layPara.width = winWidth;
                    layPara.height = (int) (winWidth / aspectRatio);
                }
            } else if (VideoConstant.VIDEO_SCALING_MODE_FILL_SCALE == videoScalingMode) { // 全屏拉伸裁剪
                if (winRatio < aspectRatio) {
                    // 视频宽高比例大按照高来计算
                    layPara.width = (int) (aspectRatio * winHeight);
                    layPara.height = winHeight;
                } else {
                    // 视频宽高比例小按照宽来计算
                    layPara.width = winWidth;
                    layPara.height = (int) (winWidth / aspectRatio);
                }
            }
            setLayoutParams(layPara);
            getHolder().setFixedSize(mSurfaceWidth, mSurfaceHeight);
        }

        mVideoScalingMode = videoScalingMode;
    }

    private void initVideoView() {
        mVideoWidth = 0;
        mVideoHeight = 0;
        mPixelSarNum = 0;
        mPixelSarDen = 0;
        getHolder().addCallback(mSHCallback);
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
        mCurrState = IDLE;
        mNextState = IDLE;
    }

    public NEVideoView setVideoPath(String path) { //设置视频文件路径
        if(path == null) {
            LogUtil.i(TAG, "setVideoPath is null" );
            return this;
        }
    	isBackground = false; //指示是否在后台
        setVideoURI(Uri.parse(path));
        return this;
    }

    public void setVideoURI(Uri uri) {
        mUri = uri;
        mSeekWhenPrepared = 0;
        openVideo();
        requestLayout();
        invalidate();
    }
    
    public void stopPlayback() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
            mCurrState = END;
            mNextState = END;
        }
    }

    private void openVideo() {
        if (mUri == null || mSurfaceHolder == null) {
        	// not ready for playback just yet, will try again later
            return;
        }

        // Tell the music playback service to pause
        // TODO: these constants need to be published somewhere in the framework 
        Intent i = new Intent("com.android.music.musicservicecommand");
        i.putExtra("command", "pause");
        mContext.sendBroadcast(i);

        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        try {

            NELivePlayer neMediaPlayer = null;
            if (mUri != null) {
            	neMediaPlayer = NELivePlayer.create();
//            	neMediaPlayer.setHardwareDecoder(mHardwareDecoder);
            }
            mMediaPlayer = neMediaPlayer;
            mMediaPlayer.setBufferStrategy(mBufferStrategy);
            mMediaPlayer.setHardwareDecoder(mHardwareDecoder);
            mMediaPlayer.setOnPreparedListener(mPreparedListener);
            mIsPrepared = false;
            mMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
            mMediaPlayer.setOnCompletionListener(mCompletionListener);
            mMediaPlayer.setOnErrorListener(mErrorListener);
            mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
            mMediaPlayer.setOnInfoListener(mInfoListener);
            mMediaPlayer.setOnSeekCompleteListener(mSeekCompleteListener);
            if (mUri != null) {
                mMediaPlayer.setDataSource(mUri.toString());
                mCurrState = INITIALIZED;
                mNextState = PREPARING;
            }
            mMediaPlayer.setDisplay(mSurfaceHolder);
            mMediaPlayer.prepareAsync();
            mCurrState = PREPARING;
            attachMediaController();
        } catch (IOException ex) {
        	Log.e(TAG, "Unable to open content: " + mUri, ex);
            mErrorListener.onError(mMediaPlayer, -1, 0);
            return;
        } catch (IllegalArgumentException ex) {
        	Log.e(TAG, "Unable to open content: " + mUri, ex);
            mErrorListener.onError(mMediaPlayer, -1, 0);
            return;
        }
    }

    public void setMediaController(NEVideoController controller, int mVideoScalingMode) {
        if (mMediaController != null) {
            mMediaController.hide();
        }
        mMediaController = controller;
        this.mVideoScalingMode = mVideoScalingMode;
        attachMediaController();
    }

    public void setBufferPrompt(View buffer) {
        if (mBuffer != null)
        	mBuffer.setVisibility(View.GONE);
        mBuffer = buffer;
    }

    private void attachMediaController() {
        if (mMediaPlayer != null && mMediaController != null) {
            mMediaController.setMediaPlayer(this);
        }
    }

    OnVideoSizeChangedListener mSizeChangedListener = new OnVideoSizeChangedListener() {
        public void onVideoSizeChanged(NELivePlayer mp, int width, int height,
                int sarNum, int sarDen) {
        	Log.d(TAG, "onVideoSizeChanged: " + width + "x"+ height);
            mVideoWidth = mp.getVideoWidth();
            mVideoHeight = mp.getVideoHeight();
            mPixelSarNum = sarNum;
            mPixelSarDen = sarDen;
            if (mVideoWidth != 0 && mVideoHeight != 0)
                setVideoScalingMode(mVideoScalingMode);
        }
    };

    NELivePlayer.OnPreparedListener mPreparedListener = new NELivePlayer.OnPreparedListener() {
        public void onPrepared(NELivePlayer mp) {
        	Log.d(TAG, "onPrepared");
        	mCurrState = PREPARED;
        	mNextState = STARTED;
        	// briefly show the mediacontroller
        	mIsPrepared = true;

            if (mOnPreparedListener != null) {
                mOnPreparedListener.onPrepared(mMediaPlayer);
            }
            mVideoWidth = mp.getVideoWidth();
            mVideoHeight = mp.getVideoHeight();

            if (mSeekWhenPrepared != 0)
                seekTo(mSeekWhenPrepared);
            if (mVideoWidth != 0 && mVideoHeight != 0) {
            	setVideoScalingMode(mVideoScalingMode);
                if (mSurfaceWidth == mVideoWidth && mSurfaceHeight == mVideoHeight) {
                    if (mNextState == STARTED) {
                    	if (!isPaused()) {
                    		start();
                    	}
                        if (mMediaController != null)
                            mMediaController.show();
                    } else if (!isPlaying() && (mSeekWhenPrepared != 0 || getCurrentPosition() > 0)) {
                        if (mMediaController != null)
                            mMediaController.show();
                    }
                }
            } else if (mNextState == STARTED) {
            	if (!isPaused()) {
            		start();
            	}else {
            		pause();
            	}
            }
            if (isFirstPrepared && mMediaController != null) {
                mMediaController.show();
            }
            isFirstPrepared = false;
        }
    };

    private OnCompletionListener mCompletionListener = new OnCompletionListener() {
        public void onCompletion(NELivePlayer mp) {
        	LogUtil.d(TAG, "onCompletion");
        	mCurrState = PLAYBACKCOMPLETED;
            if (mMediaController != null)
                mMediaController.hide();
            if (mOnCompletionListener != null) {
                mOnCompletionListener.onCompletion(mMediaPlayer);
                return;
            }

            
            if (getWindowToken() != null  && mMediaType.equals("livestream")) {
                // 适配Android6.0
                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(mContext, android.R.style.Theme_Material_Light_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(mContext);
                }

                builder.setTitle("Completed!")
                        .setMessage("播放结束！")
                        .setPositiveButton("OK", null)
                        .setCancelable(false)
                        .show();
            }
        }
    };

    private OnErrorListener mErrorListener = new OnErrorListener() {
        public boolean onError(NELivePlayer mp, int a, int b) {
        	Log.d(TAG, "Error: " + a + "," + b);
        	mCurrState = ERROR;
            if (mMediaController != null) {
                mMediaController.hide();
            }

            /* If an error handler has been supplied, use it and finish. */
            if (mOnErrorListener != null) {
                if (mOnErrorListener.onError(mMediaPlayer, a, b)) {
                    return true;
                }
            }

            /* Otherwise, pop up an error dialog so the user knows that
             * something bad has happened. Only try and pop up the dialog
             * if we're attached to a window. When we're going away and no
             * longer have a window, don't bother showing the user an error.
             */
            if (getWindowToken() != null) {
                new AlertDialog.Builder(mContext)
                        .setTitle("Error")
                        .setMessage("There is an unknown error")
                        .setPositiveButton("Ok",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                    	/* If we get here, there is no onError listener, so
                                         * at least inform them that the video is over.
                                         */
                                        if (mOnCompletionListener != null)
                                            mOnCompletionListener.onCompletion(mMediaPlayer);
                                    }
                                })
                        .setCancelable(false)
                        .show();
            }
            return true;
        }
    };

    private OnBufferingUpdateListener mBufferingUpdateListener = new OnBufferingUpdateListener() {
        public void onBufferingUpdate(NELivePlayer mp, int percent) {
            mCurrentBufferPercentage = percent;
            if (mOnBufferingUpdateListener != null)
                mOnBufferingUpdateListener.onBufferingUpdate(mp, percent);
        }
    };

    private OnInfoListener mInfoListener = new OnInfoListener() {
        @Override
        public boolean onInfo(NELivePlayer mp, int what, int extra) {
        	Log.d(TAG, "onInfo: " + what + ", " + extra);
            if (mOnInfoListener != null) {
                mOnInfoListener.onInfo(mp, what, extra);
            } else if (mMediaPlayer != null) {
                if (what == NEPlayStatusType.NELP_BUFFERING_START) {
                	Log.d(TAG, "onInfo: NELP_BUFFERING_START");
                    if (mBuffer != null)
                    	mBuffer.setVisibility(View.VISIBLE);
                } else if (what == NEPlayStatusType.NELP_BUFFERING_END) {
                	Log.d(TAG, "onInfo: NELP_BUFFERING_END");
                    if (mBuffer != null)
                    	mBuffer.setVisibility(View.GONE);
                } else {
                    Log.d(TAG, "onInfo: NELP_BUFFERING_END");
                }
            }

            return true;
        }
    };

    private OnSeekCompleteListener mSeekCompleteListener = new OnSeekCompleteListener() {
        @Override
        public void onSeekComplete(NELivePlayer mp) {
        	Log.d(TAG, "onSeekComplete");
            if (mOnSeekCompleteListener != null)
                mOnSeekCompleteListener.onSeekComplete(mp);
        }
    };

    /**
     * Register a callback to be invoked when the media file
     * is loaded and ready to go.
     *
     * @param l The callback that will be run
     */
    public void setOnPreparedListener(OnPreparedListener l) {
        mOnPreparedListener = l;
    }

    /**
     * Register a callback to be invoked when the end of a media file
     * has been reached during playback.
     *
     * @param l The callback that will be run
     */
    public void setOnCompletionListener(OnCompletionListener l) {
        mOnCompletionListener = l;
    }

    /**
     * Register a callback to be invoked when an error occurs
     * during playback or setup.  If no listener is specified,
     * or if the listener returned false, VideoView will inform
     * the user of any errors.
     *
     * @param l The callback that will be run
     */
    public void setOnErrorListener(OnErrorListener l) {
        mOnErrorListener = l;
    }

    public void setOnBufferingUpdateListener(OnBufferingUpdateListener l) {
        mOnBufferingUpdateListener = l;
    }

    public void setOnSeekCompleteListener(OnSeekCompleteListener l) {
        mOnSeekCompleteListener = l;
    }

    public void setOnInfoListener(OnInfoListener l) {
        mOnInfoListener = l;
    }

    SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback() {
        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            Log.d("wenwen", "surface changed");
            mSurfaceHolder = holder;
            if (mMediaPlayer != null) {
                mMediaPlayer.setDisplay(mSurfaceHolder);
            }

            mSurfaceWidth = w;
            mSurfaceHeight = h;
            if (mMediaPlayer != null && mIsPrepared && mVideoWidth == w && mVideoHeight == h) {
                if (mSeekWhenPrepared != 0)
                    seekTo(mSeekWhenPrepared);
                if (!isPaused()) {
                	start();
                }
                if (mMediaController != null) {
                    mMediaController.show();
                }
            }
        }

        public void surfaceCreated(SurfaceHolder holder) {
            mSurfaceHolder = holder;

            if (mNextState != RESUME && !isBackground) {
        		openVideo();
        	} 
        	else {
        		if (mHardwareDecoder) {
        			openVideo();
        			isBackground = false; //不在后台
        		}
        		else if (mPauseInBackground) {
        			//mMediaPlayer.setDisplay(mSurfaceHolder);
        			if (!isPaused())
        				start();
        			isBackground = false; //不在后台
        		}
        	}
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            mSurfaceHolder = null;
            if (mMediaController != null) mMediaController.hide();
            if (mMediaPlayer != null) {
            	if(mHardwareDecoder) {
            		mSeekWhenPrepared = mMediaPlayer.getCurrentPosition();
            		if (mMediaPlayer != null) {
                        mMediaPlayer.release();
                        mMediaPlayer = null;
                        mCurrState = IDLE;
                    }
            		isBackground = true;
            	}
            	else if (!mPauseInBackground) {
            		mMediaPlayer.setDisplay(null);
            		isBackground = true;
            	}
            	else {
            		pause();
            		isBackground = true;
            	}
            	
            	mNextState = RESUME;   
            }
        }
    };

    public void MediaControlsVisibity(boolean showing) {
    	if (showing) {
            mMediaController.show();
    	}
    	else {
            mMediaController.hide();
    	}
    }

    @Override
    public void start() {
    	if (mMediaPlayer != null && mIsPrepared) {
            mMediaPlayer.start();
            mCurrState = STARTED;
        }
    	mNextState = STARTED;
    }

    @Override
    public void pause() {
    	if (mMediaPlayer != null && mIsPrepared) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
                mCurrState = PAUSED;
            }
        }
    	mNextState = PAUSED;
    }

    @Override
    public int getDuration() {
    	if (mMediaPlayer != null && mIsPrepared) {
            if (mDuration > 0)
                return (int) mDuration;
            mDuration = mMediaPlayer.getDuration();
            return (int) mDuration;
        }
        
        return -1;
    }
    
    public int getPlayableDuration() {
    	if (mMediaPlayer != null && mIsPrepared) {
    		if (mPlayableDuration > 0)
    			return (int) mPlayableDuration;
    		mPlayableDuration = mMediaPlayer.getPlayableDuration();
    		return (int) mPlayableDuration;
    	}
    	
        return -1;
    }

    @Override
    public int getCurrentPosition() {
    	if (mMediaPlayer != null && mIsPrepared) {
    		return (int)mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    @Override
    public void seekTo(long msec) {
    	if (mMediaPlayer != null && mIsPrepared) {
            mMediaPlayer.seekTo(msec);
            mSeekWhenPrepared = 0;
        } else {
            mSeekWhenPrepared = msec;
        }
    }
    
    public void seekAndChangeUrl(long msec, String path) {
    	mUri = Uri.parse(path);
        //mMediaPlayer.stop();
    	stopPlayback();
        mSeekWhenPrepared = msec;
        openVideo();
        requestLayout();
        invalidate();
    }

    @Override
    public boolean isPlaying() {
    	if (mMediaPlayer != null && mIsPrepared) {
    		return mMediaPlayer.isPlaying();
    	}
    	return false;
    }
    
    public void manualPause(boolean paused) {
    	manualPause = paused;
    }
    
    public boolean isPaused() {
    	//return (mCurrentState == PLAY_STATE_PAUSED) ? true : false;
    	return manualPause;
    }

    @Override
    public int getBufferPercentage() {
        if (mMediaPlayer != null)
            return mCurrentBufferPercentage;
        return 0;
    }

    public boolean canPause() {
        return true;
    }

    public boolean canSeekBackward() {
        return true;
    }

    public boolean canSeekForward() {
        return true;
    }
    
    public void setMediaType(String MediaType) {
    	mMediaType = MediaType;
    }
    
    public String getMediaType() {
    	return mMediaType;
    }
    
    public void setBufferStrategy(int bufferStrategy) {
    	mBufferStrategy = bufferStrategy;
    }
    
    public boolean isHardware() {
    	return mHardwareDecoder;
    }
    
    public void setHardwareDecoder(boolean enabled) {
    	mHardwareDecoder = enabled;
    	if (mHardwareDecoder) {
    		mPauseInBackground = true;
    	}
    }
    
    public boolean isInBackground() {
    	return isBackground;
    }
    
    public void setPauseInBackground(boolean enabled) {
    	mPauseInBackground = enabled;
    	
    	if (mHardwareDecoder) {
    		mPauseInBackground = true;
    	}
    }
    
	public void setMute(boolean mute) {
		if (mMediaPlayer == null)
    		return;
    	mMute = mute;
    	mMediaPlayer.setMute(mMute);
    }
	
    @SuppressLint("SdCardPath")
	public void getSnapshot() {
    	NEMediaInfo mediaInfo = mMediaPlayer.getMediaInfo();
    	LogUtil.d(TAG, "VideoDecoderMode = " + mediaInfo.mVideoDecoderMode);
    	Log.d(TAG, "MediaPlayerName = " + mediaInfo.mMediaPlayerName);
    	Log.d(TAG, "VideoStreamType = " + mediaInfo.mVideoStreamType);
    	Log.d(TAG, "AudioDecoderMode = " + mediaInfo.mAudioDecoderMode);
    	Log.d(TAG, "AudioStreamType = " + mediaInfo.mAudioStreamType);

        if (mediaInfo == null || mediaInfo.mAudioDecoderMode == null) {
            Toast.makeText(mContext, "暂无视频流，无法截图", Toast.LENGTH_SHORT).show();
            return;
        }
    	
    	if (mediaInfo.mVideoDecoderMode.equals("MediaCodec"))
    	{
            LogUtil.d(TAG, "================= hardware unsupport snapshot ==============");
    	}
    	else
    	{
	    	Bitmap bitmap = Bitmap.createBitmap(mVideoWidth, mVideoHeight, Config.ARGB_8888);
	    	//Bitmap bitmap = null;
	    	mMediaPlayer.getSnapshot(bitmap);
            String picName = StorageUtil.getWritePath(
                    mContext,
                    "snap_image_" + StringUtil.get36UUID() + ".jpg",
                    StorageType.TYPE_IMAGE);
            File f = new File(picName);
	    	try {
				f.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
	    	FileOutputStream fOut = null;
			try {
				fOut = new FileOutputStream(f);
				if (picName.substring(picName.lastIndexOf(".") + 1, picName.length()).equals("jpg")) {
					bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
	   			}
	   			else if (picName.substring(picName.lastIndexOf(".") + 1, picName.length()).equals("png")) {
	   				bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
	   			}
				fOut.flush();
				fOut.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}			
			
			Toast.makeText(mContext, "截图成功", Toast.LENGTH_SHORT).show();
    	}
    }
    
    public String getVersion() {
    	if (mMediaPlayer == null)
    		return null;
    	return mMediaPlayer.getVersion();
    }
	
    public void release_resource() {
    	if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
            mCurrState = IDLE;
        }
    }
}
