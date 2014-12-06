package com.mikerusoft.transform;

import com.mikerusoft.utils.Outputter;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Grinfeld Mikhail
 * @since 5/25/2014.
 */
public class StringTransformer implements Transformer {

    private Log log = LogFactory.getLog(StringTransformer.class);

    @Override
    public boolean match(Object o) {
        return o.getClass().equals(CharSequence.class) || o.getClass().equals(String.class);
    }

    @Override
    public void transform(Object o, Outputter<String> out) throws IOException, InvocationTargetException, IllegalAccessException {
        out.write(("\"" + StringEscapeUtils.escapeEcmaScript((String) o) + "\""));
    }

    @Override public int matchPriority() { return Transformer.HIGH_PRIORITY + 1; }
}
