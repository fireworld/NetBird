package cc.colorcat.netbird.request;

import android.support.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import cc.colorcat.netbird.io.InputWrapper;
import cc.colorcat.netbird.util.IoUtils;

/**
 * Created by cxx on 16-12-15.
 * xx.ch@outlook.com
 */

final class FileBody extends RequestBody {
    private String name;
    private File file;
    private String type;
    private Request.UploadListener listener;
    private long contentLength = -1;

    static FileBody create(Request.Pack pack, @Nullable Request.UploadListener listener) {
        return new FileBody(pack.name, pack.file, pack.contentType, listener);
    }

    private FileBody(String name, File file, String type, Request.UploadListener listener) {
        this.name = name;
        this.file = file;
        this.type = type;
        this.listener = listener;
    }

    @Override
    public String contentType() {
        return type;
    }

    @Override
    public long contentLength() throws IOException {
        if (contentLength != -1) return contentLength;
        long size = file.length();
        if (size > 0) {
            contentLength = size;
        }
        return contentLength;
    }

    @Override
    public void writeTo(OutputStream os) throws IOException {
        InputStream is = null;
        try {
            is = new FileInputStream(file);
            if (listener != null) {
                long contentLength = contentLength();
                if (contentLength > 0) {
                    is = InputWrapper.create(is, contentLength, listener);
                }
            }
            IoUtils.justDump(is, os);
        } finally {
            IoUtils.close(is);
        }
    }

    String name() {
        return name;
    }

    String fileName() {
        return file.getName();
    }
}
