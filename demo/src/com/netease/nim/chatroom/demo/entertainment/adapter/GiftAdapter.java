package com.netease.nim.chatroom.demo.entertainment.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.netease.nim.chatroom.demo.R;
import com.netease.nim.chatroom.demo.entertainment.constant.GiftConstant;
import com.netease.nim.chatroom.demo.entertainment.constant.GiftType;
import com.netease.nim.chatroom.demo.entertainment.model.Gift;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hzxuwen on 2016/3/29.
 */
public class GiftAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    private List<Gift> gifts;

    public GiftAdapter(Context context) {
        super();
        gifts = new ArrayList<Gift>();
        inflater = LayoutInflater.from(context);
        for (int i = 0; i < GiftConstant.images.length; i++) {
            Gift gift = new Gift(GiftType.typeOfValue(i), GiftConstant.titles[i], 1, GiftConstant.images[i]);
            gifts.add(gift);
        }
    }

    public GiftAdapter(List<Gift> gifts, Context context) {
        super();
        this.gifts = gifts;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        if (null != gifts) {
            return gifts.size();
        } else {
            return 0;
        }
    }

    @Override
    public Object getItem(int position) {
        return gifts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.gift_item, null);
            viewHolder = new ViewHolder();
            viewHolder.title = (TextView) convertView.findViewById(R.id.title);
            viewHolder.image = (ImageView) convertView.findViewById(R.id.image);
            viewHolder.count = (TextView) convertView.findViewById(R.id.count);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.title.setText(gifts.get(position).getTitle());
        viewHolder.image.setImageResource(gifts.get(position).getImageId());
        viewHolder.count.setText(gifts.get(position).getCount() + "");
        return convertView;
    }

}

class ViewHolder {
    public TextView title;
    public ImageView image;
    public TextView count;
}
