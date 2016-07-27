package com.mikerusoft.jsonable.utils;

import java.util.Map;
import java.util.Properties;

/**
 * This is old class to store Configuration. Use {@link ContextInfo} instead
 * Class is store only because of backward compatibility, actually it's fill
 * new {@link ContextInfo}
 *
 * @author Grinfeld Mikhail
 * @since 12/4/2014.
 */
@Deprecated
public class Configuration implements ContextData {

    public static final String EXCLUDE_CLASS_PROPERTY = "exclude_class";
    public static final String CLASS_PROPERTY = "class_property";
    public static final String DEFAULT_CLASS_PROPERTY_VALUE = "class";

    public static final String INCLUDE_NULL_PROPERTY = "include_null";
    public static final String ENUM_AS_CLASS_PROPERTY = "enum_class";

    /**
     * Enum for moving to new type of storing configuration, without hurting existed users
     */
    private enum Names{
        exclude_class {
            @Override
            public String getValue() {
                return String.valueOf(ContextManager.get().isExcludeClass());
            }

            @Override
            public void setValue(String value) {
                ContextManager.get().setExcludeClass(Boolean.valueOf(value));
            }
        },
        class_property {
            @Override
            public String getValue() {
                return ContextManager.get().getClassProperty();
            }

            @Override
            public void setValue(String value) {
                ContextManager.get().setClassProperty(value);
            }
        },
        include_null {
            @Override
            public String getValue() {
                return String.valueOf(ContextManager.get().isIncludeNull());
            }

            @Override
            public void setValue(String value) {
                ContextManager.get().setIncludeNull(Boolean.valueOf(value));
            }
        },
        enum_class {
            @Override
            public String getValue() {
                return String.valueOf(ContextManager.get().isEnumAsClass());
            }

            @Override
            public void setValue(String value) {
                ContextManager.get().setEnumAsClass(Boolean.valueOf(value));
            }
        };

        public abstract String getValue();
        public abstract void setValue(String value);

    }

    @Deprecated
    /**
     * Class deprecated. Use {@link ContextInfo}
     */
    public Configuration() {
    }

    @Deprecated
    /**
     * Class deprecated. Use {@link ContextInfo}
     */
    public Configuration(Properties defaults) {
        if (defaults != null) {
            for (Map.Entry<Object, Object> p : defaults.entrySet()) {
                setProperty(String.valueOf(p.getKey()), String.valueOf(p.getValue()));
            }
        }
    }

    @Deprecated
    /**
     * Class deprecated. Use {@link ContextInfo}
     */
    public static String getStringProperty(Configuration c, String name, String def) {
        try {
            Names n = Names.valueOf(name);
            return n.getValue();
        } catch (Exception ignore) {}
        return def;
    }

    @Deprecated
    /**
     * Class deprecated. Use {@link ContextInfo}
     */
    public static Boolean getBooleanProperty(Configuration c, String name, Boolean def) {
        String value = getStringProperty(c, name, null);
        if (value == null)
            return def;
        return Boolean.valueOf(value.toLowerCase());
    }

    @Deprecated
    /**
     * Class deprecated. Use {@link ContextInfo}
     */
    public static Integer getIntegerProperty(Configuration c, String name, Integer def) {
        String value = getStringProperty(c, name, null);
        if (value == null)
            return def;
        return Integer.valueOf(value.toLowerCase());
    }

    @Deprecated
    /**
     * Class deprecated. Use {@link ContextInfo}
     */
    public static Long getLongProperty(Configuration c, String name, Long def) {
        String value = getStringProperty(c, name, null);
        if (value == null)
            return def;
        return Long.valueOf(value.toLowerCase());
    }

    @Deprecated
    /**
     * Class deprecated. Use {@link ContextInfo}
     */
    public Object setProperty(String name, String value) {
        String current = null;
        try {
            Names n = Names.valueOf(name);
            current = n.getValue();
            n.setValue(value);
        } catch (Exception ignore) {}
        return current;
    }
}
