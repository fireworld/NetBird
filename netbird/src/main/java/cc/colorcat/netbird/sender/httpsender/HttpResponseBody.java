package cc.colorcat.netbird.sender.httpsender;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

import cc.colorcat.netbird.response.ResponseBody;
import cc.colorcat.netbird.util.IoUtils;
import cc.colorcat.netbird.util.Utils;

/**
 * Created by cxx on 2016/12/15.
 * xx.ch@outlook.com
 */

final class HttpResponseBody extends ResponseBody {
    @Nullable
    private String charset;

    static HttpResponseBody create(@NonNull InputStream is, @Nullable String charset) {
        return new HttpResponseBody(is, charset);
    }

    private HttpResponseBody(@NonNull InputStream is, @Nullable String charset) {
        super(is);
        this.charset = charset;
    }

    @Override
    public String string() throws IOException {
        return IoUtils.readAndClose(is, charset);
    }

    @Override
    public Reader reader() {
        Charset c = Utils.charset(this.charset);
        return c != null ? new InputStreamReader(is, c) : new InputStreamReader(is);
    }
}
