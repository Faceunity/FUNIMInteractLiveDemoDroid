package com.netease.nim.chatroom.demo.custom;

import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.EGL14;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.faceunity.core.camera.FUCamera;
import com.faceunity.core.camera.FUCameraPreviewData;
import com.faceunity.core.entity.FUCameraConfig;
import com.faceunity.core.entity.FURenderOutputData;
import com.faceunity.core.enumeration.CameraFacingEnum;
import com.faceunity.core.enumeration.CameraTypeEnum;
import com.faceunity.core.enumeration.FUInputTextureEnum;
import com.faceunity.core.enumeration.FUTransformMatrixEnum;
import com.faceunity.core.faceunity.FURenderKit;
import com.faceunity.core.faceunity.OffLineRenderHandler;
import com.faceunity.core.listener.OnFUCameraListener;
import com.faceunity.core.utils.CameraUtils;
import com.faceunity.nama.FURenderer;
import com.faceunity.nama.data.FaceUnityDataFactory;
import com.faceunity.nama.listener.FURendererListener;
import com.netease.nim.chatroom.demo.entertainment.activity.RoomLiveActivity;
import com.netease.nim.chatroom.demo.profile.CSVUtils;
import com.netease.nim.chatroom.demo.profile.Constant;
import com.netease.nim.chatroom.demo.utils.PreferenceUtil;
import com.netease.nimlib.sdk.avchat.model.AVChatImageFormat;
import com.netease.nimlib.sdk.avchat.video.AVChatExternalVideoCapturer;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

/**
 * 用户自定义数据采集及数据处理，接入 faceunity 美颜贴纸
 *
 * @author Richie on 2019.12.20
 */
public class CameraRenderer extends AVChatExternalVideoCapturer implements Camera.PreviewCallback {
    private static final String TAG = "CameraRenderer";
    private static final int DEFAULT_CAMERA_WIDTH = 1280;
    private static final int DEFAULT_CAMERA_HEIGHT = 720;
    private static final int PREVIEW_BUFFER_COUNT = 3;
    private Activity mActivity;
//    private Camera mCamera;
//    private byte[][] mPreviewCallbackBuffer;
    private int mCameraWidth = DEFAULT_CAMERA_WIDTH;
    private int mCameraHeight = DEFAULT_CAMERA_HEIGHT;
    private int mCameraFacing = Camera.CameraInfo.CAMERA_FACING_FRONT;
    private int mCameraOrientation = 270;
    private int mCameraTextureId;
//    private SurfaceTexture mSurfaceTexture;
//    private boolean mIsPreviewing;
//    private Handler mBackgroundHandler;
//    private Handler mPostHandler;
//    private byte[] mReadbackByte;
    private int mSkippedFrames = 5;
    // FU美颜
    private FURenderer mFURenderer;
    private FaceUnityDataFactory mFaceUnityDataFactory;
    private FUCamera fuCamera;
    private OffLineRenderHandler mOffLineRenderHandler;
    private FURendererListener mRenderListener;
    private CSVUtils mCSVUtils;
//    private OffLineRenderHandler.Renderer mOffLineRenderHandlerRenderer;
    private FURenderOutputData outputData;
    private boolean mIsFuBeautyOpen;

    public CameraRenderer(Activity activity, FaceUnityDataFactory faceUnityDataFactory, FURendererListener rendererListener) {
        mActivity = activity;
        mRenderListener = rendererListener;
        mFaceUnityDataFactory = faceUnityDataFactory;
//        FURenderer.getInstance();
//        FURenderKit.getInstance().setReadBackSync(true);
//        FURenderKit.getInstance().createEGLContext();
        mFURenderer = FURenderer.getInstance();
        mFURenderer.setInputTextureType(FUInputTextureEnum.FU_ADM_FLAG_EXTERNAL_OES_TEXTURE);
        mFURenderer.setCameraFacing(CameraFacingEnum.CAMERA_FRONT);
        mFURenderer.setInputOrientation(CameraUtils.INSTANCE.getCameraOrientation(Camera.CameraInfo.CAMERA_FACING_FRONT));
        mFURenderer.setInputTextureMatrix(FUTransformMatrixEnum.CCROT0_FLIPVERTICAL);
        mFURenderer.setInputBufferMatrix(FUTransformMatrixEnum.CCROT0_FLIPVERTICAL);
        mFURenderer.setOutputMatrix(FUTransformMatrixEnum.CCROT0);
        mFURenderer.setMarkFPSEnable(true);
        fuCamera = FUCamera.getInstance();
        mOffLineRenderHandler = OffLineRenderHandler.getInstance();

        String fuBeautyOpen = PreferenceUtil.getString(activity, PreferenceUtil.KEY_FACEUNITY_ISON);
        mIsFuBeautyOpen = "true".equals(fuBeautyOpen);
    }

    public FURenderer getFURenderer(){
        return mFURenderer;
    }

    private volatile byte[] mInputBuffer;
    private Object mInputBufferLock = new Object();


    private byte[] getCurrentBuffer() {
        synchronized (mInputBufferLock) {
            byte[] currentInputBuffer = new byte[mInputBuffer.length];
            System.arraycopy(mInputBuffer, 0, currentInputBuffer, 0, currentInputBuffer.length);
            return currentInputBuffer;
        }
    }


    /**
     * 切换相机
     */
    public void switchCamera() {
        Log.d(TAG, "switchCamera: ");
        fuCamera.switchCamera();
        boolean isFront = mCameraFacing == Camera.CameraInfo.CAMERA_FACING_FRONT;
        mCameraFacing = isFront ? Camera.CameraInfo.CAMERA_FACING_BACK : Camera.CameraInfo.CAMERA_FACING_FRONT;
        mSkippedFrames = 5;
        if (mFURenderer != null) {
            mFURenderer.setCameraFacing(mCameraFacing == Camera.CameraInfo.CAMERA_FACING_FRONT ? CameraFacingEnum.CAMERA_FRONT : CameraFacingEnum.CAMERA_BACK);
            mFURenderer.setInputOrientation(CameraUtils.INSTANCE.getCameraOrientation(mCameraFacing));

            if (mCameraFacing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                mFURenderer.setInputTextureMatrix(FUTransformMatrixEnum.CCROT0_FLIPVERTICAL);
                mFURenderer.setInputBufferMatrix(FUTransformMatrixEnum.CCROT0_FLIPVERTICAL);
                mFURenderer.setOutputMatrix(FUTransformMatrixEnum.CCROT0);
            }else {
                mFURenderer.setInputTextureMatrix(FUTransformMatrixEnum.CCROT0);
                mFURenderer.setInputBufferMatrix(FUTransformMatrixEnum.CCROT0);
                mFURenderer.setOutputMatrix(FUTransformMatrixEnum.CCROT0_FLIPHORIZONTAL);
            }


        }
    }


    private OnFUCameraListener onFUCameraListener = new OnFUCameraListener() {
        @Override
        public void onPreviewFrame(FUCameraPreviewData fuCameraPreviewData) {
            if (fuCameraPreviewData != null) {
                synchronized (mInputBufferLock) {
                    mInputBuffer = new byte[fuCameraPreviewData.getBuffer().length];
                    System.arraycopy(fuCameraPreviewData.getBuffer(), 0, mInputBuffer, 0, mInputBuffer.length);
                }
            }
            mOffLineRenderHandler.requestRender();
        }
    };

    private OffLineRenderHandler.Renderer mOffLineRenderHandlerRenderer = new OffLineRenderHandler.Renderer() {
        @Override
        public void onDrawFrame() {
            if (mInputBuffer == null) {
                return;
            }
            SurfaceTexture surfaceTexture = fuCamera.getSurfaceTexture();
            try {
                surfaceTexture.updateTexImage();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            int orientation = mCameraOrientation;
            long start = System.nanoTime();

            byte[] inputBuffer = getCurrentBuffer();

            if (mIsFuBeautyOpen) {
                if (mFaceUnityDataFactory.getCurrentMakeupStatus() == null) {
                    outputData = mFURenderer.onDrawFrameInputWithReturn(inputBuffer, mCameraTextureId, mCameraWidth, mCameraHeight);
                    Log.e(TAG, "onPreviewFrame: dual " + EGL14.eglGetCurrentContext());
                } else {
                    outputData = mFURenderer.onDrawFrameInputWithReturn(inputBuffer, 0, mCameraWidth, mCameraHeight);
                    Log.e(TAG, "onPreviewFrame: single " + EGL14.eglGetCurrentContext());
                }
                long time = System.nanoTime() - start;
                Log.d("testtest", "frametime = "+time);
                mCSVUtils.writeCsv(null, time);
                if (mSkippedFrames > 0) {
                    mSkippedFrames--;
                } else {
                    if (mPostHandler != null && orientation == mCameraOrientation && outputData != null) {
                        mPostHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                synchronized (mInputBufferLock) {
                                    onByteBufferFrameCaptured(outputData.getImage().getBuffer(), outputData.getImage().getBuffer().length, mCameraWidth, mCameraHeight, mCameraOrientation, 30, AVChatImageFormat.NV21, SystemClock.elapsedRealtime(), mCameraFacing == 1);
                                }
                            }
                        });
                    }
                }
            } else {
                if (mPostHandler != null) {
                    mPostHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            onByteBufferFrameCaptured(inputBuffer, inputBuffer.length, mCameraWidth, mCameraHeight, mCameraOrientation, 30, AVChatImageFormat.NV21, SystemClock.elapsedRealtime(), mCameraFacing == 1);
                        }
                    });
                }
            }

        }
    };


    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {


    }



    private Handler mPostHandler;

    private void startBackgroundThread() {
        HandlerThread handlerThread = new HandlerThread("poster");
        handlerThread.start();
        mPostHandler = new Handler(handlerThread.getLooper());
    }

    private void stopBackgroundThread() {
        mPostHandler.getLooper().quitSafely();
        mPostHandler = null;
    }

    private void initCsvUtil(Context context) {
        mCSVUtils = new CSVUtils(context);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault());
        String dateStrDir = format.format(new Date(System.currentTimeMillis()));
        dateStrDir = dateStrDir.replaceAll("-", "").replaceAll("_", "");
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.getDefault());
        String dateStrFile = df.format(new Date());
        String filePath = Constant.filePath + dateStrDir + File.separator + "excel-" + dateStrFile + ".csv";
        Log.d(TAG, "initLog: CSV file path:" + filePath);
        StringBuilder headerInfo = new StringBuilder();
        headerInfo.append("version：").append(mFURenderer.getVersion()).append(CSVUtils.COMMA)
                .append("机型：").append(android.os.Build.MANUFACTURER).append(android.os.Build.MODEL)
                .append("处理方式：Texture").append(CSVUtils.COMMA);
        mCSVUtils.initHeader(filePath, headerInfo);
    }

    @Override
    public void startCapture(int i, int i1, int i2) {
        startBackgroundThread();
        mOffLineRenderHandler.onResume();
        mOffLineRenderHandler.setRenderer(mOffLineRenderHandlerRenderer);
        mOffLineRenderHandler.queueEvent(new Runnable() {
            @Override
            public void run() {
                if (mFURenderer != null) {
                    mFURenderer.prepareRenderer(mRenderListener);
                }
                initCsvUtil(mActivity);
                mCameraTextureId = GlUtil.createTextureObject(GLES11Ext.GL_TEXTURE_EXTERNAL_OES);
                FUCameraConfig config = new FUCameraConfig();
//                if (android.os.Build.VERSION.SDK_INT > 19) {
//                    config.setCameraType(CameraTypeEnum.CAMERA2);
//                }
                config.setCameraFacing(mCameraFacing == Camera.CameraInfo.CAMERA_FACING_FRONT ? CameraFacingEnum.CAMERA_FRONT : CameraFacingEnum.CAMERA_BACK);
                fuCamera.openCamera(config, mCameraTextureId, onFUCameraListener);
            }
        });
//        .post(new Runnable() {
//            @Override
//            public void run() {
//                // 由于此场景用相机渲染，在打开相机时需要使用FU创建的GL环境
//                FURenderKit.getInstance().createEGLContext();
//                mFaceUnityDataFactory.bindCurrentRenderer();
//                initCsvUtil(mActivity);
//                mCameraTextureId = GlUtil.createTextureObject(GLES11Ext.GL_TEXTURE_EXTERNAL_OES);
//                openCamera(mCameraFacing);
//                startPreview();
//            }
//        });
    }

    @Override
    public void stopCapture() {
        Log.e(TAG, "stopCapture: 销毁资源");
        if (mPostHandler == null) {
            return;
        }
        CountDownLatch countDownLatch = new CountDownLatch(1);
        mOffLineRenderHandler.queueEvent(new Runnable() {
            @Override
            public void run() {
                fuCamera.closeCamera();
                if (mCameraTextureId > 0) {
                    GLES20.glDeleteTextures(1, new int[]{mCameraTextureId}, 0);
                    mCameraTextureId = 0;
                }
                if (mFURenderer != null) {
                    mFURenderer.release();
                    Log.d("testtest", "release called");
                }
                mCSVUtils.close();
                countDownLatch.countDown();
            }
        });
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mOffLineRenderHandler.onPause();
        stopBackgroundThread();
        mSkippedFrames = 5;
    }

    @Override
    public void changeCaptureFormat(int i, int i1, int i2) {

    }

    @Override
    public void dispose() {

    }

}
