package cc.colorcat.netbird.dispatcher.httpdispatcher;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.InputStream;
import java.nio.charset.Charset;

import cc.colorcat.netbird.Headers;
import cc.colorcat.netbird.io.InputWrapper;
import cc.colorcat.netbird.response.Response;
import cc.colorcat.netbird.response.SubResponseBody;
import cc.colorcat.netbird.util.Utils;

/**
 * Created by cxx on 2016/12/15.
 * xx.ch@outlook.com
 */

final class HttpResponseBody extends SubResponseBody {

    /**
     * @return SubResponseBody or null if the given <tt>is</tt> is null
     */
    @Nullable
    public static HttpResponseBody create(@NonNull Headers headers, InputStream is, Response.LoadListener listener) {
        if (is == null) return null;
        InputStream data = is;
        if (listener != null) {
            long contentLength = Utils.quiteParse(headers.value("Content-Length"), -1L);
            if (contentLength > 0) {
                data = InputWrapper.create(data, contentLength, listener);
            }
        }
        return new HttpResponseBody(data, Utils.charset(headers.value("Content-Type")));
    }

    private HttpResponseBody(@NonNull InputStream is, @Nullable Charset charset) {
        super(is, charset);
    }
}
