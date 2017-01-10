package cc.colorcat.netbird.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadFactory;

import cc.colorcat.netbird.response.Response;

/**
 * Created by cxx on 16-12-13.
 * xx.ch@outlook.com
 */

public class Utils {
    private static final Handler HANDLER = new Handler(Looper.getMainLooper());


    public static void postOnUi(Runnable runnable) {
        HANDLER.post(runnable);
    }

    public static boolean isUiThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }


    public static String smartEncode(String value) {
        try {
            String decodedValue = decode(value);
            if (!value.equals(decodedValue)) {
                return value;
            }
        } catch (Exception e) {
            LogUtils.e(e);
        }
        return encode(value);
    }

    private static String encode(String value) {
        try {
            return URLEncoder.encode(value, Const.UTF8);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private static String decode(String value) {
        try {
            return URLDecoder.decode(value, Const.UTF8);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String checkedHttp(String url) {
        if (!url.startsWith("http")) {
            throw new IllegalArgumentException("Bad url, the scheme must be http or https");
        }
        return url;
    }

    public static void checkHeader(String name, String value) {
        if (name == null) throw new NullPointerException("name == null");
        if (name.isEmpty()) throw new IllegalArgumentException("name is empty");
        for (int i = 0, length = name.length(); i < length; i++) {
            char c = name.charAt(i);
            if (c <= '\u001f' || c >= '\u007f') {
                throw new IllegalArgumentException(String.format(Locale.getDefault(),
                        "Unexpected char %#04x at %d in header name: %s", (int) c, i, name));
            }
        }
        if (value == null) throw new NullPointerException("value == null");
        for (int i = 0, length = value.length(); i < length; i++) {
            char c = value.charAt(i);
            if (c <= '\u001f' || c >= '\u007f') {
                throw new IllegalArgumentException(String.format(Locale.getDefault(),
                        "Unexpected char %#04x at %d in %s value: %s", (int) c, i, name, value));
            }
        }
    }

    @Nullable
    public static Charset charset(String charset) {
        try {
            return Charset.forName(charset);
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    public static String parseCharset(String contentType) {
        if (contentType != null) {
            String[] params = contentType.split(";");
            final int length = params.length;
            for (int i = 1; i < length; i++) {
                String[] pair = params[i].trim().split("=");
                if (pair.length == 2) {
                    if (pair[0].equalsIgnoreCase("charset")) {
                        return pair[1];
                    }
                }
            }
        }
        return null;
    }


    /**
     * concat url query parameter
     */
    public static String concatQuery(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            sb.append(entry.getKey()).append('=').append(entry.getValue()).append('&');
        }
        int lastIndex = sb.length() - 1;
        if (lastIndex > 0 && '&' == sb.charAt(lastIndex)) {
            sb.deleteCharAt(lastIndex);
        }
        return sb.toString();
    }

    public static String concatQuery(List<String> names, List<String> values) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0, size = names.size(); i < size; i++) {
            if (i > 0) sb.append('&');
            sb.append(names.get(i)).append('=').append(values.get(i));
        }
        return sb.toString();
    }


    @Nullable
    public static Bitmap decodeStream(InputStream is, int reqWidth, int reqHeight) {
        Bitmap bitmap = null;
        if (reqWidth <= 0 || reqHeight <= 0) {
            bitmap = BitmapFactory.decodeStream(is);
        } else {
            BufferedInputStream bis = new BufferedInputStream(is);
            try {
                bis.mark(bis.available());
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(bis, null, options);
                options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
                options.inJustDecodeBounds = false;
                bis.reset();
                bitmap = BitmapFactory.decodeStream(bis, null, options);
            } catch (IOException e) {
                LogUtils.e(e);
            } finally {
                IoUtils.close(bis);
            }
        }
        return bitmap;
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public static boolean isEmpty(CharSequence txt) {
        return txt == null || txt.length() == 0;
    }

    public static <T> T nullElse(T value, T other) {
        return value != null ? value : other;
    }

    public static <T> T nonNull(T value) {
        return nonNull(value, "value == null");
    }

    public static <T> T nonNull(T value, String msg) {
        if (value == null) {
            throw new NullPointerException(msg);
        }
        return value;
    }

    public static <T, E extends Throwable> T nullThrow(T value, E e) throws E {
        if (value == null) {
            throw e;
        }
        return value;
    }

    public static <T extends CharSequence> T nonEmpty(T txt, String msg) {
        if (Utils.isEmpty(txt)) {
            throw new IllegalArgumentException(msg);
        }
        return txt;
    }

    public static <T extends CharSequence> T emptyElse(T value, T other) {
        return !Utils.isEmpty(value) ? value : other;
    }

    public static String formatMsg(String responseMsg, Exception e) {
        return String.format(Locale.getDefault(), "Response Msg: %s%nException Detail: %s", responseMsg, e);
    }

    public static boolean isNullOrEmpty(Collection<?> col) {
        return col == null || col.isEmpty();
    }

    /**
     * Returns an immutable copy of {@code list}.
     */
    public static <T> List<T> immutableList(List<T> list) {
        return Collections.unmodifiableList(new ArrayList<>(list));
    }


    public static ThreadFactory threadFactory(final String name, final boolean daemon) {
        return new ThreadFactory() {
            @Override
            public Thread newThread(@NonNull Runnable runnable) {
                Thread result = new Thread(runnable, name);
                result.setDaemon(daemon);
                return result;
            }
        };
    }

    public static boolean equal(Object a, Object b) {
        return a == b || (a != null && a.equals(b));
    }

    public static boolean equalsIgnoreCase(String a, String b) {
        return a == b || (a != null && a.equalsIgnoreCase(b));
    }

    public static <T extends Comparable<? super T>> boolean equalList(List<T> one, List<T> two) {
        if (one == null && two == null) {
            return true;
        }

        if (one != null && two != null) {
            if (one.size() != two.size()) {
                return false;
            }
            one = new ArrayList<>(one);
            two = new ArrayList<>(two);
            Collections.sort(one);
            Collections.sort(two);
            return one.equals(two);
        }

        return false;
    }

    public static long quiteParse(String value, long defValue) {
        long result = defValue;
        try {
            result = Long.parseLong(value);
        } catch (Exception ignore) {

        }
        return result;
    }

    public static void logHeader(Response data) {
        LogUtils.d("Headers", "--------------------------- start ---------------------------");
        Map<String, List<String>> map = data.headers().toMap();
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            String name = entry.getKey();
            for (String value : entry.getValue()) {
                LogUtils.i("Headers", name + " : " + value);
            }
        }
        LogUtils.d("Headers", "--------------------------- end ---------------------------");
    }

    private Utils() {
        throw new AssertionError("no instance");
    }
}
