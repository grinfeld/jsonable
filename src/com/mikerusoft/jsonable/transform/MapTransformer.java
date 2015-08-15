package com.mikerusoft.jsonable.transform;

import com.mikerusoft.jsonable.utils.Configuration;
import com.mikerusoft.jsonable.utils.ContextManager;
import com.mikerusoft.jsonable.utils.Outputter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * Transforms Java Map to Json map
 * @author Grinfeld Mikhail
 * @since 5/25/2014.
 */
public class MapTransformer  extends TransformerImpl {

    private Log log = LogFactory.getLog(MapTransformer.class);

    @Override
    public boolean match(Object o) {
        return matchClass(o.getClass());
    }

    @Override
    public boolean matchClass(Class<?> clazz) {
        return Map.class.isAssignableFrom(clazz);
    }

    @Override
    public void transform(Object o, Outputter<String> out, String... groups) throws IOException, InvocationTargetException, IllegalAccessException {
        Configuration c = ContextManager.get(Configuration.class);
        boolean includeNull = Configuration.getBooleanProperty(c, Configuration.INCLUDE_NULL_PROPERTY, false);
        Map<?, ?> m = (Map<?, ?>)o;
        int i=0;
        out.write("{");
        for (Map.Entry<?, ?> entry : m.entrySet()) {
            Object p = entry.getValue();
            if (includeNull || p != null) {
                String key = "\"" + String.valueOf(entry.getKey()).replaceAll("\"", "\\\"") + "\"";
                out.write(key);
                out.write(":");
                TransformerFactory.get(p).transform(p, out, groups);
                if (i != m.size() - 1)
                    out.write(",");
                i++;
            }
        }
        out.write("}");
    }

    @Override public int matchPriority() { return Transformer.HIGH_PRIORITY; }
}
