package cc.colorcat.netbird.request;

import java.io.IOException;
import java.io.OutputStream;


public abstract class RequestBody {
    /**
     * Returns the Content-Type header for this body.
     */
    public abstract String contentType();

    /**
     * Returns the number of bytes that will be written to {@code OutputStream} or -1 if that count is unknown.
     */
    public long contentLength() throws IOException {
        return -1;
    }

    /**
     * Writes the content of this request to {@code out}.
     */
    public abstract void writeTo(OutputStream os) throws IOException;
}
