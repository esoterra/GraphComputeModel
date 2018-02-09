package programs;

import execution.Execution;
import sets.Set;

public interface ProgramBuilder<T> {
    T node(T node, Set<T> connections);
    T assignment(Set<T> targetNodes, Set<T> valueNodes);
    T union(Set<T> inputs);
    T intersect(Set<T> inputs);
    T difference(Set<T> inputs);
    T literal(Set<T> inputs);
    T connectionsOf(Set<T> inputs);

    Execution<T> getExecution();
}
