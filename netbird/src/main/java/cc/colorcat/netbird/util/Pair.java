package cc.colorcat.netbird.util;

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

/**
 * Created by cxx on 2017/2/20.
 * xx.ch@outlook.com
 */

public final class Pair {
    private boolean ignoreCase = false;
    private List<String> names;
    private List<String> values;

    public Pair() {
        this(4, false);
    }

    public Pair(int capacity) {
        this(capacity, false);
    }

    public Pair(int capacity, boolean ignoreCase) {
        this.names = new ArrayList<>(capacity);
        this.values = new ArrayList<>(capacity);
        this.ignoreCase = ignoreCase;
    }

    public void add(String name, String value) {
        realAdd(name, value);
    }

    public void set(String name, String value) {
        realRemoveAll(name);
        realAdd(name, value);
    }

    public void addIfNot(String name, String value) {
        if (!contains(name)) {
            realAdd(name, value);
        }
    }

    public void removeAll(String name) {
        realRemoveAll(name);
    }

    public void clear() {
        names.clear();
        values.clear();
    }

    @Nullable
    public String value(String name) {
        for (int i = 0, size = names.size(); i < size; i++) {
            if (compareName(name, names.get(i))) {
                return values.get(i);
            }
        }
        return null;
    }

    public String value(String name, String defValue) {
        return Utils.nullElse(value(name), defValue);
    }

    public List<String> values(String name) {
        List<String> result = null;
        for (int i = 0, size = names.size(); i < size; i++) {
            if (compareName(name, names.get(i))) {
                if (result == null) result = new ArrayList<>(2);
                result.add(values.get(i));
            }
        }
        return result != null ? Collections.unmodifiableList(result) : Collections.<String>emptyList();
    }

    public List<String> names() {
        return Utils.immutableList(names);
    }

    public List<String> values() {
        return Utils.immutableList(values);
    }

    public Set<String> nameSet() {
        Set<String> result;
        List<String> ns = new ArrayList<>(names);
        int nullIndex = ns.indexOf(null);
        if (nullIndex >= 0) {
            ns.removeAll(Arrays.asList(new String[]{null}));
        }
        if (ignoreCase) {
            result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
            result.addAll(ns);
        } else {
            result = new TreeSet<>(ns);
        }
        if (nullIndex >= 0) {
            result = new HashSet<>(result);
            result.add(null);
        }
        return result;
    }

    public Map<String, List<String>> toMap() {
        Map<String, List<String>> result = new HashMap<>();
        for (String name : nameSet()) {
            result.put(name, values(name));
        }
        return Collections.unmodifiableMap(result);
    }

    private boolean realAdd(String name, String value) {
        return names.add(name) && values.add(value);
    }

    private void realRemoveAll(String name) {
        for (int i = names.size() - 1; i >= 0; i--) {
            if (compareName(name, names.get(i))) {
                names.remove(i);
                values.remove(i);
            }
        }
    }

    private boolean contains(String name) {
        for (int i = 0, size = names.size(); i < size; i++) {
            if (compareName(name, names.get(i))) {
                return true;
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
