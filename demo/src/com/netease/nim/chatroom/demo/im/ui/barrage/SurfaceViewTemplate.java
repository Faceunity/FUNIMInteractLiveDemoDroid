package com.netease.nim.chatroom.demo.im.ui.barrage;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by huangjun on 2016/5/8.
 */
public abstract class SurfaceViewTemplate extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private SurfaceHolder mHolder;
    private Canvas canvas;
    private Thread thread;
    private boolean isRunning;
    private Object lock = new Object();

    public SurfaceViewTemplate(Context context) {
        this(context, null);
    }

    public SurfaceViewTemplate(Context context, AttributeSet attrs) {
        super(context, attrs);

        mHolder = getHolder();
        mHolder.addCallback(this);

        // 设置画布 背景透明
        setZOrderOnTop(true);
        mHolder.setFormat(PixelFormat.TRANSLUCENT);

        //设置可获得焦点
        setFocusable(true);
        setFocusableInTouchMode(true);

        //设置常亮
        this.setKeepScreenOn(true);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        isRunning = true;
        thread = new Thread(this);
        thread.start(); // 开启线程
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        notifyHasTask(); // 释放锁
        isRunning = false; // 通知关闭线程
    }

    @Override
    public void run() {
        // 不断的进行draw
        while (isRunning) {
            draw();

            // sleep
            try {
                Thread.sleep(getRunTimeInterval());

                // wait
                synchronized (lock) {
                    if (!hasTask()) {
                        lock.wait();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void draw() {
        try {
            // get canvas
            canvas = mHolder.lockCanvas(); // 如果SurfaceView不在前台，这里会阻塞
            if (canvas != null) {
                // clear screen
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

                // do draw task
                onDrawView(canvas);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (canvas != null)
                mHolder.unlockCanvasAndPost(canvas);
        }
    }

    protected abstract void onDrawView(Canvas canvas);

    protected abstract int getRunTimeInterval();

    protected abstract boolean hasTask();

    protected void notifyHasTask() {
        synchronized (lock) {
            lock.notify();
        }
    }
}