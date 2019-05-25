package dn.android.architecture.image.custom.request;

import android.support.annotation.Nullable;
import android.widget.ImageView;

import java.lang.ref.SoftReference;

import dn.android.architecture.image.custom.config.DisplayConfig;
import dn.android.architecture.image.custom.loader.SimpleImageLoader;
import dn.android.architecture.image.custom.policy.LoaderPolicy;
import dn.android.architecture.image.custom.utils.MD5Utils;

/**
 * List 集合中，如果需要排序，List的元素(对象)就得实现 Comparator<T>接口
 */
public class BitmapRequest implements java.lang.Comparable<BitmapRequest> {
    // 持有ImageView的软引用
    private SoftReference<ImageView> imageViewSoftReference;
    // 图片路径
    private String imageUrl;
    // Md5 加密的图片路径
    private String imageUriMd5;
    // 下载完成的监听
    public SimpleImageLoader.ImageListener imageListener;

    // 加载策略
    private LoaderPolicy loaderPolicy = SimpleImageLoader.getInstance().getConfig().getLoaderPolicy();
    // 优先级编号
    private int serialNo;
    // 自定义的图片显示过程
    private DisplayConfig displayConfig;

    public BitmapRequest(ImageView imageView, String imageUrl, DisplayConfig config,
                         SimpleImageLoader.ImageListener imageListener) {
        this.imageViewSoftReference = new SoftReference<>(imageView);
        // 解决图片错位问题
        imageView.setTag(imageUrl);
        this.imageUrl = imageUrl;
        this.imageUriMd5 = MD5Utils.toMD5(imageUrl);
        this.imageListener = imageListener;
        if (config != null) {
            this.displayConfig = config;
        }
    }

    public int getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(int serialNo) {
        this.serialNo = serialNo;
    }

    /**
     * 优先级的确定，丢给接口去比较
     * @param o
     * @return
     */
    @Override
    public int compareTo(BitmapRequest o) {
        return loaderPolicy.compareTo(o, this);
    }


    /**
     * 用于contains，判断是否包含.
     * 加载策略和优先级编号都一样，即相同
     */
    @Override
    public int hashCode() {
        int result = loaderPolicy != null ? loaderPolicy.hashCode() : 0;
        result = 31 * result + serialNo;
        return result;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        BitmapRequest that = (BitmapRequest) obj;
        if (serialNo != that.serialNo) return false;

        return loaderPolicy != null ? loaderPolicy.equals(that.loaderPolicy) : that.loaderPolicy == null;
    }

    public ImageView getImageView() {
        return imageViewSoftReference.get();
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getImageUriMd5() {
        return imageUriMd5;
    }

    public LoaderPolicy getLoaderPolicy() {
        return loaderPolicy;
    }

    public DisplayConfig getDisplayConfig() {
        return displayConfig;
    }


}
