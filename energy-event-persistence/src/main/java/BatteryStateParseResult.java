import energy.avro.BatteryState;

public class BatteryStateParseResult {

    public BatteryState getParsedValue() {
        return parsedValue;
    }

    public boolean isSlow() {
        return slow;
    }


    public BatteryStateParseResult(BatteryState batteryState, boolean isSlow, String rawValue) {
        this.parsedValue = batteryState;
        this.slow = isSlow;
        this.rawValue = rawValue;
    }

    private BatteryState parsedValue;
    private boolean slow;

    public String getRawValue() {
        return rawValue;
    }

    private String rawValue;
}
