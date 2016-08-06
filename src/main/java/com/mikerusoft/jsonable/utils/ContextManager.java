package com.mikerusoft.jsonable.utils;

/**
 * @author Grinfeld Mikhail
 * @since 12/4/2014.
 *
 * TODO: when removing use of Configuration - move this class into ConfInfo in order to avoid getting it from outside of package
 */
public class ContextManager {
    public static final ThreadLocal<ConfInfo> userThreadLocal = new ThreadLocal<ConfInfo>();

    public static Configuration set(Configuration obj) {
        // do nothing. We leave this method because of backward compatibility
        // actually if anybody called this method - before
        return new Configuration();
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

    public static ConfInfo get() {
        ConfInfo o = userThreadLocal.get();
        if (o == null) {
            o = new ConfInfo();
            userThreadLocal.set(o);
        }
        return o;
    }
}
