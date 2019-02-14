package com.netease.nim.chatroom.demo.entertainment.activity;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RelativeLayout;
import com.netease.nim.chatroom.demo.R;
import com.netease.nim.chatroom.demo.base.ui.TActivity;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by hzxuwen on 2016/7/12.
 */
public class LiveModeChooseActivity extends TActivity {

    private static final String TAG = LiveModeChooseActivity.class.getSimpleName();
    @Bind(R.id.video_live_layout)
    RelativeLayout videoLiveLayout;
    @Bind(R.id.audio_live_layout)
    RelativeLayout audioLiveLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.live_mode_choose_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        toolbar.setLogo(R.drawable.actionbar_logo_white);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.actionbar_white_back_icon);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK);
                finish();
            }
        });

        ButterKnife.bind(this);
    }

    @OnClick({R.id.video_live_layout, R.id.audio_live_layout})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.video_live_layout:
                LiveActivity.start(LiveModeChooseActivity.this,true, true);
                break;
            case R.id.audio_live_layout:
                LiveActivity.start(LiveModeChooseActivity.this,false, true);
                break;
        }
    }





    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        super.onBackPressed();
    }
}
