package cc.colorcat.netbird.request;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
    private List<String> paramNames;
    private List<String> paramValues;
    private List<String> headerNames;
    private List<String> headerValues;
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
        this.paramNames = builder.paramNames;
        this.paramValues = builder.paramValues;
        this.headerNames = builder.headerNames;
        this.headerValues = builder.headerValues;
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
        if (paramNames.isEmpty() && packs == null) {
            return null;
        }
        if (paramNames.isEmpty() && packs != null && packs.size() == 1) {
            return FileBody.create(packs.get(0), uploadListener);
        }
        if (!paramNames.isEmpty() && packs == null) {
            return FormBody.create(paramNames, paramValues);
        }
        int size = packs.size();
        List<FileBody> files = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            FileBody body = FileBody.create(packs.get(i), uploadListener);
            files.add(body);
        }
        return MixBody.create(FormBody.create(paramNames, paramValues), files);
    }

    @NonNull
    public List<String> paramNames() {
        return Utils.safeImmutableList(paramNames);
    }

    @NonNull
    public List<String> paramValues() {
        return Utils.safeImmutableList(paramValues);
    }

    @NonNull
    public List<String> headerNames() {
        return Utils.safeImmutableList(headerNames);
    }

    @NonNull
    public List<String> headerValues() {
        return Utils.safeImmutableList(headerValues);
    }

    public String encodedParams() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0, size = paramNames.size(); i < size; i++) {
            if (i > 0) sb.append('&');
            sb.append(Utils.smartEncode(paramNames.get(i))).append('=').append(Utils.smartEncode(paramValues.get(i)));
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
        if (!paramNames.equals(request.paramNames)) return false;
        if (!paramValues.equals(request.paramValues)) return false;
        if (headerNames != null ? !headerNames.equals(request.headerNames) : request.headerNames != null)
            return false;
        if (headerValues != null ? !headerValues.equals(request.headerValues) : request.headerValues != null)
            return false;
        return packs != null ? packs.equals(request.packs) : request.packs == null;
    }

    @Override
    public int hashCode() {
        int result = url != null ? url.hashCode() : 0;
        result = 31 * result + (path != null ? path.hashCode() : 0);
        result = 31 * result + method.hashCode();
        result = 31 * result + paramNames.hashCode();
        result = 31 * result + paramValues.hashCode();
        result = 31 * result + (headerNames != null ? headerNames.hashCode() : 0);
        result = 31 * result + (headerValues != null ? headerValues.hashCode() : 0);
        result = 31 * result + (packs != null ? packs.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(@NonNull Request o) {
        if (this.equals(o)) {
            return 0;
        }
        return 1;
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
            return new Pack(Utils.nonEmpty(name, "name is empty"), Utils.nonEmpty(contentType, "contentType is empty"), file);
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
        private List<String> paramNames = new ArrayList<>();
        private List<String> paramValues = new ArrayList<>();
        private List<String> headerNames;
        private List<String> headerValues;
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
            this.paramNames = req.paramNames;
            this.paramValues = req.paramValues;
            this.headerNames = req.headerNames;
            this.headerValues = req.headerValues;
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
         */
        public Builder<T> add(String name, String value) {
            Utils.nonEmpty(name, "name is null/empty");
            Utils.nonEmpty(value, "value is null/empty");
            realAdd(name, value);
            return this;
        }

        /**
         * 添加请求参数
         *
         * @param name  请求参数的名称
         * @param value 请求参数的值
         */
        public Builder<T> add(String name, int value) {
            return add(name, String.valueOf(value));
        }

        /**
         * 添加请求参数
         *
         * @param name  请求参数的名称
         * @param value 请求参数的值
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
         */
        public Builder<T> add(String name, double value) {
            return add(name, String.valueOf(value));
        }

        public Builder<T> set(String name, String value) {
            Utils.nonEmpty(name, "name is null/empty");
            Utils.nonEmpty(value, "value is null/empty");
            realRemove(name);
            realAdd(name, value);
            return this;
        }

        public Builder<T> set(String name, int value) {
            return set(name, String.valueOf(value));
        }

        public Builder<T> set(String name, long value) {
            return set(name, String.valueOf(value));
        }

        public Builder<T> set(String name, float value) {
            return set(name, String.valueOf(value));
        }

        public Builder<T> set(String name, double value) {
            return set(name, String.valueOf(value));
        }

        public Builder<T> addIfNot(String name, String value) {
            Utils.nonEmpty(name, "name is null/empty");
            Utils.nonEmpty(value, "value is null/empty");
            if (!paramNames.contains(name)) {
                realAdd(name, value);
            }
            return this;
        }

        public Builder<T> addIfNot(String name, int value) {
            return addIfNot(name, String.valueOf(value));
        }

        public Builder<T> addIfNot(String name, long value) {
            return addIfNot(name, String.valueOf(value));
        }

        public Builder<T> addIfNot(String name, float value) {
            return addIfNot(name, String.valueOf(value));
        }

        public Builder<T> addIfNot(String name, double value) {
            return addIfNot(name, String.valueOf(value));
        }

        /**
         * 除清指定名称的参数
         *
         * @param name 需要清除的参数的名称
         */
        public Builder<T> remove(String name) {
            Utils.nonEmpty(name, "name is null/empty");
            realRemove(name);
            return this;
        }

        private boolean realAdd(String name, String value) {
            return paramNames.add(name) && paramValues.add(value);
        }

        private boolean realRemove(String name) {
            boolean result = false;
            for (int i = paramNames.size() - 1; i >= 0; i--) {
                if (paramNames.get(i).equals(name)) {
                    paramNames.remove(i);
                    paramValues.remove(i);
                    result = true;
                }
            }
            return result;
        }

        /**
         * @return 返回所有请求参数的名称，不可修改，顺序与 {@link Builder#values()} 一一对应。
         */
        public List<String> names() {
            return Utils.safeImmutableList(paramNames);
        }

        /**
         * @return 返回所有请求参数的值，不可修改，顺序与 {@link Builder#names()} 一一对应。
         */
        public List<String> values() {
            return Utils.safeImmutableList(paramValues);
        }

        public String value(String name) {
            Utils.nonEmpty(name, "name is null/empty");
            int index = paramNames.indexOf(name);
            if (index != -1) {
                return paramValues.get(index);
            }
            return null;
        }

        public List<String> values(String name) {
            Utils.nonEmpty(name, "name is null/empty");
            List<String> values = new ArrayList<>(2);
            for (int i = 0, size = paramNames.size(); i < size; i++) {
                if (paramNames.get(i).equals(name)) {
                    values.add(paramValues.get(i));
                }
            }
            return values;
        }

        /**
         * 清除所有已添加的请求参数
         */
        public Builder<T> clear() {
            paramNames.clear();
            paramValues.clear();
            return this;
        }

        /**
         * 添加需要上传的文件
         *
         * @param name      参数名
         * @param mediaType 文件类型，如 image/png
         * @param file      文件全路径
         */
        public Builder<T> addPack(String name, String mediaType, File file) {
            if (packs == null) {
                packs = new ArrayList<>();
            }
            packs.add(Pack.create(name, mediaType, file));
            return this;
        }

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
         */
        public Builder<T> addHeader(String name, String value) {
            Utils.checkHeader(name, value);
            realAddHeader(name, value);
            return this;
        }

        /**
         * 设置一个请求 Header 参数，如果已添加了名称相同的 Header 则原来的会被清除。
         *
         * @param name  Header 的名称
         * @param value Header 的值
         */
        public Builder<T> setHeader(String name, String value) {
            Utils.checkHeader(name, value);
            removeHeader(name);
            realAddHeader(name, value);
            return this;
        }

        /**
         * @return 返回所有已添加的 Header 的名称，顺序与 {@link Builder#headerValues()} 一一对应
         */
        public List<String> headerNames() {
            return Utils.safeImmutableList(headerNames);
        }

        /**
         * @return 返回所有已添加的 Header 的值，顺序与 {@link Builder#headerNames()} 一一对应
         */
        public List<String> headerValues() {
            return Utils.safeImmutableList(headerValues);
        }

        /**
         * 清除所有已添加的 Header 参数
         */
        public Builder<T> clearHeaders() {
            if (headerNames != null) {
                headerNames.clear();
                headerValues.clear();
            }
            return this;
        }


        private void realAddHeader(String name, String value) {
            if (headerNames == null) {
                headerNames = new ArrayList<>(8);
                headerValues = new ArrayList<>(8);
            }
            headerNames.add(name);
            headerValues.add(value);
        }

        private void removeHeader(String name) {
            if (headerNames != null) {
                for (int i = headerNames.size() - 1; i >= 0; i--) {
                    String n = headerNames.get(i);
                    if (name.equalsIgnoreCase(n)) {
                        headerNames.remove(i);
                        headerValues.remove(i);
                    }
                }
            }
        }

        @CallSuper
        public Request<T> build() {
            if (packs != null && !packs.isEmpty()) method = Method.POST;
            if (tag == null) tag = System.currentTimeMillis();
            return new Request<>(this);
        }
    }
}