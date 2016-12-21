package cc.colorcat.netbird;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import cc.colorcat.netbird.request.Request;
import cc.colorcat.netbird.response.NetworkData;
import cc.colorcat.netbird.response.Response;
import cc.colorcat.netbird.response.ResponseBody;
import cc.colorcat.netbird.sender.Sender;
import cc.colorcat.netbird.sender.httpsender.HttpSender;
import cc.colorcat.netbird.util.Const;
import cc.colorcat.netbird.util.LogUtils;
import cc.colorcat.netbird.util.Utils;

/**
 * Created by cxx on 2016/12/12.
 * xx.ch@outlook.com
 */

public final class NetBird {
    private static final Response FAIL_RESPONSE = Response.newFailure(Const.CODE_EXECUTING, Const.MSG_EXECUTING);
    private static final NetworkData FAIL_DATA = NetworkData.onFailure(Const.CODE_EXECUTING, Const.MSG_EXECUTING);
    private final Set<Request<?>> runningReqs = new CopyOnWriteArraySet<>();
    private final ExecutorService executor;
    private final Sender sender;
    private final String baseUrl;
    private List<Processor<Request>> requestProcessors;
    private List<Processor<Response>> responseProcessors;


    private NetBird(Builder builder) {
        this.executor = Utils.nullElse(builder.executor, defaultService());
        this.baseUrl = builder.baseUrl;
        LogUtils.init(builder.ctx);
        this.requestProcessors = Utils.immutableList(builder.requestProcessors);
        this.responseProcessors = Utils.immutableList(builder.responseProcessors);
        this.sender = new HttpSender(builder.connectTimeOut, builder.readTimeOut);
    }

    private static ExecutorService defaultService() {
        return new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(), Utils.threadFactory("NetBird", false));
    }

    public <T> Object sendRequest(@NonNull final Request<T> req) {
        final Object tag = System.currentTimeMillis();
        Request<T> request = processRequest(req);
        if (runningReqs.add(request)) {
            executor.submit(new Task<>(this, req, tag));
        } else {
            req.onStart();
            processResponse(FAIL_RESPONSE);
            @SuppressWarnings("unchecked")
            NetworkData<? extends T> data = FAIL_DATA;
            req.deliver(data);
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

    private Response processResponse(final Response resp) {
        Response response = resp;
        for (int i = 0, size = responseProcessors.size(); i < size; i++) {
            response = responseProcessors.get(i).process(response);
        }
        return response;
    }

    public void cancel(@NonNull Object tag) {
        sender.cancel(tag);
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
                    data = NetworkData.onFailure(response.code(), response.msg());
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
        private Context ctx;
        private int readTimeOut = 3000;
        private int connectTimeOut = 5000;

        public Builder(String baseUrl, Context ctx) {
            Utils.nonNull(baseUrl, "baseUrl == null");
            if (!baseUrl.toLowerCase().startsWith("http")) {
                throw new IllegalArgumentException("illegal scheme, the scheme must be http or https");
            }
            this.ctx = Utils.nonNull(ctx, "ctx == null");
            this.baseUrl = baseUrl;
        }

        public Builder executor(ExecutorService executor) {
            this.executor = executor;
            return this;
        }

        public Builder addRequestProcessor(Processor<Request> reqProcessor) {
            requestProcessors.add(reqProcessor);
            return this;
        }

        public Builder addResponseProcessor(Processor<Response> repProcessor) {
            responseProcessors.add(repProcessor);
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
