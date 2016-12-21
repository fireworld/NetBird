package cc.colorcat.netbird;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import cc.colorcat.netbird.util.Utils;


public final class Headers {
    public static final Headers EMPTY = new Headers();

    private String[] namesAndValues;

    public static Headers create(@NonNull Map<String, List<String>> headers) {
        int size = headers.size();
        List<String> names = new ArrayList<>(size);
        List<String> values = new ArrayList<>(size);
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            String n = entry.getKey();
            List<String> vs = entry.getValue();
            for (String v : vs) {
                names.add(n);
                values.add(v);
            }
        }
        return create(names, values);
    }

    public static Headers create(@NonNull List<String> names, @NonNull List<String> values) {
        int size = names.size();
        if (size != values.size()) {
            throw new IllegalArgumentException("Expected alternating header names and values");
        }
        String[] headers = new String[size << 1];
        for (int i = 0; i < size; i++) {
            headers[i << 1] = names.get(i);
            headers[(i << 1) + 1] = values.get(i);
        }
        return new Headers(headers);
    }

    private Headers(String... namesAndValues) {
        if (namesAndValues.length % 2 != 0) {
            throw new IllegalArgumentException("Expected alternating header names and values");
        }
        this.namesAndValues = namesAndValues;
    }

    @Nullable
    public String name(int index) {
        return namesAndValues[index << 1];
    }

    public String value(int index) {
        return namesAndValues[(index << 1) + 1];
    }

    public String value(int index, String defValue) {
        return Utils.nullElse(value(index), defValue);
    }

    @Nullable
    public String value(String name) {
        for (int i = 0, size = size(); i < size; i++) {
            if (Utils.equalsIgnoreCase(name, name(i))) {
                return value(i);
            }
        }
        return null;
    }

    public String value(String name, String defValue) {
        return Utils.nullElse(value(name), defValue);
    }

    @NonNull
    public List<String> values(String name) {
        List<String> values = null;
        for (int i = size() - 1; i >= 0; i--) {
            if (Utils.equalsIgnoreCase(name, name(i))) {
                if (values == null) values = new ArrayList<>(2);
                values.add(value(i));
            }
        }
        return Utils.nullElse(values, Collections.<String>emptyList());
    }

    @NonNull
    public Set<String> names() {
        boolean hasNull = false;
        Set<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (int i = size() - 1; i >= 0; i--) {
            if (name(i) != null) {
                result.add(name(i));
            } else {
                hasNull = true;
            }
        }
        if (hasNull) {
            Set<String> set = new HashSet<>(result);
            set.add(null);
            result = set;
        }
        return result;
    }

    public Map<String, List<String>> toMap() {
        Map<String, List<String>> result = new HashMap<>();
        for (String name : names()) {
            result.put(name, values(name));
        }
        return result;
    }

    public int size() {
        return namesAndValues.length >> 1;
    }

    @Override
    public String toString() {
        return Arrays.toString(namesAndValues);
    }
}
