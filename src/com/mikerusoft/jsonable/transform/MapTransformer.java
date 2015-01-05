package com.mikerusoft.jsonable.transform;

import com.mikerusoft.jsonable.utils.Outputter;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * @author Grinfeld Mikhail
 * @since 5/25/2014.
 */
public class MapTransformer implements Transformer {

    private Log log = LogFactory.getLog(MapTransformer.class);

    @Override
    public boolean match(Object o) {
        return Map.class.isAssignableFrom(o.getClass());
    }

    @Override
    public void transform(Object o, Outputter<String> out, String... groups) throws IOException, InvocationTargetException, IllegalAccessException {
        Map<?, ?> m = (Map<?, ?>)o;
        int i=0;
        out.write("{");
        for (Map.Entry<?, ?> entry : m.entrySet()) {
            String key = "\"" + StringEscapeUtils.escapeEcmaScript(String.valueOf(entry.getKey())) + "\"";
            out.write(key);
            out.write(":");
            Object p = entry.getValue();
            TransformerFactory.get(p).transform(p, out, groups);
            if (i != m.size() - 1)
                out.write(",");
            i++;
        }
        out.write("}");
    }

    @Override public int matchPriority() { return Transformer.HIGH_PRIORITY; }
}
