package com.mikerusoft.jsonable.parser;

import com.mikerusoft.jsonable.transform.TransformerFactory;
import com.mikerusoft.jsonable.utils.Configuration;
import com.mikerusoft.jsonable.utils.ContextManager;
import com.mikerusoft.jsonable.utils.OutputStreamOutputter;
import com.mikerusoft.jsonable.utils.StringBuilderOutputter;
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

    public static void write (Object o, OutputStream out, String...groups) throws IOException, InvocationTargetException, IllegalAccessException {
        write(o, out, null, groups);
    }

    public static void write (Object o, StringBuilder out, String...groups) throws IOException, InvocationTargetException, IllegalAccessException {
        write(o, out, null, groups);
    }

    public static void write (Object o, OutputStream out, Configuration c, String...groups) throws IOException, InvocationTargetException, IllegalAccessException {
        ContextManager.set(c);
        TransformerFactory.get(o).transform(o, new OutputStreamOutputter(out), groups);
    }

    public static void write (Object o, StringBuilder out, Configuration c, String...groups) throws IOException, InvocationTargetException, IllegalAccessException {
        ContextManager.set(c);
        TransformerFactory.get(o).transform(o, new StringBuilderOutputter(out), groups);
    }
}
