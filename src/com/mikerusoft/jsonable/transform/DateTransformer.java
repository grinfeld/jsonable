package com.mikerusoft.jsonable.transform;

import com.mikerusoft.jsonable.annotations.DateField;
import com.mikerusoft.jsonable.utils.Outputter;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;

/**
 * Transforms Date field defined by {@link com.mikerusoft.jsonable.annotations.DateField} annotation
 * @author Grinfeld Mikhail
 * @since 1/5/2015.
 */
public class DateTransformer implements Transformer {

    public static final int TIMESTAMP_TYPE = 0;
    public static final int STRING_TYPE = 1;

    @Override
    public boolean match(Object o) {
        return matchClass(o.getClass());
    }

    @Override
    public boolean matchClass(Class<?> clazz) {
        DateField an = clazz.getAnnotation(DateField.class);
        return (Date.class.isAssignableFrom(clazz) && an != null);
    }

    @Override
    public void transform(Object o, Outputter<String> out, String... groups) throws IOException, IllegalAccessException, InvocationTargetException {
        int type = o.getClass().getAnnotation(DateField.class).type();
        switch (type) {
            case TIMESTAMP_TYPE:
                out.write(String.valueOf(((Date) o).getTime()));
                break;
            case STRING_TYPE:
                out.write(o.toString().replaceAll("\"", "\\\""));
                break;
        }
    }

    @Override
    public int matchPriority() {
        return Transformer.HIGH_PRIORITY;
    }
}
