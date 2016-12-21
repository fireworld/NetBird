package cc.colorcat.netbird.util;

/**
 * Created by cxx on 2016/12/14.
 * xx.ch@outlook.com
 */

public final class Const {
    public static final String UTF8 = "UTF-8";

    public static final int CODE_CONNECT_ERROR = -100;
    public static final int CODE_EXECUTING = -101;
    public static final int CODE_UNKNOWN = -102;

    public static final String MSG_CONNECT_ERROR = "connect error";
    public static final String MSG_EXECUTING = "executing";
    public static final String MSG_UNKNOWN = "unknown";

    private Const() {
        throw new AssertionError("no instance");
    }
}
