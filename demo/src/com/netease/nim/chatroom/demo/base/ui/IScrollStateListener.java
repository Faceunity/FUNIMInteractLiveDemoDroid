package com.netease.nim.chatroom.demo.base.ui;

public interface IScrollStateListener {

    /**
     * move to scrap heap
     */
    public void reclaim();


    /**
     * on idle
     */
    public void onImmutable();
}
