package sets;

import java.util.Collections;
import java.util.Iterator;

public class EmptySet<T> extends Set<T> {
    @Override
    public Set<T> intersect(Set<T> other) {
        return this;
    }

    @Override
    public Set<T> union(Set<T> other) {
        return other;
    }

    @Override
    public Set<T> difference(Set<T> other) {
        return other;
    }

    @Override
    public boolean contains(T other) {
        return false;
    }

    @Override
    public Iterator<T> iterator() {
        return Collections.emptyIterator();
    }

    @Override
    public boolean equals(Set<T> other) {
        return other.size() == 0;
    }

    @Override
    public int size() {
        return 0;
    }
}
