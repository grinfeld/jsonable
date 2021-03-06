package com.mikerusoft.jsonable.transform;

import com.mikerusoft.jsonable.utils.Outputter;

import java.io.IOException;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Grinfeld Mikhail
 * @since 5/25/2014.
 */
public interface Transformer {
    public static final int HIGH_PRIORITY = 0;
    public static final int LOW_PRIORITY = Integer.MAX_VALUE;
    public static final int MAX_INHERITANCE_DEPTH = 20;

    /**
     * Tests if Object matches current Transformer
     * @param o object to test
     * @return return true if object matches current Transofrmer
     */
    boolean match(Object o);

    boolean matchClass(Class<?> clazz);

    /**
     * Transforms Object to JSON and writes into OutputStream
     * @param o Object to transform to JSON
     * @param out StringBuilder to write into
     * @param groups list of groups to use for current conversion  @throws IOException
     * @throws IOException on writing output failure
     * @throws IllegalAccessException on failure to create appropriate class
     * @throws InvocationTargetException on failure to create appropriate class
     * @throws InstantiationException on failure to create appropriate class
     */
    void transform(Object o, Outputter<String> out, String... groups) throws IOException, IllegalAccessException, InvocationTargetException, InstantiationException;

    /**
     * Transforms Object to JSON and writes into OutputStream
     * @param ao meta data we worked on it
     * @param o Object to transform to JSON
     * @param out StringBuilder to write into
     * @param groups list of groups to use for current conversion  @throws IOException
     * @throws IOException on writting output failure
     * @throws IllegalAccessException on failure to create appropriate class
     * @throws InvocationTargetException on failure to create appropriate class
     * @throws InstantiationException on failure to create appropriate class
     */
    void transform(AnnotatedElement ao, Object o, Outputter<String> out, String... groups) throws IOException, IllegalAccessException, InvocationTargetException, InstantiationException;

    /**
     * Defines match priority, i.e. when 2 or more Transformers matches Object, defines order between them. Lower, means match better
     * @return priority
     */
    int matchPriority();
}
