package sets;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;

public class SetImp<T> extends Set<T> {
    private final HashSet<T> contents;

    public SetImp(T... contents) {
        this.contents = new HashSet<>(Arrays.asList(contents));
    }

    SetImp(HashSet<T> contents) {
        this.contents = contents;
    }

    @Override
    public Set<T> union(Set<T> other) {
        SetBuilder<T> outputBuilder = new SetBuilder<>();

        for(T element : this) {
            outputBuilder.add(element);
        }

        for(T element : other) {
            outputBuilder.add(element);
        }

        return outputBuilder.toSet();
    }

    @Override
    public Set<T> intersect(Set<T> other) {
        SetBuilder<T> outputBuilder = new SetBuilder<>();

        Set<T> smaller;
        Set<T> larger;

        if(this.size() < other.size()) {
            smaller = this;
            larger = other;
        } else {
            smaller = other;
            larger = this;
        }

        for(T temp : smaller) {
            if(larger.contains(temp)) {
                outputBuilder.add(temp);
            }
        }

        return outputBuilder.toSet();
    }

    @Override
    public Set<T> difference(Set<T> other) {
        SetBuilder<T> outputBuilder = new SetBuilder<>();

        for(T element : this) {
            if(!other.contains(element)) {
                outputBuilder.add(element);
            }
        }

        for(T element : other) {
            if(!this.contains(element)) {
                outputBuilder.add(element);
            }
        }

        return outputBuilder.toSet();
    }

    @Override
    public boolean contains(T other) {
        return contents.contains(other);
    }

    @Override
    public Iterator<T> iterator() {
        return contents.iterator();
    }

    @Override
    public int size() {
        return contents.size();
    }

    @Override
    public boolean equals(Set<T> other) {
        if(other == null) {
            return false;
        }

        if(this.size() != other.size()) {
            return false;
        }

        for (T temp : this) {
            if (!other.contains(temp)) {
                return false;
            }
        }

        return true;
    }

    //TODO verify hashcode works
    public int hashcode() {
        return contents.hashCode();
    }
}
