package cc.colorcat.netbird.sender.httpsender;

import android.support.annotation.NonNull;

import java.io.InputStream;

import cc.colorcat.netbird.Headers;
import cc.colorcat.netbird.response.Response;
import cc.colorcat.netbird.response.ResponseBody;


/**
 * Created by cxx on 16-12-13.
 * xx.ch@outlook.com
 */

final class HttpResponse extends Response {

    static HttpResponse create(@NonNull Headers headers, InputStream is, int code, @NonNull String msg, LoadListener listener) {
        HttpResponseBody body = HttpResponseBody.create(headers, is, listener);
        return new HttpResponse(code, msg, headers, body);
    }

    private HttpResponse(int code, @NonNull String msg, Headers headers, ResponseBody body) {
        super(code, msg, headers, body);
    }
}
