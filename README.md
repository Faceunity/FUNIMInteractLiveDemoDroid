# FUNIMInteractLiveDemo（android）

## 概述

FUNIMInteractLiveDemo 是集成了 Faceunity 面部跟踪和虚拟道具功能和[云信互动直播](https://yunxin.163.com/interact-demo) SDK 的 Demo 。
本文是 FaceUnity SDK 快速对接云信互动直播 SDK 的导读说明，关于 FaceUnity SDK 的更多详细说明，请参看 [FULiveDemo](https://github.com/Faceunity/FULiveDemoDroid/tree/dev).

# 快速集成方法
## 添加module
添加faceunity module到工程中，在app dependencies里添加`compile project(':faceunity')`
## 修改代码
### 初始化与监听回调
在LiveActivity的
onCreate方法中添加
```
beautyControlView = (BeautyControlView) findView(R.id.fu_beauty_control);

public boolean onVideoFrameFilter(AVChatVideoFrame frame, boolean maybeDualInput) {
    ...
    if (mFURenderer == null) {
        initFURender();
        mFURenderer.onSurfaceCreated();
        beautyControlView.setOnFUControlListener(mFURenderer);
    }

    byte[] backByte = new byte[frame.width * frame.height * 3 / 2];
    mFURenderer.onDrawFrame(frame.data, frame.width, frame.height, backByte, frame.width, frame.height);
    System.arraycopy(backByte, 0, frame.data, 0, backByte.length);
    frame.dataLen = backByte.length;
    frame.rotation = 90;
    frame.format = ImageFormat.NV21;
}

 private void releaseVideoEffect() {
     if (liveType == LiveType.VIDEO_TYPE) {
       // 释放资源
       if (mFURenderer != null) {
           mFURenderer.onSurfaceDestroyed();
           mFURenderer = null;
        }
  }
```
## 修改默认美颜参数
修改faceunity中faceunity中以下代码
```
private float mFaceBeautyALLBlurLevel = 1.0f;//精准磨皮
private float mFaceBeautyType = 0.0f;//美肤类型
private float mFaceBeautyBlurLevel = 0.7f;//磨皮
private float mFaceBeautyColorLevel = 0.5f;//美白
private float mFaceBeautyRedLevel = 0.5f;//红润
private float mBrightEyesLevel = 1000.7f;//亮眼
private float mBeautyTeethLevel = 1000.7f;//美牙

private float mFaceBeautyFaceShape = 4.0f;//脸型
private float mFaceBeautyEnlargeEye = 0.4f;//大眼
private float mFaceBeautyCheekThin = 0.4f;//瘦脸
private float mFaceBeautyEnlargeEye_old = 0.4f;//大眼
private float mFaceBeautyCheekThin_old = 0.4f;//瘦脸
private float mChinLevel = 0.3f;//下巴
private float mForeheadLevel = 0.3f;//额头
private float mThinNoseLevel = 0.5f;//瘦鼻
private float mMouthShape = 0.4f;//嘴形
```
参数含义与取值范围参考[这里](http://www.faceunity.com/technical/android-beauty.html)，如果使用界面，则需要同时修改界面中的初始值。

------
**互动直播开发指南文档地址：** http://dev.netease.im/docs/product/互动直播/SDK开发集成/Android开发集成