package cc.colorcat.netbird.parser;

import android.support.annotation.NonNull;

import java.io.IOException;

import cc.colorcat.netbird.response.NetworkData;
import cc.colorcat.netbird.response.Response;
import cc.colorcat.netbird.util.IoUtils;
import cc.colorcat.netbird.util.Utils;

/**
 * Created by cxx on 16-12-14.
 * xx.ch@outlook.com
 */

public class StringParser implements Parser<String> {
    private static StringParser utf8;
    private static StringParser parser;

    private String charset;

    public static StringParser create(@NonNull String charset) {
        return new StringParser(Utils.nonEmpty(charset, "charset is empty"));
    }

    public static StringParser getDefault() {
        if (parser == null) {
            parser = new StringParser();
        }
        return parser;
    }

    public static StringParser getUtf8() {
        if (utf8 == null) {
            utf8 = new StringParser("UTF-8");
        }
        return utf8;
    }

    private StringParser() {
    }

    private StringParser(String charset) {
        this.charset = charset;
    }

    @NonNull
    @Override
    public NetworkData<? extends String> parse(@NonNull Response data) {
        try {
            String charset = data.charset(this.charset);
            String value = IoUtils.readAndClose(data.body().stream(), charset);
            return NetworkData.onSuccess(value);
        } catch (IOException e) {
            return NetworkData.onFailure(data.code(), Utils.formatMsg(data.msg(), e));
        }
    }
}
