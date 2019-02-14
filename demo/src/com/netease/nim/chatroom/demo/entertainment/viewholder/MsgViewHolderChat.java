package com.netease.nim.chatroom.demo.entertainment.viewholder;

import android.text.method.LinkMovementMethod;
import android.text.style.ImageSpan;
import android.widget.TextView;

import com.netease.nim.chatroom.demo.DemoCache;
import com.netease.nim.chatroom.demo.R;
import com.netease.nim.chatroom.demo.base.ui.TViewHolder;
import com.netease.nim.chatroom.demo.im.session.emoji.MoonUtil;
import com.netease.nimlib.sdk.chatroom.constant.MemberType;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomMessage;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;

/**
 * Created by hzxuwen on 2016/3/24.
 */
public class MsgViewHolderChat extends TViewHolder {
    private ChatRoomMessage message;

    private TextView bodyText;
    private TextView nameText;

    @Override
    protected int getResId() {
        return R.layout.message_item_text;
    }

    @Override
    protected void inflate() {
        bodyText = findView(R.id.nim_message_item_text_body);
        nameText = findView(R.id.message_item_name);
    }

    @Override
    protected void refresh(Object item) {
        message = (ChatRoomMessage) item;
        setNameTextView();
        MoonUtil.identifyFaceExpression(DemoCache.getContext(), bodyText, message.getContent(), ImageSpan.ALIGN_BOTTOM);
        bodyText.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public void setNameTextView() {
        if (message.getMsgType() != MsgTypeEnum.notification) {
            // 聊天室中显示姓名
            if (message.getChatRoomMessageExtension() != null) {
                nameText.setText(message.getChatRoomMessageExtension().getSenderNick());
            } else {
                nameText.setText(DemoCache.getUserInfo() == null ? DemoCache.getAccount() : DemoCache.getUserInfo().getName());
            }

            if(message.getRemoteExtension() != null && message.getRemoteExtension().containsKey("type")) {
                MemberType type = MemberType.typeOfValue((Integer) message.getRemoteExtension().get("type"));
                nameText.setTextColor(context.getResources().getColor(type == MemberType.CREATOR ?
                        R.color.color_yellow_FAEC55 : R.color.color_green_C2FF9A));
            }
        }
    }
}
