package dn.android.architecture.image.custom.loader;

import dn.android.architecture.image.custom.request.BitmapRequest;

public interface Loader {
    /**
     * 加载图片
     * @param request
     */
    void loadImage(BitmapRequest request);
}
