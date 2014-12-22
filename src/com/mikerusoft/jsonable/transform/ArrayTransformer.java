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
public class ArrayTransformer implements Transformer {

    private Log log = LogFactory.getLog(ArrayTransformer.class);

    @Override
    public boolean match(Object o) { return o.getClass().isArray(); }

    @Override
    public void transform(Object o, Outputter<String> out) throws IOException, InvocationTargetException, IllegalAccessException {
        out.write("[");
        int length = Array.getLength(o);
        for (int i=0; i<length; i++) {
            Object p = Array.get(o, i);
            TransformerFactory.get(p).transform(p, out);
            if (i != length - 1)
                out.write(",");
        }
        out.write("]");
    }

    @Override public int matchPriority() { return Transformer.HIGH_PRIORITY; }
}
