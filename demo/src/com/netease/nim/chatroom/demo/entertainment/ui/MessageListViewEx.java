package com.netease.nim.chatroom.demo.entertainment.ui;

import android.content.Context;
import android.util.AttributeSet;

import com.netease.nim.chatroom.demo.base.util.ScreenUtil;
import com.netease.nim.chatroom.demo.im.ui.listview.MessageListView;

/**
 * Created by hzxuwen on 2016/3/31.
 */
public class MessageListViewEx extends MessageListView {
    private int maxHeight = ScreenUtil.dip2px(80);

    public MessageListViewEx(Context context) {
        super(context);
    }

    public MessageListViewEx(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MessageListViewEx(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (maxHeight > 0){
            int hSize = MeasureSpec.getSize(heightMeasureSpec);
            int hMode = MeasureSpec.getMode(heightMeasureSpec);

            switch (hMode){
                case MeasureSpec.AT_MOST:
                    heightMeasureSpec = MeasureSpec.makeMeasureSpec(Math.min(hSize, maxHeight), MeasureSpec.AT_MOST);
                    break;
                case MeasureSpec.UNSPECIFIED:
                    heightMeasureSpec = MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.UNSPECIFIED);
                    break;
                case MeasureSpec.EXACTLY:
                    heightMeasureSpec = MeasureSpec.makeMeasureSpec(Math.min(hSize, maxHeight), MeasureSpec.EXACTLY);
                    break;
            }
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
