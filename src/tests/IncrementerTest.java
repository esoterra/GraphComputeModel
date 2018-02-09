package tests;

import execution.Execution;
import execution.ExecutionException;
import programs.StringProgramBuilder;
import sets.SetBuilder;
import sets.SetImp;

public class IncrementerTest {
    private static final int LENGTH = 200;

    public static void main(String[] args) {
        System.out.println("Begin Setup");
        System.out.println();

        StringProgramBuilder builder = new StringProgramBuilder();
        SetBuilder<String> indexBuilder = new SetBuilder<>();
        SetBuilder<String> incrementerBuilder = new SetBuilder<>();
        SetBuilder<String> decrementerBuilder = new SetBuilder<>();

        String indexName;
        for(int i = 0; i< LENGTH; i++) {
            indexName = "I"+i;
            if(i == LENGTH -1) {
                builder.addNode(indexName, "dec"+i);
            } else {
                if(i == 0) {
                    builder.addNode(indexName,"inc"+i);
                } else {
                    builder.node(indexName, new SetImp<>("inc"+i, "dec"+i));
                }
            }
            indexBuilder.add(indexName);
        }

        for(int i = 0; i< LENGTH -1; i++) {
            incrementerBuilder.add(builder.addNode("inc"+i, "I"+(i+1)));
        }

        for(int i = 1; i< LENGTH; i++) {
            builder.addNode("dec"+i, "I"+(i-1));
            decrementerBuilder.add("dec"+i);
        }

//        String Index = builder.node("Index", indexBuilder.toSet());
        String Inc = builder.literal(incrementerBuilder.toSet());
        String Dec = builder.literal(decrementerBuilder.toSet());

        String cIndex = builder.literal("I0");

        builder.addAssignment(
                builder.literal(cIndex),
                builder.connectionsOf(
                        builder.intersect(new SetImp<>(
                                builder.connectionsOf(cIndex),
                                Inc
                        ))
                )
        );

        Execution<String> execution = builder.getExecution();

        System.out.println("Begin Execution");
        System.out.println();
        try {
            int counter = 0;
            while(execution.executeStep()) {
                System.out.println("T"+counter++ + ": " + execution.queryNode(cIndex));
            }
            System.out.println("T"+counter + ": " + execution.queryNode(cIndex));
            System.out.println();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}
