package dn.android.open.ipc.lib;

import android.text.TextUtils;

import java.lang.reflect.Method;
import java.util.HashMap;

public class CacheCenter {
    // 缓存内容：
    // "dn.android.open.ipc.UserManager" ----UserManager.class
    // "dn.android.open.ipc.UserManager" ----method[] getPerson setPerson
    // "dn.android.open.ipc.UserManager" ----Object userManagerObject

    private CacheCenter() {
    }

    private static CacheCenter instance = new CacheCenter();

    public static CacheCenter getInstance() {
        return instance;
    }

    // name String --- class
    private HashMap<String, Class<?>> mClasses = new HashMap<>();

    // 类对应的方法 app -- class -->n method String name
    private HashMap<Class<?>, HashMap<String, Method>> mMethods = new HashMap<>();

    // String --- 对象
    private HashMap<String, Object> mObjects = new HashMap<>();

    /**
     * @param clazz 将其抽丝剥茧
     */
    public void register(Class<?> clazz) {
        mClasses.put(clazz.getName(), clazz);
        // 分为注册类，注册方法
    }

    private void registerMethod(Class<?> clazz) {
        Method[] methods = clazz.getMethods();
        for (Method m :
                methods) {
            if (mMethods.get(clazz) == null) {
                mMethods.put(clazz, new HashMap<String, Method>());
            }
            HashMap<String, Method> map = mMethods.get(clazz);
            map.put(m.getName(), m);
        }
    }

    public Method getMethod(String className, String name) {
        Class clazz = getClassType(className);
        if (name != null) {
            if (mMethods.get(clazz) == null) {
                mMethods.put(clazz, new HashMap<String, Method>());
            }
            HashMap<String, Method> methods = mMethods.get(clazz);
            Method method = methods.get(name);
            if (method != null) {
                return method;
            }
        }
        return null;
    }


    public Class<?> getClassType(String name) {
        if (TextUtils.isEmpty(name)) {
            return null;
        }
        Class<?> clazz = mClasses.get(name);
        if (clazz == null) {
            try {
                clazz = Class.forName(name);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return clazz;
    }
}
