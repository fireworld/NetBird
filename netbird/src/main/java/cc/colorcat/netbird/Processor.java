package cc.colorcat.netbird;

import android.support.annotation.NonNull;

/**
 * Created by cxx on 2016/12/20.
 * xx.ch@outlook.com
 */

public interface Processor<T> {

    @NonNull
    T process(@NonNull T t);
}
