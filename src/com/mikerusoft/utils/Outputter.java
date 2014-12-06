package com.mikerusoft.utils;

import java.io.IOException;

/**
 * @author Grinfeld Mikhail
 * @since 12/6/2014.
 */
public interface Outputter<K> {
    public void write(K data) throws IOException;
}
