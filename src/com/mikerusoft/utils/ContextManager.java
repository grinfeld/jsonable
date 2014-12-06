package com.mikerusoft.utils;

/**
 * @author Grinfeld Mikhail
 * @since 12/4/2014.
 */
public class ContextManager {
    public static final ThreadLocal<ContextData> userThreadLocal = new ThreadLocal<ContextData>();

    public static <T extends ContextData> void set(T obj) {
        if (obj != null)
            userThreadLocal.set(obj);
    }

    public static void unset() {
        userThreadLocal.remove();
    }

    public static <T extends ContextData> T get(Class<T> clazz) {
        ContextData o = userThreadLocal.get();
        if (o == null)
            return null;
        return clazz.cast(o);
    }
}
