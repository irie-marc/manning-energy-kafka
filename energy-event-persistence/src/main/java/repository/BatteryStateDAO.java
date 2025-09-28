package repository;

import energy.avro.BatteryState;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.Define;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;
import java.util.UUID;

public interface BatteryStateDAO {

    @SqlQuery("SELECT email FROM <table> WHERE id = :id")
    @RegisterBeanMapper(BatteryStateDaoImpl.class)
    public BatteryStateDaoImpl getBatteryState(@Define("table") String table, @Bind("id") String id);

    @SqlQuery("SELECT * FROM <table> WHERE device_id = :device_id")
    @RegisterBeanMapper(BatteryStateDaoImpl.class)
    public List<BatteryStateDaoImpl> getBatteryStatesByDeviceId(@Define("table") String table, @Bind("device_id") UUID deviceId);


    @SqlUpdate("INSERT INTO <table> (id, device_id, charging_source, charging, current_capacity) VALUES (:id, :device_id, :charging_source, :charging, :current_capacity)")
    void addBatteryState(@Define("table") String table,
                         @Bind("id") UUID id,
                         @Bind("device_id") UUID device_id,
                         @Bind("charging_source") String charging_source,
                         @Bind("charging") int charging,
                         @Bind("current_capacity") int currentCapacity);
}
