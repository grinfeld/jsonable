package com.mikerusoft.jsonable.transform;

import com.mikerusoft.jsonable.utils.ConfInfo;
import com.mikerusoft.jsonable.utils.Outputter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;

/**
 * @author Grinfeld Mikhail
 * @since 5/25/2014.
 */
public class PrimitiveTransformer  extends TransformerImpl {

    private Log log = LogFactory.getLog(PrimitiveTransformer.class);

    @Override
    public boolean match(Object o) {
        return matchClass(o.getClass());
    }

    @Override
    public boolean matchClass(Class<?> clazz) {
        return clazz.isPrimitive() || Boolean.class.equals(clazz) || Byte.class.equals(clazz) ||
            Short.class.equals(clazz) || Character.class.equals(clazz) ||
            Integer.class.equals(clazz) || Long.class.equals(clazz) ||
            Double.class.equals(clazz) || Float.class.equals(clazz) || clazz.equals(BigDecimal.class);
    }

    @Override
    public void transform(Object o, Outputter<String> out, String... groups) throws IOException, InvocationTargetException, IllegalAccessException {
        boolean includePrimitiveClass = ConfInfo.isIncludePrimitiveClass(); // Configuration.getBooleanProperty(c, Configuration.INCLUDE_PRIMITIVE_CLASS_PROPERTY, false);
        if (!includePrimitiveClass) {
            out.write(String.valueOf(o));
        } else {
            String cl = ConfInfo.getClassProperty(); // Configuration.getStringProperty(c, Configuration.CLASS_PROPERTY, Configuration.DEFAULT_CLASS_PROPERTY_VALUE);
            out.write("{\"value\": " + String.valueOf(o) + ",\"" + cl + "\":\"" + o.getClass().getName() + "\"}");
        }

    }

    @Override public int matchPriority() { return Transformer.HIGH_PRIORITY; }
}
