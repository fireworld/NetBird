package cc.colorcat.netbird.request;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import cc.colorcat.netbird.io.ByteOutputStream;

/**
 * Created by cxx on 16-12-15.
 * xx.ch@outlook.com
 */

class MixBody extends RequestBody {
    private static final String MIX = "multipart/form-data; boundary=";
    private static final byte[] CRLF = {'\r', '\n'};
    private static final byte[] DASHDASH = {'-', '-'};

    private final String boundary = "===" + System.currentTimeMillis() + "===";
    private FormBody formBody;
    private List<FileBody> fileBodies;
    private long contentLength = -1;

    public static MixBody create(FormBody formBody, List<FileBody> fileBodies) {
        return new MixBody(formBody, fileBodies);
    }

    private MixBody(FormBody formBody, List<FileBody> fileBody) {
        this.formBody = formBody;
        this.fileBodies = fileBody;
    }

    @Override
    public String contentType() {
        return MIX + boundary;
    }

    @Override
    public long contentLength() throws IOException {
        if (contentLength == -1) {
            long size = writeOrCountBytes(null, true);
            if (size > 0) {
                contentLength = size;
            }
        }
        return contentLength;
    }

    @Override
    public void writeTo(OutputStream os) throws IOException {
        writeOrCountBytes(os, false);
    }


    private long writeOrCountBytes(OutputStream os, boolean countBytes) throws IOException {
        long byteCount = 0L;

        OutputStream o = countBytes ? new ByteArrayOutputStream() : os;
        ByteOutputStream bos = new ByteOutputStream(o);

        for (int i = 0, size = formBody.size(); i < size; i++) {
            String name = formBody.name(i);
            String value = formBody.value(i);

            bos.write(DASHDASH);
            bos.writeUtf8(boundary);
            bos.write(CRLF);

            bos.writeUtf8("Content-Disposition: form-data; name=\"" + name + "\"");
            bos.write(CRLF);

            bos.writeUtf8("Content-Type: " + formBody.contentType());
            bos.write(CRLF);

            bos.write(CRLF);
            bos.writeUtf8(value);
            bos.write(CRLF);
        }

        for (int i = 0, size = fileBodies.size(); i < size; i++) {
            FileBody body = fileBodies.get(i);
            long contentLength = body.contentLength();
            if (contentLength != -1) {
                bos.write(DASHDASH);
                bos.writeUtf8(boundary);
                bos.write(CRLF);

                bos.writeUtf8("Content-Disposition: form-data; name=\"" + body.name() + "\"; filename=\"" + body.fileName() + "\"");
                bos.write(CRLF);

                bos.writeUtf8("Content-Type: " + body.contentType());
                bos.write(CRLF);

                bos.writeUtf8("Content-Transfer-Encoding: BINARY");
                bos.write(CRLF);

                bos.write(CRLF);
                if (countBytes) {
                    byteCount += contentLength;
                } else {
                    body.writeTo(bos);
                }

                bos.write(CRLF);
            } else if (countBytes) {
                bos.close();
                return -1L;
            }
        }

        bos.write(CRLF);
        bos.write(DASHDASH);
        bos.writeUtf8(boundary);
        bos.write(DASHDASH);
        bos.write(CRLF);

        if (countBytes) {
            byteCount += bos.size();
            bos.close();
        }

        return byteCount;
    }
}
