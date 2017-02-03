package cc.colorcat.netbird.sender.httpsender;

import android.content.Context;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cc.colorcat.netbird.Headers;
import cc.colorcat.netbird.request.Method;
import cc.colorcat.netbird.request.Request;
import cc.colorcat.netbird.request.RequestBody;
import cc.colorcat.netbird.response.Response;
import cc.colorcat.netbird.sender.Dispatcher;
import cc.colorcat.netbird.util.Const;
import cc.colorcat.netbird.util.IoUtils;
import cc.colorcat.netbird.util.LogUtils;
import cc.colorcat.netbird.util.Utils;


/**
 * Created by cxx on 16-11-15.
 * xx.ch@outlook.com
 */

public final class HttpDispatcher implements Dispatcher {
    private Map<Object, HttpURLConnection> running = new ConcurrentHashMap<>();
    private boolean enableCache = false;
    private int connectTimeOut = 10000;
    private int readTimeOut = 10000;

    @Override
    public void setConnectTimeOut(int milliseconds) {
        if (milliseconds <= 0) {
            throw new IllegalArgumentException("ConnectTimeOut must be greater than 0");
        }
        this.connectTimeOut = milliseconds;
    }

    @Override
    public void setReadTimeOut(int milliseconds) {
        if (milliseconds <= 0) {
            throw new IllegalArgumentException("ReadTimeOut must be greater than 0");
        }
        this.readTimeOut = milliseconds;
    }

    @Override
    public void enableCache(Context ctx, long cacheSize) {
        if (cacheSize <= 0L) {
            throw new IllegalArgumentException("cacheSize must be greater than 0");
        }
        try {
            File cachePath = new File(ctx.getCacheDir(), "NetBird");
            Class.forName("android.net.http.HttpResponseCache")
                    .getMethod("install", File.class, long.class)
                    .invoke(null, cachePath, cacheSize);
            this.enableCache = true;
        } catch (Exception e) {
            LogUtils.e(e);
            this.enableCache = false;
        }
    }

    @NonNull
    @Override
    public Response dispatch(String baseUrl, Request<?> req) {
        int code = Const.CODE_CONNECT_ERROR;
        String msg = Const.MSG_CONNECT_ERROR;
        Map<String, List<String>> map = null;
        InputStream is = null;
        try {
            HttpURLConnection conn = createConnection(baseUrl, req);
            running.put(req.tag(), conn);
            if (req.method() == Method.POST) {
                addBodyIfExists(conn, req);
            }
            code = conn.getResponseCode();
            msg = Utils.nullElse(conn.getResponseMessage(), "");
            map = conn.getHeaderFields();
            if (code == HttpURLConnection.HTTP_OK) {
                is = conn.getInputStream();
            }
        } catch (IOException e) {
            LogUtils.e(e);
            msg = Utils.emptyElse(e.getMessage(), Utils.formatMsg(msg, e));
        }
        Headers headers = Headers.EMPTY;
        if (map != null) {
            headers = Headers.create(map);
        }
        return HttpResponse.create(headers, is, code, msg, req.loadListener());
    }

    @Override
    public void finish(Request<?> req) {
        final Object tag = req.tag();
        HttpURLConnection conn = running.get(tag);
        if (conn != null) {
            conn.disconnect();
            running.remove(tag);
        }
    }

    @Override
    public void cancel(Request<?> req) {
        final Object tag = req.tag();
        HttpURLConnection conn = running.get(tag);
        if (conn != null) {
            conn.disconnect();
        }
    }

    @Override
    public void cancelAll() {
        Collection<HttpURLConnection> connections = running.values();
        for (HttpURLConnection conn : connections) {
            conn.disconnect();
        }
    }

    private HttpURLConnection createConnection(String baseUrl, Request<?> req) throws IOException {
        String url = url(baseUrl, req);
        Method m = req.method();
        if (m == Method.GET) {
            String params = req.encodedParams();
            if (!Utils.isEmpty(params)) {
                url = url + '?' + req.encodedParams();
            }
        }
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setConnectTimeout(connectTimeOut);
        conn.setReadTimeout(readTimeOut);
        conn.setDoInput(true);
        conn.setRequestMethod(m.name());
        if (m == Method.POST) {
            conn.setDoOutput(true);
        }
        conn.setUseCaches(this.enableCache);
        List<String> names = req.headerNames();
        if (!names.isEmpty()) {
            List<String> values = req.headerValues();
            for (int i = names.size() - 1; i >= 0; i--) {
                conn.addRequestProperty(names.get(i), values.get(i));
            }
        }
        conn.setRequestProperty("Connection", "Keep-Alive");
        return conn;
    }

    private static String url(String baseUrl, Request<?> req) {
        String url = Utils.emptyElse(req.url(), baseUrl);
        String path = req.path();
        if (!Utils.isEmpty(path)) {
            url += path;
        }
        return url;
    }

    private static void addBodyIfExists(HttpURLConnection conn, Request<?> req) throws IOException {
        RequestBody body = req.body();
        if (body != null) {
            long contentLength = body.contentLength();
            if (contentLength > 0) {
                conn.setRequestProperty("Content-Type", body.contentType());
                OutputStream os = null;
                try {
                    os = conn.getOutputStream();
                    body.writeTo(os);
                    os.flush();
                } finally {
                    IoUtils.close(os);
                }
            }
        }
    }
}
