package dn.android.architecture.image.custom.cache;

import android.graphics.Bitmap;
import android.util.LruCache;

import dn.android.architecture.image.custom.request.BitmapRequest;

public class MemoryCache implements BitmapCache {
    /**
     * LruCache: 缓存淘汰算法
     * <p>
     * 链表，最新访问/存入的元素提到链表首位，链表满了，删除末尾的数据，末尾的数据表示较少使用的数据。
     */
    private LruCache<String, Bitmap> lruCache;

    public MemoryCache() {
        // 设置手机整个内存的1/8 为缓存大小
        int maxSize = (int) (Runtime.getRuntime().freeMemory() / 1024 / 8);
        lruCache = new LruCache<String, Bitmap>(maxSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                // 计算一张 Bitmap所占用空间的大小的方法: 横向字节数 * 高度
                return value.getRowBytes() * value.getHeight();
            }
        };
    }

    @Override
    public void put(BitmapRequest request, Bitmap bitmap) {
        lruCache.put(request.getImageUriMd5(), bitmap);
    }

    @Override
    public Bitmap get(BitmapRequest request) {
        return lruCache.get(request.getImageUriMd5());
    }

    @Override
    public void remove(BitmapRequest request) {
        lruCache.remove(request.getImageUriMd5());
    }
}
