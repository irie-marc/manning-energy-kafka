package repository;

import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.Define;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;
import java.util.UUID;

public interface DeviceMetadataDAO {

  @SqlQuery("SELECT t.id, t.device_id, t.message_id, is_parsed, " +
          "EXTRACT(EPOCH FROM time_stamp_arrival) AS time_stamp_arrival, " +
          "EXTRACT(EPOCH FROM time_stamp_parse_start) AS time_stamp_parse_start, " +
          "number_of_events, " +
          "EXTRACT(EPOCH FROM time_stamp_parse_completed) AS time_stamp_parse_completed, " +
          "parsing_duration_nano_seconds " +
          "FROM <table> t LEFT JOIN <parse_info_table> m " +
          "ON t.message_id=m.message_id " +
          "WHERE t.device_id = :device_id")
  @RegisterBeanMapper(DeviceMetadataDAOImpl.class)
  public List<DeviceMetadataDAOImpl> getMetadataByDeviceId(
      @Define("table") String table,
      @Define("parse_info_table") String parse_info_table,
      @Bind("device_id") UUID deviceId);

  @SqlUpdate(
      "INSERT INTO <table> (id, device_id, message_id, is_parsed, time_stamp_arrival, time_stamp_parse_start, number_of_events) "
          + "VALUES (:id, :device_id, :message_id, :is_parsed, to_timestamp(:time_stamp_arrival), to_timestamp(:time_stamp_parse_start), :number_of_events)")
  void addDeviceMetadata(
      @Define("table") String table,
      @Bind("id") UUID id,
      @Bind("device_id") UUID device_id,
      @Bind("message_id") UUID message_id,
      @Bind("is_parsed") boolean is_parsed,
      @Bind("time_stamp_arrival") long time_stamp_arrival,
      @Bind("time_stamp_parse_start") long time_stamp_parse_start,
      @Bind("number_of_events") long number_of_events);

  @SqlUpdate(
      "INSERT INTO <table> (id, message_id, time_stamp_parse_completed, parsing_duration_nano_seconds) "
          + "VALUES (:id, :message_id, to_timestamp(:time_stamp_parse_completed), :parsing_duration_nano_seconds)")
  void addDeviceMetadataParseInfo(
      @Define("table") String table,
      @Bind("id") UUID id,
      @Bind("message_id") UUID message_id,
      @Bind("time_stamp_parse_completed") long time_stamp_parse_completed,
      @Bind("parsing_duration_nano_seconds") long parsing_duration_nano_seconds);
}
