package dn.android.architecture.image.custom.loader;

import android.graphics.Bitmap;
import android.widget.ImageView;

import dn.android.architecture.image.custom.config.DisplayConfig;
import dn.android.architecture.image.custom.config.LoaderConfig;
import dn.android.architecture.image.custom.request.BitmapRequest;
import dn.android.architecture.image.custom.request.RequestQueue;

public class SimpleImageLoader {
    // 配置
    private LoaderConfig config;
    // 请求队列
    private RequestQueue requestQueue;

    private SimpleImageLoader() {
    }

    private SimpleImageLoader(LoaderConfig config) {
        this.config = config;
        this.requestQueue = new RequestQueue(config.getThreadCount());
        // 开启请求队列
        requestQueue.start();
    }

    private static volatile SimpleImageLoader instance;

    public static SimpleImageLoader getInstance(LoaderConfig config) {
        if (instance == null) {
            synchronized (SimpleImageLoader.class) {
                if (instance == null) {
                    instance = new SimpleImageLoader(config);
                }
            }
        }
        return instance;
    }

    public static SimpleImageLoader getInstance() {
        if (instance == null) {
            throw new UnsupportedOperationException("The SimpleImageLoader not init.");
        }
        return instance;
    }


    public void displayImage(ImageView imageView, String uri) {
        displayImage(imageView, uri, null, null);
    }

    public void displayImage(ImageView imageView, String uri, DisplayConfig config, ImageListener listener) {
        // 实例化一个请求
        BitmapRequest request = new BitmapRequest(imageView, uri, config, listener);
        // 添加到队列
        requestQueue.addRequest(request);
    }

    public static interface ImageListener {
        void onComplete(ImageView imageView, Bitmap bitmap, String uri);
    }

    /**
     * 全局配置
     *
     * @return
     */
    public LoaderConfig getConfig() {
        return config;
    }
}
