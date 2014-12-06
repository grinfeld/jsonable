package com.mikerusoft.transform;

import com.mikerusoft.utils.Outputter;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Grinfeld Mikhail
 * @since 5/25/2014.
 */
public interface Transformer {
    public static final int HIGH_PRIORITY = 0;
    public static final int LOW_PRIORITY = Integer.MAX_VALUE;

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
     * @throws IOException
     */
    void transform(Object o, Outputter<String> out) throws IOException, IllegalAccessException, InvocationTargetException;

    /**
     * Defines match priority, i.e. when 2 or more Transformers matches Object, defines order between them. Lower, means match better
     * @return priority
     */
    int matchPriority();
}
