package cc.colorcat.netbird.request;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import cc.colorcat.netbird.io.ByteOutputStream;

final class FormBody extends RequestBody {
    private static final String CONTENT_TYPE = "application/x-www-form-urlencoded";
//    private static final String CONTENT_TYPE = "text/plain; charset=UTF-8";

    private final Parameters namesAndValues;

    public static FormBody create(Parameters namesAndValues) {
        return new FormBody(namesAndValues);
    }

    private FormBody(Parameters namesAndValues) {
        this.namesAndValues = namesAndValues;
    }

    public int size() {
        return namesAndValues.size();
    }

    public String name(int index) {
        return namesAndValues.name(index);
    }

    public String value(int index) {
        return namesAndValues.value(index);
    }

    @Override
    public String contentType() {
        return CONTENT_TYPE;
    }

    @Override
    public long contentLength() throws IOException {
        return writeOrCountBytes(null, true);
    }

    @Override
    public void writeTo(OutputStream os) throws IOException {
        writeOrCountBytes(os, false);
    }

    private long writeOrCountBytes(OutputStream os, boolean countBytes) throws IOException {
        long byteCount = 0L;

        OutputStream o = countBytes ? new ByteArrayOutputStream() : os;
        ByteOutputStream bos = new ByteOutputStream(o);

        for (int i = 0, size = namesAndValues.size(); i < size; i++) {
            if (i > 0) bos.writeByte('&');
            bos.writeUtf8(namesAndValues.name(i));
            bos.writeByte('=');
            bos.writeUtf8(namesAndValues.value(i));
        }

        if (countBytes) {
            byteCount = bos.size();
            bos.close();
        }

        return byteCount;
    }
}
