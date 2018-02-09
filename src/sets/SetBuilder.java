package sets;

import java.util.HashSet;
import java.util.Iterator;

public class SetBuilder<T> {
    private HashSet<T> contents = new HashSet<>();
    private Set<T> lastSet = null;

    public synchronized boolean contains(T element) {
        return contents.contains(element);
    }

    public synchronized void add(T element) {
        rebuildReference();
        contents.add(element);
    }

    public synchronized void remove(T element) {
        rebuildReference();
        contents.remove(element);
    }

    public synchronized void addAll(Set<T> elements) {
        rebuildReference();
        for (T element : elements) {
            contents.add(element);
        }
    }

    private void rebuildReference() {
        if(lastSet != null) {
            contents = new HashSet<>(contents);
            lastSet = null;
        }
    }

    public synchronized void clear() {
        contents = new HashSet<>();
        lastSet = null;
    }

    public synchronized int size() {
        return contents.size();
    }

    public synchronized Set<T> toSet() {
        if(lastSet == null) {
            lastSet = new SetImp<>(contents);
        }
        return lastSet;
    }
}
