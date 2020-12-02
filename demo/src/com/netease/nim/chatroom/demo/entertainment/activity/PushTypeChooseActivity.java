package com.netease.nim.chatroom.demo.entertainment.activity;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RelativeLayout;


import com.netease.nim.chatroom.demo.R;
import com.netease.nim.chatroom.demo.base.ui.TActivity;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class PushTypeChooseActivity extends TActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.push_type_choose_activity);

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
    }

    @OnClick({R.id.ll_room_push, R.id.ll_anchor_push})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_room_push:
                LiveModeChooseActivity.start(PushTypeChooseActivity.this, true);
                break;
            case R.id.ll_anchor_push:
                LiveModeChooseActivity.start(PushTypeChooseActivity.this, false);
                break;
        }
    }


    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        super.onBackPressed();
    }
}
