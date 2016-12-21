package cc.colorcat.netbird.response;

import android.support.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import cc.colorcat.netbird.util.IoUtils;
import cc.colorcat.netbird.util.Utils;

/**
 * Created by cxx on 2016/12/12.
 * xx.ch@outlook.com
 */

public abstract class ResponseBody {
    protected InputStream is;

    protected ResponseBody(@NonNull InputStream is) {
        this.is = Utils.nonNull(is, "is == null");
    }

    public InputStream stream() {
        return is;
    }

    public abstract String string() throws IOException;

    public byte[] bytes() throws IOException {
        return IoUtils.readAndClose(is);
    }

    public abstract Reader reader();
}
