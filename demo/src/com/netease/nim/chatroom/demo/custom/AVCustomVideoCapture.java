package com.netease.nim.chatroom.demo.custom;

import com.netease.nimlib.sdk.avchat.video.AVChatExternalVideoCapturer;

/**
 * @author benyq
 * @time 2020/11/11
 * @e-mail 1520063035@qq.com
 * @note
 */
public class AVCustomVideoCapture extends AVChatExternalVideoCapturer {
    @Override
    public void startCapture(int i, int i1, int i2) {

    }

    @Override
    public void stopCapture() throws InterruptedException {

    }

    @Override
    public void changeCaptureFormat(int i, int i1, int i2) {

    }

    @Override
    public void dispose() {

    }

    @Override
    public int onByteBufferFrameCaptured(byte[] bytes, int i, int i1, int i2, int i3, int i4, int i5, long l, boolean b) {
        return super.onByteBufferFrameCaptured(bytes, i, i1, i2, i3, i4, i5, l, b);
    }
}
