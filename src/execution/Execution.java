package execution;

import sets.*;
import execution.ExecutionException.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

//TODO: Ensure nodes whose NodeClass have changed are queued for update
//TODO: Queue connectionsOf operations for update that operate on modified nodes
//TODO: Make value updates within a step parallel to ensure Determinism (WIP)
//TODO: Handle multi-assignment case

public class Execution<T> {
    private static final boolean DEBUG = false;

    private final Set<T> emptySet = new EmptySet<>();

    private final NodeClassTable<T> classTable;
    private Set<T> opNodes;
    private Set<T> literalNodes;
    private Set<T> connectionsOfNodes;
    private Set<T> assignmentNodes;

    private final Digraph<T> connections;
    private final Digraph<T> values;

    private final SetBuilder<T> nextUpdate = new SetBuilder<>();
    private final Map<T, Set<T>> valueChanges = new HashMap<>();
    private final SetBuilder<T> nextAssignments = new SetBuilder<>();

    public Execution(NodeClassTable<T> classTable, Digraph<T> connections, Digraph<T> values) {
        this.classTable = classTable;
        this.connections = connections;
        this.values = values;

        nextUpdate.addAll(getConnectionsFrom(classTable.nodeFor(NodeClass.LITERAL)));
    }

    public synchronized boolean executeStep() throws ExecutionException {
        opNodes = getConnectionsFrom(classTable.nodeFor(NodeClass.OPERATION));
        literalNodes = getConnectionsFrom(classTable.nodeFor(NodeClass.LITERAL));
        connectionsOfNodes = getConnectionsFrom(classTable.nodeFor(NodeClass.CONNECTIONS_OF));
        assignmentNodes = getConnectionsFrom(classTable.nodeFor(NodeClass.ASSIGNMENT));

        Set<T> updateSet = nextUpdate.toSet();
        nextUpdate.clear();

        NodeClass currentNodeClass;
        int count = 0;

        while(updateSet.hasContents()) {
            if(DEBUG) System.out.println("> Loop cycle " + count++ + "");
            if(DEBUG) System.out.println();

            //TODO: Add queueing system to push updates to the value sets at the end of each sub-step
            for(T currentNode : updateSet) {
                currentNodeClass = getNodeClass(currentNode);

                switch (currentNodeClass) {
                    case ASSIGNMENT:
                        nextAssignments.add(currentNode);
                        continue;

                    case ASSIGNMENT_VALUE:
                        nextAssignments.addAll(getConnectionsTo(currentNode).intersect(assignmentNodes));
                        continue;

                    case OPERATION:
                    case SIMPLE:
                        continue;

                    case INVALID:
                        throw new InvalidOperation("Invalid Operation Type Evaluated");
                }

                valueChanges.put(currentNode, processNode(currentNode, currentNodeClass));
            }

            Iterator<Map.Entry<T,Set<T>>> it = valueChanges.entrySet().iterator();
            Map.Entry<T,Set<T>> entry;

            while(it.hasNext()) {
                entry = it.next();
                if (updateValuesAt(entry.getKey(), entry.getValue())) {
                    nextUpdate.addAll(getConnectionsTo(entry.getKey()));
                }
                it.remove();
            }

            updateSet = nextUpdate.toSet();
            nextUpdate.clear();

        }

        //*** Process the Queued Assignments ***
        Set<T> inputNodes;
        SetBuilder<T> assignmentValues;
        SetBuilder<T> alteredNodes;

        for(T currentAssignment : nextAssignments.toSet()) {
            inputNodes = getConnectionsFrom(currentAssignment);
            assignmentValues = new SetBuilder<>();
            alteredNodes = new SetBuilder<>();

            for (T currentInput : inputNodes) {
                if(getNodeClass(currentInput) == NodeClass.ASSIGNMENT_VALUE) {
                    assignmentValues.addAll(Set.unionAll(getValues(getConnectionsFrom(currentInput))));
                } else {
                    alteredNodes.addAll(getValues(currentInput));
                }
            }

            if(DEBUG) System.out.println("Assign: " + alteredNodes.toSet() + " = " + assignmentValues.toSet());

            for(T alteredNode : alteredNodes.toSet()) {
                if(updateConnectionsFrom(alteredNode, assignmentValues.toSet())) {
                    if(literalNodes.contains(alteredNode)) {
                        nextUpdate.add(alteredNode);
                    }
                    nextUpdate.addAll(values.getReverse(alteredNode).intersect(connectionsOfNodes));
                }
            }
        }
        nextAssignments.clear();
        return nextUpdate.size() != 0;
    }

    /**
     * Determines the NodeClass of a given node
     *
     * @param node the node being checked
     * @return the NodeClass of that node
     */
    private NodeClass getNodeClass(T node) {
        if(opNodes.contains(node) || node.equals(classTable.nodeFor(NodeClass.OPERATION))) {
            return NodeClass.OPERATION;
        }

        Set<T> potentialClasses = getConnectionsTo(node).intersect(opNodes);
        if(potentialClasses.size() == 0) {
            return NodeClass.SIMPLE;
        } else if(potentialClasses.size() == 1) {
            return classTable.classOf(potentialClasses.iterator().next());
        } else {
            return NodeClass.INVALID;
        }
    }

    /**
     * Takes the identified Node and performs the operation it represents on its inputs to
     * determine the new value of that node
     *
     * @param node the node being processed
     * @return the new value that node should have
     */
    private Set<T> processNode(T node, NodeClass nodeClass) throws ExecutionException {
        Set<T> inputNodes = getConnectionsFrom(node);

        switch (nodeClass) {
            case UNION:
                return Set.unionAll(getValues(inputNodes));

            case INTERSECT:
                return Set.intersectAll(getValues(inputNodes));

            case DIFFERENCE:
                return Set.differenceAll(getValues(inputNodes));

            case LITERAL:
                return getConnectionsFrom(node);

            case CONNECTIONS_OF:
                return Set.unionAll(getConnectionsFrom(Set.unionAll(getValues(inputNodes))));
        }

        return emptySet;
    }

    //Values -----------------------------------------------------------------------------------------------------------
    private boolean updateValuesAt(T node, Set<T> newValues) {
        Set<T> oldValues = getValues(node);
        if(DEBUG) System.out.println(node + ": " + getConnectionsFrom(node) + " ( " + oldValues + " => " + newValues + " )");

        if(newValues.equals(oldValues)) {
            return false;
        } else {
            values.update(node, newValues);
            return true;
        }
    }

    public synchronized Set<T> queryNode(T node) {
        return getConnectionsFrom(node);
    }

    private Set<T> getValues(T node) {
        Set<T> nodeValues = values.get(node);
        if(nodeValues == null) {
            return emptySet;
        }
        return nodeValues;
    }

    private Set<Set<T>> getValues(Set<T> nodes) {
        SetBuilder<Set<T>> outputBuilder = new SetBuilder<>();

        for (T node : nodes) {
            outputBuilder.add(getValues(node));
        }

        return outputBuilder.toSet();
    }

    //Connections ------------------------------------------------------------------------------------------------------
    /**
     * Removes all existing connections from the specified node and makes connections from the
     * specified node to all of the nodes specified in the newConnections Set. This method also returns
     * whether or not the invocation of this method changed anything; if the previous and new connection
     * set are the same the return value will be false.
     *
     * @param node the node being updated
     * @param newConnections the nodes new connections
     * @return whether the connection set changed
     */
    private boolean updateConnectionsFrom(T node, Set<T> newConnections) {
        return connections.update(node, newConnections);
    }

    /**
     * Gets the Set of the nodes that this node connects to
     * @param node the target node
     * @return the connection set of that node
     */
    private Set<T> getConnectionsFrom(T node) {
        return connections.get(node);
    }

    /**
     * Takes the specified nodes and gets the union of the nodes that they connect to
     * @param sets the set of target nodes
     * @return the union of the target nodes connection sets
     */
    private Set<Set<T>> getConnectionsFrom(Set<T> sets) {
        SetBuilder<Set<T>> outputBuilder = new SetBuilder<>();

        for (T set : sets) {
            outputBuilder.add(getConnectionsFrom(set));
        }

        return outputBuilder.toSet();
    }

    /**
     * Gets the Set of the nodes that connect to this node
     * @param node the target node
     * @return the nodes that the target node appears in the connection set of
     */
    private Set<T> getConnectionsTo(T node) {
        return connections.getReverse(node);
    }

    /**
     * Gets the Set of the nodes that connect to these nodes
     * @param nodes the target nodes
     * @return the nodes that any of the target nodes appear in the connection set of
     */
    private Set<Set<T>> getConnectionsTo(Set<T> nodes) {
        SetBuilder<Set<T>> outputBuilder = new SetBuilder<>();

        for (T set : nodes) {
            outputBuilder.add(getConnectionsTo(set));
        }

        return outputBuilder.toSet();
    }
}
