package cc.colorcat.netbirddemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.widget.ImageView;

import java.util.List;

import cc.colorcat.netbird.Headers;
import cc.colorcat.netbird.NetBird;
import cc.colorcat.netbird.Processor;
import cc.colorcat.netbird.parser.BitmapParser;
import cc.colorcat.netbird.request.Method;
import cc.colorcat.netbird.request.Request;
import cc.colorcat.netbird.response.Response;
import cc.colorcat.netbird.util.LogUtils;
import cc.colorcat.netbird.util.Utils;

/**
 * Created by cxx on 2016/12/21.
 * xx.ch@outlook.com
 */

public class ApiService {
    public static final String TAG = "ApiService";

    private static NetBird bird;
    private static final String baseUrl = "http://www.imooc.com/api";

//    static {
//        bird = new NetBird.Builder(baseUrl)
////                .addRequestProcessor(new Processor<Request>() {
////                    @NonNull
////                    @Override
////                    public Request process(@NonNull Request request) {
////                        return request.newBuilder().add("test1", "value1").add("test2", "value2").build();
////                    }
////                })
//                .addRequestProcessor(new LogReqProcessor())
//                .addResponseProcessor(new LogRepProcessor())
//                .build();
//    }

    public static void init(Context ctx) {
        bird = new NetBird.Builder(baseUrl)
                .dispatcher(new OkDispatcher(ctx))
//                .addRequestProcessor(new Processor<Request>() {
//                    @NonNull
//                    @Override
//                    public Request process(@NonNull Request request) {
//                        return request.newBuilder().add("test1", "value1").add("test2", "value2").build();
//                    }
//                })
                .addRequestProcessor(new LogReqProcessor())
                .addResponseProcessor(new LogRepProcessor())
                .build();
    }

    public static Object call(Request<?> req) {
        return bird.dispatch(req);
    }

    public static void cancel(Object tag) {
        bird.cancel(tag);
    }

    public static void cancelAll() {
        bird.cancelAll();
    }

    public static void cancelWait(Object tag) {
        bird.cancelWait(tag);
    }

    public static void cancelAllWait() {
        bird.cancelAllWait();
    }

    public static Object display(final ImageView view, String url) {
        Request<Bitmap> req = new Request.Builder<>(BitmapParser.getParser())
                .url(url)
                .callback(new Response.SimpleCallback<Bitmap>() {
                    @Override
                    public void onSuccess(@NonNull Bitmap result) {
                        view.setImageBitmap(result);
                    }

                    @Override
                    public void onFailure(int code, @NonNull String msg) {
                        LogUtils.e("NetBird", code + " : " + msg);
                    }
                }).build();
        return call(req);
    }

    private static class LogRepProcessor implements Processor<Response> {
        @NonNull
        @Override
        public Response process(@NonNull Response response) {
            if (LogUtils.isDebug) {
                LogUtils.ii(TAG, "-------------------------------------- response --------------------------------------");
                LogUtils.ii(TAG, "response --> " + response.code() + "--" + response.msg());
                Headers headers = response.headers();
                for (int i = 0, size = headers.size(); i < size; i++) {
                    String name = headers.name(i);
                    String value = headers.value(i);
                    LogUtils.ii(TAG, "response header --> " + name + " = " + value);
                }
                LogUtils.ii(TAG, "--------------------------------------------------------------------------------------");
            }
            return response;
        }
    }

    private static class LogReqProcessor implements Processor<Request> {
        @NonNull
        @Override
        @SuppressWarnings("unchecked")
        public Request process(@NonNull Request req) {
            if (LogUtils.isDebug) {
                Method m = req.method();
                LogUtils.ii(TAG, "---------------------------------------- " + m.name() + " -----------------------------------------");
                String url = url(req);
                if (m == Method.GET) {
                    String params = req.encodedParams();
                    if (!Utils.isEmpty(params)) {
                        url = url + '?' + params;
                    }
                    LogUtils.dd(TAG, "req url --> " + url);
                } else {
                    LogUtils.dd(TAG, "req url --> " + url);
                    logPairs(req.paramNames(), req.paramValues(), "parameter");
                    logPacks(req);
                }
                logPairs(req.headerNames(), req.headerValues(), "header");
                LogUtils.ii(TAG, "--------------------------------------------------------------------------------------");
            }
            return req;
        }

        private static void logPairs(List<String> names, List<String> values, String mark) {
            for (int i = 0, size = names.size(); i < size; i++) {
                LogUtils.dd(TAG, "req " + mark + " -- > " + names.get(i) + " = " + values.get(i));
            }
        }

        @SuppressWarnings("unchecked")
        private static void logPacks(Request req) {
            List<Request.Pack> packs = req.packs();
            for (int i = 0, size = packs.size(); i < size; i++) {
                Request.Pack pack = packs.get(i);
                LogUtils.dd(TAG, "req pack --> " + pack.name + "--" + pack.contentType + "--" + pack.file.getAbsolutePath());
            }
        }

        private static String url(Request<?> req) {
            String url = Utils.emptyElse(req.url(), baseUrl);
            String path = req.path();
            if (!Utils.isEmpty(path)) {
                url += path;
            }
            return url;
        }
    }
}
