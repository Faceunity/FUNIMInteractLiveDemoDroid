package com.netease.nim.chatroom.demo.im.ui.blur;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.lang.ref.WeakReference;

public class BlurBitmapWorkerTask extends AsyncTask<Void, Void, Bitmap> {
    private static final String TAG = "BlurImageView";
    private static final int ANIMATION_DURATION = 1000;

    private final Context context;
    private final WeakReference<ImageView> blurImageViewRef;
    private final WeakReference<ImageView> bgImageViewRef;
    private final String key;
    private final int scaleRatio;
    private final int blurRadius;
    private final boolean memoryCache;
    private final boolean animation;

    private int imageResId; // 本地资源图片
    private Bitmap loadedBitmap; // 已经加载好的位图

    public BlurBitmapWorkerTask(Context context, ImageView blurImageView, ImageView bgImageView, String key,
                                int scaleRatio, int blurRadius, boolean memoryCache, int imageResId, boolean animation) {
        this.context = context;
        this.blurImageViewRef = new WeakReference<>(blurImageView); // Use a WeakReference to ensure the ImageView can be garbage collected
        this.bgImageViewRef = new WeakReference<>(bgImageView); // Use a WeakReference to ensure the ImageView can be garbage collected
        this.key = key;
        this.scaleRatio = scaleRatio;
        this.blurRadius = blurRadius;
        this.memoryCache = memoryCache;
        this.imageResId = imageResId;
        this.loadedBitmap = null;
        this.animation = animation;
    }

    public BlurBitmapWorkerTask(Context context, ImageView blurImageView, ImageView bgImageView, String key,
                                int scaleRatio, int blurRadius, boolean memoryCache, Bitmap loadedBitmap, boolean animation) {
        this(context, blurImageView, bgImageView, key, scaleRatio, blurRadius, memoryCache, -1, animation);
        this.loadedBitmap = loadedBitmap;
    }

    @Override
    protected Bitmap doInBackground(Void... params) {
        // decode
        Log.i(TAG, "decode origin bitmap");
        Bitmap originBitmap = (imageResId > 0 && loadedBitmap == null) ?
                BitmapFactory.decodeResource(context.getResources(), imageResId) : loadedBitmap;
        // blur
        Log.i(TAG, "doing blur image...");
        Bitmap blurBitmap = FastBlurUtil.blur(originBitmap, scaleRatio, blurRadius);
        Log.i(TAG, "blur image done");

        // cache
        if (memoryCache && blurBitmap != null && ImageLoader.getInstance() != null) {
            Log.i(TAG, "cache blur image");
            ImageLoader.getInstance().getMemoryCache().put(key, blurBitmap);
        }

        return blurBitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        // Once complete, see if ImageView is still around and set bitmap.
        if (blurImageViewRef != null && bitmap != null) {
            final ImageView imageView = blurImageViewRef.get();
            if (imageView != null) {
                imageView.setImageBitmap(bitmap);
            }

            if(animation) {
                doAnimation();
            }
        }
    }

    private void doAnimation() {
        if(blurImageViewRef != null && bgImageViewRef != null) {
            final ImageView bgImageView = bgImageViewRef.get();
            final ImageView blurImageView = blurImageViewRef.get();
            ObjectAnimator bgAnimator = ObjectAnimator.ofFloat(bgImageView, View.ALPHA, 1f, 0.0f).setDuration(ANIMATION_DURATION);
            ObjectAnimator blurAnimator = ObjectAnimator.ofFloat(blurImageView, View.ALPHA, 0.0f, 1.0f).setDuration(ANIMATION_DURATION);
            AnimatorSet set = new AnimatorSet();
            set.playTogether(bgAnimator, blurAnimator);
            set.start();
        }
    }

    public static void executeAsyncBlur(Context context, ImageView blurImageView, ImageView bgImageView, String key,
                                        int scaleRatio, int blurRadius, boolean memoryCache, int imageResId, boolean animation) {
        doAsyncTask(new BlurBitmapWorkerTask(context, blurImageView, bgImageView, key, scaleRatio, blurRadius,
                memoryCache, imageResId, animation));
    }

    public static void executeAsyncBlur(Context context, ImageView blurImageView, ImageView bgImageView, String key,
                                        int scaleRatio, int blurRadius, boolean memoryCache, Bitmap loadedBitmap, boolean animation) {
        doAsyncTask(new BlurBitmapWorkerTask(context, blurImageView, bgImageView, key, scaleRatio, blurRadius,
                memoryCache, loadedBitmap, animation));
    }

    private static void doAsyncTask(BlurBitmapWorkerTask task) {
        if (Build.VERSION.SDK_INT >= 11) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            task.execute();
        }
    }
}