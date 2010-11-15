package org.mdf.mockdata.reflection;

import java.util.ArrayList;
import java.util.EmptyStackException;

public class UnsynchronizedStack<T> extends ArrayList<T> {
    private static final long serialVersionUID = 1L;

    public T push(T item) {
        add(item);
        return item;
    }

    public T pop() {
        return remove(size() - 1);
    }

    public synchronized T peek() {
        int len = size();
        if (len == 0)
            throw new EmptyStackException();
        return get(len - 1);
    }
}
