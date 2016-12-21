package cc.colorcat.netbird.io;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * Created by cxx on 2016/12/19.
 * xx.ch@outlook.com
 */

public class ByteOutputStream extends FilterOutputStream {
    private static final Charset UTF8 = Charset.forName("UTF-8");

    private long written;

    public ByteOutputStream(OutputStream out) {
        super(out);
    }

    private void incCount(int value) {
        long temp = written + value;
        if (temp < 0) {
            temp = Long.MAX_VALUE;
        }
        written = temp;
    }

    @Override
    public synchronized void write(int b) throws IOException {
        out.write(b);
        incCount(1);
    }

    @Override
    public synchronized void write(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
        incCount(len);
    }

    public void writeUtf8(String s) throws IOException {
        byte[] bytes = s.getBytes(UTF8);
        write(bytes, 0, bytes.length);
    }

    public final void writeByte(char c) throws IOException {
        out.write(c);
        incCount(1);
    }

    @Override
    public void flush() throws IOException {
        out.flush();
    }

    public final long size() {
        return written;
    }
}
