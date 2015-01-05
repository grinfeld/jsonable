package com.mikerusoft.jsonable.transform;

import com.mikerusoft.jsonable.annotations.CustomField;
import com.mikerusoft.jsonable.annotations.IgnoreJson;
import com.mikerusoft.jsonable.utils.Configuration;
import com.mikerusoft.jsonable.utils.ContextManager;
import com.mikerusoft.jsonable.utils.Outputter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Transforms any object fields to JSON, except those which annotated with {@link com.mikerusoft.jsonable.annotations.IgnoreJson}
 * or modifiers marked as transient
 *
 * @author Grinfeld Mikhail
 * @since 5/25/2014.
 */
public class ObjectTransformer implements Transformer {

    private Log log = LogFactory.getLog(ObjectTransformer.class);

    private Map<String, Field[]> fieldCache = new ConcurrentHashMap<String, Field[]>();
    private Map<String, Method[]> methodCache = new ConcurrentHashMap<String, Method[]>();

    @Override
    public boolean match(Object o) {
        return !(o == null || o.getClass().isPrimitive() || o.getClass().isEnum() || o.getClass().isArray());
    }

    @Override
    public void transform(Object o, Outputter<String> out, String... groups) throws IOException, IllegalAccessException, InvocationTargetException {
        Class<?> inherit = o.getClass();
        out.write("{");
        int count = 0;
        do {
            Field[] fields = fieldCache.get(inherit.getName());
            if (fields == null) {
                fields = inherit.getDeclaredFields();
                fieldCache.put(inherit.getName(), fields);
            }
            Method[] methods = methodCache.get(inherit.getName());
            if (methods == null){
                methods = inherit.getDeclaredMethods();
                methodCache.put(inherit.getName(), methods);
            }
            count = count + write(o, fields, methods, out, count, groups);
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

    private int write(Object o, Field[] fields, Method[] methods, Outputter<String> out, int counter, String...groups) throws IllegalAccessException, IOException, InvocationTargetException {
        if (ArrayUtils.isEmpty(groups)) {
            for (Field f : fields) {
                if (f.getAnnotation(IgnoreJson.class) == null && !Modifier.isTransient(f.getModifiers())) {
                    String name = f.getName();
                    f.setAccessible(true);
                    Object part = f.get(o);
                    if (counter != 0) {
                        out.write(",");
                    }
                    out.write("\"");
                    out.write(name);
                    out.write("\":");
                    TransformerFactory.get(part).transform(part, out, groups);
                    counter++;
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
                if (counter != 0) {
                    out.write(",");
                }
                out.write("\"");
                out.write(customName);
                out.write("\":");
                TransformerFactory.get(part).transform(part, out, groups);
                counter++;
            }
        }

        return counter;
    }

    @Override public int matchPriority() { return Transformer.LOW_PRIORITY; }
}
