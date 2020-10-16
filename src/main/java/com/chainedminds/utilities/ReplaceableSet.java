package com.chainedminds.utilities;

import java.util.HashSet;

public class ReplaceableSet<E> extends HashSet<E> {

    public boolean replace(E e) {

        remove(e);

        return add(e);
    }
}
