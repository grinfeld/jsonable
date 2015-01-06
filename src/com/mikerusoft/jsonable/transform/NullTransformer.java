package com.mikerusoft.jsonable.transform;

import com.mikerusoft.jsonable.utils.Outputter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Transforms Null
 * @author Grinfeld Mikhail
 * @since 5/25/2014.
 */
public class NullTransformer implements Transformer {

    private Log log = LogFactory.getLog(NullTransformer.class);

    @Override
    public boolean match(Object o) {
        return o == null;
    }

    @Override
    public void transform(Object o, Outputter<String> out, String... groups) throws IOException, InvocationTargetException, IllegalAccessException {
        out.write("null");
    }

    @Override public int matchPriority() { return Transformer.LOW_PRIORITY; }
}
