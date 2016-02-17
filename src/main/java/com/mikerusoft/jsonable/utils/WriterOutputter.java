package com.mikerusoft.jsonable.utils;

import java.io.IOException;
import java.io.Writer;

public class WriterOutputter implements Outputter<String> {
    Writer writer;

    public WriterOutputter(Writer writer) {
        this.writer = writer;
    }

    @Override
    public void write(String data) throws IOException {
        writer.write(data);
    }
}
