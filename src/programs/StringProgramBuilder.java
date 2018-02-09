package programs;

import execution.*;
import sets.Set;
import sets.SetBuilder;
import sets.SetImp;

import java.util.HashMap;
import java.util.Map;

public class StringProgramBuilder implements ProgramBuilder<String> {
    private final NodeClassTable<String> classTable;
    private final Map<String, Set<String>> connections = new HashMap<>();

    private final SetBuilder<String> assignmentBuilder = new SetBuilder<>();
    private final SetBuilder<String> assignmentValueBuilder = new SetBuilder<>();

    private final SetBuilder<String> unionBuilder = new SetBuilder<>();
    private final SetBuilder<String> intersectBuilder = new SetBuilder<>();
    private final SetBuilder<String> differenceBuilder = new SetBuilder<>();
    private final SetBuilder<String> literalBuilder = new SetBuilder<>();
    private final SetBuilder<String> connectionsOfBuilder = new SetBuilder<>();

    public StringProgramBuilder() {
        this(NodeClassTable.getDefault());
    }

    public StringProgramBuilder(NodeClassTable<String> classTable) {
        this.classTable = classTable;
    }

    private final Map<String, Integer> counter = new HashMap<>();

    private String getUniqueString(String baseString) {
        Integer count = counter.get(baseString);
        if(count == null) {
            counter.put(baseString, 1);
            return baseString + 0;
        } else {
            counter.put(baseString, count+1);
            return baseString + count;
        }
    }

    @Override
    public String node(String node, Set<String> connectionSet) {
        connections.put(node, connectionSet);
        return node;
    }

    public String addNode(String node, String onlyConnection) {
        return node(node, new SetImp<>(onlyConnection));
    }

    @Override
    public String assignment(Set<String> targetNodes, Set<String> valueNodes) {
        String valueNodeName = node(getUniqueString("AssignmentValue"), valueNodes);
        assignmentValueBuilder.add(valueNodeName);

        SetBuilder<String> connectionsBuilder = new SetBuilder<>();
        connectionsBuilder.add(valueNodeName);
        connectionsBuilder.addAll(targetNodes);

        String assignmentNodeName = node(getUniqueString("Assignment"), connectionsBuilder.toSet());
        assignmentBuilder.add(assignmentNodeName);

        return assignmentNodeName;
    }

    public String addAssignment(String targetNode, String valueNode) {
        return assignment(new SetImp<>(targetNode), new SetImp<>(valueNode));
    }

    @Override
    public String union(Set<String> inputs) {
        String unionNodeName = node(getUniqueString("Union"), inputs);
        unionBuilder.add(unionNodeName);
        return unionNodeName;
    }

    public String addUnion(String onlyInput) {
        return union(new SetImp<>(onlyInput));
    }

    @Override
    public String intersect(Set<String> inputs) {
        String intersectNodeName = node(getUniqueString("Intersect"), inputs);
        intersectBuilder.add(intersectNodeName);
        return intersectNodeName;
    }

    public String addIntersect(String onlyInput) {
        return intersect(new SetImp<>(onlyInput));
    }

    @Override
    public String difference(Set<String> inputs) {
        String differenceNodeName = node(getUniqueString("Difference"), inputs);
        differenceBuilder.add(differenceNodeName);
        return differenceNodeName;
    }

    public String addDifference(String onlyInput) {
        return difference(new SetImp<>(onlyInput));
    }

    @Override
    public String literal(Set<String> inputs) {
        String literalNodeName = node(getUniqueString("Literal"), inputs);
        literalBuilder.add(literalNodeName);
        return literalNodeName;
    }

    public String literal(String onlyInput) {
        return literal(new SetImp<>(onlyInput));
    }

    @Override
    public String connectionsOf(Set<String> inputs) {
        String connectionsOfNodeName = node(getUniqueString("ConnectionsOf"), inputs);
        connectionsOfBuilder.add(connectionsOfNodeName);
        return connectionsOfNodeName;
    }

    public String connectionsOf(String onlyInput) {
        return connectionsOf(new SetImp<>(onlyInput));
    }

    @Override
    public Execution<String> getExecution() {
        Digraph<String> connectionMap = new Digraph<>(connections);
        connectionMap.update(classTable.nodeFor(NodeClass.ASSIGNMENT), assignmentBuilder.toSet());
        connectionMap.update(classTable.nodeFor(NodeClass.ASSIGNMENT_VALUE), assignmentValueBuilder.toSet());
        connectionMap.update(classTable.nodeFor(NodeClass.UNION), unionBuilder.toSet());
        connectionMap.update(classTable.nodeFor(NodeClass.INTERSECT), intersectBuilder.toSet());
        connectionMap.update(classTable.nodeFor(NodeClass.DIFFERENCE), differenceBuilder.toSet());
        connectionMap.update(classTable.nodeFor(NodeClass.LITERAL), literalBuilder.toSet());
        connectionMap.update(classTable.nodeFor(NodeClass.CONNECTIONS_OF), connectionsOfBuilder.toSet());

        connectionMap.update(classTable.nodeFor(NodeClass.OPERATION), new SetImp<>(
                classTable.nodeFor(NodeClass.ASSIGNMENT),
                classTable.nodeFor(NodeClass.ASSIGNMENT_VALUE),
                classTable.nodeFor(NodeClass.UNION),
                classTable.nodeFor(NodeClass.INTERSECT),
                classTable.nodeFor(NodeClass.DIFFERENCE),
                classTable.nodeFor(NodeClass.LITERAL),
                classTable.nodeFor(NodeClass.CONNECTIONS_OF)
        ));

        return new Execution<>(classTable, connectionMap, new Digraph<>());
    }
}
