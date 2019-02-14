package com.netease.nim.chatroom.demo.im.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;

import com.netease.nim.chatroom.demo.DemoCache;
import com.netease.nim.chatroom.demo.R;
import com.netease.nim.chatroom.demo.im.session.image.ImageLoaderKit;
import com.netease.nimlib.sdk.nos.model.NosThumbParam;
import com.netease.nimlib.sdk.nos.util.NosThumbImageUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.assist.ViewScaleType;
import com.nostra13.universalimageloader.core.imageaware.NonViewAware;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public class AvatarImageView extends CircleImageView {

    public static final int DEFAULT_THUMB_SIZE = (int) DemoCache.getContext().getResources().getDimension(R.dimen.avatar_max_size);

    private DisplayImageOptions options;

    private final DisplayImageOptions createImageOptions() {
        int defaultIcon = R.drawable.avatar_def;
        return new DisplayImageOptions.Builder()
                .showImageOnLoading(defaultIcon)
                .showImageOnFail(defaultIcon)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
    }

    public AvatarImageView(Context context) {
        super(context);
    }

    public AvatarImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AvatarImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ImageViewEx, defStyle, 0);
        a.recycle();

        this.options = createImageOptions();
    }

    public void loadAvatarByUrl(String url) {
        loadAvatar(url, DEFAULT_THUMB_SIZE);
    }

    /**
     * 加载图片
     */
    public void loadAvatar(final String url, final int thumbSize) {
        // 先显示默认头像
        setImageResource(R.drawable.avatar_def);

        // 判断是否需要ImageLoader加载
        boolean needLoad = ImageLoaderKit.isImageUriValid(url);

        // ImageLoader异步加载
        if (needLoad) {
            setTag(url); // 解决ViewHolder复用问题
            /**
             * 若使用网易云信云存储，这里可以设置下载图片的压缩尺寸，生成下载URL
             * 如果图片来源是非网易云信云存储，请不要使用NosThumbImageUtil
             */
            final String thumbUrl = thumbSize > 0 ? NosThumbImageUtil.makeImageThumbUrl(url,
                    NosThumbParam.ThumbType.Crop, thumbSize, thumbSize) : url;

            // 异步从cache or NOS加载图片
            ImageLoader.getInstance().displayImage(thumbUrl, new NonViewAware(new ImageSize(thumbSize, thumbSize),
                    ViewScaleType.CROP), options, new SimpleImageLoadingListener() {
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    if (getTag() != null && getTag().equals(url)) {
                        setImageBitmap(loadedImage);
                    }
                }
            });
        } else {
            setTag(null);
        }
    }
}
