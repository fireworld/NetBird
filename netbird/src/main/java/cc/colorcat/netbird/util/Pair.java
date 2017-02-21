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
 * 文中所述之 “数据对” 指 "name=value" 这样成对的数据
 * <p>
 * 本类管理数据对的添加、设置、移除、清理、查看等
 * Note1: 本类允许数据对的 name 和 value 为 {@code null}
 * Note2: 本类非线程安全
 * <p>
 * 方法 {@link Pair#compareName(String, String)} 将决定 name 的匹配方式
 * 你可以覆写 {@link Pair#compareName(String, String)} 来影响 name 的匹配方式，如大小写敏感等
 * <p>
 * Created by cxx on 2017/2/20.
 * xx.ch@outlook.com
 */
public class Pair {
    private List<String> names;
    private List<String> values;

    /**
     * @param capacity 初始化容量大小
     * @throws IllegalArgumentException 如果 capacity 为负将抛出此异常
     */
    protected Pair(int capacity) {
        this.names = new ArrayList<>(capacity);
        this.values = new ArrayList<>(capacity);
    }

    protected Pair(List<String> names, List<String> values) {
        this.names = names;
        this.values = values;
    }

    /**
     * 添加一个数据对（无论此前是否添加过）
     */
    public void add(String name, String value) {
        realAdd(name, value);
    }

    /**
     * 设置一个数据对，如果此前已添加，将移除之前添加的所有与 name 匹配的数据对
     * Note: name 匹配方式取决于 {@link Pair#compareName(String, String)}
     */
    public void set(String name, String value) {
        realRemoveAll(name);
        realAdd(name, value);
    }

    /**
     * 如果此前没有添加过与 name 匹配的数据对就添加，否则忽略
     * Note: name 匹配方式取决于 {@link Pair#compareName(String, String)}
     */
    public void addIfNot(String name, String value) {
        if (!contains(name)) {
            realAdd(name, value);
        }
    }

    /**
     * 移除所有与 name 匹配的数据对
     * Note: name 匹配方式取决于 {@link Pair#compareName(String, String)}
     */
    public void removeAll(String name) {
        realRemoveAll(name);
    }

    /**
     * 清除所有已添加的数据对
     */
    public void clear() {
        names.clear();
        values.clear();
    }

    /**
     * 获取指定索引值的 name
     *
     * @param index 索引值
     * @return 数据对的 name
     * @throws IndexOutOfBoundsException 如果 index 越界将抛出此异常
     */
    public String name(int index) {
        return names.get(index);
    }

    /**
     * 获取指定索引值的 value
     *
     * @param index 索引值
     * @return 数据对的 value
     * @throws IndexOutOfBoundsException 如果 index 越界将抛出此异常
     */
    public String value(int index) {
        return values.get(index);
    }

    /**
     * 根据 name 查找数据对，并返回数据对的 value, 如果存在多个则返回最先添加的
     *
     * @param name 待查找的数据对的 name, name 匹配方式取决于 {@link Pair#compareName(String, String)}
     * @return 返回查找到的数据对的 value, 如果不存在就返回 {@code null}
     */
    @Nullable
    public String value(String name) {
        for (int i = 0, size = names.size(); i < size; i++) {
            if (compareName(name, names.get(i))) {
                return values.get(i);
            }
        }
        return null;
    }

    /**
     * 根据 name 查找数据对，并返回数据对的 value, 如果存在多个则返回最先添加的
     *
     * @param name 待查找的数据对的 name, name 匹配方式取决于 {@link Pair#compareName(String, String)}
     * @return 返回查找到的数据对的 value, 如果不存在就返回 defValue
     */
    public String value(String name, String defValue) {
        return Utils.nullElse(value(name), defValue);
    }

    /**
     * 根据 name 查找数据对，并返回所有匹配的数据对的 value
     *
     * @param name 待查找的数据对的 name, name 匹配方式取决于 {@link Pair#compareName(String, String)}
     * @return 返回不可修改的 {@link List}，如果不存在则返回空 {@link List}
     */
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

    /**
     * @return 所有已添加的数据对的 name，不可修改，顺序与 {@link Pair#values()} 的返回值一一对应
     */
    public List<String> names() {
        return Utils.immutableList(names);
    }

    /**
     * @return 所有已添加的数据对的 value，不可修改，顺序与 {@link Pair#names()} 的返回值一一对应
     */
    public List<String> values() {
        return Utils.immutableList(values);
    }

    /**
     * @return 所有已添加的数据对的 name 集合，不可修改
     * Note: name 匹配方式取决于 {@link Pair#compareName(String, String)}
     */
    public Set<String> nameSet() {
        Set<String> result;
        List<String> ns = new ArrayList<>(names);
        int nullIndex = ns.indexOf(null);
        if (nullIndex >= 0) {
            ns.removeAll(Arrays.asList(new String[]{null}));
        }
        if (compareName("A", "a")) {
            result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
            result.addAll(ns);
        } else {
            result = new TreeSet<>(ns);
        }
        if (nullIndex >= 0) {
            result = new HashSet<>(result);
            result.add(null);
        }
        return Collections.unmodifiableSet(result);
    }

    /**
     * @return name 为结果的键, value 的列表为结果的值
     * Note: name 匹配方式取决于 {@link Pair#compareName(String, String)}
     */
    public Map<String, List<String>> toMap() {
        Map<String, List<String>> result = new HashMap<>();
        for (String name : nameSet()) {
            result.put(name, values(name));
        }
        return Collections.unmodifiableMap(result);
    }

    /**
     * @return 返回已添加的数据对的数量
     */
    public int size() {
        return names.size();
    }

    public boolean isEmpty() {
        return names.isEmpty();
    }

    /**
     * 此方法不会忽略大小写（无论 ignoreCase 为 true 还是 false）
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Pair pair = (Pair) o;

        if (!names.equals(pair.names)) return false;
        return values.equals(pair.values);

    }

    /**
     * 此方法不会忽略大小写（无论 ignoreCase 为 true 还是 false）
     */
    @Override
    public int hashCode() {
        int result = names.hashCode();
        result = 31 * result + values.hashCode();
        return result;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (int i = 0, size = names.size(); i < size; i++) {
            if (i > 0) result.append(", ");
            result.append(names.get(i)).append("=").append(values.get(i));
        }
        return result.toString();
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

    protected boolean compareName(String name, String anotherName) {
        return name == anotherName || name != null && name.equals(anotherName);
    }

//    private boolean compareName(String name, String anotherName) {
//        if (name == anotherName) return true;
//        if (ignoreCase) {
//            return name != null && name.equalsIgnoreCase(anotherName);
//        } else {
//            return name != null && name.equals(anotherName);
//        }
//    }
}
