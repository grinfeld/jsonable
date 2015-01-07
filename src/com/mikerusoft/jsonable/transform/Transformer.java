package com.mikerusoft.jsonable.transform;

import com.mikerusoft.jsonable.utils.Outputter;

import java.io.IOException;
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

    /**
     * Transforms Object to JSON and writes into OutputStream
     * @param o Object to transform to JSON
     * @param out StringBuilder to write into
     * @param groups list of groups to use for current conversion
     * @throws IOException
     */
    void transform(Object o, Outputter<String> out, String... groups) throws IOException, IllegalAccessException, InvocationTargetException;

    /**
     * Defines match priority, i.e. when 2 or more Transformers matches Object, defines order between them. Lower, means match better
     * @return priority
     */
    int matchPriority();
}
