package com.mikerusoft.jsonable.transform;

import com.mikerusoft.jsonable.annotations.*;
import com.mikerusoft.jsonable.utils.ConfInfo;
import com.mikerusoft.jsonable.utils.Outputter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Transformer for classes defined with annotation @JsonClass
 * @author Grinfeld Mikhail
 * @since 5/25/2014.
 */
public class JsonDefinedTransformer extends TransformerImpl {

    private Log log = LogFactory.getLog(JsonDefinedTransformer.class);

    private Map<String, Pair<List<Field>, List<Method>>> cache = new ConcurrentHashMap<String, Pair<List<Field>, List<Method>>>();

    @Override
    public boolean match(Object o) {
        return matchClass(o.getClass());
    }

    @Override
    public boolean matchClass(Class<?> clazz) {
        return clazz.getAnnotation(JsonClass.class) != null;
    }

    @Override
    public void transform(Object o, Outputter<String> out, String... groups) throws IOException, IllegalAccessException, InvocationTargetException {
        Class<?> clazz = o.getClass();
        Class<?> inherit = clazz;
        out.write("{");

        Pair<List<Field>, List<Method>> metadata = cache.get(clazz.getName());
        List<Method> allMethods = new ArrayList<Method>();
        List<Field> allFields = new ArrayList<Field>();
        if (metadata == null) {
            do {
                Field[] fields = inherit.getDeclaredFields();
                if (fields != null)
                    allFields.addAll(Arrays.asList(fields));
                Method[] methods = inherit.getDeclaredMethods();
                if (methods != null)
                    allMethods.addAll(Arrays.asList(methods));
                inherit = inherit.getSuperclass();
            } while (inherit != null && inherit.isAnnotationPresent(JsonClass.class) && !Object.class.equals(inherit));
            cache.put(clazz.getName(), new ImmutablePair<List<Field>, List<Method>>(allFields, allMethods));
        } else {
            allFields = metadata.getLeft();
            allMethods = metadata.getRight();
        }
        int count = write(o, allFields, allMethods, out, groups);
        if (count > 0) {
            boolean excludeClass = ConfInfo.isExcludeClass(); // Configuration.getBooleanProperty(c, Configuration.EXCLUDE_CLASS_PROPERTY, false);
            if (!excludeClass) {
                String cl = ConfInfo.getClassProperty(); // Configuration.getStringProperty(c, Configuration.CLASS_PROPERTY, Configuration.DEFAULT_CLASS_PROPERTY_VALUE);
                out.write((",\"" + cl + "\":\"" + o.getClass().getName() + "\""));
            }
        }
        out.write("}");
    }

    private boolean inGroup(String[] requestedGroups, String[] dataGroups) {
        if (requestedGroups == null || requestedGroups.length == 0)
            return true;
        if (dataGroups == null || dataGroups.length == 0)
            return true;
        return new ArrayList<String>(Arrays.asList(requestedGroups)).removeAll(Arrays.asList(dataGroups));
    }

    private boolean displayNullField(AccessibleObject ao) {
        DisplayNull dn = ao.getAnnotation(DisplayNull.class);
        return dn != null && dn.value();
    }

    private int write(Object o, List<Field> fields, List<Method> methods, Outputter<String> out, String... groups) throws IllegalAccessException, IOException, InvocationTargetException {
        // Configuration c = ContextManager.get(Configuration.class);
        boolean includeNull = ConfInfo.isIncludeNull(); // Configuration.getBooleanProperty(c, Configuration.INCLUDE_NULL_PROPERTY, false);
        // TODO: we need to ensure that if method or field with the same property name has been executed - don't do it again for overrided methods
        int count = 0;
        for (Field f : fields) {
            JsonField an = f.getAnnotation(JsonField.class);
            if (an != null && inGroup(groups, an.groups())) {
                f.setAccessible(true);
                Object part = f.get(o);
                if (includeNull || displayNullField(f) || part != null) {
                    String name = StringUtils.isEmpty(an.name()) ? f.getName() : an.name();
                    if (count != 0) {
                        out.write(",");
                    }
                    out.write("\"");
                    out.write(name.replaceAll("\"", "\\\""));
                    out.write("\":");
                    TransformerFactory.get(part).transform(f, part, out, groups);
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
                if (includeNull || displayNullField(m) || part != null) {
                    if (count != 0) {
                        out.write(",");
                    }
                    out.write("\"");
                    out.write(customName.replaceAll("\"", "\\\""));
                    out.write("\":");
                    TransformerFactory.get(part).transform(m, part, out, groups);
                    count++;
                }
            }
        }

        return count;
    }

    @Override public int matchPriority() { return Transformer.HIGH_PRIORITY; }
}
