package com.mikerusoft.jsonable.parser;

import com.mikerusoft.jsonable.transform.TransformerFactory;
import com.mikerusoft.jsonable.utils.*;
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
        write(o, out, null, null, groups);
    }

    public static void write (Object o, OutputStream out, String charset,  String...groups) throws IOException, InvocationTargetException, IllegalAccessException {
        write(o, out, null, charset, groups);
    }

    public static void write (Object o, OutputStream out, Configuration c, String...groups) throws IOException, InvocationTargetException, IllegalAccessException {
        write(o, out, c, null, groups);
    }

    public static void write (Object o, OutputStream out, Configuration c, String charset, String...groups) throws IOException, InvocationTargetException, IllegalAccessException {
        write(o, new OutputStreamOutputter(out, charset), c, groups);
    }

    public static void write (Object o, StringBuilder out, String...groups) throws IOException, InvocationTargetException, IllegalAccessException {
        write(o, out, null, groups);
    }

    public static void write (Object o, StringBuilder out, Configuration c, String...groups) throws IOException, InvocationTargetException, IllegalAccessException {
        write(o, new StringBuilderOutputter(out), c, groups);
    }

    public static void write (Object o, Outputter<String> out, Configuration c, String...groups) throws IOException, InvocationTargetException, IllegalAccessException {
        ContextManager.set(c);
        TransformerFactory.get(o).transform(o, out, groups);
    }
}
