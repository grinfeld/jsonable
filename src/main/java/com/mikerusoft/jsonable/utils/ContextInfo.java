package com.mikerusoft.jsonable.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Grinfeld Mikhail
 * @since 7/27/2016.
 */
public class ContextInfo implements ContextData {
    private static final String DEFAULT_CLASS_PROPERTY_VALUE = "class";

    private String classProperty = DEFAULT_CLASS_PROPERTY_VALUE;
    private boolean excludeClass = false;
    private boolean includeNull = false;
    private boolean enumAsClass = false;
    private Map<Class, ParserAdapter> adapters = new ConcurrentHashMap<>();

    public String getClassProperty() { return StringUtils.isEmpty(classProperty) ? DEFAULT_CLASS_PROPERTY_VALUE : classProperty; }
    public ContextInfo setClassProperty(String classProperty) { this.classProperty = classProperty; return this; }
    public boolean isExcludeClass() { return excludeClass; }
    public ContextInfo setExcludeClass(boolean excludeClass) { this.excludeClass = excludeClass; return this;}
    public boolean isIncludeNull() { return includeNull; }
    public ContextInfo setIncludeNull(boolean includeNull) { this.includeNull = includeNull; return this; }
    public boolean isEnumAsClass() { return enumAsClass; }
    public ContextInfo setEnumAsClass(boolean enumAsClass) { this.enumAsClass = enumAsClass; return this; }

    public Map<Class, ParserAdapter> getAdapters() {
        return Collections.unmodifiableMap(adapters);
    }

    /**
     *
     * @param clazz class name to create Adapater for
     * @param params list of bean properties
     * @return
     */
    public ContextInfo registerAdapter(Class<?> clazz, String[] params) {
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
