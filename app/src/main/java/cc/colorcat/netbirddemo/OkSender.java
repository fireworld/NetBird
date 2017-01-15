package cc.colorcat.netbirddemo;

import android.content.Context;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

import cc.colorcat.netbird.Headers;
import cc.colorcat.netbird.io.InputWrapper;
import cc.colorcat.netbird.request.Method;
import cc.colorcat.netbird.request.Request;
import cc.colorcat.netbird.response.Response;
import cc.colorcat.netbird.response.ResponseBodyImp;
import cc.colorcat.netbird.sender.Sender;
import cc.colorcat.netbird.util.Const;
import cc.colorcat.netbird.util.LogUtils;
import cc.colorcat.netbird.util.Utils;
import okhttp3.Cache;
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

public class OkSender implements Sender {
    private OkHttpClient client;

    public OkSender(@NonNull Context ctx) {
        File cachePath = new File(ctx.getCacheDir(), "NetBird");
        Cache cache = new Cache(cachePath, 50 * 1024 * 1000);
        client = new OkHttpClient.Builder().cache(cache).build();
    }

    @NonNull
    @Override
    public Response send(String baseUrl, Request<?> req, Object tag) {
        okhttp3.Request request = req.method() == Method.GET ? byGet(baseUrl, req, tag) : byPost(baseUrl, req, tag);
        int code = Const.CODE_CONNECT_ERROR;
        String msg = Const.MSG_CONNECT_ERROR;
        try {
            okhttp3.Response rep = client.newCall(request).execute();
            code = rep.code();
            msg = Utils.nullElse(rep.message(), msg);
            okhttp3.ResponseBody body = rep.body();
            InputStream data = body.byteStream();
            Headers headers = Headers.EMPTY;
            okhttp3.Headers oHeaders = rep.headers();
            if (oHeaders != null) {
                headers = Headers.create(oHeaders.toMultimap());
            }
            if (rep.isSuccessful() && data != null) {
                long length = body.contentLength();
                if (length > 0) {
                    data = InputWrapper.create(data, length, req.loadListener());
                }
                Charset charset = body.contentType().charset(Charset.defaultCharset());
                return Response.newSuccess(code, msg, headers, new ResponseBodyImp(data, charset));
            }
        } catch (IOException e) {
            LogUtils.e(e);
            msg = Utils.emptyElse(e.getMessage(), Utils.formatMsg(msg, e));
        }

        return Response.newFailure(code, msg);
    }

    private static Response createResponse(Request<?> req, okhttp3.Response rep) {
        int code = rep.code();
        String msg = Utils.nullElse(rep.message(), "");
        okhttp3.ResponseBody body = rep.body();
        InputStream data = body.byteStream();
        Headers headers = Headers.EMPTY;
        okhttp3.Headers oHeaders = rep.headers();
        if (oHeaders != null) {
            headers = Headers.create(oHeaders.toMultimap());
        }
        if (rep.isSuccessful() && data != null) {
            long length = body.contentLength();
            if (length > 0) {
                data = InputWrapper.create(data, length, req.loadListener());
            }
            Charset charset = body.contentType().charset(Charset.defaultCharset());
            return Response.newSuccess(code, msg, headers, new ResponseBodyImp(data, charset));
        } else {
            return Response.newFailure(code, msg);
        }
    }

    private okhttp3.Request byGet(String baseUrl, Request<?> req, Object tag) {
        okhttp3.Request.Builder builder = new okhttp3.Request.Builder();
        setHeader(builder, req);
        String url = Utils.url(baseUrl, req);
        String params = req.encodedParams();
        if (!Utils.isEmpty(params)) {
            url = url + '?' + params;
        }
        return builder.url(url).build();
    }

    private okhttp3.Request byPost(String baseUrl, Request<?> req, Object tag) {
        okhttp3.Request.Builder builder = new okhttp3.Request.Builder();
        setHeader(builder, req);
        String url = Utils.url(baseUrl, req);
        RequestBody body = req.packs().isEmpty() ? formBody(req) : multiBody(req);
        return builder.url(url).post(body).build();
    }

    private okhttp3.RequestBody formBody(Request<?> req) {
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

    private okhttp3.RequestBody multiBody(Request<?> req) {
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

    private void setHeader(okhttp3.Request.Builder builder, Request<?> req) {
        List<String> hNames = req.headerNames();
        if (!hNames.isEmpty()) {
            List<String> hValues = req.headerValues();
            for (int i = 0, size = hNames.size(); i < size; i++) {
                builder.addHeader(hNames.get(i), hValues.get(i));
            }
        }
    }


    @Override
    public void cancel(Object tag) {

    }

    @Override
    public void cancelAll() {

    }
}
