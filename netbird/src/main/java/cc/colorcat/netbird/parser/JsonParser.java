package cc.colorcat.netbird.parser;

import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import cc.colorcat.netbird.response.NetworkData;
import cc.colorcat.netbird.response.Response;
import cc.colorcat.netbird.util.IoUtils;
import cc.colorcat.netbird.util.Utils;

/**
 * Created by cxx on 17-1-6.
 * xx.ch@outlook.com
 */

public class JsonParser implements Parser<JSONObject> {
    private static JsonParser utf8;

    public static JsonParser create(@NonNull String charset) {
        return new JsonParser(Utils.nonEmpty(charset, "charset is empty"));
    }

    public static JsonParser getUtf8() {
        if (utf8 == null) {
            utf8 = new JsonParser("UTF-8");
        }
        return utf8;
    }

    private String charset;

    private JsonParser(String charset) {
        this.charset = charset;
    }

    @NonNull
    @Override
    public NetworkData<? extends JSONObject> parse(@NonNull Response data) {
        try {
            String charset = data.charset(this.charset);
            String value = IoUtils.readAndClose(data.body().stream(), charset);
            JSONObject obj = new JSONObject(value);
            return NetworkData.onSuccess(obj);
        } catch (IOException | JSONException e) {
            return NetworkData.onFailure(data.code(), Utils.formatMsg(data.msg(), e));
        }
    }
}
