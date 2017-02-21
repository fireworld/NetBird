package cc.colorcat.netbird;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import cc.colorcat.netbird.util.Pair;

public final class Headers extends Pair {
    private static Headers emptyHeaders;

    /**
     * @throws NullPointerException 如果 namesAndValues 为空将抛出此异常
     */
    public static Headers create(Map<String, List<String>> namesAndValues) {
        if (namesAndValues == null) {
            throw new NullPointerException("namesAndValues is null");
        }
        int size = namesAndValues.size();
        List<String> names = new ArrayList<>(size);
        List<String> values = new ArrayList<>(size);
        for (Map.Entry<String, List<String>> entry : namesAndValues.entrySet()) {
            String k = entry.getKey();
            List<String> vs = entry.getValue();
            for (int i = 0, s = vs.size(); i < s; i++) {
                names.add(k);
                values.add(vs.get(i));
            }
        }
        return new Headers(names, values);
    }

    /**
     * @param names  数据对 name 列表，顺序应与 values 一一对应
     * @param values 数所对 value 列表，顺序应与 names 一一对应
     * @return {@link Pair}
     * @throws NullPointerException     如果 names / values 为空将抛出此异常
     * @throws IllegalArgumentException 如果 names.size() != values.size() 将抛出此异常
     */
    public static Headers create(List<String> names, List<String> values) {
        if (names == null || values == null) {
            throw new NullPointerException("names or values is null");
        }
        if (names.size() != values.size()) {
            throw new IllegalArgumentException("names.size() != values.size()");
        }
        return new Headers(names, values);
    }

    public static Headers create(int capacity) {
        return new Headers(capacity);
    }

    public static Headers emptyHeaders() {
        if (emptyHeaders == null) {
            emptyHeaders = new Headers(Collections.<String>emptyList(), Collections.<String>emptyList());
        }
        return emptyHeaders;
    }

    protected Headers(int capacity) {
        super(capacity);
    }

    protected Headers(List<String> names, List<String> values) {
        super(names, values);
    }

    @Override
    public boolean compareName(String name, String anotherName) {
        return name == anotherName || name != null && name.equalsIgnoreCase(anotherName);
    }

    //    public static final Headers EMPTY = new Headers();
//
//    private String[] namesAndValues;
//
//    public static Headers create(@NonNull Map<String, List<String>> headers) {
//        int size = headers.size();
//        List<String> names = new ArrayList<>(size);
//        List<String> values = new ArrayList<>(size);
//        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
//            String n = entry.getKey();
//            List<String> vs = entry.getValue();
//            for (String v : vs) {
//                names.add(n);
//                values.add(v);
//            }
//        }
//        return create(names, values);
//    }
//
//    public static Headers create(@NonNull List<String> names, @NonNull List<String> values) {
//        int size = names.size();
//        if (size != values.size()) {
//            throw new IllegalArgumentException("Expected alternating header names and values");
//        }
//        String[] headers = new String[size << 1];
//        for (int i = 0; i < size; i++) {
//            headers[i << 1] = names.get(i);
//            headers[(i << 1) + 1] = values.get(i);
//        }
//        return new Headers(headers);
//    }
//
//    private Headers(String... namesAndValues) {
//        if (namesAndValues.length % 2 != 0) {
//            throw new IllegalArgumentException("Expected alternating header names and values");
//        }
//        this.namesAndValues = namesAndValues;
//    }
//
//    @Nullable
//    public String name(int index) {
//        return namesAndValues[index << 1];
//    }
//
//    public String value(int index) {
//        return namesAndValues[(index << 1) + 1];
//    }
//
//    public String value(int index, String defValue) {
//        return Utils.nullElse(value(index), defValue);
//    }
//
//    @Nullable
//    public String value(String name) {
//        for (int i = 0, size = size(); i < size; i++) {
//            if (Utils.equalsIgnoreCase(name, name(i))) {
//                return value(i);
//            }
//        }
//        return null;
//    }
//
//    public String value(String name, String defValue) {
//        return Utils.nullElse(value(name), defValue);
//    }
//
//    @NonNull
//    public List<String> values(String name) {
//        List<String> values = null;
//        for (int i = size() - 1; i >= 0; i--) {
//            if (Utils.equalsIgnoreCase(name, name(i))) {
//                if (values == null) values = new ArrayList<>(2);
//                values.add(value(i));
//            }
//        }
//        return Utils.nullElse(values, Collections.<String>emptyList());
//    }
//
//    @NonNull
//    public Set<String> names() {
//        boolean hasNull = false;
//        Set<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
//        for (int i = size() - 1; i >= 0; i--) {
//            if (name(i) != null) {
//                result.add(name(i));
//            } else {
//                hasNull = true;
//            }
//        }
//        if (hasNull) {
//            Set<String> set = new HashSet<>(result);
//            set.add(null);
//            result = set;
//        }
//        return result;
//    }
//
//    public Map<String, List<String>> toMap() {
//        Map<String, List<String>> result = new HashMap<>();
//        for (String name : names()) {
//            result.put(name, values(name));
//        }
//        return result;
//    }
//
//    public int size() {
//        return namesAndValues.length >> 1;
//    }
//
//    @Override
//    public String toString() {
//        return Arrays.toString(namesAndValues);
//    }
}
