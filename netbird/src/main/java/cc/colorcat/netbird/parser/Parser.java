package cc.colorcat.netbird.parser;

import android.support.annotation.NonNull;

import cc.colorcat.netbird.response.NetworkData;
import cc.colorcat.netbird.response.Response;


/**
 * Created by cxx on 16/3/13.
 * xx.ch@outlook.com
 */
public interface Parser<T> {
    /**
     * 将 ResponseBody 解析为目标 Response。
     */
    @NonNull
    NetworkData<? extends T> parse(@NonNull Response data);
}