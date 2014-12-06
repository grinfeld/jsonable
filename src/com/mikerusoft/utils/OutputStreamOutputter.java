package com.mikerusoft.utils;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Grinfeld Mikhail
 * @since 12/6/2014.
 */
public class OutputStreamOutputter implements Outputter<String> {
    OutputStream out;

    public OutputStreamOutputter(OutputStream out) {
        this.out = out;
    }

    @Override
    public void write(String data) throws IOException {
        out.write(data.getBytes());
    }
}
