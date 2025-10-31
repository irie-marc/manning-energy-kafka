package repository;

import energy.avro.DeviceMessageMetadata;
import org.jdbi.v3.core.Jdbi;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class MetadataRepository {
  final String TABLE_NAME_METADATA = "device_message_metadata";
  private final String TABLE_NAME_PARSE_INFO = "device_message_metadata_parse_info";

  private final Jdbi jdbi;

  public MetadataRepository(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  public List<DeviceMetadataDAOImpl> findDeviceMetadataByDeviceId(UUID deviceId) {
    DeviceMetadataDAO deviceMetadataDAO = jdbi.onDemand(DeviceMetadataDAO.class);
    return deviceMetadataDAO
        .getMetadataByDeviceId(TABLE_NAME_METADATA, TABLE_NAME_PARSE_INFO, deviceId);
    /*
        .stream()
        .map(
            (metadata) ->
                new DeviceMessageMetadata(
                    metadata.getMessage_id().toString(),
                    metadata.getDevice_id().toString(),
                    metadata.isIs_parsed(),
                    metadata.getTime_stamp_arrival(),
                    metadata.getTime_stamp_parse_start(),
                    metadata.getTime_stamp_parse_completed(),
                    metadata.getParsing_duration_nano_seconds(),
                    metadata.getNumber_of_events()))
        .collect(Collectors.toList()); */
  }

  public void saveMetadata(DeviceMessageMetadata metadata) {
    DeviceMetadataDAO dao = jdbi.onDemand(DeviceMetadataDAO.class);
    dao.addDeviceMetadata(
        TABLE_NAME_METADATA,
        UUID.randomUUID(),
        UUID.fromString(metadata.getDeviceId().toString()),
        UUID.fromString(metadata.getMessageId().toString()),
        metadata.getIsParsed(),
        metadata.getTimeStampArrival(),
        metadata.getTimeStampParseStart(),
        metadata.getNumberOfEvents());

    if (metadata.getIsParsed()) {
      dao.addDeviceMetadataParseInfo(
          TABLE_NAME_PARSE_INFO,
          UUID.randomUUID(),
          UUID.fromString(metadata.getMessageId().toString()),
          metadata.getTimeStampParseCompleted(),
          metadata.getParsingDurationNanoSeconds());
    }
  }
}
