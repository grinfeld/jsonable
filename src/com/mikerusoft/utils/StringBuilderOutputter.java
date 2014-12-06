package com.mikerusoft.utils;

/**
 * @author Grinfeld Mikhail
 * @since 12/6/2014.
 */
public class StringBuilderOutputter implements Outputter<String> {
    StringBuilder out;

    public StringBuilderOutputter(StringBuilder out) {
        this.out = out;
    }

    @Override
    public void write(String data) {
        out.append(data);
    }
}
