package cc.colorcat.netbirddemo;

import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.io.Reader;

import cc.colorcat.netbird.parser.Parser;
import cc.colorcat.netbird.response.NetworkData;
import cc.colorcat.netbird.response.Response;
import cc.colorcat.netbird.util.IoUtils;
import cc.colorcat.netbird.util.Utils;


/**
 * Created by cxx on 16-11-1.
 * xx.ch@outlook.com
 */

public class GsonParser<T> implements Parser<T> {
    private static final Gson GSON;
    private TypeToken<T> token;

    static {
        GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd").serializeNulls().create();
    }

    public GsonParser(@NonNull TypeToken<T> token) {
        this.token = Utils.nonNull(token, "token == null");
    }

    @NonNull
    @Override
    public NetworkData<? extends T> parse(@NonNull Response data) {
        Reader reader = null;
        try {
            reader = data.body().reader();
            T t = GSON.fromJson(reader, token.getType());
            return NetworkData.newSuccess(t);
        } catch (JsonParseException e) {
            return NetworkData.newFailure(data.code(), Utils.formatMsg(data.msg(), e));
        } finally {
            IoUtils.close(reader);
        }
    }
}
