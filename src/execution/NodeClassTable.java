package execution;

import static execution.NodeClass.*;

/**
 * An association between nodes and the NodeClass they represent
 * @param <T>
 */
public class NodeClassTable<T> {
    private final T operation;

    private final T union, intersect, difference;

    private final T assignment, assignmentValue;

    private final T literal;
    private final T connectionsOf;

    public static NodeClassTable<String> getDefault() {
        return new NodeClassTable<>(
                "_Operations",
                "_Union",
                "_Intersect",
                "_Difference",
                "_Assignment",
                "_AssignmentValue",
                "_Literal",
                "_ConnectionsOf"
        );
    }

    /**
     *
     * @param operation
     * @param union
     * @param intersect
     * @param difference
     * @param assignment
     * @param assignmentValue
     * @param literal
     * @param connectionsOf
     */
    public NodeClassTable(T operation, T union, T intersect, T difference, T assignment, T assignmentValue, T literal, T connectionsOf) {
        this.operation = operation;
        this.union = union;
        this.intersect = intersect;
        this.difference = difference;
        this.assignment = assignment;
        this.assignmentValue = assignmentValue;
        this.literal = literal;
        this.connectionsOf = connectionsOf;
    }


    public T nodeFor(NodeClass type) {
        switch (type) {
            case OPERATION:
                return operation;
            case UNION:
                return union;
            case INTERSECT:
                return intersect;
            case DIFFERENCE:
                return difference;
            case ASSIGNMENT:
                return assignment;
            case ASSIGNMENT_VALUE:
                return assignmentValue;
            case LITERAL:
                return literal;
            case CONNECTIONS_OF:
                return connectionsOf;
        }
        return operation;
    }

    public NodeClass classOf(T node) {
        //TODO: Address possible performance problems
        if (node.equals(operation)) {
            return OPERATION;
        } else if (node.equals(union)) {
            return UNION;
        } else if (node.equals(intersect)) {
            return INTERSECT;
        } else if (node.equals(difference)) {
            return DIFFERENCE;
        } else if (node.equals(assignment)) {
            return ASSIGNMENT;
        } else if (node.equals(assignmentValue)) {
            return ASSIGNMENT_VALUE;
        } else if (node.equals(literal)) {
            return LITERAL;
        } else if (node.equals(connectionsOf)) {
            return CONNECTIONS_OF;
        }
        return SIMPLE;
    }
}
