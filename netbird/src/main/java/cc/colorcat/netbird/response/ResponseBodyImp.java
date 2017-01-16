package cc.colorcat.netbird.response;

import android.support.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

import cc.colorcat.netbird.util.IoUtils;

/**
 * Created by cxx on 2017/1/15.
 * xx.ch@outlook.com
 */

public class ResponseBodyImp extends ResponseBody {
    private Charset charset;

    public ResponseBodyImp(@NonNull InputStream is, Charset charset) {
        super(is);
        this.charset = charset;
    }

    @Override
    public String string() throws IOException {
        return IoUtils.readAndClose(is, charset);
    }

    @Override
    public Reader reader() {
        return charset != null ? new InputStreamReader(is, charset) : new InputStreamReader(is);

    }
}
