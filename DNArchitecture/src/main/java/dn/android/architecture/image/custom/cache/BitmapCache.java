package dn.android.architecture.image.custom.cache;

import android.graphics.Bitmap;

import dn.android.architecture.image.custom.request.BitmapRequest;

public interface BitmapCache {
    /**
     * 缓存Bitmap
     * @param request
     * @param bitmap
     */
    void put(BitmapRequest request, Bitmap bitmap);

    /**
     * 通过请求取Bitmap
     * @param request
     * @return
     */
    Bitmap get(BitmapRequest request);

    /**
     * 移除缓存
     * @param request
     */
    void remove(BitmapRequest request);
}
