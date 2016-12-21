package cc.colorcat.netbird.request;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
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
public final class Request<T> implements Comparable<Request> {
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

    private Request(Builder<T> builder) {
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
    }

    public Builder<T> newBuilder() {
        return new Builder<>(this);
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
        return packs == null ? Collections.<Pack>emptyList() : Collections.unmodifiableList(packs);
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
        return Collections.unmodifiableList(paramNames);
    }

    @NonNull
    public List<String> paramValues() {
        return Collections.unmodifiableList(paramValues);
    }

    @NonNull
    public List<String> headerNames() {
        return headerNames == null ? Collections.<String>emptyList() : Collections.unmodifiableList(headerNames);
    }

    @NonNull
    public List<String> headerValues() {
        return headerValues == null ? Collections.<String>emptyList() : Collections.unmodifiableList(headerValues);
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

//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (!(o instanceof Request)) return false;
//
//        Request<?> request = (Request<?>) o;
//
//        if (!Utils.equal(url, request.url)) return false;
//        if (!Utils.equal(path, request.path)) return false;
//        if (method != request.method) return false;
//        if (!Utils.equalList(paramNames, request.paramNames)) return false;
//        if (!Utils.equalList(paramValues, request.paramValues)) return false;
//        if (!Utils.equalList(headerNames, request.headerNames)) return false;
//        if (!Utils.equalList(headerValues, request.headerValues)) return false;
//        return Utils.equalList(packs, request.packs);
//    }
//
//
//    @Override
//    public int hashCode() {
//        int result = (url != null ? url.hashCode() : 0);
//        result = 31 * result + (path != null ? path.hashCode() : 0);
//        result = 31 * result + method.hashCode();
//        result = 31 * result + paramNames.hashCode();
//        result = 31 * result + paramValues.hashCode();
//        result = 31 * result + (headerNames != null ? headerNames.hashCode() : 0);
//        result = 31 * result + (headerValues != null ? headerValues.hashCode() : 0);
//        result = 31 * result + (packs != null ? packs.hashCode() : 0);
//        return result;
//    }


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

        private Builder(Request<T> req) {
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
        }

        public Builder(@NonNull Parser<? extends T> parser) {
            this.parser = Utils.nonNull(parser, "parser == null");
        }

        public Builder<T> url(String url) {
            this.url = url;
            return this;
        }

        public Builder<T> path(String path) {
            this.path = path;
            return this;
        }

        public Builder<T> method(Method method) {
            this.method = method;
            return this;
        }

        public Builder<T> callback(Response.Callback<? super T> callback) {
            this.callback = callback;
            return this;
        }

        public Builder<T> loadListener(Response.LoadListener listener) {
            this.loadListener = listener;
            return this;
        }

        public Builder<T> uploadListener(UploadListener listener) {
            this.uploadListener = listener;
            return this;
        }

        public Builder<T> add(String name, String value) {
            paramNames.add(Utils.nonEmpty(name, "name is empty"));
            paramValues.add(Utils.nonEmpty(value, "paramValues is empty"));
            return this;
        }

        public Builder<T> add(String name, int value) {
            add(name, String.valueOf(value));
            return this;
        }

        public Builder<T> add(String name, long value) {
            add(name, String.valueOf(value));
            return this;
        }

        public Builder<T> add(String name, float value) {
            add(name, String.valueOf(value));
            return this;
        }

        public Builder<T> add(String name, double value) {
            add(name, String.valueOf(value));
            return this;
        }

        public Builder<T> addFile(String name, String mediaType, File file) {
            if (packs == null) {
                packs = new ArrayList<>();
            }
            packs.add(Pack.create(name, mediaType, file));
            return this;
        }


        public Builder<T> addHeader(String name, String value) {
            Utils.checkHeader(name, value);
            realAddHeader(name, value);
            return this;
        }

        public Builder<T> setHeader(String name, String value) {
            Utils.checkHeader(name, value);
            removeHeader(name);
            realAddHeader(name, value);
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

        public Request<T> build() {
            if (packs != null && !packs.isEmpty()) this.method = Method.POST;
            return new Request<>(this);
        }
    }
}