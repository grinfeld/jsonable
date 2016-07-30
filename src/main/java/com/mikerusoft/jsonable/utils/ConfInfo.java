package com.mikerusoft.jsonable.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Grinfeld Mikhail
 * @since 7/27/2016.
 */
public class ConfInfo implements ContextData {

    private static Log log = LogFactory.getLog(ConfInfo.class);

    private static final String DEFAULT_CLASS_PROPERTY_VALUE = "class";

    private static ConfInfo get() {
        return ContextManager.get();
    }

    public static void unset() {
        ContextManager.unset();
    }

    private String classProperty = DEFAULT_CLASS_PROPERTY_VALUE;
    private boolean excludeClass = false;
    private boolean includeNull = false;
    private boolean enumAsClass = false;
    private Map<Class, ParserAdapter> adapters = new ConcurrentHashMap<>();



    public static String getClassProperty() { return StringUtils.isEmpty(get().classProperty) ? DEFAULT_CLASS_PROPERTY_VALUE : get().classProperty; }
    public static void setClassProperty(String classProperty) { get().classProperty = classProperty; }
    public static boolean isExcludeClass() { return get().excludeClass; }
    public static void setExcludeClass(boolean excludeClass) { get().excludeClass = excludeClass; }
    public static boolean isIncludeNull() { return get().includeNull; }
    public static void setIncludeNull(boolean includeNull) { get().includeNull = includeNull; }
    public static boolean isEnumAsClass() { return get().enumAsClass; }
    public static void setEnumAsClass(boolean enumAsClass) { get().enumAsClass = enumAsClass; }

    public static Map<Class, ParserAdapter> getAdapters() {
        return Collections.unmodifiableMap(get().adapters);
    }

    /**
     *
     * @param clazz class name to create Adapater for
     * @param params list of bean properties
     * @return ConfInfo to apply next register or other method using sugr syntax
     */
    public ConfInfo registerAdapter(Class<?> clazz, String[] params) {
        adapters.put(clazz, new ParserAdapter(clazz, params));
        return this;
    }

    /**
     * Class to store information about the way to transform/serialize data in some 3rd party bean
     * @author Grinfeld Mikhail
     * @since 7/27/2016.
     */
    final private class ParserAdapter {
        final Class<?> clazz;
        final String[] params;

        private ParserAdapter(Class<?> clazz, String[] params) {
            this.clazz = clazz;
            this.params = params;
        }

        public Class<?> getClazz() { return clazz; }
        public String[] getParams() { return params == null ? null : Arrays.copyOf(params, params.length); }
    }
}
