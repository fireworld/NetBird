package cc.colorcat.netbird.sender;

import android.content.Context;
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

    void setConnectTimeOut(int milliseconds);

    void setReadTimeOut(int milliseconds);

    void enableCache(Context ctx, long cacheSize);

    void finish(Request<?> req);

    void cancel(Request<?> req);

    void cancelAll();
}