package org.acme;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

public class GZIPOutputStreamWrapper extends GZIPOutputStream {

    public GZIPOutputStreamWrapper(OutputStream out) throws IOException {
        super(out);
    }
}