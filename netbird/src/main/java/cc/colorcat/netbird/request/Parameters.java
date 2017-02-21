package cc.colorcat.netbird.request;

import cc.colorcat.netbird.util.Pair;

/**
 * Created by cxx on 2017/2/21.
 * xx.ch@outlook.com
 */
public final class Parameters extends Pair {

    public static Parameters create(int capacity) {
        return new Parameters(capacity);
    }

    private Parameters(int capacity) {
        super(capacity);
    }

    @Override
    public boolean compareName(String name, String anotherName) {
        return name == anotherName || name != null && name.equals(anotherName);
    }
}
