package com.mikerusoft.jsonable.transform;

import com.mikerusoft.jsonable.adapters.MethodWrapper;
import com.mikerusoft.jsonable.adapters.ParserAdapter;
import com.mikerusoft.jsonable.utils.ConfInfo;
import com.mikerusoft.jsonable.utils.Outputter;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

/**
 * @author Grinfeld Mikhail
 * @since 7/31/2016 9:22 PM
 */
public class AdapterTransformer extends TransformerImpl {
    @Override
    public boolean match(Object o) {
        return o != null && matchClass(o.getClass());
    }

    @Override
    public boolean matchClass(Class<?> clazz) {
        return ConfInfo.getAdapter(clazz) != null;
    }


    // TODO: decide how to deal with adapter that doesn't have getter methods ?
    @Override
    public void transform(Object o, Outputter<String> out, String... groups) throws IOException, IllegalAccessException, InvocationTargetException, InstantiationException {
        ParserAdapter<?> adapter = ConfInfo.getAdapter(o.getClass());
        Collection<MethodWrapper> methodWrappers = adapter.getParams();

        out.write("{");
        int count=0;
        for (MethodWrapper wrapper : methodWrappers) {
            Method m = wrapper.getGetter();
            if (m != null) {
                Object res = m.invoke(o);
                if (count > 0)
                    out.write(",");
                out.write("\"" + wrapper.getName() + "\":");
                TransformerFactory.get(res).transform(res, out, groups);
                count++;
            }
        }
        boolean excludeClass = ConfInfo.isExcludeClass(); // Configuration.getBooleanProperty(c, Configuration.EXCLUDE_CLASS_PROPERTY, false);
        if (!excludeClass && count > 0) {
            String cl = ConfInfo.getClassProperty(); // Configuration.getStringProperty(c, Configuration.CLASS_PROPERTY, Configuration.DEFAULT_CLASS_PROPERTY_VALUE);
            out.write((",\"" + cl + "\":\"" + o.getClass().getName() + "\""));
        }
        out.write("}");
    }

    @Override
    public int matchPriority() {
        return Transformer.HIGH_PRIORITY;
    }
}
