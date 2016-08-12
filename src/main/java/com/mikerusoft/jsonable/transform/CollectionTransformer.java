package com.mikerusoft.jsonable.transform;

import com.mikerusoft.jsonable.utils.Outputter;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

/**
 * Converts Collection into Json array. It's has low priority, so if found more appropriate Transformer, it will be used instead of this.
 */
public class CollectionTransformer extends TransformerImpl {

    @Override
    public boolean match(Object o) {
        return matchClass(o.getClass());
    }

    @Override
    public boolean matchClass(Class<?> clazz) {
        return Collection.class.isAssignableFrom(clazz);
    }

    @Override
    public void transform(Object o, Outputter<String> out, String... groups) throws IOException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Collection<?> l = (Collection<?>) o;
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
        return Transformer.LOW_PRIORITY - 1;
    }
}
