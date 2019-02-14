package com.netease.nim.chatroom.demo.im.ui.barrage;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.netease.nim.chatroom.demo.base.util.ScreenUtil;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

/**
 * 弹幕容器
 */
public class BarrageView extends RelativeLayout {

    private static final String TAG = "BarrageView";

    private static final int DEFAULT_RANDOM_COLOR_NUM = 10;

    private static final boolean OUTPUT_LOG = true;

    private Random random;

    // 配置管理
    private BarrageConfig config;

    // 轨道管理
    private Set<Integer> linesUnavailable = new HashSet<>();
    private Queue<SoftReference<TextView>> textViewCache = new LinkedList<>();
    private int lineCount;
    private int lineHeight;

    // 字幕管理
    private Queue<String> textCache = new LinkedList<>();

    public BarrageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BarrageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void init(final BarrageConfig config) {
        this.random = new Random();
        int totalLineHeight = getBottom() - getTop() - getPaddingTop() - getPaddingBottom();
        this.lineHeight = ScreenUtil.sp2px(config.getMaxTextSizeSp());
        this.lineCount = totalLineHeight / lineHeight;

        // random colors
        if (config.getColors() == null || config.getColors().isEmpty()) {
            List<Integer> colors = new ArrayList<>(DEFAULT_RANDOM_COLOR_NUM);
            for (int i = 0; i < DEFAULT_RANDOM_COLOR_NUM; i++) {
                colors.add(Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256)));
            }
            config.setColors(colors);
        }
        this.config = config;

        log("barrage init, lineHeight=" + lineHeight + ", lineCount=" + lineCount);
    }

    public void addTextBarrage(String text) {
        textCache.add(text);

        checkAndRunTextBarrage();
    }

    private void checkAndRunTextBarrage() {
        if (textCache.isEmpty()) {
            return;
        }

        // line
        int availableLine = getAvailableLine();
        if (availableLine < 0) {
            return; // pend
        }

        // find cached text view
        TextView textView = null;
        SoftReference<TextView> softReference;
        while (true) {
            softReference = textViewCache.poll();
            if (softReference == null || (textView = softReference.get()) != null) {
                break; // queue is empty or find available cached object
            }
        }

        // create text view
        if (textView == null) {
            textView = buildTextView(textCache.poll(), availableLine);
        } else {
            textView = reuseTextView(textView, textCache.poll(), availableLine);
        }

        // run
        buildTranslationAnimator(textView, availableLine).start();
    }

    private TextView buildTextView(String text, int line) {
        if (TextUtils.isEmpty(text)) {
            return null;
        }

        // text content
        TextView textView = new TextView(getContext());
        textView.setText(text);
        textView.setSingleLine();

        // text size
        int textSizeSp = config.getMinTextSizeSp() + random.nextInt(config.getMaxTextSizeSp() - config.getMinTextSizeSp() + 1);
        textView.setTextSize(textSizeSp);

        // text color
        int textColor;
        if (config.getColors() != null && !config.getColors().isEmpty()) {
            textColor = config.getColors().get(random.nextInt(config.getColors().size()));
        } else {
            textColor = Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256));
        }
        textView.setTextColor(textColor);

        // layout param
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        params.topMargin = line * lineHeight;
        textView.setLayoutParams(params);

        // add view to container
        addView(textView);

        // log
        StringBuilder log = new StringBuilder();
        log.append("build text barrage")
                .append(", line=").append(line)
                .append(", text=").append(text)
                .append(", size=").append(textSizeSp)
                .append(", color=").append(textColor);
        log(log.toString());

        return textView;
    }

    private TextView reuseTextView(TextView textView, String text, int line) {
        textView.setText(text);
        LayoutParams params = (RelativeLayout.LayoutParams) textView.getLayoutParams();
        params.topMargin = line * lineHeight;
        textView.setLayoutParams(params);

        // re add view to container
        addView(textView);

        // log
        log("reuse text barrage");

        return textView;
    }

    private int getAvailableLine() {
        int line = -1;
        for (int i = 0; i < lineCount; i++) {
            if (!linesUnavailable.contains(i)) {
                line = i;
                break;
            }
        }

        if (line >= 0 && line < lineCount) {
            linesUnavailable.add(line); // 占用
        }

        return line;
    }

    private ObjectAnimator buildTranslationAnimator(final TextView target, final int line) {
        final int duration = config.getDuration() + random.nextInt() % 500;
        final int textLength = (int) target.getPaint().measureText(target.getText().toString());
        final int freeLineDuration = (int) ((textLength + 50.0f) / (1.0f * getWidth() / duration));

        ObjectAnimator animator = ObjectAnimator.ofFloat(target, "translationX", getWidth(), -textLength).setDuration(duration);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                log("text barrage run, line=" + line + ", duration=" + duration + ", freeLineDuration=" + freeLineDuration);

                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        onLineAvailable(line);
                    }
                }, freeLineDuration);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                onTextBarrageDone(target, line);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                onTextBarrageDone(target, line);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        return animator;
    }

    private void onLineAvailable(final int line) {
        log("free line, line=" + line);

        linesUnavailable.remove(line);
        checkAndRunTextBarrage();
    }

    private void onTextBarrageDone(final TextView view, final int line) {
        log("text barrage completed, line=" + line);

        // should remove strong reference
        removeView(view);

        // add to cache for reuse
        textViewCache.add(new SoftReference<>(view));

        checkAndRunTextBarrage();
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (View.GONE == visibility) {

        } else {

        }
    }

    private void log(String message) {
        if (OUTPUT_LOG) {
            Log.i(TAG, message);
        }
    }
}
