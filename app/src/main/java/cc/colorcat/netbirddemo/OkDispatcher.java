package cc.colorcat.netbirddemo;

import android.content.Context;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import cc.colorcat.netbird.Headers;
import cc.colorcat.netbird.io.InputWrapper;
import cc.colorcat.netbird.request.Method;
import cc.colorcat.netbird.request.Request;
import cc.colorcat.netbird.response.Response;
import cc.colorcat.netbird.response.ResponseBody;
import cc.colorcat.netbird.response.SubResponseBody;
import cc.colorcat.netbird.sender.Dispatcher;
import cc.colorcat.netbird.util.Const;
import cc.colorcat.netbird.util.LogUtils;
import cc.colorcat.netbird.util.Utils;
import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

/**
 * Created by cxx on 2017/1/14.
 * xx.ch@outlook.com
 */

public class OkDispatcher implements Dispatcher {
    private Map<Object, Call> running = new ConcurrentHashMap<>();
    private OkHttpClient client;

    public OkDispatcher() {
        client = new OkHttpClient.Builder().build();
    }

    @Override
    public OkDispatcher connectTimeOut(int timeOut) {
        if (timeOut > 0) {
            client = client.newBuilder().connectTimeout(timeOut, TimeUnit.MILLISECONDS).build();
        }
        return this;
    }

    @Override
    public OkDispatcher readTimeOut(int timeOut) {
        if (timeOut > 0) {
            client = client.newBuilder().readTimeout(timeOut, TimeUnit.MILLISECONDS).build();
        }
        return this;
    }

    @Override
    public OkDispatcher enableCache(Context ctx, long cacheSize) {
        File cachePath = new File(ctx.getCacheDir(), "NetBird");
        Cache cache = new Cache(cachePath, cacheSize);
        client = client.newBuilder().cache(cache).build();
        return this;
    }

    @NonNull
    @Override
    public Response dispatch(String baseUrl, Request<?> req) {
        okhttp3.Request request = req.method() == Method.GET ? byGet(baseUrl, req) : byPost(baseUrl, req);
        int code = Const.CODE_CONNECT_ERROR;
        String msg = Const.MSG_CONNECT_ERROR;
        try {
            Call call = client.newCall(request);
            running.put(req.tag(), call);
            okhttp3.Response rep = call.execute();
            code = rep.code();
            msg = Utils.nullElse(rep.message(), msg);
            okhttp3.ResponseBody okBody = rep.body();
            InputStream data = okBody.byteStream();
            Headers headers = Headers.EMPTY;
            okhttp3.Headers okHeaders = rep.headers();
            if (okHeaders != null) {
                headers = Headers.create(okHeaders.toMultimap());
            }
            if (rep.isSuccessful()) {
                long length = okBody.contentLength();
                ResponseBody body = SubResponseBody.create(data, req.loadListener(), length, okBody.contentType().charset());
                if (body != null) return Response.newSuccess(code, msg, headers, body);
            }
        } catch (IOException e) {
            LogUtils.e(e);
            msg = Utils.emptyElse(e.getMessage(), Utils.formatMsg(msg, e));
        }
        return Response.newFailure(code, msg);
    }

    @Override
    public void finish(Request<?> req) {
        final Object tag = req.tag();
        running.remove(tag);
        LogUtils.i("Size", "OkDispatcher Running Size = " + running.size());
    }

    @Override
    public void cancel(Request<?> req) {
        final Object tag = req.tag();
        Call call = running.get(tag);
        if (call != null) {
            call.cancel();
        }
    }

    @Override
    public void cancelAll() {
        Collection<Call> calls = running.values();
        for (Call call : calls) {
            call.cancel();
        }
    }

    private static okhttp3.Request byGet(String baseUrl, Request<?> req) {
        okhttp3.Request.Builder builder = new okhttp3.Request.Builder().tag(req.tag());
        addHeader(builder, req);
        String url = Utils.url(baseUrl, req);
        String params = req.encodedParams();
        if (!Utils.isEmpty(params)) {
            url = url + '?' + params;
        }
        return builder.url(url).build();
    }

    private static okhttp3.Request byPost(String baseUrl, Request<?> req) {
        okhttp3.Request.Builder builder = new okhttp3.Request.Builder().tag(req.tag());
        addHeader(builder, req);
        String url = Utils.url(baseUrl, req);
        RequestBody body = req.packs().isEmpty() ? formBody(req) : multiBody(req);
        return builder.url(url).post(body).build();
    }

    private static void addHeader(okhttp3.Request.Builder builder, Request<?> req) {
        List<String> hNames = req.headerNames();
        if (!hNames.isEmpty()) {
            List<String> hValues = req.headerValues();
            for (int i = 0, size = hNames.size(); i < size; i++) {
                builder.addHeader(hNames.get(i), hValues.get(i));
            }
        }
    }

    private static okhttp3.RequestBody formBody(Request<?> req) {
        FormBody.Builder builder = new FormBody.Builder();
        List<String> names = req.paramNames();
        if (!names.isEmpty()) {
            List<String> values = req.paramValues();
            for (int i = 0, size = names.size(); i < size; i++) {
                builder.add(names.get(i), values.get(i));
            }
        }
        return builder.build();
    }

    private static okhttp3.RequestBody multiBody(Request<?> req) {
        MultipartBody.Builder builder = new MultipartBody.Builder();
        List<String> names = req.paramNames();
        if (!names.isEmpty()) {
            List<String> values = req.paramValues();
            for (int i = 0, size = names.size(); i < size; i++) {
                builder.addFormDataPart(names.get(i), values.get(i));
            }
        }
        List<Request.Pack> packs = req.packs();
        for (int i = 0, size = packs.size(); i < size; i++) {
            Request.Pack pack = packs.get(i);
            RequestBody body = fileBody(pack, req.uploadListener());
            builder.addFormDataPart(pack.name, pack.file.getName(), body);
        }
        return builder.build();
    }

    private static RequestBody fileBody(final Request.Pack pack, final Request.UploadListener listener) {
        return new RequestBody() {
            @Override
            public MediaType contentType() {
                return MediaType.parse(pack.contentType);
            }

            @Override
            public long contentLength() throws IOException {
                return pack.file.length();
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                File file = pack.file;
                InputStream is = listener != null ? InputWrapper.create(file, listener) : new FileInputStream(file);
                BufferedSource bs = Okio.buffer(Okio.source(is));
                sink.writeAll(bs);
            }
        };
    }
}
