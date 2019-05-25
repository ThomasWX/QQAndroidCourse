package dn.android.architecture.image.custom.config;

import dn.android.architecture.image.custom.cache.BitmapCache;
import dn.android.architecture.image.custom.policy.LoaderPolicy;

public class LoaderConfig {
    // 缓存策略
    private BitmapCache bitmapCache;
    // 加载策略
    private LoaderPolicy loaderPolicy;
    // 默认线程数
    private int threadCount = Runtime.getRuntime().availableProcessors();
    // 显示的配置
    private DisplayConfig displayConfig;

    // 建造者模式
    private LoaderConfig(){}
    public static class Builder{
        private LoaderConfig config;
        public Builder(){
            config = new LoaderConfig();
        }
        public Builder setCachePolicy(BitmapCache bitmapCache){
            config.bitmapCache = bitmapCache;
            return this;
        }

        public Builder setLoadPolicy(LoaderPolicy policy){
            config.loaderPolicy = policy;
            return this;
        }

        public Builder setThreadCount(int count){
            config.threadCount = count;
            return this;
        }

        /**
         * 设置加载过程中的图片
         *
         */
        public Builder setLoadingImage(int resId){
            config.displayConfig.loadingImage = resId;
            return this;
        }


        /**
         * 设置加载过程中的图片
         *
         */
        public Builder setFailedImage(int resId){
            config.displayConfig.failedImage = resId;
            return this;
        }

        public LoaderConfig build(){
            return config;
        }
    }

    public BitmapCache getBitmapCache() {
        return bitmapCache;
    }

    public LoaderPolicy getLoaderPolicy() {
        return loaderPolicy;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public DisplayConfig getDisplayConfig() {
        return displayConfig;
    }
}
