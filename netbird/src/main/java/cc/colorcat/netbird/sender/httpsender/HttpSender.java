package cc.colorcat.netbird.sender.httpsender;

import android.support.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cc.colorcat.netbird.Headers;
import cc.colorcat.netbird.request.Method;
import cc.colorcat.netbird.request.Request;
import cc.colorcat.netbird.request.RequestBody;
import cc.colorcat.netbird.response.Response;
import cc.colorcat.netbird.sender.Sender;
import cc.colorcat.netbird.util.Const;
import cc.colorcat.netbird.util.LogUtils;
import cc.colorcat.netbird.util.Utils;


/**
 * Created by cxx on 16-11-15.
 * xx.ch@outlook.com
 */

public final class HttpSender implements Sender {
    private Map<Object, HttpURLConnection> running = new ConcurrentHashMap<>();
    private int connectTimeOut = 5000;
    private int readTimeOut = 3000;

    public HttpSender(int connectTimeOut, int readTimeOut) {
        this.connectTimeOut = connectTimeOut;
        this.readTimeOut = readTimeOut;
    }

    @NonNull
    @Override
    public Response send(String baseUrl, @NonNull Request<?> req, Object tag) {
        int code = Const.CODE_CONNECT_ERROR;
        String msg = Const.MSG_CONNECT_ERROR;
        Map<String, List<String>> map = null;
        InputStream is = null;
        try {
            HttpURLConnection conn = createConnection(baseUrl, req);
            running.put(tag, conn);
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
    public void cancel(Object tag) {
        if (tag != null) {
            HttpURLConnection conn = running.get(tag);
            if (conn != null) {
                conn.disconnect();
            }
            running.remove(tag);
        }
    }

    @Override
    public void cancelAll() {
        for (Object tag : running.keySet()) {
            running.get(tag).disconnect();
            running.remove(tag);
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
        conn.setUseCaches(false);
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
                OutputStream os = conn.getOutputStream();
                body.writeTo(os);
                os.flush();
            }
        }
    }
}
