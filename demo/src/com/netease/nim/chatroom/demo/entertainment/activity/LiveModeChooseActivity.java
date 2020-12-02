package com.netease.nim.chatroom.demo.entertainment.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.netease.nim.chatroom.demo.R;
import com.netease.nim.chatroom.demo.base.ui.TActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by hzxuwen on 2016/7/12.
 */
public class LiveModeChooseActivity extends TActivity {

    private static final String IS_ROOM_LIVE = "is_room_live";

    @BindView(R.id.video_live_layout)
    RelativeLayout videoLiveLayout;

    @BindView(R.id.audio_live_layout)
    RelativeLayout audioLiveLayout;


    @BindView(R.id.tv_push_hint)
    TextView tvPushHint;

    private boolean isRoomLive = false;

    public static void start(Activity activity, boolean isRoomPush) {
        Intent intent = new Intent(activity, LiveModeChooseActivity.class);
        intent.putExtra(IS_ROOM_LIVE, isRoomPush);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.live_mode_choose_activity);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        toolbar.setLogo(R.drawable.actionbar_logo_white);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.actionbar_white_back_icon);
        toolbar.setNavigationOnClickListener(v -> {
            setResult(RESULT_OK);
            finish();
        });

        ButterKnife.bind(this);
        isRoomLive = getIntent().getBooleanExtra(IS_ROOM_LIVE, false);
        tvPushHint.setText(isRoomLive ? "房间推流" : "主播推流");
    }

    @OnClick({R.id.video_live_layout, R.id.audio_live_layout})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.video_live_layout:
                if (isRoomLive) {
                    RoomLiveActivity.start(LiveModeChooseActivity.this, true);
                } else {
                    LiveActivity.start(LiveModeChooseActivity.this, true, true);
                }
                break;

            case R.id.audio_live_layout:
                if (isRoomLive) {
                    RoomLiveActivity.start(LiveModeChooseActivity.this, false);
                } else {
                    LiveActivity.start(LiveModeChooseActivity.this, false, true);
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        super.onBackPressed();
    }
}
