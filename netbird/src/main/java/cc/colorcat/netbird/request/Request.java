package cc.colorcat.netbird.request;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cc.colorcat.netbird.Headers;
import cc.colorcat.netbird.io.ProgressListener;
import cc.colorcat.netbird.parser.Parser;
import cc.colorcat.netbird.response.NetworkData;
import cc.colorcat.netbird.response.Response;
import cc.colorcat.netbird.util.Utils;


/**
 * Created by cxx on 16/2/28.
 * xx.ch@outlook.com
 */
@SuppressWarnings("unused")
public class Request<T> implements Comparable<Request> {
    private Parameters params;
    private Headers headers;
    private String url;
    private String path;
    private Method method;
    private Parser<? extends T> parser;
    private List<Pack> packs;
    private Response.Callback<? super T> callback;

    private Response.LoadListener loadListener;
    private UploadListener uploadListener;

    private Object tag;

    protected Request(Builder<T> builder) {
        this.params = builder.params;
        this.headers = builder.headers;
        this.url = builder.url;
        this.path = builder.path;
        this.method = builder.method;
        this.parser = builder.parser;
        this.packs = builder.packs;
        this.callback = builder.callback;
        this.loadListener = builder.loadListener;
        this.uploadListener = builder.uploadListener;
        this.tag = builder.tag;
    }

    public Builder<T> newBuilder() {
        return new Builder<>(this);
    }

    public Object tag() {
        return tag;
    }

    @NonNull
    public String url() {
        return url;
    }

    @NonNull
    public String path() {
        return path;
    }

    public Method method() {
        return method;
    }

    @NonNull
    public List<Pack> packs() {
        return Utils.safeImmutableList(packs);
    }

    @Nullable
    public Response.LoadListener loadListener() {
        return loadListener;
    }

    @Nullable
    public UploadListener uploadListener() {
        return uploadListener;
    }

    @Nullable
    public RequestBody body() {
        if (params.isEmpty() && packs == null) {
            return null;
        }
        if (params.isEmpty() && packs != null && packs.size() == 1) {
            return FileBody.create(packs.get(0), uploadListener);
        }
        if (!params.isEmpty() && packs == null) {
            return FormBody.create(params);
        }
        int size = packs.size();
        List<FileBody> files = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            FileBody body = FileBody.create(packs.get(i), uploadListener);
            files.add(body);
        }
        return MixBody.create(FormBody.create(params), files);
    }

    @NonNull
    public List<String> paramNames() {
        return params.names();
    }

    @NonNull
    public List<String> paramValues() {
        return params.values();
    }

    @NonNull
    public List<String> headerNames() {
        return Utils.nullElse(headers, Headers.emptyHeaders()).names();
    }

    @NonNull
    public List<String> headerValues() {
        return Utils.nullElse(headers, Headers.emptyHeaders()).values();
    }

    public String encodedParams() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0, size = params.size(); i < size; i++) {
            if (i > 0) sb.append('&');
            sb.append(Utils.smartEncode(params.name(i))).append('=').append(Utils.smartEncode(params.value(i)));
        }
        return sb.toString();
    }

    public void onStart() {
        if (callback != null) {
            if (Utils.isUiThread()) {
                callback.onStart();
            } else {
                Utils.postOnUi(new Runnable() {
                    @Override
                    public void run() {
                        callback.onStart();
                    }
                });
            }
        }
    }

    public NetworkData<? extends T> parse(@NonNull Response response) {
        return parser.parse(response);
    }

    public void deliver(@NonNull final NetworkData<? extends T> data) {
        if (Utils.isUiThread()) {
            realDeliver(data);
        } else {
            Utils.postOnUi(new Runnable() {
                @Override
                public void run() {
                    realDeliver(data);
                }
            });
        }
    }

    private void realDeliver(final NetworkData<? extends T> data) {
        if (callback != null) {
            if (data.isSuccess) {
                callback.onSuccess(data.data);
            } else {
                callback.onFailure(data.code, data.msg);
            }
            callback.onFinish();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Request<?> request = (Request<?>) o;

        if (url != null ? !url.equals(request.url) : request.url != null) return false;
        if (path != null ? !path.equals(request.path) : request.path != null) return false;
        if (method != request.method) return false;
        if (!params.equals(request.params)) return false;
        if (headers != null ? !headers.equals(request.headers) : request.headers != null) return false;
        return packs != null ? packs.equals(request.packs) : request.packs == null;

    }

    @Override
    public int hashCode() {
        int result = params.hashCode();
        result = 31 * result + (headers != null ? headers.hashCode() : 0);
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (path != null ? path.hashCode() : 0);
        result = 31 * result + method.hashCode();
        result = 31 * result + (packs != null ? packs.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(@NonNull Request o) {
        if (this.equals(o)) {
            return 0;
        }
        return this.hashCode() - o.hashCode();
    }

    public interface UploadListener extends ProgressListener {

        @Override
        void onChanged(long written, long total, int percent);
    }

    public static class Pack implements Comparable<Pack> {
        public final String name;
        public final String contentType;
        public final File file;

        static Pack create(String name, String contentType, File file) {
            if (file == null || !file.exists()) {
                throw new IllegalArgumentException("file is not exists");
            }
            Utils.nonEmpty(name, "name is empty");
            Utils.nonEmpty(contentType, "contentType is empty");
            return new Pack(name, contentType, file);
        }

        private Pack(String name, String contentType, File file) {
            this.name = name;
            this.contentType = contentType;
            this.file = file;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Pack)) return false;

            Pack pack = (Pack) o;

            if (!name.equals(pack.name)) return false;
            if (!contentType.equals(pack.contentType)) return false;
            return file.getAbsolutePath().equals(pack.file.getAbsolutePath());

        }

        @Override
        public int hashCode() {
            int result = name.hashCode();
            result = 31 * result + contentType.hashCode();
            result = 31 * result + file.getAbsolutePath().hashCode();
            return result;
        }

        @Override
        public int compareTo(@NonNull Pack o) {
            int n = name.compareTo(o.name);
            if (n != 0) {
                return n;
            }
            int c = contentType.compareTo(o.contentType);
            if (c != 0) {
                return c;
            }
            return file.getAbsolutePath().compareTo(file.getAbsolutePath());
        }
    }


    public static class Builder<T> {
        private Parameters params;
        private Headers headers;
        private String url;
        private String path;
        private Method method = Method.GET;
        private Parser<? extends T> parser;
        private List<Pack> packs;
        private Response.Callback<? super T> callback;

        private Response.LoadListener loadListener;
        private UploadListener uploadListener;

        private Object tag;

        protected Builder(Request<T> req) {
            this.params = req.params;
            this.headers = req.headers;
            this.url = req.url;
            this.path = req.path;
            this.method = req.method;
            this.parser = req.parser;
            this.packs = req.packs;
            this.callback = req.callback;
            this.loadListener = req.loadListener;
            this.uploadListener = req.uploadListener;
            this.tag = req.tag;
        }

        /**
         * @param parser 数据解析，将 {@link Response} 解析为目标数据
         * @see cc.colorcat.netbird.parser.BitmapParser
         * @see cc.colorcat.netbird.parser.FileParser
         * @see cc.colorcat.netbird.parser.JsonParser
         * @see cc.colorcat.netbird.parser.StringParser
         */
        public Builder(@NonNull Parser<? extends T> parser) {
            this.parser = Utils.nonNull(parser, "parser == null");
            this.params = Parameters.create(8);
        }

        public Builder<T> tag(Object tag) {
            this.tag = tag;
            return this;
        }

        /**
         * @param parser 数据解析，将 {@link Response} 解析为目标数据
         * @see cc.colorcat.netbird.parser.BitmapParser
         * @see cc.colorcat.netbird.parser.FileParser
         * @see cc.colorcat.netbird.parser.JsonParser
         * @see cc.colorcat.netbird.parser.StringParser
         */
        public Builder<T> parser(@NonNull Parser<? extends T> parser) {
            this.parser = Utils.nonNull(parser, "parser == null");
            return this;
        }

        /**
         * @param url 请求的 http/https 地址，如果没有设置则使用构建 NetBird 时的 baseUrl
         */
        public Builder<T> url(String url) {
            this.url = Utils.checkedHttp(url);
            return this;
        }

        /**
         * @param path 请求的路径
         */
        public Builder<T> path(String path) {
            this.path = path;
            return this;
        }

        public Builder<T> method(Method method) {
            this.method = method;
            return this;
        }

        /**
         * @param callback 请求结果的回调，{@link cc.colorcat.netbird.response.Response.Callback} 中的方法均在主线程执行
         */
        public Builder<T> callback(Response.Callback<? super T> callback) {
            this.callback = callback;
            return this;
        }

        /**
         * @param listener 下载进度监听器，服务器必须返回数据的长度才有效 {@link Response#contentLength()}
         */
        public Builder<T> loadListener(Response.LoadListener listener) {
            loadListener = listener;
            return this;
        }

        /**
         * @param listener 上传进度监听器
         */
        public Builder<T> uploadListener(UploadListener listener) {
            uploadListener = listener;
            return this;
        }

        /**
         * 添加请求参数
         *
         * @param name  请求参数的名称
         * @param value 请求参数的值
         * @throws IllegalArgumentException 如果 name/value 为 null 或空字符串将抛出此异常
         */
        public Builder<T> add(String name, String value) {
            Utils.nonEmpty(name, "name is null/empty");
            Utils.nonEmpty(value, "value is null/empty");
            params.add(name, value);
//            realAdd(name, value);
            return this;
        }

        /**
         * 添加请求参数
         *
         * @param name  请求参数的名称
         * @param value 请求参数的值
         * @throws IllegalArgumentException 如果 name/value 为 null 或空字符串将抛出此异常
         */
        public Builder<T> add(String name, int value) {
            return add(name, String.valueOf(value));
        }

        /**
         * 添加请求参数
         *
         * @param name  请求参数的名称
         * @param value 请求参数的值
         * @throws IllegalArgumentException 如果 name/value 为 null 或空字符串将抛出此异常
         */
        public Builder<T> add(String name, long value) {
            return add(name, String.valueOf(value));
        }

        /**
         * 添加请求参数
         *
         * @param name  请求参数的名称
         * @param value 请求参数的值
         */
        public Builder<T> add(String name, float value) {
            return add(name, String.valueOf(value));
        }

        /**
         * 添加请求参数
         *
         * @param name  请求参数的名称
         * @param value 请求参数的值
         * @throws IllegalArgumentException 如果 name/value 为 null 或空字符串将抛出此异常
         */
        public Builder<T> add(String name, double value) {
            return add(name, String.valueOf(value));
        }

        /**
         * 设置请求参数
         * 此操作将清除已添加的所有名称为 name 的参数对，然后添加所提供的参数对。
         *
         * @param name  请求参数的名称
         * @param value 请求参数的值
         * @throws IllegalArgumentException 如果 name/value 为 null 或空字符串将抛出此异常
         */
        public Builder<T> set(String name, String value) {
            Utils.nonEmpty(name, "name is null/empty");
            Utils.nonEmpty(value, "value is null/empty");
            params.set(name, value);
            return this;
        }

        /**
         * 设置请求参数
         * 此操作将清除已添加的所有名称为 name 的参数对，然后添加所提供的参数对。
         *
         * @param name  请求参数的名称
         * @param value 请求参数的值
         * @throws IllegalArgumentException 如果 name/value 为 null 或空字符串将抛出此异常
         */
        public Builder<T> set(String name, int value) {
            return set(name, String.valueOf(value));
        }

        /**
         * 设置请求参数
         * 此操作将清除已添加的所有名称为 name 的参数对，然后添加所提供的参数对。
         *
         * @param name  请求参数的名称
         * @param value 请求参数的值
         * @throws IllegalArgumentException 如果 name/value 为 null 或空字符串将抛出此异常
         */
        public Builder<T> set(String name, long value) {
            return set(name, String.valueOf(value));
        }

        public Builder<T> set(String name, float value) {
            return set(name, String.valueOf(value));
        }

        /**
         * 设置请求参数
         * 此操作将清除已添加的所有名称为 name 的参数对，然后添加所提供的参数对。
         *
         * @param name  请求参数的名称
         * @param value 请求参数的值
         * @throws IllegalArgumentException 如果 name/value 为 null 或空字符串将抛出此异常
         */
        public Builder<T> set(String name, double value) {
            return set(name, String.valueOf(value));
        }

        /**
         * 如果不存在名称为 name 的参数对则添加所提供的参数对，否则忽略之。
         *
         * @param name  请求参数的名称
         * @param value 请求参数的值
         * @throws IllegalArgumentException 如果 name/value 为 null 或空字符串将抛出此异常
         */
        public Builder<T> addIfNot(String name, String value) {
            Utils.nonEmpty(name, "name is null/empty");
            Utils.nonEmpty(value, "value is null/empty");
            params.addIfNot(name, value);
            return this;
        }

        /**
         * 如果不存在名称为 name 的参数对则添加所提供的参数对，否则忽略之。
         *
         * @param name  请求参数的名称
         * @param value 请求参数的值
         * @throws IllegalArgumentException 如果 name/value 为 null 或空字符串将抛出此异常
         */
        public Builder<T> addIfNot(String name, int value) {
            return addIfNot(name, String.valueOf(value));
        }

        /**
         * 如果不存在名称为 name 的参数对则添加所提供的参数对，否则忽略之。
         *
         * @param name  请求参数的名称
         * @param value 请求参数的值
         * @throws IllegalArgumentException 如果 name/value 为 null 或空字符串将抛出此异常
         */
        public Builder<T> addIfNot(String name, long value) {
            return addIfNot(name, String.valueOf(value));
        }

        /**
         * 如果不存在名称为 name 的参数对则添加所提供的参数对，否则忽略之。
         *
         * @param name  请求参数的名称
         * @param value 请求参数的值
         * @throws IllegalArgumentException 如果 name/value 为 null 或空字符串将抛出此异常
         */
        public Builder<T> addIfNot(String name, float value) {
            return addIfNot(name, String.valueOf(value));
        }

        /**
         * 如果不存在名称为 name 的参数对则添加所提供的参数对，否则忽略之。
         *
         * @param name  请求参数的名称
         * @param value 请求参数的值
         * @throws IllegalArgumentException 如果 name/value 为 null 或空字符串将抛出此异常
         */
        public Builder<T> addIfNot(String name, double value) {
            return addIfNot(name, String.valueOf(value));
        }

        /**
         * 除清所有名称为 name 的参数对
         *
         * @param name 需要清除的参数的名称
         * @throws IllegalArgumentException 如果 name/value 为 null 或空字符串将抛出此异常
         */
        public Builder<T> remove(String name) {
            Utils.nonEmpty(name, "name is null/empty");
            params.removeAll(name);
            return this;
        }

        /**
         * @return 返回所有请求参数的名称，不可修改，顺序与 {@link Builder#values()} 一一对应。
         */
        public List<String> names() {
            return params.names();
        }

        /**
         * @return 返回所有请求参数的值，不可修改，顺序与 {@link Builder#names()} 一一对应。
         */
        public List<String> values() {
            return params.values();
        }

        /**
         * @return 返回添加的与 name 对应的 value, 如果存在多个则返回先添加的，如果没有则返回 null
         * @throws IllegalArgumentException 如果 name 为 null或空字符串将抛出此异常
         */
        @Nullable
        public String value(String name) {
            Utils.nonEmpty(name, "name is null/empty");
            return params.value(name);
        }

        /**
         * @return 返回所有添加的与 name 对应的 value
         * @throws IllegalArgumentException 如果 name 为 null或空字符串将抛出此异常
         */
        @NonNull
        public List<String> values(String name) {
            Utils.nonEmpty(name, "name is null/empty");
            return params.values(name);
        }

        /**
         * 清除所有已添加的请求参数
         */
        public Builder<T> clear() {
            params.clear();
            return this;
        }

        /**
         * 添加需要上传的文件
         * name, mediaType, file 最终会被打包为 {@link Pack} 之后再添加
         *
         * @param name      参数名
         * @param mediaType 文件类型，如 image/png
         * @param file      文件全路径
         * @throws IllegalArgumentException 如果 name/mediaType 为 null 或空字符串，或 file 为 null 或不存在，均将抛出此异常。
         */
        public Builder<T> addPack(String name, String mediaType, File file) {
            if (packs == null) {
                packs = new ArrayList<>();
            }
            packs.add(Pack.create(name, mediaType, file));
            return this;
        }

        /**
         * @return 返回所有已添加准备上传的文件
         */
        public List<Pack> packs() {
            return Utils.safeImmutableList(packs);
        }

        /**
         * 清除所有已添加并准备上传的文件——仅是不上传，并非从磁盘删除
         */
        public Builder<T> clearPacks() {
            if (packs != null) {
                packs.clear();
            }
            return this;
        }

        /**
         * 添加一个请求 Header 参数，如果已添加了名称相同的 Header 不会清除之前的。
         *
         * @param name  Header 的名称
         * @param value Header 的值
         * @throws NullPointerException     如果 name/value 为 null, 将抛出此异常
         * @throws IllegalArgumentException 如果 name/value 不符合 Header 规范要求将抛出此异常
         */
        public Builder<T> addHeader(String name, String value) {
            Utils.checkHeader(name, value);
            if (headers == null) {
                headers = createHeader();
            }
            headers.add(name, value);
            return this;
        }

        /**
         * 设置一个请求 Header 参数，如果已添加了名称相同的 Header 则原来的都会被清除。
         *
         * @param name  Header 的名称
         * @param value Header 的值
         * @throws NullPointerException     如果 name/value 为 null, 将抛出此异常
         * @throws IllegalArgumentException 如果 name/value 不符合 Header 规范要求将抛出此异常
         */
        public Builder<T> setHeader(String name, String value) {
            Utils.checkHeader(name, value);
            if (headers == null) {
                headers = createHeader();
            }
            headers.set(name, value);
            return this;
        }

        /**
         * 如果不存在名称为 name 的 header 则添加，否则忽略之。
         *
         * @param name  Header 的名称
         * @param value Header 的值
         * @throws NullPointerException     如果 name/value 为 null, 将抛出此异常
         * @throws IllegalArgumentException 如果 name/value 不符合 Header 规范要求将抛出此异常
         */
        public Builder<T> addHeaderIfNot(String name, String value) {
            Utils.checkHeader(name, value);
            if (headers == null) {
                headers = createHeader();
            }
            headers.addIfNot(name, value);
            return this;
        }

        /**
         * @return 返回所有已添加的 Header 的名称，顺序与 {@link Builder#headerValues()} 一一对应
         */
        public List<String> headerNames() {
            return Utils.nullElse(headers, Headers.emptyHeaders()).names();
        }

        /**
         * @return 返回所有已添加的 Header 的值，顺序与 {@link Builder#headerNames()} 一一对应
         */
        public List<String> headerValues() {
            return Utils.nullElse(headers, Headers.emptyHeaders()).values();
        }

        /**
         * @return 返回添加的与 name 对应的 value, 如果存在多个则返回先添加的，如果没有则返回 null
         * @throws IllegalArgumentException 如果 name 为 null或空字符串将抛出此异常
         */
        @Nullable
        public String headerValue(String name) {
            Utils.nonEmpty(name, "name is null/empty");
            return Utils.nullElse(headers, Headers.emptyHeaders()).value(name);
        }

        /**
         * @return 返回所有添加的与 name 对应的 value
         * @throws IllegalArgumentException 如果 name 为 null或空字符串将抛出此异常
         */
        @NonNull
        public List<String> headerValues(String name) {
            Utils.nonEmpty(name, "name is null/empty");
            return Utils.nullElse(headers, Headers.emptyHeaders()).values(name);
        }

        /**
         * 清除所有已添加的 Header 参数
         */
        public Builder<T> clearHeaders() {
            if (headers != null) {
                headers.clear();
            }
            return this;
        }

        private static Headers createHeader() {
            return Headers.create(4);
        }

        @CallSuper
        public Request<T> build() {
            if (packs != null && !packs.isEmpty()) method = Method.POST;
            if (tag == null) tag = System.currentTimeMillis();
            return new Request<>(this);
        }
    }
}