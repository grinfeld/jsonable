package com.mikerusoft.jsonable.transform;

import com.mikerusoft.jsonable.utils.Outputter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;

/**
 * Transforms java Array into JSON array
 * @author Grinfeld Mikhail
 * @since 5/25/2014.
 */
public class ArrayTransformer extends TransformerImpl {

    private Log log = LogFactory.getLog(ArrayTransformer.class);

    @Override
    public boolean match(Object o) { return matchClass(o.getClass()); }

    @Override
    public boolean matchClass(Class<?> clazz) {
        return clazz.isArray();
    }

    @Override
    public void transform(Object o, Outputter<String> out, String... groups) throws IOException, InvocationTargetException, IllegalAccessException {
        out.write("[");
        int length = Array.getLength(o);
        for (int i=0; i<length; i++) {
            Object p = Array.get(o, i);
            TransformerFactory.get(p).transform(p, out, groups);
            if (i != length - 1)
                out.write(",");
        }
        out.write("]");
    }

    @Override public int matchPriority() { return Transformer.HIGH_PRIORITY; }
}
