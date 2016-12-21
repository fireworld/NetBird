package cc.colorcat.netbird.io;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import cc.colorcat.netbird.util.Utils;


/**
 * Created by cxx on 2016/12/12.
 * xx.ch@outlook.com
 */
public class InputWrapper extends InputStream {
    private InputStream delegate;
    private ProgressListener listener;
    private final long contentLength;
    private long finished = 0;
    private int currentPercent;
    private int lastPercent = currentPercent;

    public static InputWrapper create(@NonNull InputStream is) {
        return create(is, -1, null);
    }

    /**
     * @param is            数据读取的来源
     * @param contentLength is 所包含的数据总长度
     * @param listener      读取数据进度监听器
     * @return InputWrapper
     */
    public static InputWrapper create(@NonNull InputStream is, long contentLength, @Nullable ProgressListener listener) {
        return new InputWrapper(is, contentLength, listener);
    }

    /**
     * @param file 数据来源于此文件
     * @return InputWrapper
     * @throws RuntimeException 如果 file 不存在将抛出此异常
     */
    public static InputWrapper create(@NonNull File file) {
        return create(file, null);
    }

    /**
     * @param file     数据来源于此文件
     * @param listener 读取数据进度监听器
     * @return InputWrapper
     * @throws RuntimeException 如果 file 不存在将抛出此异常
     */
    public static InputWrapper create(@NonNull File file, @Nullable ProgressListener listener) {
        try {
            return new InputWrapper(new FileInputStream(file), file.length(), listener);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private InputWrapper(@NonNull InputStream is, long contentLength, ProgressListener listener) {
        this.delegate = Utils.nonNull(is, "is == null");
        this.contentLength = contentLength;
        if (this.contentLength > 0) {
            this.listener = listener;
        }
    }

    @Override
    public int read(@NonNull byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(@NonNull byte[] b, int off, int len) throws IOException {
        int read = delegate.read(b, off, len);
        if (listener != null && contentLength > 0) {
            finished += read;
            currentPercent = (int) (finished * 100 / contentLength);
            if (currentPercent > lastPercent) {
                updateProgress(finished, contentLength, currentPercent);
                lastPercent = currentPercent;
            }
        }
        return read;
    }

    private void updateProgress(final long finished, final long contentLength, final int percent) {
        if (Utils.isUiThread()) {
            listener.onChanged(finished, contentLength, percent);
        } else {
            Utils.postOnUi(new Runnable() {
                @Override
                public void run() {
                    listener.onChanged(finished, contentLength, percent);
                }
            });
        }
    }

    @Override
    public long skip(long n) throws IOException {
        return delegate.skip(n);
    }

    @Override
    public int available() throws IOException {
        return delegate.available();
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

    @Override
    public void mark(int readLimit) {
        delegate.mark(readLimit);
    }

    @Override
    public void reset() throws IOException {
        delegate.reset();
    }

    @Override
    public boolean markSupported() {
        return delegate.markSupported();
    }

    @Override
    public int read() throws IOException {
        return delegate.read();
    }
}
