package com.netease.nim.chatroom.demo.base.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.netease.nim.chatroom.demo.base.util.ReflectionUtil;
import com.netease.nim.chatroom.demo.base.util.log.LogUtil;


public abstract class TActivity extends AppCompatActivity {

    private boolean destroyed = false;

    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LogUtil.ui("activity: " + getClass().getSimpleName() + " onCreate()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtil.ui("activity: " + getClass().getSimpleName() + " onDestroy()");
        destroyed = true;
    }

    @Override
    public void onBackPressed() {
        invokeFragmentManagerNoteStateNotSaved();
        super.onBackPressed();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LogUtil.ui("activity: " + getClass().getSimpleName() + " onConfigurationChanged()->" + (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT ? "PORTRAIT" : "LANDSCAPE"));
    }

    protected void showKeyboard(boolean isShow) {

        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        if (inputMethodManager == null) {
            return;
        }
        if (isShow) {
            if (getCurrentFocus() == null) {
                inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            } else {
                inputMethodManager.showSoftInput(getCurrentFocus(), 0);
            }
        } else {
            if (getCurrentFocus() != null) {
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }

        }
    }

    /**
     * 延时弹出键盘
     *
     * @param focus ：键盘的焦点项
     */
    protected void showKeyboardDelayed(View focus) {
        final View viewToFocus = focus;
        if (focus != null) {
            focus.requestFocus();
        }
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                if (viewToFocus == null || viewToFocus.isFocused()) {
                    showKeyboard(true);
                }
            }
        }, 200);
    }

    /**
     * 判断页面是否已经被销毁（异步回调时使用）
     */
    protected boolean isDestroyedCompatible() {
        if (Build.VERSION.SDK_INT >= 17) {
            return isDestroyedCompatible17();
        } else {
            return destroyed || super.isFinishing();
        }
    }

    @TargetApi(17)
    private boolean isDestroyedCompatible17() {
        return super.isDestroyed();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void invokeFragmentManagerNoteStateNotSaved() {
        ReflectionUtil.invokeMethod(getSupportFragmentManager(), "noteStateNotSaved", null);
    }


    protected final Handler getHandler() {
        if (handler == null) {
            handler = new Handler(getMainLooper());
        }
        return handler;
    }

    protected <T extends View> T findView(int resId) {
        return (T) (findViewById(resId));
    }

    protected <T extends View> T findViewByTag(ViewGroup parent, String tag) {
        return (T) (parent.findViewWithTag(tag));
    }

    protected <T extends View> T findViewById(ViewGroup parent, int resId) {
        return (T) (parent.findViewById(resId));
    }

}
