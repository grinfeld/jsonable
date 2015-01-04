package com.mikerusoft.jsonable.transform;

import com.mikerusoft.jsonable.annotations.*;
import com.mikerusoft.jsonable.utils.Configuration;
import com.mikerusoft.jsonable.utils.ContextManager;
import com.mikerusoft.jsonable.utils.Outputter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Transformer for classes defined with annotation @JsonClass
 * @author Grinfeld Mikhail
 * @since 5/25/2014.
 */
public class JsonDefinedTransformer implements Transformer {

    private Log log = LogFactory.getLog(JsonDefinedTransformer.class);

    @Override
    public boolean match(Object o) {
        return o.getClass().getAnnotation(JsonClass.class) != null;
    }

    @Override
    public void transform(Object o, Outputter<String> out, String... groups) throws IOException, IllegalAccessException, InvocationTargetException {
        Class<?> inherit = o.getClass();
        out.write("{");
        int count = 0;
        do {
            count = count + write(o, inherit.getDeclaredFields(), inherit.getDeclaredMethods(), out, count, groups);
        } while ((inherit = inherit.getSuperclass()) != null);
        if (count > 0) {
            Configuration c = ContextManager.get(Configuration.class);
            boolean excludeClass = Configuration.getBooleanProperty(c, Configuration.EXCLUDE_CLASS_PROPERTY, false);
            if (!excludeClass) {
                String cl = Configuration.getStringProperty(c, Configuration.CLASS_PROPERTY, Configuration.DEFAULT_CLASS_PROPERTY_VALUE);
                out.write((",\"" + cl + "\":\"" + o.getClass().getName() + "\""));
            }
        }
        out.write("}");
    }

    private boolean inGroup(String[] requestedGroups, String[] dataGroups) {
        if (requestedGroups == null || requestedGroups.length == 0)
            return true;
        if (dataGroups == null)
            return false;
        return new ArrayList<String>(Arrays.asList(requestedGroups)).removeAll(Arrays.asList(dataGroups));
    }

    private int write(Object o, Field[] fields, Method[] methods, Outputter<String> out, int count, String... groups) throws IllegalAccessException, IOException, InvocationTargetException {
        for (Field f : fields) {
            JsonField an = f.getAnnotation(JsonField.class);
            if (an != null && inGroup(groups, an.groups())) {
                f.setAccessible(true);
                Object part = f.get(o);
                if (part != null) {
                    String name = StringUtils.isEmpty(an.name()) ? f.getName() : an.name();
                    if (count != 0) {
                        out.write(",");
                    }
                    out.write("\"");
                    out.write(name);
                    out.write("\":");
                    TransformerFactory.get(part).transform(part, out);
                    count++;
                }
            }
        }

        for (Method m : methods) {
            CustomField an = m.getAnnotation(CustomField.class);
            // actually we want "getters" to use, but we allow any name.
            if (an != null && ArrayUtils.isEmpty(m.getParameterTypes()) && inGroup(groups, an.groups())) {
                String customName = an.name();
                m.setAccessible(true);
                Object part = m.invoke(o);
                if (part != null) {
                    if (count != 0) {
                        out.write(",");
                    }
                    out.write("\"");
                    out.write(customName);
                    out.write("\":");
                    TransformerFactory.get(part).transform(part, out);
                    count++;
                }
            }
        }

        return count;
    }

    @Override public int matchPriority() { return Transformer.HIGH_PRIORITY; }
}