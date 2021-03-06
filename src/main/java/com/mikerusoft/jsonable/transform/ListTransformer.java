package com.mikerusoft.jsonable.transform;

import com.mikerusoft.jsonable.utils.Outputter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Transforms list into JSON array
 * @author Grinfeld Mikhail
 * @since 5/25/2014.
 */
public class ListTransformer extends TransformerImpl {

    private Log log = LogFactory.getLog(ListTransformer.class);

    @Override
    public boolean match(Object o) {
        return matchClass(o.getClass());
    }

    @Override
    public boolean matchClass(Class<?> clazz) {
        return List.class.isAssignableFrom(clazz);
    }

    @Override
    public void transform(Object o, Outputter<String> out, String... groups) throws IOException, InvocationTargetException, IllegalAccessException, InstantiationException {
        List<?> l = (List<?>)o;
        out.write("[");
        for (int i=0; i<l.size(); i++) {
            Object p = l.get(i);
            TransformerFactory.get(p).transform(p, out, groups);
            if (i != l.size() - 1)
                out.write(",");
        }
        out.write("]");
    }

    @Override public int matchPriority() { return Transformer.HIGH_PRIORITY + 1; }
}
