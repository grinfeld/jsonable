package com.mikerusoft.parser;

import com.mikerusoft.transform.TransformerFactory;
import com.mikerusoft.utils.Configuration;
import com.mikerusoft.utils.ContextManager;
import com.mikerusoft.utils.OutputStreamOutputter;
import com.mikerusoft.utils.StringBuilderOutputter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Grinfeld Mikhail
 * @since 12/3/2014.
 */
public class JsonWriter {

    private Log log = LogFactory.getLog(JsonWriter.class);

    public static void write (Object o, OutputStream out) throws IOException, InvocationTargetException, IllegalAccessException {
        write(o, out, null);
    }

    public static void write (Object o, StringBuilder out) throws IOException, InvocationTargetException, IllegalAccessException {
        write(o, out, null);
    }

    public static void write (Object o, OutputStream out, Configuration c) throws IOException, InvocationTargetException, IllegalAccessException {
        ContextManager.set(c);
        TransformerFactory.get(o).transform(o, new OutputStreamOutputter(out));
    }

    public static void write (Object o, StringBuilder out, Configuration c) throws IOException, InvocationTargetException, IllegalAccessException {
        ContextManager.set(c);
        TransformerFactory.get(o).transform(o, new StringBuilderOutputter(out));
    }
}
