public class ExperimentResult {
    private String operationType;
    private int value;
    private boolean success;
    private long time;
    private long operations;

    public ExperimentResult(String operationType, int value, boolean success, long time, long operations) {
        this.operationType = operationType;
        this.value = value;
        this.success = success;
        this.time = time;
        this.operations = operations;
    }

    public String getOperationType() {
        return operationType;
    }

    public int getValue() {
        return value;
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