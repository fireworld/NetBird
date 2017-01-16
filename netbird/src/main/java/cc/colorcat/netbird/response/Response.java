package cc.colorcat.netbird.response;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.ref.WeakReference;

import cc.colorcat.netbird.Headers;
import cc.colorcat.netbird.io.ProgressListener;
import cc.colorcat.netbird.util.Utils;


/**
 * Created by cxx on 16-3-11.
 * xx.ch@outlook.com
 */
public class Response {
    protected int code;
    protected String msg;
    protected Headers headers;
    protected ResponseBody body;

    public static Response newFailure(int code, String msg) {
        return new Response(code, Utils.nonNull(msg, "msg == null"), null, null);
    }

    public static Response newSuccess(int code, @NonNull String msg, @NonNull Headers headers, @NonNull ResponseBody body) {
        return new Response(code,
                Utils.nonNull(msg, "msg == null"),
                Utils.nonNull(headers, "headers == null"),
                Utils.nonNull(body, "body == null")
        );
    }

    public static Response create(int code, @NonNull String msg, Headers headers, ResponseBody body) {
        return new Response(code, Utils.nonNull(msg, "msg == null"), headers, body);
    }

    protected Response(int code, @NonNull String msg, Headers headers, ResponseBody body) {
        this.code = code;
        this.msg = msg;
        this.headers = Utils.nullElse(headers, Headers.EMPTY);
        this.body = body;
    }

    public ResponseBody body() {
        return body;
    }

    public int code() {
        return code;
    }

    public String msg() {
        return msg;
    }

    public long contentLength() {
        return headers != null ? Utils.quiteParse(headers.value("Content-Length"), -1L) : -1L;
    }

    @Nullable
    public String contentType() {
        return headers != null ? headers.value("Content-Type") : null;
    }

    @Nullable
    public String charset() {
        return Utils.parseCharset(contentType());
    }

    public String charset(String defCharset) {
        return Utils.emptyElse(charset(), defCharset);
    }

    public Headers headers() {
        return headers;
    }

    /**
     * on MainThread
     */
    public interface Callback<R> {
        void onStart();

        void onSuccess(@NonNull R result);

        void onFailure(int code, @NonNull String msg);

        void onFinish();
    }

    public static abstract class SimpleCallback<R> implements Callback<R> {
        @Override
        public void onStart() {

        }

        @Override
        public void onFinish() {

        }
    }

    public static abstract class WeakCallback<R, V> implements Callback<R> {
        private WeakReference<V> ref;

        public WeakCallback(@NonNull V v) {
            ref = new WeakReference<>(Utils.nonNull(v, "V == null"));
        }

        @Override
        public final void onStart() {
            V v = ref.get();
            if (v != null) {
                onStart(v);
            }
        }

        @Override
        public final void onSuccess(@NonNull R result) {
            V v = ref.get();
            if (v != null) {
                onSuccess(v, result);
            }
        }

        @Override
        public final void onFailure(int code, @NonNull String msg) {
            V v = ref.get();
            if (v != null) {
                onFailure(v, code, msg);
            }
        }

        @Override
        public final void onFinish() {
            V v = ref.get();
            if (v != null) {
                onFinish(v);
            }
        }

        public void onStart(@NonNull V v) {
        }

        public abstract void onSuccess(@NonNull V v, @NonNull R result);

        public abstract void onFailure(@NonNull V v, int code, @NonNull String msg);

        public void onFinish(@NonNull V v) {
        }
    }

    public interface LoadListener extends ProgressListener {

        @Override
        void onChanged(long read, long total, int percent);
    }
}
