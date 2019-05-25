package dn.android.architecture.image.custom.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public abstract class BitmapDecoder {
    public Bitmap decodeBitmap(int reqWidth, int reqHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        // 设置只读图片宽高，不读取整张图片
        options.inJustDecodeBounds = true;

        decodeBitmapWithOptions(options);
        // 计算图片缩放比例
        calculateScaleSizeWithOptions(options, reqWidth, reqHeight);
        return decodeBitmapWithOptions(options);
    }

    /**
     *
     * @param options
     * @param reqWidth ImageView的宽度
     * @param reqHeight ImageView的高度
     */
    protected void calculateScaleSizeWithOptions(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // 计算缩放的比例
        // 图片的原始宽高
        int width = options.outWidth;
        int height = options.outHeight;

        int inSampleSize = 1;
        if (width > reqWidth || height > reqHeight) {
            // 宽高的缩放比例
            int widthScaleRatio = Math.round((float) width / (float) reqWidth);
            int heightSacleRatio = Math.round((float) height / (float) reqHeight);

            // 有的是长图，有的是宽图
            inSampleSize = Math.max(widthScaleRatio, heightSacleRatio);
        }

        // 全景图
        // 当 inSampleSize 为2 ，图片的宽高变为原来的1/2
        options.inSampleSize = inSampleSize;
        // 每个像素两个字节
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        // Bitmap 占用内存
        options.inJustDecodeBounds = false;
        // 版本小于4.4，当系统内存不足时可以回收Bitmap
        options.inPurgeable = true;
        options.inInputShareable = true;
    }

    protected abstract Bitmap decodeBitmapWithOptions(BitmapFactory.Options options);
}
