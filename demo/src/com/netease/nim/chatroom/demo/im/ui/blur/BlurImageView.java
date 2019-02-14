package com.netease.nim.chatroom.demo.im.ui.blur;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.netease.nim.chatroom.demo.R;
import com.nostra13.universalimageloader.cache.disc.impl.ext.LruDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.assist.ViewScaleType;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.core.imageaware.NonViewAware;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.utils.StorageUtils;

import java.io.File;
import java.io.IOException;

/**
 * Created by huangjun on 2016/4/15.
 *
 * 高斯模糊控件
 * 支持场景1：本地图片资源进行高斯模糊（可以先显示原图，异步高斯模糊完成后，再显示blur图）
 * 支持场景2：URL图片进行高斯模糊（可以先显示一个背景图，异步下载并做完高斯模糊后，再显示blur图）
 */
public class BlurImageView extends RelativeLayout {

    private final String TAG = "BlurImageView";

    private final static int DEFAULT_BLUR_FACTOR = 6;
    private final static int DEFAULT_SCALE_RATIO = 8;

    public static boolean UILInit = false; // UIL是否已经在初始化完成
    private Context context;

    private int blurRadius; // 高斯模糊半径(>=1)：影响blur时间因素1
    private int scaleRatio; // 图片压缩比例（>=1)：影响blur时间因素2
    private boolean memoryCache; // 是否使用ImageLoader在内存缓存blur后的图片
    private int localImageResId; // 需要blur的本地图片资源
    private int backgroundImageResId; // 背景本地资源（先显示背景图，再显示blur图，支持渐变动画切换）
    private boolean preShowLocalOriginImage; // 是否先显示本地原图
    private boolean enableProgress; // 图片来源为URL时，下载过程中是否要有进度条
    private boolean transitionAnimation; // 是否要有背景图和blur图的渐变动画切换

    private ImageView backgroundImageView;
    private ImageView blurImageView;
    private ImageLoader imageLoader;
    private DisplayImageOptions displayImageOptions;
    private LoadingCircleProgressView loadingCircleProgressView;

    public BlurImageView(Context context) {
        this(context, null);
    }

    public BlurImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * *************************** 初始化 ***************************
     */

    public BlurImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        this.context = context.getApplicationContext();

        // read attribute
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.BlurImageView, defStyleAttr, 0);
        blurRadius = ta.getInt(R.styleable.BlurImageView_blur_radius, DEFAULT_BLUR_FACTOR);
        scaleRatio = ta.getInt(R.styleable.BlurImageView_scale_ratio, DEFAULT_SCALE_RATIO);
        memoryCache = ta.getBoolean(R.styleable.BlurImageView_memory_cache, true);
        localImageResId = ta.getResourceId(R.styleable.BlurImageView_image_res, 0);
        backgroundImageResId = ta.getResourceId(R.styleable.BlurImageView_background_image_res, 0);
        preShowLocalOriginImage = ta.getBoolean(R.styleable.BlurImageView_pre_show_local_origin_image, true);
        enableProgress = ta.getBoolean(R.styleable.BlurImageView_enable_progress, true);
        transitionAnimation = ta.getBoolean(R.styleable.BlurImageView_transition_animation, true);
        ta.recycle();

        // init
        initUIL(context);
        initImageView();

        // local image
        if (localImageResId > 0) {
            initLocalImage();
        } else {
            setBackgroundImageView(backgroundImageResId);
            if(enableProgress) {
                initProgressView();
            }
            initUILDisplayImageOptions();
        }
    }

    private void initImageView() {
        backgroundImageView = new ImageView(context);
        backgroundImageView.setLayoutParams(
                new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        backgroundImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        addView(backgroundImageView);

        blurImageView = new ImageView(context);
        blurImageView.setLayoutParams(
                new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        blurImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        addView(blurImageView);
    }

    private void formatParam() {
        if(blurRadius <= 0) {
            blurRadius = DEFAULT_BLUR_FACTOR;
        }

        if(scaleRatio <= 0) {
            scaleRatio = DEFAULT_SCALE_RATIO;
        }
    }

    /**
     * If you disable progress, then it won't show a loading progress view when you're loading image.
     * Default the progress view is enabled.
     */
    public void disableProgress() {
        this.enableProgress = false;
    }

    public void setProgressBarBgColor(int bgColor) {
        this.loadingCircleProgressView.setProgressBgColor(bgColor);
    }

    public void setProgressBarColor(int color) {
        this.loadingCircleProgressView.setProgressColor(color);
    }

    /**
     * *************************** 高斯模糊参数设置 ***************************
     */

    public void setBlurRadius(int blurRadius) {
        this.blurRadius = blurRadius;
        formatParam();
    }

    public void setScaleRatio(int scaleRatio) {
        this.scaleRatio = scaleRatio;
        formatParam();
    }

    /**
     * *************************** 场景1：资源图片高斯模糊 ***************************
     */

    private void initLocalImage() {
        formatParam();
        blurLocalImage();
    }

    public void setBlurImageRes(int blurImageResId) {
        setBlurImageRes(blurImageResId, blurRadius, scaleRatio);
    }

    public void setBlurImageRes(int blurImageResId, int blurRadius, int scaleRatio) {
        this.localImageResId = blurImageResId;
        this.blurRadius = blurRadius;
        this.scaleRatio = scaleRatio;
        formatParam();
        blurLocalImage();
    }

    private void blurLocalImage() {
        final String key = FastBlurUtil.generateBlurImageKey(localImageResId, blurRadius, scaleRatio);

        // cache
        Bitmap blurBitmap;
        if((blurBitmap = ImageLoader.getInstance().getMemoryCache().get(key)) != null) {
            blurImageView.setImageBitmap(blurBitmap);
            return;
        }

        // show origin image
        if(preShowLocalOriginImage) {
            backgroundImageView.setImageResource(localImageResId);
            backgroundImageView.setAlpha(1f);
        }

        // async blur & cache and show
        BlurBitmapWorkerTask.executeAsyncBlur(context, blurImageView, backgroundImageView, key, scaleRatio,
                blurRadius, memoryCache, localImageResId, transitionAnimation);
    }

    /**
     * *************************** 场景2：URL图片高斯模糊 ***************************
     */

    public void setBackgroundImageView(int backgroundResId) {
        if(backgroundResId <= 0) {
            return;
        }

        backgroundImageView.setImageResource(backgroundResId);
        backgroundImageView.setAlpha(1f);
    }

    public void setBlurImageURL(String blurImageUrl) {
        cancelImageRequestForSafety();
        blurURLImage(blurImageUrl);
    }

    private void blurURLImage(String blurImageUrl) {
        final String key = FastBlurUtil.generateBlurImageKey(blurImageUrl, blurRadius, scaleRatio);

        // hit blur cache
        Bitmap blurBitmap;
        if((blurBitmap = ImageLoader.getInstance().getMemoryCache().get(key)) != null) {
            blurImageView.setImageBitmap(blurBitmap);
            Log.i(TAG, "hit blur cache, key=" + key);
            return;
        }

        if(backgroundImageResId > 0) {
            backgroundImageView.setAlpha(1f);
        }

        // load from disk or network
        ImageLoader.getInstance().displayImage(blurImageUrl, new NonViewAware(new ImageSize(blurImageView.getWidth(), blurImageView.getHeight()), ViewScaleType.CROP),
                displayImageOptions, new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        Log.i(TAG, "loading image completed");
                        // async blur & cache and show
                        BlurBitmapWorkerTask.executeAsyncBlur(context, blurImageView, backgroundImageView, key,
                                scaleRatio, blurRadius, memoryCache, loadedImage, transitionAnimation);
                    }
                }, new ImageLoadingProgressListener() {
                    @Override
                    public void onProgressUpdate(String imageUri, View view, int current, int total) {
                        if(!enableProgress) {
                            return;
                        }

                        if (current < total) {
                            float p = (float) current / total;
                            Log.i(TAG, "download image progress:" + String.format("%.1f", p * 100) + "%");
                            loadingCircleProgressView.setVisibility(VISIBLE);
                            loadingCircleProgressView.setCurrentProgressRatio(p);
                        } else {
                            loadingCircleProgressView.setVisibility(GONE);
                            Log.i(TAG, "download image progress 100%");
                        }
                    }
                });
    }

    /**
     * *************************** ImageLoader ***************************
     */

    private void initUIL(final Context context) {
        if(!UILInit) {
            int MAX_CACHE_MEMORY_SIZE = (int) (Runtime.getRuntime().maxMemory() / 8);
            File cacheDir = StorageUtils.getOwnCacheDirectory(context, context.getPackageName() + "/cache/image/");

            try {
                ImageLoaderConfiguration config = new ImageLoaderConfiguration
                        .Builder(context)
                        .threadPoolSize(3) // 线程池内加载的数量
                        .threadPriority(Thread.NORM_PRIORITY - 2) // 降低线程的优先级，减小对UI主线程的影响
                        .denyCacheImageMultipleSizesInMemory()
                        .memoryCache(new LruMemoryCache(MAX_CACHE_MEMORY_SIZE))
                        .discCache(new LruDiskCache(cacheDir, new Md5FileNameGenerator(), 0))
                        .defaultDisplayImageOptions(DisplayImageOptions.createSimple())
                        .tasksProcessingOrder(QueueProcessingType.LIFO)
                        .imageDownloader(new BaseImageDownloader(context, 5 * 1000, 30 * 1000)) // connectTimeout (5 s), readTimeout (30 s)超时时间
                        .writeDebugLogs()
                        .build();
                ImageLoader.getInstance().init(config);

                UILInit = true;
            } catch (IOException e) {
                e.printStackTrace();
                Log.i(TAG, "init UIL error" + e.getMessage());
            }
        }

        this.imageLoader = ImageLoader.getInstance();
    }

    private void initUILDisplayImageOptions() {
        // 磁盘缓存URL对应的图片，内存不缓存，只缓存blur后的图片。
        displayImageOptions = new DisplayImageOptions.Builder()
                .cacheOnDisk(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
    }

    public void cancelImageRequestForSafety() {
        imageLoader.cancelDisplayTask(blurImageView);
    }

    public void clear() {
        cancelImageRequestForSafety();
        blurImageView.setImageBitmap(null);
    }

    /**
     * *************************** URL图片下载圆形进度条 ***************************
     */

    private void initProgressView() {
        loadingCircleProgressView = new LoadingCircleProgressView(context);
        LayoutParams progressBarLayoutParams =
                new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        progressBarLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        loadingCircleProgressView.setLayoutParams(progressBarLayoutParams);
        loadingCircleProgressView.setVisibility(GONE);

        addView(loadingCircleProgressView);
    }
}
