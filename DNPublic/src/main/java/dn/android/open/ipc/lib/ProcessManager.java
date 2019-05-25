package dn.android.open.ipc.lib;

public class ProcessManager {
    private static final ProcessManager ourInstance = new ProcessManager();

    public static ProcessManager getInstance() {
        return ourInstance;
    }

    private ProcessManager() {
    }

    private CacheCenter cacheCenter = CacheCenter.getInstance();

    /**
     * 暴露一个注册方法
     * 主要是解析方法(如解析UserManager的方法)，并缓存到一个 表中
     */
    public void register(Class<?> clazz){
        cacheCenter.register(clazz);
    }
}
