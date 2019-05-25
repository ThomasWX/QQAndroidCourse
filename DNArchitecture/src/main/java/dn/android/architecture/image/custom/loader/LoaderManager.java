package dn.android.architecture.image.custom.loader;

import java.util.HashMap;
import java.util.Map;

public class LoaderManager {

    /*
     为何不用线程安全的HashMap呢？
     因为loaderMap的元素固定的，外部不能add元素，所以不存在线程安全问题
      */

    // 缓存所有支持的Loader类型，以便以后新增Loader进行扩展
    private Map<String,Loader> loaderMap = new HashMap<>();

    private static LoaderManager instance = new LoaderManager();

    public static LoaderManager getInstance() {
        return instance;
    }

    private LoaderManager(){
        // 可参考ContextImpl中的register服务思想
        register("http",new UrlLoader());
        register("https",new UrlLoader());
        register("file",new LocalLoader());
    }

    private void register(String schema, Loader loader) {
        loaderMap.put(schema, loader);
    }

    public Loader getLoader(String schema){
        if (loaderMap.containsKey(schema)){
            return  loaderMap.get(schema);
        }
        return new NullLoader();
    }
}
