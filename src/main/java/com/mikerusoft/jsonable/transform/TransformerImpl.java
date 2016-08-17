package com.mikerusoft.jsonable.transform;

import com.mikerusoft.jsonable.utils.Outputter;

import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Grinfeld Mikhail
 * @since 8/15/2015.
 */
public abstract class TransformerImpl implements Transformer {

    @Override
    public void transform(AnnotatedElement ao, Object o, Outputter<String> out, String... groups) throws IOException, IllegalAccessException, InvocationTargetException, InstantiationException {
        transform(o, out, groups);
    }
}
