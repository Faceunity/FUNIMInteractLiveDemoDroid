package com.netease.nim.chatroom.demo.entertainment.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.netease.nim.chatroom.demo.R;
import com.netease.nim.chatroom.demo.base.ui.TActivity;
import com.netease.nim.chatroom.demo.base.util.ScreenUtil;
import com.netease.nim.chatroom.demo.entertainment.constant.NetStateType;
import com.netease.nim.chatroom.demo.entertainment.helper.NetDetectHelpter;
import com.netease.nim.chatroom.demo.im.business.LogoutHelper;
import com.netease.nim.chatroom.demo.permission.MPermission;
import com.netease.nim.chatroom.demo.permission.annotation.OnMPermissionDenied;
import com.netease.nim.chatroom.demo.permission.annotation.OnMPermissionGranted;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nimlib.sdk.auth.AuthServiceObserver;

/**
 * Created by hzxuwen on 2016/3/2.
 */
public class IdentifyActivity extends TActivity implements View.OnClickListener {
    private final int BASIC_PERMISSION_REQUEST_CODE = 100;
    public final static int REQ_CODE = 21;
    private RelativeLayout identifyActivityRl;
    private Button masterBtn;
    private Button audienceBtn;
    private ImageView netDetectStateIv;
    private ImageView netDetectRefreshIv;
    private ProgressBar netDetectLoadingPb;
    private ImageView netDetectDetailInfoIv;
    private TextView netDetectStateContentTv;
    private TextView netDetectTimeTipsTv;
    private NetDetectHelpter.NetDetectResult netDetectResult;
    private NetStateType netGrade;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.identify_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        toolbar.setLogo(R.drawable.actionbar_logo_white);
        setSupportActionBar(toolbar);

        findViews();
        initData();
        registerObservers(true);
        requestBasicPermission(); // 申请APP基本权限
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        registerObservers(false);
    }

    private void registerObservers(boolean register) {
        NIMClient.getService(AuthServiceObserver.class).observeOnlineStatus(userStatusObserver, register);
        NetDetectHelpter.getInstance().observeNetDetectStatus(netDetectObserver, register);
    }

    Observer<StatusCode> userStatusObserver = new Observer<StatusCode>() {
        @Override
        public void onEvent(StatusCode statusCode) {
            if (statusCode.wontAutoLogin()) {
                LogoutHelper.logout(IdentifyActivity.this, true);
            }
        }
    };

    NetDetectHelpter.NetDetectObserver netDetectObserver = new NetDetectHelpter.NetDetectObserver() {
        @Override
        public void onNetDetectResult(NetDetectHelpter.NetDetectResult result) {
            netDetectResult = result;
            updateNetDetectComplete();
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_entertainment, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_logout:
                LogoutHelper.logout(IdentifyActivity.this, false);
                break;
            case R.id.action_about:
                startActivity(new Intent(IdentifyActivity.this, AboutActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void findViews() {
        identifyActivityRl = (RelativeLayout) findViewById(R.id.identify_activity_rl);
        masterBtn = (Button) findViewById(R.id.master_btn);
        audienceBtn = (Button) findViewById(R.id.audience_btn);
        netDetectStateIv = (ImageView) findViewById(R.id.net_detect_state_iv);
        netDetectDetailInfoIv = (ImageView) findViewById(R.id.net_detect_state_content_iv);
        netDetectStateContentTv = (TextView) findViewById(R.id.net_detect_state_content_tv);
        netDetectTimeTipsTv = (TextView) findViewById(R.id.net_detect_time_tips_tv);
        netDetectRefreshIv = (ImageView) findViewById(R.id.net_detect_refresh_iv);
        netDetectLoadingPb = (ProgressBar) findViewById(R.id.net_detect_loading_pb);

        masterBtn.setOnClickListener(this);
        audienceBtn.setOnClickListener(this);
        netDetectDetailInfoIv.setOnClickListener(this);
        netDetectStateIv.setOnClickListener(this);
    }

    private void initData() {
        updateNetDetectData();
    }

    private void updateNetDetectData() {
        if (NetDetectHelpter.getInstance().getNetDetectStatus().intValue() == NetDetectHelpter.STATUS_RUNNING) {
            updateNetDetectLoading();
        } else if (NetDetectHelpter.getInstance().getNetDetectStatus().intValue() == NetDetectHelpter.STATUS_COMPLETE) {
            netDetectResult = NetDetectHelpter.getInstance().getNetDetectResult();
            updateNetDetectComplete();
        } else {
            updateNetDetectLoading();
            NetDetectHelpter.getInstance().startNetDetect();
        }
    }

    private void updateNetDetectLoading() {
        netDetectStateContentTv.setText("网络状况：检测中...");
        netDetectStateIv.setImageResource(R.drawable.icon_net_detect_wifi_enable_0);
        netDetectDetailInfoIv.setVisibility(View.GONE);
        netDetectRefreshIv.setVisibility(View.GONE);
        netDetectLoadingPb.setVisibility(View.VISIBLE);
        netDetectStateIv.setEnabled(false);
        netDetectTimeTipsTv.setVisibility(View.VISIBLE);
        netDetectTimeTipsTv.setText("预计耗时5s");
    }

    private void updateNetDetectComplete() {
        netDetectStateIv.setEnabled(true);
        netDetectRefreshIv.setVisibility(View.VISIBLE);
        netDetectLoadingPb.setVisibility(View.GONE);
        netDetectTimeTipsTv.setVisibility(View.GONE);
        if (netDetectResult.getCode() == 200) {
            netDetectDetailInfoIv.setVisibility(View.VISIBLE);
            netGrade = calculateGrade();
            switch (netGrade) {
                case SMOOTH:
                    netDetectStateContentTv.setText("网络状况：通畅");
                    netDetectStateIv.setImageResource(R.drawable.icon_net_detect_wifi_enable_4);
                    break;
                case COMMON:
                    netDetectStateContentTv.setText("网络状况：一般");
                    netDetectStateIv.setImageResource(R.drawable.icon_net_detect_wifi_enable_3);
                    break;
                case POOR:
                    netDetectStateContentTv.setText("网络状况：较差");
                    netDetectStateIv.setImageResource(R.drawable.icon_net_detect_wifi_enable_2);
                    break;
                case BAD:
                    netDetectStateContentTv.setText("网络状况：极差");
                    netDetectStateIv.setImageResource(R.drawable.icon_net_detect_wifi_enable_1);
                    break;
            }
        } else {
            netDetectDetailInfoIv.setVisibility(View.GONE);
            netDetectStateContentTv.setText("网络状况：检测失败");
            netDetectStateIv.setImageResource(R.drawable.icon_net_detect_wifi_enable_0);
        }
    }

    private NetStateType calculateGrade() {
        double netStateIndex = ((double) netDetectResult.getLoss() / 20) * 0.5 + ((double) netDetectResult.getRttAvg() / 1200) * 0.25 + ((double) netDetectResult.getMdev() / 150) * 0.25;
        if (netStateIndex < 0.2625) {
            return NetStateType.SMOOTH;
        } else if (netStateIndex < 0.55) {
            return NetStateType.COMMON;
        } else if (netStateIndex < 1) {
            return NetStateType.POOR;
        } else {
            return NetStateType.BAD;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.master_btn:
//                UserPreferences.setTeacherIdentify(true);
                startActivityForResult(new Intent(IdentifyActivity.this, LiveModeChooseActivity.class), REQ_CODE);
                break;
            case R.id.audience_btn:
                startActivityForResult(new Intent(IdentifyActivity.this, EnterRoomActivity.class), REQ_CODE);
                break;
            case R.id.net_detect_state_content_iv:
                showNetDetectDetailInfo();
                break;
            case R.id.net_detect_state_iv:
                updateNetDetectLoading();
                NetDetectHelpter.getInstance().startNetDetect();
                break;
            default:
                break;
        }
    }

    private void showNetDetectDetailInfo() {
        RelativeLayout netDetectDetailLayout = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.net_detect_detail_layout, null);
        TextView netDetectGradeTv = (TextView) netDetectDetailLayout.findViewById(R.id.net_detect_state_content_grade_tv);
        TextView netDetectInfoTv = (TextView) netDetectDetailLayout.findViewById(R.id.net_detect_state_content_info_tv);
        ImageView netDetectPopupWindowClose = (ImageView) netDetectDetailLayout.findViewById(R.id.net_detect_popup_window_close);
        switch (netGrade) {
            case SMOOTH:
                netDetectGradeTv.setText("音视频均流畅");
                break;
            case COMMON:
                netDetectGradeTv.setText("视频偶尔卡顿，音频流畅");
                break;
            case POOR:
                netDetectGradeTv.setText("视频卡顿，音频流畅");
                break;
            case BAD:
                netDetectGradeTv.setText("音视频均卡顿");
                break;
            default:
                break;
        }
        String infoText = "丢 包 率：" + netDetectResult.getLoss() + "%"
                + "\r\n平均延时：" + netDetectResult.getRttAvg() + "ms"
                + "\r\n最大延时：" + netDetectResult.getRttMax() + "ms"
                + "\r\n最小延时：" + netDetectResult.getRttMin() + "ms"
                + "\r\n网络抖动：" + netDetectResult.getMdev() + "ms";
        netDetectInfoTv.setText(infoText);
        final PopupWindow popupWindow = new PopupWindow(this);
        popupWindow.setContentView(netDetectDetailLayout);
        netDetectPopupWindowClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (popupWindow != null) {
                    popupWindow.dismiss();
                }
            }
        });
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setWidth(ScreenUtil.dip2px(230));
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.showAtLocation(identifyActivityRl, Gravity.NO_GRAVITY, (ScreenUtil.screenWidth - ScreenUtil.dip2px(230)) / 2, ScreenUtil.dip2px(205));
    }

    /**
     * 基本权限管理
     */
    private void requestBasicPermission() {
        MPermission.with(IdentifyActivity.this)
                .addRequestCode(BASIC_PERMISSION_REQUEST_CODE)
                .permissions(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                .request();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQ_CODE) {
            if (NetDetectHelpter.getInstance().getNetDetectStatus().intValue() == NetDetectHelpter.STATUS_COMPLETE) {
                netDetectTimeTipsTv.setVisibility(View.VISIBLE);
                long time = (System.currentTimeMillis() - NetDetectHelpter.getInstance().getTimeStamp()) / 60000;
                if (time < 2) {
                    netDetectTimeTipsTv.setText("1分钟前检测");
                } else {
                    netDetectTimeTipsTv.setText(time + "分钟前检测");
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        MPermission.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    @OnMPermissionGranted(BASIC_PERMISSION_REQUEST_CODE)
    public void onBasicPermissionSuccess() {
        Toast.makeText(this, "授权成功", Toast.LENGTH_SHORT).show();
    }

    @OnMPermissionDenied(BASIC_PERMISSION_REQUEST_CODE)
    public void onBasicPermissionFailed() {
        Toast.makeText(this, "授权失败", Toast.LENGTH_SHORT).show();
    }
}
