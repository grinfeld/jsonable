package com.mikerusoft.jsonable.utils;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Grinfeld Mikhail
 * @since 12/6/2014.
 */
public class OutputStreamOutputter implements Outputter<String> {
    OutputStream out;
    String charset = null;

    public OutputStreamOutputter(OutputStream out) {
        this.out = out;
    }

    public OutputStreamOutputter(OutputStream out, String charset) {
        this.out = out;
        this.charset = charset;
    }

    @Override
    public void write(String data) throws IOException {
        if (charset == null)
            out.write(data.getBytes());
        else
            out.write(data.getBytes(charset));
    }
}
