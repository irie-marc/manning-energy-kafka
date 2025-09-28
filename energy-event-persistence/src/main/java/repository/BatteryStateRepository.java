package repository;

import org.jdbi.v3.core.Jdbi;

import java.util.List;
import java.util.UUID;

public class BatteryStateRepository {
    final String TABLE_NAME = "battery_state";

    private final Jdbi jdbi;

    public BatteryStateRepository(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    public void saveBatteryState(UUID deviceId, String chargingSource, int charging, int currentCapacity) {
        BatteryStateDAO batteryStateDAO = jdbi.onDemand(BatteryStateDAO.class);
        batteryStateDAO.addBatteryState(TABLE_NAME, UUID.randomUUID(), deviceId, chargingSource, charging, currentCapacity);
    }

    public List<BatteryStateDaoImpl> findBatteryStatesById(UUID deviceId) {
        BatteryStateDAO batteryStateDAO = jdbi.onDemand(BatteryStateDAO.class);
        return batteryStateDAO.getBatteryStatesByDeviceId(TABLE_NAME, deviceId);
    }
}
