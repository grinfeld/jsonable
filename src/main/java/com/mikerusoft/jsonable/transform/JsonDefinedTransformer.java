package com.mikerusoft.jsonable.transform;

import com.mikerusoft.jsonable.annotations.*;
import com.mikerusoft.jsonable.refelection.Invoker;
import com.mikerusoft.jsonable.refelection.ReflectionCache;
import com.mikerusoft.jsonable.utils.ConfInfo;
import com.mikerusoft.jsonable.utils.Outputter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Transformer for classes defined with annotation @JsonClass
 * @author Grinfeld Mikhail
 * @since 5/25/2014.
 */
public class JsonDefinedTransformer extends TransformerImpl {

    private Log log = LogFactory.getLog(JsonDefinedTransformer.class);

    @Override
    public boolean match(Object o) {
        return matchClass(o.getClass());
    }

    @Override
    public boolean matchClass(Class<?> clazz) {
        return clazz.getAnnotation(JsonClass.class) != null;
    }

    @Override
    public void transform(Object o, Outputter<String> out, String... groups) throws IOException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Class<?> clazz = o.getClass();

        out.write("{");

        // Configuration c = ContextManager.get(Configuration.class);
        boolean includeNull = ConfInfo.isIncludeNull(); // Configuration.getBooleanProperty(c, Configuration.INCLUDE_NULL_PROPERTY, false);
        // TODO: we need to ensure that if method or field with the same property name has been executed - don't do it again for overridden methods
        int count = 0;

        Collection<Invoker> invokers = ReflectionCache.get().getInvokers(clazz);
        for (Invoker i : invokers) {
            if (i.getEnabled()) {
                if (ReflectionCache.inGroup(i.getSetterGroups(), groups)) {
                    Object part = i.get(o);
                    if (includeNull || displayNullField(i) || part != null) {
                        String name = i.getGetterName();
                        if (count != 0) {
                            out.write(",");
                        }
                        out.write("\"");
                        out.write(name.replaceAll("\"", "\\\""));
                        out.write("\":");
                        TransformerFactory.get(part).transform(part, out, groups);
                        count++;
                    }
                }
            }
        }

        if (count > 0) {
            boolean excludeClass = ConfInfo.isExcludeClass(); // Configuration.getBooleanProperty(c, Configuration.EXCLUDE_CLASS_PROPERTY, false);
            if (!excludeClass) {
                String cl = ConfInfo.getClassProperty(); // Configuration.getStringProperty(c, Configuration.CLASS_PROPERTY, Configuration.DEFAULT_CLASS_PROPERTY_VALUE);
                out.write((",\"" + cl + "\":\"" + o.getClass().getName() + "\""));
            }
        }
        out.write("}");
    }

    private boolean displayNullField(Invoker ao) {
        DisplayNull dn = ao.getGetterAnnotation(DisplayNull.class);
        return dn != null && dn.value();
    }

    @Override public int matchPriority() { return Transformer.HIGH_PRIORITY - 1; }
}
