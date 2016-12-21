package cc.colorcat.netbird.io;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import cc.colorcat.netbird.util.Utils;


/**
 * Created by cxx on 2016/12/12.
 * xx.ch@outlook.com
 */

public class OutputWrapper extends OutputStream {
    private OutputStream delegate;
    private final long contentLength;
    private ProgressListener listener;
    private long finished = 0;
    private int currentPercent;
    private int lastPercent = currentPercent;

    public static OutputWrapper create(@NonNull OutputStream os) {
        return new OutputWrapper(os, 0, null);
    }

    /**
     * @param os            数据将写入此 OutputStream
     * @param contentLength 需要写入的数据总长度
     * @param listener      写入数据进度监听器
     * @return OutputWrapper
     */
    public static OutputWrapper create(@NonNull OutputStream os, long contentLength, @Nullable ProgressListener listener) {
        return new OutputWrapper(os, contentLength, listener);
    }

    /**
     * @param file 数据将写入此文件
     * @return OutputWrapper
     * @throws RuntimeException 如果 file 文件不存在将抛出此异常
     */
    public static OutputWrapper create(@NonNull File file) {
        return create(file, -1, null);
    }

    /**
     * @param file          数据将写入此文件
     * @param contentLength 需要写入的数据总长度
     * @param listener      写入数据进度监听器
     * @return OutputWrapper
     * @throws RuntimeException 如果 file 不存在将抛出此异常
     */
    public static OutputWrapper create(@NonNull File file, long contentLength, @Nullable ProgressListener listener) {
        try {
            return new OutputWrapper(new FileOutputStream(file), contentLength, listener);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private OutputWrapper(OutputStream os, long contentLength, ProgressListener listener) {
        this.delegate = Utils.nonNull(os, "os == null");
        this.contentLength = contentLength;
        this.listener = listener;
    }


    @Override
    public void write(@NonNull byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    @Override
    public void write(@NonNull byte[] b, int off, int len) throws IOException {
        delegate.write(b, off, len);
        if (listener != null && contentLength > 0) {
            finished += len;
            currentPercent = (int) (finished * 100 / contentLength);
            if (currentPercent > lastPercent) {
                updateProgress(finished, contentLength, currentPercent);
                lastPercent = currentPercent;
            }
        }
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
    public void flush() throws IOException {
        delegate.flush();
    }

    @Override
    public void close() throws IOException {
        delegate.close();
        delegate = null;
        listener = null;
    }

    @Override
    public void write(int b) throws IOException {
        delegate.write(b);
    }
}
