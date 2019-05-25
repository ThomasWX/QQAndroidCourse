package dn.android.architecture.image.custom.request;

import android.util.Log;

import java.util.concurrent.BlockingQueue;

import dn.android.architecture.image.custom.loader.Loader;
import dn.android.architecture.image.custom.loader.LoaderManager;

/**
 * 转发器 请求转发线程，不断从请求队列中获取请求
 */
public class RequestDispatcher extends Thread{
    private static final String TAG = "RequestDispatcher";
    // 请求队列
    private BlockingQueue<BitmapRequest> requestBlockingQueue;

    public RequestDispatcher(BlockingQueue<BitmapRequest> requestBlockingQueue) {
        this.requestBlockingQueue = requestBlockingQueue;
    }

    @Override
    public void run() {
        super.run();
        // 判断是否被打断
        while (isInterrupted()){
            try {
                // 阻塞式函数，如果没有请求，会一直阻塞在这里
                BitmapRequest request = requestBlockingQueue.take();
                // 处理请求对象

                // 判断是本地加载还是网络加载
                String schema = parseSchema(request.getImageUrl());
                // 获取加载器
                Loader loader = LoaderManager.getInstance().getLoader(schema);
                loader.loadImage(request);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    private String parseSchema(String imageUrl) {
        if (imageUrl.contains("://")){
            return imageUrl.split("://")[0];
        } else {
            Log.i(TAG, "不支持此类型");
            return null;
        }
    }
}
