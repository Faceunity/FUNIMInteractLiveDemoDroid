package com.netease.nim.chatroom.demo.entertainment.helper;

import android.widget.SeekBar;

/**
 * Created by hzqiujiadi on 2018/6/19.
 * hzqiujiadi ashqalcn@gmail.com
 */
public abstract class SeekBarContext implements SeekBar.OnSeekBarChangeListener {
    private int progress;
    private boolean isTrackingTouch;

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        this.progress = progress;
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        isTrackingTouch = true;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        isTrackingTouch = false;
        onStopTrackingTouch(progress);
    }

    public boolean isTrackingTouch() {
        return isTrackingTouch;
    }

    protected abstract void onStopTrackingTouch(int progress);
}
