package execution;

import sets.EmptySet;
import sets.Set;
import sets.SetBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * An Abstract Data Type representing an unweighted directed association between nodes and other
 * nodes which can be queried in both directions.
 * @param <T> The type of the nodes
 */
public class Digraph<T> {
    /**
     * An Immutable empty set
     */
    private final Set<T> EMPTY = new EmptySet<>();

    /**
     * A mapping from a node represented by type T, to the Set of nodes it connects to
     */
    private final Map<T, Set<T>> forwardConnections;
    /**
     * A mapping from a node represented by type T, to a SetBuilder of the nodes which connect to it
     */
    private final Map<T, SetBuilder<T>> backwardConnections = new HashMap<>();

    /**
     * Empty Digraph Constructor
     */
    public Digraph() {
        this.forwardConnections = new HashMap<>();
    }

    /**
     * @param connections the initial forward connections of the Digraph
     */
    public Digraph(Map<T, Set<T>> connections) {
        this.forwardConnections = new HashMap<>(connections);

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

    /**
     * Updates the connection set of the given node to be the new connection set
     * @param node the given node
     * @param newConnections the new connection set
     * @return change indicator
     */
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

    /**
     * Queries the nodes that the given node connects to
     * @param node the given node
     * @return the connection set of the given node
     */
    public synchronized Set<T> get(T node) {
        Set<T> connections = forwardConnections.get(node);
        if(connections == null) {
            return EMPTY;
        }
        return connections;
    }

    /**
     * Queries the nodes that connect to the given node
     * @param node the given node
     * @return the set of all nodes who connect to the given node
     */
    public synchronized Set<T> getReverse(T node) {
        return backwardConnections.get(node).toSet();
    }
}
