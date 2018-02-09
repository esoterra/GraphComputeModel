package tests;

import execution.Execution;
import execution.ExecutionException;
import programs.StringProgramBuilder;
import sets.EmptySet;
import sets.Set;
import sets.SetBuilder;
import sets.SetImp;

public class TuringMachineTest {
    private static final Set<String> EMPTY_SET = new EmptySet<>();
    private static final String LEFT = "Dir(L)", RIGHT = "Dir(R)";

    private static final String[] STATE = new String[]{ "State(0)", "State(1)", "State(2)" };
    private static final boolean[] STATE_HALTS = new boolean[]{ false, false, true };

    private static final String[] SYMBOL = new String[]{ "Symbol(_)", "Symbol(0)", "Symbol(1)" };

    private static final String[][] EVENTS = new String[][] {
            new String[] { "Event(0,A)", "Event(0,B)", "Event(0,C)" },
            new String[] { "Event(1,A)", "Event(1,B)", "Event(1,C)" },
            new String[] { "Event(2,A)", "Event(2,B)", "Event(2,C)" }
    };

    private static final String[][] NEXT_STATE = new String[][] {
            new String[] { STATE[1], STATE[1], STATE[1] },
            new String[] { STATE[0], STATE[0], STATE[0] },
            null
    };

    private static final String[][] WRITE_SYMBOL = new String[][] {
            new String[] { SYMBOL[1], SYMBOL[1], SYMBOL[1] },
            new String[] { SYMBOL[2], SYMBOL[2], SYMBOL[2] },
            null
    };

    private static final String[][] SHIFT = new String[][] {
            new String[] { RIGHT, RIGHT, RIGHT },
            new String[] { RIGHT, RIGHT, RIGHT },
            null
    };

    private static final String EMPTY_SYMBOL = SYMBOL[0];
    private static final int TAPE_LENGTH = 10;

    public static void main(String[] args) {
        StringProgramBuilder b = new StringProgramBuilder();

        //Tape Construction
        SetBuilder<String> indexBuilder = new SetBuilder<>();
        SetBuilder<String> valueBuilder = new SetBuilder<>();
        SetBuilder<String> incrementerBuilder = new SetBuilder<>();
        SetBuilder<String> decrementerBuilder = new SetBuilder<>();

        String indexName, valueName, incName, decName;
        for(int i = 0; i< TAPE_LENGTH; i++) {
            indexName = "I"+i;
            valueName = "val"+i;
            incName = "inc"+i;
            decName = "dec"+i;

            if(i == TAPE_LENGTH-1) {
                b.node(indexName, new SetImp<>(decName, valueName));
            } else {
                if(i == 0) {
                    b.node(indexName, new SetImp<>(incName, valueName));
                } else {
                    b.node(indexName, new SetImp<>(incName, decName, valueName));
                }
            }

            indexBuilder.add(indexName);
            valueBuilder.add(b.addNode(valueName, EMPTY_SYMBOL));
        }

        for(int i = 0; i< TAPE_LENGTH-1; i++) {
            incrementerBuilder.add(b.addNode("inc"+i, "I"+(i+1)));
        }

        for(int i = 1; i< TAPE_LENGTH; i++) {
            b.addNode("dec"+i, "I"+(i-1));
            decrementerBuilder.add("dec"+i);
        }

        String Val = b.literal(valueBuilder.toSet());
        String Inc = b.literal(incrementerBuilder.toSet());
        String Dec = b.literal(decrementerBuilder.toSet());
        String Shift = b.literal(new SetImp<>(Inc, Dec));

        //Table Construction
        String nextState, writeSymbol, direction;

        for(int stateNum = 0; stateNum < STATE.length; stateNum++) {
            for(int symbolNum = 0; symbolNum < SYMBOL.length; symbolNum++) {
                if(STATE_HALTS[stateNum]) {
                    b.node(EVENTS[stateNum][symbolNum], EMPTY_SET);
                    continue;
                }

                nextState = NEXT_STATE[stateNum][symbolNum];
                writeSymbol = WRITE_SYMBOL[stateNum][symbolNum];
                if(SHIFT[stateNum][symbolNum].equals(LEFT)) {
                    direction = Dec;
                } else {
                    direction = Inc;
                }
                b.node(EVENTS[stateNum][symbolNum], new SetImp<>(nextState, writeSymbol, direction));
            }
        }

        //State Node setup
        String stateNode = b.literal(new SetImp<>(STATE[0], STATE[1], STATE[2]));

        SetBuilder<String> state0Connections = new SetBuilder<>();
        SetBuilder<String> state1Connections = new SetBuilder<>();
        SetBuilder<String> state2Connections = new SetBuilder<>();

        for(int i=0; i < SYMBOL.length; i++) {
            state0Connections.add(EVENTS[0][i]);
            state1Connections.add(EVENTS[1][i]);
            state2Connections.add(EVENTS[2][i]);
        }

        b.node(STATE[0], state0Connections.toSet());
        b.node(STATE[1], state1Connections.toSet());
        b.node(STATE[2], state2Connections.toSet());

        //Symbol Node setup
        String symbolNode = b.literal(new SetImp<>(SYMBOL[0], SYMBOL[1], SYMBOL[2]));

        SetBuilder<String> symbolAConnections = new SetBuilder<>();
        SetBuilder<String> symbolBConnections = new SetBuilder<>();
        SetBuilder<String> symbolCConnections = new SetBuilder<>();

        for(int i=0; i < STATE.length; i++) {
            symbolAConnections.add(EVENTS[i][0]);
            symbolBConnections.add(EVENTS[i][1]);
            symbolCConnections.add(EVENTS[i][2]);
        }

        b.node(SYMBOL[0], symbolAConnections.toSet());
        b.node(SYMBOL[1], symbolBConnections.toSet());
        b.node(SYMBOL[2], symbolCConnections.toSet());

        //Logic
        String cState = b.literal(STATE[0]);
        String cIndex = b.literal("I0");

        String cValue = b.intersect(new SetImp<>(b.connectionsOf(cIndex), Val));
        String cSymbol = b.connectionsOf(cValue);
        String cEvent = b.intersect(new SetImp<>(b.connectionsOf(cState), b.connectionsOf(cSymbol)));

        String decision = b.connectionsOf(cEvent);
        String dNextState = b.intersect(new SetImp<>(decision, stateNode));
        String dWriteSymbol = b.intersect(new SetImp<>(decision, symbolNode));
        String dShift = b.intersect(new SetImp<>(decision, Shift));

        b.addAssignment(b.literal(cState), dNextState);
        b.addAssignment(cValue, dWriteSymbol);

        b.addAssignment(
                b.literal(cIndex),
                b.connectionsOf(b.intersect(new SetImp<>(b.connectionsOf(cIndex), b.connectionsOf(dShift))))
        );

        Execution<String> execution = b.getExecution();

        try {
//            System.out.println("Tape Result:");
//            for(int i = 0; i< TAPE_LENGTH; i++) {
//                System.out.println(execution.queryNode("val"+i));
//            }
//            System.out.println("Indexes:");
            while(execution.executeStep()) {
                System.out.println(execution.queryNode(cIndex));
            }
            System.out.println("Tape Result:");
            for(int i = 0; i< TAPE_LENGTH; i++) {
                System.out.println(execution.queryNode("val"+i));
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}
