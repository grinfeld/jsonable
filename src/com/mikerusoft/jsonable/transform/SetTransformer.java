package com.mikerusoft.jsonable.transform;

import com.mikerusoft.jsonable.utils.Outputter;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

/**
 * Transforms Set into Json array
 */
public class SetTransformer implements Transformer {
    @Override
    public boolean match(Object o) {
        return matchClass(o.getClass());
    }

    @Override
    public boolean matchClass(Class<?> clazz) {
        return Set.class.isAssignableFrom(clazz);
    }

    @Override
    public void transform(Object o, Outputter<String> out, String... groups) throws IOException, IllegalAccessException, InvocationTargetException {
        Set<?> l = (Set<?>)o;
        out.write("[");
        int i = 0;
        for (Object p : l) {
            TransformerFactory.get(p).transform(p, out, groups);
            if (i != l.size() - 1)
                out.write(",");
            i++;
        }
        out.write("]");
    }

    @Override
    public int matchPriority() {
        return Transformer.HIGH_PRIORITY + 1;
    }
}
