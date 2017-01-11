package cc.colorcat.netbird.response;

import cc.colorcat.netbird.util.Utils;

/**
 * Created by cxx on 16-3-11.
 * xx.ch@outlook.com
 */
public class NetworkData<T> {
    public final boolean isSuccess;
    public final int code;
    public final String msg;
    public final T data;

    public static <R> NetworkData<? extends R> newSuccess(R data) {
        return new NetworkData<>(200, "ok", Utils.nonNull(data, "data == null"));
    }

    public static <R> NetworkData<? extends R> newFailure(int code, String msg) {
        return new NetworkData<>(code, Utils.nonEmpty(msg, "msg is empty"), null);
    }

    private NetworkData(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        isSuccess = (data != null);
    }
}