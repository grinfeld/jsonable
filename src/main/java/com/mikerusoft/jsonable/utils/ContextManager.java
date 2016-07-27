package com.mikerusoft.jsonable.utils;

/**
 * @author Grinfeld Mikhail
 * @since 12/4/2014.
 */
public class ContextManager {
    public static final ThreadLocal<ContextInfo> userThreadLocal = new ThreadLocal<ContextInfo>();

    public static Configuration set(Configuration obj) {
        // do nothing. We leave this method because of backward compatibility
        // actually if anybody called this method - before
        return null;
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

    public static ContextInfo get() {
        ContextInfo o = userThreadLocal.get();
        if (o == null) {
            o = new ContextInfo();
            userThreadLocal.set(o);
        }
        return o;
    }
}
