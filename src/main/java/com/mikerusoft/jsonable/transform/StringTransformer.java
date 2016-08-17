package com.mikerusoft.jsonable.transform;

import com.mikerusoft.jsonable.utils.Outputter;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Transforms String into Json. Double quot is escaped
 * @author Grinfeld Mikhail
 * @since 5/25/2014.
 */
public class StringTransformer  extends TransformerImpl {

    private Log log = LogFactory.getLog(StringTransformer.class);

    @Override
    public boolean match(Object o) {
        return matchClass(o.getClass());
    }

    @Override
    public boolean matchClass(Class<?> clazz) {
        return clazz.equals(CharSequence.class) || clazz.equals(String.class);
    }

    @Override
    public void transform(Object o, Outputter<String> out, String... groups) throws IOException, InvocationTargetException, IllegalAccessException {
        out.write("\"" + StringEscapeUtils.escapeJson((String) o).trim() + "\"");
    }

    @Override public int matchPriority() { return Transformer.HIGH_PRIORITY + 1; }
}
