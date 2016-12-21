package cc.colorcat.netbirddemo;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.widget.ImageView;

import java.util.List;

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
    private static final String baseUrl = "https://www.zzb18.com/zzbh5";

    static {
        bird = new NetBird.Builder(baseUrl)
                .addRequestProcessor(new Processor<Request>() {
                    @SuppressWarnings("unchecked")
                    @NonNull
                    @Override
                    public Request process(@NonNull Request req) {
                        if (LogUtils.isDebug) {
                            LogUtils.ii(TAG, "-------------------------------------- " + req.method().name() + " --------------------------------------");
                            LogUtils.ii(TAG, "Url --> " + url(req));
                            if (Method.POST == req.method()) {
                                List<String> names = req.paramNames();
                                List<String> values = req.paramValues();
                                for (int i = 0, size = names.size(); i < size; i++) {
                                    LogUtils.ii(TAG, "Parameter --> " + names.get(i) + " = " + values.get(i));
                                }

                                List<Request.Pack> packs = req.packs();
                                for (int i = 0, size = packs.size(); i < size; i++) {
                                    Request.Pack park = packs.get(i);
                                    LogUtils.ii(TAG, "File FieldName --> " + park.name);
                                    LogUtils.ii(TAG, "File ContentType --> " + park.contentType);
                                    LogUtils.ii(TAG, "File Path --> " + park.file.getAbsolutePath());
                                }
                            }
                            List<String> hNames = req.headerNames();
                            List<String> hValues = req.headerValues();
                            for (int i = 0, size = hNames.size(); i < size; i++) {
                                LogUtils.ii(TAG, "Header --> " + hNames.get(i) + " = " + hValues.get(i));
                            }
                            LogUtils.ii(TAG, "----------------------------------------------------------------------------------");
                        }
                        return req;
                    }
                }).addResponseProcessor(new Processor<Response>() {
                    @NonNull
                    @Override
                    public Response process(@NonNull Response response) {
                        if (LogUtils.isDebug) {
                            LogUtils.ii(TAG, "Response --> " + "code = " + response.code() + ", msg = " + response.msg());
                        }
                        return response;
                    }
                })
                .build();
    }

    private static String url(Request<?> req) {
        String url = Utils.emptyElse(req.url(), baseUrl);
        String path = req.path();
        if (!Utils.isEmpty(path)) {
            url += path;
        }
        if (req.method() == Method.GET) {
            String params = req.encodedParams();
            if (!Utils.isEmpty(params)) {
                url = url + '?' + req.encodedParams();
            }
        }
        return url;
    }

    public static Object call(Request<?> req) {
        return bird.sendRequest(req);
    }

    public static void cancel(Object tag) {
        bird.cancel(tag);
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
}
