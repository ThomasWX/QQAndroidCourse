package dn.android.architecture.image.custom.loader;

import android.graphics.Bitmap;
import android.widget.ImageView;

import dn.android.architecture.image.custom.cache.BitmapCache;
import dn.android.architecture.image.custom.config.DisplayConfig;
import dn.android.architecture.image.custom.request.BitmapRequest;

public abstract class AbstractLoader implements Loader {
    // 拿到用户自定义的缓存策略
    private BitmapCache bitmapCache = SimpleImageLoader.getInstance().getConfig().getBitmapCache();
    // 拿到显示配置
    private DisplayConfig displayConfig = SimpleImageLoader.getInstance().getConfig().getDisplayConfig();


    @Override
    public void loadImage(BitmapRequest request) {
        // 从缓存中获取Bitmap
        Bitmap bitmap = bitmapCache.get(request);
        if (bitmap == null) {
            // 显示默认加载中图片
            showLoadingImage(request);
            // 开始真正加载图片
            bitmap = onLoad(request);
            // 缓存图片
            cacheBitmap(request, bitmap);
        }

        deliveryToUiThread(request, bitmap);
    }

    protected void deliveryToUiThread(final BitmapRequest request, final Bitmap bitmap) {
        ImageView imageView = request.getImageView();
        if (imageView != null) {
            imageView.post(new Runnable() {
                @Override
                public void run() {
                    updateImageView(request, bitmap);
                }
            });
        }

    }

    protected void updateImageView(BitmapRequest request, Bitmap bitmap) {
        ImageView imageView = request.getImageView();
        // 加载正常
        if (bitmap != null && imageView.getTag().equals(request.getImageUrl())) {
            imageView.setImageBitmap(bitmap);
        }
        // 有可能加载失败
        if (bitmap == null && displayConfig != null && displayConfig.failedImage != -1) {
            imageView.setImageResource(displayConfig.failedImage);
        }

        // 回调
        if (request.imageListener != null) {
            request.imageListener.onComplete(imageView, bitmap, request.getImageUrl());
        }
    }

    protected void cacheBitmap(BitmapRequest request, Bitmap bitmap) {
        if (request != null && bitmap != null) {
            synchronized (AbstractLoader.class) {
                bitmapCache.put(request, bitmap);
            }
        }
    }

    /**
     * 抽象加载策略，因为加载网络图片和本地图片有差异
     *
     * @param request
     * @return
     */
    protected abstract Bitmap onLoad(BitmapRequest request);

    private void showLoadingImage(BitmapRequest request) {
        if (hasLoadingPlaceHolder()) {
            final ImageView imageView = request.getImageView();
            if (imageView != null) {
                // 切换到主线程
                imageView.post(new Runnable() {
                    @Override
                    public void run() {
                        imageView.setImageResource(displayConfig.loadingImage);
                    }
                });
            }
        }
    }

    protected boolean hasLoadingPlaceHolder() {
        return (displayConfig != null && displayConfig.loadingImage > 0);
    }
}
