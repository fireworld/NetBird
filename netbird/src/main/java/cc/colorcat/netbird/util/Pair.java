package cc.colorcat.netbird.util;

import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by cxx on 2017/2/20.
 * xx.ch@outlook.com
 */

public final class Pair {
    private boolean ignoreCase = false;
    private List<String> names;
    private List<String> values;
    private int capacity;

    public Pair() {
        this(4, false);
    }

    public Pair(int capacity) {
        this(capacity, false);
    }

    public Pair(int capacity, boolean ignoreCase) {
        this.capacity = capacity;
        this.ignoreCase = ignoreCase;
    }

    public void add(String name, String value) {
        realAdd(name, value);
    }

    public void set(String name, String value) {
        realRemove(name);
        realAdd(name, value);
    }

    public void addIfNot(String name, String value) {
        if (!contains(name)) {
            realAdd(name, value);
        }
    }

    public void remove(String name) {
        realRemove(name);
    }

    public void clear() {
        if (names != null) {
            names.clear();
            values.clear();
        }
    }

    @Nullable
    public String value(String name) {
        if (names != null) {
            for (int i = 0, size = names.size(); i < size; i++) {
                if (compareName(name, names.get(i))) {
                    return values.get(i);
                }
            }
        }
        return null;
    }

    public String value(String name, String defValue) {
        return Utils.nullElse(value(name), defValue);
    }

    public List<String> values(String name) {
        List<String> vs = null;
        if (names != null) {
            for (int i = 0, size = names.size(); i < size; i++) {
                if (compareName(name, names.get(i))) {
                    if (vs == null) vs = new ArrayList<>(2);
                    vs.add(values.get(i));
                }
            }
        }
        return vs != null ? Collections.unmodifiableList(vs) : Collections.<String>emptyList();
    }

    public List<String> names() {
        return Utils.safeImmutableList(names);
    }

    public Set<String> nameSet() {
        return names != null ? Collections.unmodifiableSet(new HashSet<>(names)) : Collections.<String>emptySet();
    }

    public List<String> values() {
        return Utils.safeImmutableList(values);
    }

    public Map<String, List<String>> toMap() {
        if (names == null) return Collections.emptyMap();
        Map<String, List<String>> result = new HashMap<>();
        for (String name : nameSet()) {
            List<String> vs = new ArrayList<>(1);
            for (int i = 0, size = names.size(); i < size; i++) {
                if (Utils.equal(name, names.get(i))) {
                    vs.add(values.get(i));
                }
            }
            result.put(name, vs);
        }
        return Collections.unmodifiableMap(result);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Pair pair = (Pair) o;

        if (ignoreCase != pair.ignoreCase) return false;
        if (names != null ? !names.equals(pair.names) : pair.names != null) return false;
        return values != null ? values.equals(pair.values) : pair.values == null;

    }

    @Override
    public int hashCode() {
        int result = (ignoreCase ? 1 : 0);
        result = 31 * result + (names != null ? names.hashCode() : 0);
        result = 31 * result + (values != null ? values.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        if (names == null) {
            return "names == null, values == null";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0, size = names.size(); i < size; i++) {
            sb.append(names.get(i)).append(": ").append(values.get(i)).append("\n");
        }
        return sb.toString();
    }

    private boolean realAdd(String name, String value) {
        if (names == null) {
            names = new ArrayList<>(capacity);
            values = new ArrayList<>(capacity);
        }
        return names.add(name) && values.add(value);
    }

    private void realRemove(String name) {
        if (names != null) {
            for (int i = names.size() - 1; i >= 0; i--) {
                if (compareName(name, names.get(i))) {
                    names.remove(i);
                    values.remove(i);
                }
            }
        }
    }

    private boolean contains(String name) {
        if (names != null) {
            for (int i = 0, size = names.size(); i < size; i++) {
                if (compareName(name, names.get(i))) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean compareName(String name, String anotherName) {
        if (name == anotherName) return true;
        if (ignoreCase) {
            return name != null && name.equalsIgnoreCase(anotherName);
        } else {
            return name != null && name.equals(anotherName);
        }
    }
}
