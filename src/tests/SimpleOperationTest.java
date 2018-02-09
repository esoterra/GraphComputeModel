package tests;

import execution.Execution;
import execution.ExecutionException;
import programs.StringProgramBuilder;
import sets.Set;
import sets.SetImp;

public class SimpleOperationTest {
    private static final Set<String> dataSet1 = new SetImp<>("A", "B", "C");
    private static final Set<String> dataSet2 = new SetImp<>("B", "C", "D");

    public static void main(String[] args) {
        System.out.println("Begin Setup");
        System.out.println();

        StringProgramBuilder builder = new StringProgramBuilder();

        String dataNode1 = builder.literal(dataSet1);
        String dataNode2 = builder.literal(dataSet2);
        Set<String> data = new SetImp<>(dataNode1, dataNode2);

        String union = builder.union(data);
        String intersect = builder.intersect(data);
        String difference = builder.difference(data);

        Execution<String> execution = builder.getExecution();

        System.out.println("Begin Execution");
        System.out.println();
        try {
            int counter = 0;
            while(execution.executeStep()) {
                System.out.println("Executed Step: " + counter++);
            }
            System.out.println();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        System.out.println("Report Results");
        printResult("Union", execution.queryNode(union), dataSet1.union(dataSet2));
        printResult("Intersect", execution.queryNode(intersect), dataSet1.intersect(dataSet2));
        printResult("Difference", execution.queryNode(difference), dataSet1.difference(dataSet2));
    }

    private static void printResult(String desc, Set<String> actual, Set<String> target) {
        if(actual.equals(target)) {
            System.out.println(desc + " - SUCCESS");
            System.out.println("> Value: " + actual.toString());
        } else {
            System.out.println(desc + " - FAIL");
            System.out.println("> Actual: " + actual.toString());
            System.out.println("> Target: " + target.toString());
        }
    }
}
