public class OperationResult {
    private boolean success;
    private long time;
    private long operations;

    public OperationResult(boolean success, long time, long operations) {
        this.success = success;
        this.time = time;
        this.operations = operations;
    }

    public boolean isSuccess() {
        return success;
    }

    public long getTime() {
        return time;
    }

    public long getOperations() {
        return operations;
    }
}