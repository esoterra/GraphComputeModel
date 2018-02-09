package execution;

import sets.EmptySet;
import sets.Set;
import sets.SetBuilder;

import java.util.HashMap;
import java.util.Map;

public class Digraph<T> {
    private final Set<T> EMPTY = new EmptySet<>();

    private final Map<T, Set<T>> forwardConnections;
    private final Map<T, SetBuilder<T>> backwardConnections = new HashMap<>();

    public Digraph() {
        this.forwardConnections = new HashMap<>();
    }

    public Digraph(Map<T, Set<T>> connections) {
        this.forwardConnections = connections;

        SetBuilder<T> currentSet;
        for(T parent : forwardConnections.keySet()) {
            for(T childNodes : forwardConnections.get(parent)) {
                currentSet = backwardConnections.get(childNodes);
                if(currentSet == null) {
                    currentSet = new SetBuilder<>();
                }
                currentSet.add(parent);
                backwardConnections.put(childNodes,currentSet);
            }
        }
    }

    public synchronized boolean update(T node, Set<T> newConnections) {
        Set<T> oldConnections = get(node);
        forwardConnections.put(node, newConnections);
        boolean changed = false;

        SetBuilder<T> temp;
        for(T newConnection : newConnections) {
            if(!oldConnections.contains(newConnection)) {
                temp = backwardConnections.get(newConnection);
                if(temp == null) {
                    temp = new SetBuilder<>();
                    backwardConnections.put(newConnection, temp);
                }
                temp.add(node);
                changed = true;
            }
        }

        for(T oldConnection : oldConnections) {
            if(!newConnections.contains(oldConnection)) {
                backwardConnections.get(oldConnection).remove(node);
                changed = true;
            }
        }

        return changed;
    }

    public Set<T> get(T node) {
        Set<T> connections = forwardConnections.get(node);
        if(connections == null) {
            return EMPTY;
        }
        return connections;
    }

    public Set<T> getReverse(T node) {
        return backwardConnections.get(node).toSet();
    }
}
