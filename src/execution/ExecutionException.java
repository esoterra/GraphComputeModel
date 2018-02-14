package execution;

public class ExecutionException extends Exception {
    ExecutionException(String desc) {
        super(desc);
    }

    static class InvalidOperation extends ExecutionException {
        InvalidOperation(String desc) {
            super(desc);
        }
    }

    static class NonDeterministicExecution extends ExecutionException {
        NonDeterministicExecution() { super("Graph Execution has become Non-Deterministic"); }
    }
}
