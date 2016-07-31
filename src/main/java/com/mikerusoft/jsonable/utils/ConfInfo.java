package com.mikerusoft.jsonable.utils;

import com.mikerusoft.jsonable.adapters.MethodWrapper;
import com.mikerusoft.jsonable.adapters.ParserAdapter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Method;
import java.util.Collection;
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
    public static ConfInfo setClassProperty(String classProperty) { get().classProperty = classProperty; return get(); }
    public static boolean isExcludeClass() { return get().excludeClass; }
    public static ConfInfo setExcludeClass(boolean excludeClass) { get().excludeClass = excludeClass; return get(); }
    public static boolean isIncludeNull() { return get().includeNull; }
    public static ConfInfo setIncludeNull(boolean includeNull) { get().includeNull = includeNull; return get(); }
    public static boolean isEnumAsClass() { return get().enumAsClass; }
    public static ConfInfo setEnumAsClass(boolean enumAsClass) { get().enumAsClass = enumAsClass; return get(); }

    public static Map<Class, ParserAdapter> getAdapters() {
        return Collections.unmodifiableMap(get().adapters);
    }

    /**
     * Creates Adapter for specified class and its properties (see {@link ParserAdapter} for more explanations)
     * serialization
     * @param clazz class name to create Adapater for
     * @param params list of bean properties
     * @return ConfInfo to apply next register or other method using sugr syntax
     */
    public static ConfInfo registerAdapter(Class<?> clazz, PropertyPair[] params) {
        get().adapters.put(clazz, new ParserAdapterBasic(clazz, params));
        return get();
    }

    /**
     * Creates Adapter for specified class and its properties (see {@link ParserAdapter} for more explanations)
     * serialization
     * @param clazz class name to create Adapater for
     * @param params list of bean properties
     * @return ConfInfo to apply next register or other method using sugr syntax
     */
    public static ConfInfo registerAdapter(Class<?> clazz, String[] params) {
        get().adapters.put(clazz, new ParserAdapterBasic(clazz, params));
        return get();
    }

    /*
     * Class to store information about the way how to transform/serialize data in some 3rd party bean
     * @author Grinfeld Mikhail
     * @since 7/27/2016.
     */
    private final static class ParserAdapterBasic implements ParserAdapter {
        final Class<?> clazz;
        final Map<String, MethodWrapper> methods;

        private ParserAdapterBasic(Class<?> clazz, String[] params) {
            if (params == null || params.length <= 0)
                throw new IllegalArgumentException("You should specify parameters for Adapter");
            if (clazz == null)
                throw new IllegalArgumentException("You should specify class for Adapter");
            this.clazz = clazz;
            this.methods = new ConcurrentHashMap<>();
            for (String param : params) {
                try {
                    Method[] pm = getPropertyMethods(param);
                    methods.put(param, new MethodWrapper(param, param, pm[0], pm[1]));
                } catch (NoSuchMethodException e) {
                    throw new IllegalArgumentException("Not found both getters and setter for param " + param);
                }
            }
        }

        private ParserAdapterBasic(Class<?> clazz, PropertyPair[] params) {
            if (params == null || params.length <= 0)
                throw new IllegalArgumentException("You should specify parameters for Adapter");
            if (clazz == null)
                throw new IllegalArgumentException("You should specify class for Adapter");
            this.clazz = clazz;
            this.methods = new ConcurrentHashMap<>();
            for (PropertyPair param : params) {
                try {
                    Method[] pm = getPropertyMethods(param.getProperty());
                    methods.put(param.getName(), new MethodWrapper(param.getProperty(), param.getName(), pm[0], pm[1]));
                } catch (NoSuchMethodException e) {
                    throw new IllegalArgumentException("Not found both getters and setter for param " + param);
                }
            }
        }

        // returns array of 2 where getter in first place and setter in second place
        private Method[] getPropertyMethods(String name) throws NoSuchMethodException {

            String suffix = name.substring(0,1).toUpperCase() + name.substring(1);

            // first we try to find getter
            Method getter = null;
            try {
                getter = clazz.getMethod("get" + suffix);
            } catch (NoSuchMethodException e) {}

            try {
                getter = clazz.getMethod("is" + suffix);
            } catch (NoSuchMethodException e) {}


            String setterName = "set" + suffix;
            if (getter != null) {
                Class<?> type = getter.getReturnType();
                // if we found getter - we want to try to find exact setter
                try {
                    Method setter = clazz.getMethod(setterName, type);
                    return new Method[] { getter, setter };
                } catch (NoSuchMethodException e) {}
            }

            // no getter or appropriate setter with parameter of same type as getter
            Method found = null;
            for (Method m : clazz.getMethods()) {
                if (setterName.equals(m.getName()) && m.getParameterTypes().length == 1) {
                    if (getter == null)
                        return new Method[] { null, m };
                    found = m;
                    if (getter.getReturnType().isAssignableFrom(m.getParameterTypes()[0]))
                        return new Method[] { getter, m };
                }
            }
            if (found != null)
                return new Method[] { getter, found };

            throw new NoSuchMethodException("No setters and getters for property" + name);
        }

        public Class<?> getClazz() { return clazz; }
        public Method getParam(String name) { return null; }
        public Collection<MethodWrapper> getParams() { return Collections.unmodifiableCollection(this.methods.values()); }
    }
}
