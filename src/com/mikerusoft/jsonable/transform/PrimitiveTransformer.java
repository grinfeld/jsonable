package com.mikerusoft.jsonable.transform;

import com.mikerusoft.jsonable.utils.Outputter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Grinfeld Mikhail
 * @since 5/25/2014.
 */
public class PrimitiveTransformer implements Transformer {

    private Log log = LogFactory.getLog(PrimitiveTransformer.class);

    @Override
    public boolean match(Object o) {
        Class<?> clazz = o.getClass();
        return clazz.isPrimitive() || Boolean.class.equals(clazz) || Byte.class.equals(clazz) ||
            Short.class.equals(clazz) || Character.class.equals(clazz) ||
            Integer.class.equals(clazz) || Long.class.equals(clazz) ||
            Double.class.equals(clazz) || Float.class.equals(clazz);
    }

    @Override
    public void transform(Object o, Outputter<String> out) throws IOException, InvocationTargetException, IllegalAccessException {
        out.write(String.valueOf(o));
    }

    @Override public int matchPriority() { return Transformer.HIGH_PRIORITY; }
}
