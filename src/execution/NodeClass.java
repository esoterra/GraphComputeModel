package execution;

public enum NodeClass {
    OPERATION,  //Operation class nodes
    INVALID,    //Nodes that are members of multiple operation classes
    SIMPLE,     //Nodes that are members of no operation classes

    //Nodes that are members of exactly one operation class
    ASSIGNMENT, ASSIGNMENT_VALUE, UNION, INTERSECT, DIFFERENCE, LITERAL, CONNECTIONS_OF;
}
