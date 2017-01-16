package cc.colorcat.netbird.response;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

import cc.colorcat.netbird.Headers;
import cc.colorcat.netbird.io.InputWrapper;
import cc.colorcat.netbird.util.IoUtils;
import cc.colorcat.netbird.util.Utils;

/**
 * Created by cxx on 2017/1/15.
 * xx.ch@outlook.com
 */

public class SubResponseBody extends ResponseBody {
    @Nullable
    protected Charset charset;

    /**
     * @return SubResponseBody or null if the given <tt>is</tt> is null
     */
    @Nullable
    public static SubResponseBody create(@NonNull Headers headers, InputStream is, Response.LoadListener listener) {
        if (is == null) return null;
        InputStream data = is;
        if (listener != null) {
            long contentLength = Utils.quiteParse(headers.value("Content-Length"), -1L);
            if (contentLength > 0) {
                data = InputWrapper.create(data, contentLength, listener);
            }
        }
        return new SubResponseBody(data, Utils.parseCharset(headers.value("Content-Type")));
    }

    @Nullable
    public static SubResponseBody create(InputStream is, Response.LoadListener listener, long contentLength, String charset) {
        return create(is, listener, contentLength, Utils.charset(charset));
    }

    @Nullable
    public static SubResponseBody create(InputStream is, Response.LoadListener listener, long contentLength, Charset charset) {
        if (is == null) return null;
        InputStream data = is;
        if (listener != null && contentLength > 0) {
            data = InputWrapper.create(data, contentLength, listener);
        }
        return new SubResponseBody(data, charset);
    }

    protected SubResponseBody(@NonNull InputStream is, @Nullable String charset) {
        this(is, Utils.charset(charset));
    }

    protected SubResponseBody(@NonNull InputStream is, @Nullable Charset charset) {
        super(is);
        this.charset = charset;
    }

    @Override
    public String string() throws IOException {
        return IoUtils.readAndClose(is, charset);
    }

    @Override
    public Reader reader() {
        return charset != null ? new InputStreamReader(is, charset) : new InputStreamReader(is);
    }
}
