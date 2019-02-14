package com.netease.nim.chatroom.demo.im.session.input;

import java.io.Serializable;

/**
 * 输入布局配置选项
 * Created by hzxuwen on 2016/4/12.
 */
public class InputConfig implements Serializable{

    public InputConfig() {

    }

    public InputConfig(boolean isTextAudioSwitchShow, boolean isMoreFunctionShow, boolean isEmojiButtonShow) {
        this.isTextAudioSwitchShow = isTextAudioSwitchShow;
        this.isMoreFunctionShow = isMoreFunctionShow;
        this.isEmojiButtonShow = isEmojiButtonShow;
    }

    /**
     * 录音选择按钮是否显示，默认显示
     */
    public boolean isTextAudioSwitchShow = true;
    /**
     * 更多按钮是否显示，默认显示
     */
    public boolean isMoreFunctionShow = true;
    /**
     * emoji表情按钮是否显示，默认显示
     */
    public boolean isEmojiButtonShow = true;
}
