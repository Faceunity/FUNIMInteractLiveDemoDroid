FUNIMInteractLiveDemo是集成了 FaceUnity 美颜贴纸功能和 **网易云信互动直播** 的 Demo。

本文是 FaceUnity SDK 快速对接云信即时通讯的导读说明，SDK 版本为 **7.2.0**。关于 SDK 的详细说明，请参看 **[FULiveDemoDroid](https://github.com/Faceunity/FULiveDemoDroid/)**。

**存在问题：在进行渲染时，旋转手机时会有一帧画面会旋转，导致画面闪烁。经过网易云信确实，是云信SDK bug**

**退出直播界面，再次进入，切换相机失败。经网易云信确认，切换相机需要在开启直播后才能调用，否则就会出这个问题。**

--------

## 集成方法

### 一、导入 SDK

- 将 faceunity 模块添加到工程中，下面是对库文件的说明。

  - assets/sticker 文件夹下 \*.bundle 是特效贴纸文件。
  - assets/makeup 文件夹下 \*.bundle 是美妆素材文件。
  - com/faceunity/nama/authpack.java 是鉴权证书文件，必须提供有效的证书才能运行 Demo，请联系技术支持获取。

  通过 Maven 依赖最新版 SDK：`implementation 'com.faceunity:nama:7.2.0'`，方便升级，推荐使用。
  
  其中，AAR 包含以下内容：
  
  ```
      +libs                                  
        -nama.jar                        // JNI 接口
      +assets
        +graphic                         // 图形效果道具
          -body_slim.bundle              // 美体道具
          -controller.bundle             // Avatar 道具
          -face_beautification.bundle    // 美颜道具
          -face_makeup.bundle            // 美妆道具
          -fuzzytoonfilter.bundle        // 动漫滤镜道具
          -fxaa.bundle                   // 3D 绘制抗锯齿
          -tongue.bundle                 // 舌头跟踪数据包
        +model                           // 算法能力模型
          -ai_face_processor.bundle      // 人脸识别AI能力模型，需要默认加载
          -ai_face_processor_lite.bundle // 人脸识别AI能力模型，轻量版
          -ai_hand_processor.bundle      // 手势识别AI能力模型
          -ai_human_processor.bundle     // 人体点位AI能力模型
      +jni                               // CNama fuai 库
        +armeabi-v7a
          -libCNamaSDK.so
          -libfuai.so
        +arm64-v8a
          -libCNamaSDK.so
          -libfuai.so
        +x86
          -libCNamaSDK.so
          -libfuai.so
        +x86_64
          -libCNamaSDK.so
          -libfuai.so
  ```
  
  如需指定应用的 so 架构，请修改 app 模块 build.gradle：
  
  ```groovy
  android {
      // ...
      defaultConfig {
          // ...
          ndk {
              abiFilters 'armeabi-v7a', 'arm64-v8a'
          }
      }
  }
  ```
  
  如需剔除不必要的 assets 文件，请修改 app 模块 build.gradle：
  
  ```groovy
  android {
      // ...
      applicationVariants.all { variant ->
          variant.mergeAssetsProvider.configure {
              doLast {
                  delete(fileTree(dir: outputDir, includes: ['model/ai_face_processor_lite.bundle',
                                                             'model/ai_hand_processor.bundle',
                                                             'graphics/controller.bundle',
                                                             'graphics/fuzzytoonfilter.bundle',
                                                             'graphics/fxaa.bundle',
                                                             'graphics/tongue.bundle']))
              }
          }
      }
  }
  ```
  

### 二、使用 SDK

#### 1. 初始化

在 `FURenderer` 类 的  `setup` 方法是对 FaceUnity SDK 全局数据初始化的封装，可以在工作线程调用，仅需初始化一次即可。

在 NimApplication 类中 执行该方法。

#### 2.创建

在 `FURenderer` 类 的  `onSurfaceCreated` 方法是对 FaceUnity SDK 使用前数据初始化的封装。

在RoomLiveActivity 中 初始化 stateObserver对象(实现SimpleAVChatStateObserver接口)，然后注册。

```java
AVChatManager.getInstance().observeAVChatState(stateObserver, register);//注册
```

在第一次回调 onVideoFrameFilter方法时，执行FURenderer.onSurfaceCreated(); 

SimpleAVChatStateObserver接口中会实现很多方法，与美颜有关的接口是 **onVideoFrameFilter 与 onDeviceEvent**。

```
protected AVChatStateObserver stateObserver = new SimpleAVChatStateObserver() {

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
        if (!isNeedEffect || !mIsFuBeautyOpen) {
            return true;
        }

        VideoFrame.Buffer buffer = input.getBuffer();
        int width = buffer.getWidth();
        int height = buffer.getHeight();
        int rotation = input.getRotation();
        int format = buffer.getFormat();
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
            mFURenderer.onDrawFrameSingleInput(i420Byte, width, height, FURenderer.INPUT_FORMAT_I420_BUFFER, readbackByte, width, height);
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
                    outputBuffer.release();
                } catch (IllegalAccessException e) {
                    Log.e(TAG, "onVideoFrameFilter: ", e);
                }
            }
        }
        input.release();
        return true;
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
                }
            }
        }

};
```

#### 3. 图像处理

在 `FURenderer` 类 的  `onDrawFrameXXX` 方法是对 FaceUnity SDK 图像处理的封装，该方法有许多重载方法适用于不同数据类型的需求。

在 RoomLiveActivity 类中，执行美颜操作的代码（代码见上一小节）。

onDrawFrameSingleInput 是单输入，输入图像buffer数组或者纹理Id，输出纹理Id
onDrawFrameDualInput 双输入，输入图像buffer数组与纹理Id，输出纹理Id。性能上，双输入优于单输入

在onDrawFrameSingleInput 与onDrawFrameDualInput 方法内，在执行底层方法之前，都会执行prepareDrawFrame()方法(执行各个特效模块的任务，将美颜参数传给底层)。

在这个demo中，需要将 处理后的数据存入 outputFrames[0] 中，所以调用 下面方法进行处理。

```java
mFURenderer.onDrawFrameSingleInput(i420Byte, width, height, FURenderer.INPUT_FORMAT_I420_BUFFER, readbackByte, width, height);
```

#### 4. 销毁

在 `FURenderer` 类 的  `onSurfaceDestroyed` 方法是对 FaceUnity SDK 退出前数据销毁的封装。

在 RoomLiveActivity 类 releaseVideoEffect 方法中，执行了释放资源操作。

#### 5. 切换相机

调用 `FURenderer` 类 的  `onCameraChanged` 方法，用于重新为 SDK 设置参数。

在 RoomLiveActivity 类中 SimpleAVChatStateObserver 的回调方法  **onDeviceEvent** 执行了该方法。

#### 6. 旋转手机

调用 `FURenderer` 类 的  `onDeviceOrientationChanged` 方法，用于重新为 SDK 设置参数。

使用方法：RoomLiveActivity 中可见

```java
1.implements SensorEventListener
2. findBeautyLayout()    
mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);

3. 

    @Override
    protected void onDestroy() {
   		mSensorManager.unregisterListener(this);
        super.onDestroy();
    }

4. 
//实现接口
@Override
public void onSensorChanged(SensorEvent event) {
    //具体代码见 RoomLiveActivity 类
}

```

**注意：** 上面一系列方法的使用，可以前往对应类查看，参考该代码示例接入即可。

### 三、接口介绍

- IFURenderer 是核心接口，提供了创建、销毁、渲染等接口。使用时通过 FURenderer.Builder 创建合适的 FURenderer 实例即可。
- IModuleManager 是模块管理接口，用于创建和销毁各个功能模块，FURenderer 是实现类。
- IFaceBeautyModule 是美颜模块的接口，用于调整美颜参数。使用时通过 FURenderer 拿到 FaceBeautyModule 实例，调用里面的接口方法即可。
- IStickerModule 是贴纸模块的接口，用于加载贴纸效果。使用时通过 FURenderer 拿到 StickerModule 实例，调用里面的接口方法即可。
- IMakeModule 是美妆模块的接口，用于加载美妆效果。使用时通过 FURenderer 拿到 MakeupModule 实例，调用里面的接口方法即可。
- IBodySlimModule 是美体模块的接口，用于调整美体参数。使用时通过 FURenderer 拿到 BodySlimModule 实例，调用里面的接口方法即可。

**关于 FaceUnity SDK 的更多详细说明，请参看 [FULiveDemoDroid](https://github.com/Faceunity/FULiveDemoDroid/)**。