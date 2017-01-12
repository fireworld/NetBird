package cc.colorcat.netbird;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import cc.colorcat.netbird.request.Request;
import cc.colorcat.netbird.response.NetworkData;
import cc.colorcat.netbird.response.Response;
import cc.colorcat.netbird.response.ResponseBody;
import cc.colorcat.netbird.sender.Sender;
import cc.colorcat.netbird.sender.httpsender.HttpSender;
import cc.colorcat.netbird.util.Const;
import cc.colorcat.netbird.util.Utils;

/**
 * Created by cxx on 2016/12/12.
 * xx.ch@outlook.com
 */
public final class NetBird {
    private static final Response FAIL_RESPONSE = Response.newFailure(Const.CODE_EXECUTING, Const.MSG_EXECUTING);
    private static final NetworkData FAIL_DATA = NetworkData.newFailure(Const.CODE_EXECUTING, Const.MSG_EXECUTING);
    private final Set<Request<?>> runningReqs = new CopyOnWriteArraySet<>();
    private final List<Processor<Request>> requestProcessors;
    private final List<Processor<Response>> responseProcessors;
    private final ExecutorService executor;
    private final String baseUrl;
    private final Sender sender;


    private NetBird(Builder builder) {
        this.requestProcessors = Utils.immutableList(builder.requestProcessors);
        this.responseProcessors = Utils.immutableList(builder.responseProcessors);
        this.executor = Utils.nullElse(builder.executor, defaultService());
        this.baseUrl = builder.baseUrl;
        this.sender = new HttpSender(builder.connectTimeOut, builder.readTimeOut);
    }

    private static ExecutorService defaultService() {
//        return new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS,
//                new SynchronousQueue<Runnable>(), Utils.threadFactory("NetBird", false));
        return new ThreadPoolExecutor(6, 10, 60L, TimeUnit.SECONDS,
                new LinkedBlockingDeque<Runnable>(), new ThreadPoolExecutor.DiscardOldestPolicy());
    }

    public <T> Object sendRequest(@NonNull Request<T> req) {
        Utils.nonNull(req, "req == null");
        final Object tag = System.currentTimeMillis();
        Request<T> request = processRequest(req);
        if (runningReqs.add(request)) {
            executor.submit(new Task<>(this, request, tag));
        } else {
            request.onStart();
            processResponse(FAIL_RESPONSE);
            @SuppressWarnings("unchecked")
            NetworkData<? extends T> data = FAIL_DATA;
            request.deliver(data);
        }
        return tag;
    }

    private Response realSend(Request<?> req, Object tag) {
        return sender.send(baseUrl, req, tag);
    }

    @SuppressWarnings("unchecked")
    private <T> Request<T> processRequest(final Request<T> req) {
        Request<T> request = req;
        for (int i = 0, size = requestProcessors.size(); i < size; i++) {
            request = requestProcessors.get(i).process(request);
        }
        return request;
    }

    private Response processResponse(final Response rsp) {
        Response response = rsp;
        for (int i = 0, size = responseProcessors.size(); i < size; i++) {
            response = responseProcessors.get(i).process(response);
        }
        return response;
    }

    public void cancel(@NonNull Object tag) {
        sender.cancel(Utils.nonNull(tag, "tag == null"));
    }

    public void cancelAll() {
        sender.cancelAll();
    }

    private static class Task<T> implements Runnable {
        private final NetBird bird;
        private final Request<T> req;
        private final Object tag;

        private Task(NetBird bird, Request<T> req, Object tag) {
            this.bird = bird;
            this.req = req;
            this.tag = tag;
        }

        @Override
        public void run() {
            try {
                req.onStart();
                Response response = bird.realSend(req, tag);
                response = bird.processResponse(response);
                ResponseBody body = response.body();
                NetworkData<? extends T> data;
                if (body != null && body.stream() != null) {
                    data = req.parse(response);
                } else {
                    data = NetworkData.newFailure(response.code(), response.msg());
                }
                req.deliver(data);
            } finally {
                bird.sender.cancel(tag);
                bird.runningReqs.remove(req);
            }
        }
    }

    public static class Builder {
        private List<Processor<Request>> requestProcessors = new ArrayList<>(4);
        private List<Processor<Response>> responseProcessors = new ArrayList<>(4);
        private ExecutorService executor;
        private String baseUrl;
        private int readTimeOut = 3000;
        private int connectTimeOut = 5000;

        /**
         * @param baseUrl http/https，如果 {@link Request.Builder#url(String)} 未设置 url 则会以此代替
         */
        public Builder(@NonNull String baseUrl) {
            this.baseUrl = Utils.checkedHttp(baseUrl);
        }

        /**
         * 配置请求线程池，非必须，一般无须设置采用默认即可
         */
        public Builder executor(@NonNull ExecutorService executor) {
            this.executor = Utils.nonNull(executor, "executor == null");
            return this;
        }

        /**
         * 添加 {@link Processor<Request>}，用于处理 {@link Request}
         * 在将 Request 发出前会依次调用添加的 {@link Processor<Request>} 处理原始的 {@link Request}，可借此打印请求日志等。
         * <p>
         * Note: 调用会依添加的顺序进行，请谨慎修改 {@link Request} 中涉及泛型的数据，否则可能导致数据解析或回调错误，如：
         * {@link cc.colorcat.netbird.parser.Parser}, {@link Response.Callback}
         */
        public Builder addRequestProcessor(@NonNull Processor<Request> reqProcessor) {
            requestProcessors.add(Utils.nonNull(reqProcessor, "reqProcessor == null"));
            return this;
        }

        /**
         * 添加 {@link Processor<Response>}, 用于处理 {@link Response}
         * 在将返回的 Response 进行解析前会依次调用添加的 {@link Processor<Response>} 处理原始的 {@link Response}，可借此打印响应日志等。
         * <p>
         * Note: 调用会依添加的顺序进行，请谨慎调用 {@link Response#body()}, {@link ResponseBody} 可能只能读取一次。
         */
        public Builder addResponseProcessor(@NonNull Processor<Response> repProcessor) {
            responseProcessors.add(Utils.nonNull(repProcessor, "repProcessor == null"));
            return this;
        }

        public Builder readTimeOut(int milliseconds) {
            if (milliseconds < 0) {
                throw new IllegalArgumentException("ReadTimeOut must be greater than 0");
            }
            this.readTimeOut = milliseconds;
            return this;
        }

        public Builder connectTimeOut(int milliseconds) {
            if (milliseconds < 0) {
                throw new IllegalArgumentException("ConnectTimeOut must be greater than 0");
            }
            this.connectTimeOut = milliseconds;
            return this;
        }

        public NetBird build() {
            return new NetBird(this);
        }
    }
}
