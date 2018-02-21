package sets;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * An immutable unordered collection with no repetition
 * @param <T> The type of the elements of the set
 */
public abstract class Set<T>  implements Iterable<T>, Comparable<Set<T>> {
    /**
     *
     * @param other
     * @return
     */
    public abstract Set<T> union(Set<T> other);
    public abstract Set<T> intersect(Set<T> other);
    public abstract Set<T> difference(Set<T> other);

    public abstract boolean contains(T other);
    public abstract Iterator<T> iterator();
    public abstract boolean equals(Set<T> other);

    public abstract int size();
    public boolean isEmpty() {
        return size() == 0;
    }
    public boolean hasContents() {
        return size() != 0;
    }

    public int compareTo(Set<T> other) {
        return Integer.compare(this.size(), other.size());
    }

    public String toString() {
        StringBuilder outputBuilder = new StringBuilder();
        outputBuilder.append("{");
        for(T element : this) {
            outputBuilder.append(element.toString());
            outputBuilder.append(",");
        }
        if(outputBuilder.length() > 1) {
            outputBuilder.deleteCharAt(outputBuilder.length()-1);
        }
        outputBuilder.append("}");
        return outputBuilder.toString();
    }

    //********************************* Utility Methods ****************************************
    public static <T> Set<T> intersectAll(Set<Set<T>> sets) {
        //Finds the smallest set in the set of sets
        Set<T> smallest = null;

        for(Set<T> someSet : sets) {
            if(smallest == null) {
                smallest = someSet;
                continue;
            }

            if(someSet.size() < smallest.size()) {
                smallest = someSet;
            }
        }

        if(smallest == null) {
            return new EmptySet<>();
        }

        //Iterates through each element of the smallest set checking if it is present in all other sets
        SetBuilder<T> outputBuilder = new SetBuilder<>();
        boolean containedInAll;

        for(T currentElement : smallest) {
            containedInAll = true;

            for(Set<T> otherSet: sets) {
                if(!otherSet.contains(currentElement)) {
                    containedInAll = false;
                }
            }

            if(containedInAll) {
                outputBuilder.add(currentElement);
            }
        }

        return outputBuilder.toSet();
    }

    public static <T> Set<T> unionAll(Set<Set<T>> sets) {
        SetBuilder<T> builder = new SetBuilder<>();

        for(Set<T> someSet: sets) {
            builder.addAll(someSet);
        }

        return builder.toSet();
    }

    public static <T> Set<T> differenceAll(Set<Set<T>> sets) {
        Map<T,Integer> counts = new HashMap<>();

        for(Set<T> set : sets) {
            for(T element : set) {
                if(counts.containsKey(element)) {
                    counts.put(element, counts.get(element)+1);
                } else {
                    counts.put(element, 1);
                }
            }
        }

        SetBuilder<T> outputBuilder = new SetBuilder<>();

        for(T element : counts.keySet()) {
            if(counts.get(element) == 1) {
                outputBuilder.add(element);
            }
        }

        return outputBuilder.toSet();
    }
}
