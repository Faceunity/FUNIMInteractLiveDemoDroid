package com.netease.nim.chatroom.demo.entertainment.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.netease.nim.chatroom.demo.R;
import com.netease.nim.chatroom.demo.entertainment.constant.MicStateEnum;
import com.netease.nim.chatroom.demo.entertainment.model.InteractionMember;
import com.netease.nim.chatroom.demo.entertainment.viewholder.InteractionMemberViewHolder;
import com.netease.nimlib.sdk.avchat.constant.AVChatType;

import java.util.List;

/**
 * Created by hzxuwen on 2016/7/15.
 */
public class InteractionAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    private List<InteractionMember> memberList;
    private MemberLinkListener memberLinkListener;

    public interface MemberLinkListener {
        void onClick(InteractionMember member);
    }

    public InteractionAdapter(List<InteractionMember> memberList, Context context, MemberLinkListener memberLinkListener) {
        super();
        inflater = LayoutInflater.from(context);
        this.memberList = memberList;
        this.memberLinkListener = memberLinkListener;
    }

    @Override
    public int getCount() {
        if (memberList != null) {
            return memberList.size();
        } else {
            return 0;
        }
    }

    @Override
    public Object getItem(int position) {
        return memberList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        InteractionMemberViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.interaction_member_item, null);
            viewHolder = new InteractionMemberViewHolder();
            viewHolder.memberAvatar = (ImageView) convertView.findViewById(R.id.member_avatar);
            viewHolder.memberName = (TextView) convertView.findViewById(R.id.member_name);
            viewHolder.memberLinkBtn = (Button) convertView.findViewById(R.id.member_link_btn);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (InteractionMemberViewHolder) convertView.getTag();
        }

        if (memberList.get(position).isSelected()) {
            viewHolder.memberName.setVisibility(View.GONE);
            viewHolder.memberLinkBtn.setVisibility(View.VISIBLE);
            if (memberList.get(position).getMicStateEnum() == MicStateEnum.CONNECTING) {
                viewHolder.memberLinkBtn.setText("正在连接中");
                viewHolder.memberLinkBtn.setEnabled(false);
            } else {
                viewHolder.memberLinkBtn.setEnabled(true);
                if (memberList.get(position).getAvChatType() == AVChatType.VIDEO) {
                    viewHolder.memberLinkBtn.setText(R.string.video_link);
                } else {
                    viewHolder.memberLinkBtn.setText(R.string.audio_link);
                }
                viewHolder.memberLinkBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        memberLinkListener.onClick(memberList.get(position));
                    }
                });
            }
        } else {
            viewHolder.memberName.setVisibility(View.VISIBLE);
            viewHolder.memberName.setText(memberList.get(position).getName());
            viewHolder.memberLinkBtn.setVisibility(View.GONE);
        }
        return convertView;
    }
}
