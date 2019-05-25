package dn.android.architecture.image.custom.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import com.jakewharton.disklrucache.DiskLruCache;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import dn.android.architecture.image.custom.request.BitmapRequest;

public class DiskCache implements BitmapCache {
    private static DiskCache diskCache;
    // 缓存路径
    private String cacheDir = "image";
    // MB
    private static final int MB = 1024 * 1024;
    // JackWharton 的杰作
    private DiskLruCache diskLruCache;

    private DiskCache(Context context) {
        initDiskCache(context);
    }

    private void initDiskCache(Context context) {
        // 得到缓存的目录
        File dir = getDiskCache(cacheDir, context);
        if (!dir.exists()) {
            dir.mkdir();
        }
        try {
            // 最后一个参数指定缓存容量
            diskLruCache = DiskLruCache.open(dir, 1, 1, 50 * MB);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File getDiskCache(String dir, Context context) {
        String cachePath;
        // 判断外部卡是否存在，如果不存在就缓存到data data目录下
        // 当前为了测试，直接设置为外置目录。
        return new File(Environment.getExternalStorageDirectory(), dir);
    }

    @Override
    public void put(BitmapRequest request, Bitmap bitmap) {
        DiskLruCache.Editor editor = null;
        OutputStream outputStream = null;

        try {
            // KEY 为什么要用MD5 ? 是因为路径必须是合法字符
            editor = diskLruCache.edit(request.getImageUriMd5());
            outputStream = editor.newOutputStream(0); // 默认传0
            // bitmap -> outputStream
            if (persistBitmapToDisk(bitmap, outputStream)){
                // 如果转换成功就提交
                editor.commit();
            } else {
                // 如果转换失败就放弃
                editor.abort();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean persistBitmapToDisk(Bitmap bitmap, OutputStream outputStream) {
        BufferedOutputStream bos = new BufferedOutputStream(outputStream);
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,bos);
        try {
            bos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
//            Util.closeQuietly(bos);
            try {
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    @Override
    public Bitmap get(BitmapRequest request) {
        // 获取快照
        try {
            DiskLruCache.Snapshot snapshot = diskLruCache.get(request.getImageUriMd5());
            InputStream inputStream = snapshot.getInputStream(0); // 对应上面outputStream(0)
            return BitmapFactory.decodeStream(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void remove(BitmapRequest request) {
//        diskLruCache.remove(request.getImageUriMd5());
    }
}
