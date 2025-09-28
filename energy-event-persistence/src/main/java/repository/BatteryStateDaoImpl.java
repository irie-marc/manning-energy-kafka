package repository;

import java.util.UUID;

public class BatteryStateDaoImpl {
    public UUID getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(UUID deviceId) {
        this.deviceId = deviceId;
    }

    public String getChargingSource() {
        return chargingSource;
    }

    public void setChargingSource(String chargingSource) {
        this.chargingSource = chargingSource;
    }

    public int getCharging() {
        return charging;
    }

    public void setCharging(int charging) {
        this.charging = charging;
    }

    public int getChargingLevel() {
        return chargingLevel;
    }

    public void setChargingLevel(int chargingLevel) {
        this.chargingLevel = chargingLevel;
    }

    private UUID deviceId;
    private String chargingSource;
    private int charging;
    private int chargingLevel;
}
