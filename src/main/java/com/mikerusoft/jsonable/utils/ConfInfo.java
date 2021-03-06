package com.mikerusoft.jsonable.utils;

import com.mikerusoft.jsonable.adapters.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Method;
import java.util.*;
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
    private boolean includePrimitiveClass = false;
    private boolean enumAsClass = false;
    private Map<Class, ParserAdapter> classAdapters = new ConcurrentHashMap<>();
    private Map<String, Boolean> packageAdapters = new ConcurrentHashMap<>();
    private Map<String, String> properties = new ConcurrentHashMap<>();
    private Map<Class, ReadInstanceFactory> readFactories = new ConcurrentHashMap<>();

    public static String getClassProperty() { return StringUtils.isEmpty(get().classProperty) ? DEFAULT_CLASS_PROPERTY_VALUE : get().classProperty; }
    public static void setClassProperty(String classProperty) { get().classProperty = classProperty; }
    public static boolean isExcludeClass() { return get().excludeClass; }
    public static void setExcludeClass(boolean excludeClass) { get().excludeClass = excludeClass; }
    public static boolean isIncludeNull() { return get().includeNull; }
    public static void setIncludeNull(boolean includeNull) { get().includeNull = includeNull; }
    public static boolean isEnumAsClass() { return get().enumAsClass; }
    public static void setEnumAsClass(boolean enumAsClass) { get().enumAsClass = enumAsClass; }
    public static boolean isIncludePrimitiveClass() { return get().includePrimitiveClass; }
    public static void setIncludePrimitiveClass(boolean includePrimitiveClass) { get().includePrimitiveClass = includePrimitiveClass; }
    public static ParserAdapter<?> getAdapter(Class<?> clazz) {
        if (clazz.getPackage() == null)
            return null;
        ParserAdapter<?> adapter = get().classAdapters.get(clazz);
        if (adapter == null) {
            String packageName = clazz.getPackage().getName();
            Boolean exists = get().packageAdapters.get(packageName);
            if (exists != null) {
                adapter = new SimpleBeanAdapter<>(clazz);
                get().classAdapters.put(clazz, adapter);
            }
        }
        return adapter;
    }
    public static String getProperty(String name) { return get().properties.get(name); }
    public static String getProperty(String name, String def) { String value = get().properties.get(name); return value == null ? def : value; }
    public static Integer getProperty(String name, Integer def) {
        String value = get().properties.get(name);
        if (value == null)
            return def;
        return Integer.valueOf(value);
    }
    public static Long getProperty(String name, Long def) {
        String value = get().properties.get(name);
        if (value == null)
            return def;
        return Long.valueOf(value);
    }
    public static boolean getProperty(String name, boolean def) {
        String value = get().properties.get(name);
        if (value == null)
            return def;
        return Boolean.valueOf(value);
    }
    public static void setProperty(String name, String value) { get().properties.put(name, value); }
    public static void setProperty(String name, Integer value) { get().properties.put(name, value == null ? null : String.valueOf(value)); }
    public static void setProperty(String name, Long value) { get().properties.put(name, value == null ? null : String.valueOf(value)); }
    public static void setProperty(String name, Boolean value) { get().properties.put(name, value == null ? null : String.valueOf(value)); }


    public static ReadInstanceFactory getFactory(Class<?> clazz) {
        ReadInstanceFactory i = get().readFactories.get(clazz);
        if (i == null)
            return new EmptyConstructorReadFactory<>(clazz);
        return i;
    }

    public static void registerFactories(ReadInstanceFactory...factories) {
        if (factories == null)
            return;
        for(ReadInstanceFactory f : factories) {
            if (f != null) {
                get().readFactories.put(f.getFactoryClass(), f);
            }
        }
    }

    /**
     * Creates Adapter for specified class and its properties (see {@link ParserAdapter} for more explanations)
     * serialization
     * @param clazz class name to create Adapater for
     * @param params list of bean properties
     */
    public static void registerAdapter(Class<?> clazz, PropertyPair[] params) {
        get().classAdapters.put(clazz, new ParserAdapterBasic<>(clazz, params));
    }

    /**
     * Creates Adapter for specified class and its properties (see {@link ParserAdapter} for more explanations)
     * serialization
     * @param clazz class name to create Adapater for
     * @param params list of bean properties
     */
    public static void registerAdapter(Class<?> clazz, String[] params) {
        get().classAdapters.put(clazz, new ParserAdapterBasic<>(clazz, params));
    }
    
    /**
     * Creates Adapter for specified class and its properties (see {@link ParserAdapter} for more explanations)
     * serialization
     * @param adapter custom implementation of adapter
     * @param <T> adapter type
     */
    public static <T> void registerAdapter(ParserAdapter<T> adapter) {
        get().classAdapters.put(adapter.getClazz(), adapter);
    }

    /**
     * registers package to read classes for serialization in the same way as {@link SimpleBeanAdapter}
     * @param packageName package to check classes
     */
    public static void registerAdapter(String packageName) {
        get().packageAdapters.put(packageName, true);
    }

    /*
     * Class to store information about the way how to transform/serialize data in some 3rd party bean
     * @author Grinfeld Mikhail
     * @since 7/27/2016.
     */
    private final static class ParserAdapterBasic<T> implements ParserAdapter<T> {
        final Class<T> clazz;
        final Map<String, MethodWrapper> methods;

        private ParserAdapterBasic(Class<T> clazz, String[] params) {
            if (params == null || params.length <= 0)
                throw new IllegalArgumentException("You should specify parameters for Adapter");
            if (clazz == null)
                throw new IllegalArgumentException("You should specify class for Adapter");
            this.clazz = clazz;
            this.methods = new ConcurrentHashMap<>();
            for (String param : params) {
                try {
                    Method[] pm = getPropertyMethods(param);
                    methods.put(param, new MethodWrapper(param, param, pm[1], pm[0]));
                } catch (NoSuchMethodException e) {
                    throw new IllegalArgumentException("Not found both getters and setter for param " + param);
                }
            }
        }

        private ParserAdapterBasic(Class<T> clazz, PropertyPair[] params) {
            if (params == null || params.length <= 0)
                throw new IllegalArgumentException("You should specify parameters for Adapter");
            if (clazz == null)
                throw new IllegalArgumentException("You should specify class for Adapter");
            this.clazz = clazz;
            this.methods = new ConcurrentHashMap<>();
            for (PropertyPair param : params) {
                try {
                    Method[] pm = getPropertyMethods(param.getProperty());
                    methods.put(param.getName(), new MethodWrapper(param.getProperty(), param.getName(), pm[1], pm[0]));
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

        public Class<T> getClazz() { return clazz; }
        public MethodWrapper getParam(String name) { return methods == null ? null : methods.get(name); }
        public Collection<MethodWrapper> getParams() { return Collections.unmodifiableCollection(this.methods.values()); }
    }
}
