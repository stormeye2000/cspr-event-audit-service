package com.stormeye.event.audit.service;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author ian@meywood.com
 */
public class EventStream extends InputStream{

    private final InputStream inputStream;
    private final long size;

    public EventStream(final InputStream inputStream, final long size) {
        this.inputStream = inputStream;
        this.size = size;
    }

    @Override
    public int read() throws IOException {
        return inputStream.read();
    }

    public long getSize() {
        return size;
    }
}
