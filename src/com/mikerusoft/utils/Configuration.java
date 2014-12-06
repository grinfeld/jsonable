package com.mikerusoft.utils;

import java.util.Properties;

/**
 * @author Grinfeld Mikhail
 * @since 12/4/2014.
 */
public class Configuration extends Properties implements ContextData {

    public static final String EXCLUDE_CLASS_PROPERTY = "exclude_class";

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
}
