package com.mikerusoft.jsonable.transform;

import com.mikerusoft.jsonable.annotations.DateField;
import com.mikerusoft.jsonable.utils.Outputter;

import java.io.IOException;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;

/**
 * Transforms Date field defined by {@link com.mikerusoft.jsonable.annotations.DateField} annotation
 * @author Grinfeld Mikhail
 * @since 1/5/2015.
 */
public class DateTransformer  extends TransformerImpl {

    public static final int TIMESTAMP_TYPE = 0;
    public static final int STRING_TYPE = 1;

    @Override
    public boolean match(Object o) {
        return matchClass(o.getClass());
    }

    @Override
    public boolean matchClass(Class<?> clazz) {
        return (Date.class.isAssignableFrom(clazz));
    }

    @Override
    public void transform(Object o, Outputter<String> out, String... groups) throws IOException, IllegalAccessException, InvocationTargetException {
        transformDate(o.getClass(), o, out);
    }

    @Override
    public void transform(AnnotatedElement ao, Object o, Outputter<String> out, String... groups) throws IOException, IllegalAccessException, InvocationTargetException {
        transformDate(ao, o, out);
    }

    protected void transformDate(AnnotatedElement ao, Object o, Outputter<String> out) throws IOException, IllegalAccessException, InvocationTargetException {
        int type = ao.getAnnotation(DateField.class) != null ? ao.getAnnotation(DateField.class).type() : TIMESTAMP_TYPE;
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
