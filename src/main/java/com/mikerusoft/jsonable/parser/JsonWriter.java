package com.mikerusoft.jsonable.parser;

import com.mikerusoft.jsonable.transform.TransformerFactory;
import com.mikerusoft.jsonable.utils.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Grinfeld Mikhail
 * @since 12/3/2014.
 */
public class JsonWriter {

    private Log log = LogFactory.getLog(JsonWriter.class);

    public static void write (Object o, Outputter<String> out, String...groups) throws IOException, InvocationTargetException, IllegalAccessException, InstantiationException {
        TransformerFactory.get(o).transform(o, out, groups);
    }

    public static void write (Object o, OutputStream out, String charset,  String...groups) throws IOException, InvocationTargetException, IllegalAccessException, InstantiationException {
        write(o, out, null, charset, groups);
    }


    public static void write (Object o, Writer out, String...groups) throws IOException, InvocationTargetException, IllegalAccessException, InstantiationException {
        write(o, out, null, groups);
    }

    public static void write (Object o, StringBuilder out, String...groups) throws IOException, InvocationTargetException, IllegalAccessException, InstantiationException {
        write(o, out, null, groups);
    }

    @Deprecated
    /**
     * No more Configuration as parameter. Use {@link ConfInfo} instead.
     */
    public static void write (Object o, StringBuilder out, Configuration c, String...groups) throws IOException, InvocationTargetException, IllegalAccessException, InstantiationException {
        write(o, new StringBuilderOutputter(out), c, groups);
    }

    @Deprecated
    /**
     * No more Configuration as parameter. Use {@link ConfInfo} instead.
     */
    public static void write (Object o, Outputter<String> out, Configuration c, String...groups) throws IOException, InvocationTargetException, IllegalAccessException, InstantiationException {
        write(o, out, groups);
    }

    @Deprecated
    /**
     * No more Configuration as parameter. Use {@link ConfInfo} instead.
     */
    public static void write (Object o, OutputStream out, Configuration c, String...groups) throws IOException, InvocationTargetException, IllegalAccessException, InstantiationException {
        write(o, out, c, null, groups);
    }

    @Deprecated
    /**
     * No more Configuration as parameter. Use {@link ConfInfo} instead.
     */
    public static void write (Object o, OutputStream out, Configuration c, String charset, String...groups) throws IOException, InvocationTargetException, IllegalAccessException, InstantiationException {
        write(o, new OutputStreamOutputter(out, charset), c, groups);
    }

    @Deprecated
    /**
     * No more Configuration as parameter. Use {@link ConfInfo} instead.
     */
    public static void write (Object o, Writer out, Configuration c, String...groups) throws IOException, InvocationTargetException, IllegalAccessException, InstantiationException {
        write(o, new WriterOutputter(out), c, groups);
    }

}
