package com.mikerusoft.jsonable.utils;

import java.util.Properties;

/**
 * @author Grinfeld Mikhail
 * @since 12/4/2014.
 */
public class Configuration extends Properties implements ContextData {

    public static final String EXCLUDE_CLASS_PROPERTY = "exclude_class";
    public static final String CLASS_PROPERTY = "class_property";
    public static final String DEFAULT_CLASS_PROPERTY_VALUE = "class";

    public static final String INCLUDE_NULL_PROPERTY = "include_null";

    public Configuration() {
    }

    public Configuration(Properties defaults) { super(defaults); }

    public static String getStringProperty(Configuration c, String name, String def) {
        if (c == null)
            return def;
        String value = c.getProperty(name);
        return value == null ? def : value;
    }

    public static Boolean getBooleanProperty(Configuration c, String name, Boolean def) {
        String value = getStringProperty(c, name, null);
        if (value == null)
            return def;
        return Boolean.valueOf(value.toLowerCase());
    }

    public static Integer getIntegerProperty(Configuration c, String name, Integer def) {
        String value = getStringProperty(c, name, null);
        if (value == null)
            return def;
        return Integer.valueOf(value.toLowerCase());
    }

    public static Long getLongProperty(Configuration c, String name, Long def) {
        String value = getStringProperty(c, name, null);
        if (value == null)
            return def;
        return Long.valueOf(value.toLowerCase());
    }
}