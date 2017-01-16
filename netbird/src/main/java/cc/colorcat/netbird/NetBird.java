package cc.colorcat.netbird;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import cc.colorcat.netbird.request.Request;
import cc.colorcat.netbird.response.NetworkData;
import cc.colorcat.netbird.response.Response;
import cc.colorcat.netbird.response.ResponseBody;
import cc.colorcat.netbird.sender.Dispatcher;
import cc.colorcat.netbird.sender.httpsender.HttpDispatcher;
import cc.colorcat.netbird.util.Const;
import cc.colorcat.netbird.util.LogUtils;
import cc.colorcat.netbird.util.Utils;


/**
 * Created by cxx on 2016/12/12.
 * xx.ch@outlook.com
 */
public final class NetBird {
    private static final Response EXECUTING_RESPONSE = Response.newFailure(Const.CODE_EXECUTING, Const.MSG_EXECUTING);
    private static final NetworkData EXECUTING_DATA = NetworkData.newFailure(Const.CODE_EXECUTING, Const.MSG_EXECUTING);
    private final int maxRunning;
    private final Set<Request<?>> runningReqs = new CopyOnWriteArraySet<>();
    private final Queue<Request<?>> waitQueue = new ConcurrentLinkedQueue<>();
    private final List<Processor<Request>> requestProcessors;
    private final List<Processor<Response>> responseProcessors;
    private final ExecutorService executor;
    private final String baseUrl;
    private final Dispatcher dispatcher;


    private NetBird(Builder builder) {
        this.requestProcessors = Utils.immutableList(builder.requestProcessors);
        this.responseProcessors = Utils.immutableList(builder.responseProcessors);
        this.maxRunning = builder.maxRunning;
        this.executor = Utils.nullElse(builder.executor, defaultService(this.maxRunning));
        this.baseUrl = builder.baseUrl;
        Dispatcher dispatcher = builder.dispatcher;
        if (dispatcher == null) {
            dispatcher = new HttpDispatcher(builder.connectTimeOut, builder.readTimeOut);
        }
        this.dispatcher = dispatcher;
    }

    private static ExecutorService defaultService(int corePoolSize) {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(corePoolSize, 10, 60L, TimeUnit.SECONDS,
                new LinkedBlockingDeque<Runnable>(), new ThreadPoolExecutor.DiscardOldestPolicy());
        executor.allowCoreThreadTimeOut(true);
        return executor;
    }

    public <T> Object dispatch(@NonNull Request<T> req) {
        Utils.nonNull(req, "req == null");
        Request<T> request = processRequest(req);
        if (!waitQueue.contains(request)) {
            if (waitQueue.offer(request)) notifyNewRequest();
        } else {
            inExecuting(request);
        }
        return request.tag();
    }

    private void notifyNewRequest() {
        if (runningReqs.size() < maxRunning && !waitQueue.isEmpty()) {
            Request<?> request = waitQueue.poll();
            if (runningReqs.add(request)) {
                executor.execute(new Task<>(this, request));
            } else {
                inExecuting(request);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void inExecuting(Request<?> req) {
        req.onStart();
        processResponse(EXECUTING_RESPONSE);
        NetworkData data = EXECUTING_DATA;
        req.deliver(data);
    }

    private <T> void execute(Request<T> req) {
        req.onStart();
        Response response = dispatcher.dispatch(baseUrl, req);
        response = processResponse(response);
        ResponseBody body = response.body();
        NetworkData<? extends T> data;
        if (body != null && body.stream() != null) {
            data = req.parse(response);
        } else {
            data = NetworkData.newFailure(response.code(), response.msg());
        }
        req.deliver(data);
    }

    private void finished(Request<?> req) {
        LogUtils.d("Size", "Running size = " + runningReqs.size());
        LogUtils.d("Size", "Waiting size = " + waitQueue.size());
        dispatcher.cancel(req.tag());
        runningReqs.remove(req);
        LogUtils.i("Size", "Running size = " + runningReqs.size());
        LogUtils.i("Size", "Waiting size = " + waitQueue.size());
        notifyNewRequest();
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
        cancelWait(Utils.nonNull(tag, "tag == null"));
        dispatcher.cancel(tag);
//        Iterator<Request<?>> iterator = runningReqs.iterator();
//        while (iterator.hasNext()) {
//            Request<?> req = iterator.next();
//            if (req.tag().equals(tag)) {
//                iterator.remove();
//            }
//        }
    }

    public void cancelAll() {
        cancelAllWait();
        dispatcher.cancelAll();
        runningReqs.clear();
    }

    public void cancelWait(@NonNull Object tag) {
        Iterator<Request<?>> iterator = waitQueue.iterator();
        while (iterator.hasNext()) {
            Request<?> req = iterator.next();
            if (req.tag().equals(tag)) {
                iterator.remove();
            }
        }
    }

    public void cancelAllWait() {
        waitQueue.clear();
    }

    private static class Task<T> implements Runnable {
        private final NetBird bird;
        private final Request<T> req;

        private Task(NetBird bird, Request<T> req) {
            this.bird = bird;
            this.req = req;
        }

        @Override
        public void run() {
            try {
                bird.execute(req);
            } finally {
                bird.finished(req);
            }
        }
    }

    public static class Builder {
        private List<Processor<Request>> requestProcessors = new ArrayList<>(4);
        private List<Processor<Response>> responseProcessors = new ArrayList<>(4);
        private ExecutorService executor;
        private Dispatcher dispatcher;
        private String baseUrl;
        private int maxRunning = 6;
        private int readTimeOut = 3000;
        private int connectTimeOut = 5000;

        /**
         * @param baseUrl http/https，如果 {@link Request.Builder#url(String)} 未设置 url 则会以此代替
         */
        public Builder(@NonNull String baseUrl) {
            this.baseUrl = Utils.checkedHttp(baseUrl);
        }

        /**
         * 设置请求客户端，非必须，默认为 {@link HttpDispatcher}
         */
        public Builder dispatcher(@NonNull Dispatcher dispatcher) {
            this.dispatcher = Utils.nonNull(dispatcher, "dispatcher == null");
            return this;
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

        public Builder maxRunning(int maxRunning) {
            if (maxRunning > 0) {
                this.maxRunning = maxRunning;
            }
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
