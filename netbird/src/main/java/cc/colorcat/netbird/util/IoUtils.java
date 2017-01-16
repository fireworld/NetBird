package cc.colorcat.netbird.util;

import android.support.annotation.Nullable;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.Charset;

/**
 * Created by cxx on 16-3-11.
 * xx.ch@outlook.com
 */
public final class IoUtils {

    public static void justDump(InputStream is, OutputStream os) throws IOException {
        byte[] buffer = new byte[2048];
        for (int length = is.read(buffer); length != -1; length = is.read(buffer)) {
            os.write(buffer, 0, length);
        }
    }

    public static void dumpAndClose(InputStream is, OutputStream os) throws IOException {
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
            bis = new BufferedInputStream(is);
            bos = new BufferedOutputStream(os);
            byte[] buffer = new byte[2048];
            for (int length = bis.read(buffer); length != -1; length = bis.read(buffer)) {
                bos.write(buffer, 0, length);
            }
            bos.flush();
        } finally {
            close(bis, bos);
        }
    }

    public static String readAndClose(InputStream is, @Nullable String charset) throws IOException {
        return readAndClose(is, Utils.charset(charset));
    }

    public static String readAndClose(InputStream is, @Nullable Charset charset) throws IOException {
        BufferedReader br = null;
        try {
            StringBuilder sb = new StringBuilder();
            Reader reader = charset != null ? new InputStreamReader(is, charset) : new InputStreamReader(is);
            br = new BufferedReader(reader);
            char[] buffer = new char[1024];
            for (int length = br.read(buffer); length != -1; length = br.read(buffer)) {
                sb.append(buffer, 0, length);
            }
            return sb.toString();
        } finally {
            close(br);
        }
    }

    public static byte[] readAndClose(InputStream is) throws IOException {
        byte[] result = null;
        BufferedInputStream bis = null;
        ByteArrayOutputStream bos = null;
        try {
            bis = new BufferedInputStream(is);
            bos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            for (int length = bis.read(buffer); length != -1; length = bis.read(buffer)) {
                bos.write(buffer, 0, length);
            }
            bos.flush();
            result = bos.toByteArray();
        } finally {
            close(bis);
            close(bos);
        }
        return result;
    }

    private static void close(Closeable c1, Closeable c2) {
        close(c1);
        close(c2);
    }

    public static void close(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException e) {
                LogUtils.e(e);
            }
        }
    }

    private IoUtils() {
        throw new AssertionError("no instance");
    }
}