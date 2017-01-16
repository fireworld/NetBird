package cc.colorcat.netbird.sender;

import android.support.annotation.NonNull;

import cc.colorcat.netbird.request.Request;
import cc.colorcat.netbird.response.Response;

/**
 * Created by mic on 16-3-11.
 * xx.ch@outlook.com
 */
public interface Dispatcher {

    @NonNull
    Response dispatch(String baseUrl, Request<?> req);

    void finish(Request<?> req);

    void cancel(Request<?> req);

    void cancelAll();
}